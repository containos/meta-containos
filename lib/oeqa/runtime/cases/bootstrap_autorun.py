import os
import subprocess
import tempfile
import time

from oeqa.runtime.case import OERuntimeTestCase
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.decorator.package import OEHasPackage

class AutorunTest(OERuntimeTestCase):

    local_files_dir = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        'files')

    def assertRun(self, cmd):
        status, output = self.target.run(cmd)
        self.assertEqual(status, 0, msg='"%s" failed. Output:\n%s' % (cmd, output))
        return output

    def mkimage(self, localFiles):
        tmpdir = '/tmp/imgbuild'
        imgfile = '/tmp/autorun.img'

        status, output = self.target.run('''
set -e -x
PATH=$PATH:/usr/sbin:/sbin
mkdosfs -C %(imgfile)s 2048
mkdir -p %(tmpdir)s
mount -o loop %(imgfile)s %(tmpdir)s
''' % {'tmpdir': tmpdir, 'imgfile': imgfile})
        assert status == 0, 'Failed to create bootstrap image. Output:\n%s' % output

        for path in localFiles:
            self.target.copyTo(path, tmpdir)

        self.assertRun('umount %s' % tmpdir)

        # Can't set the label earlier, or we trigger the by-label
        # symlink before the filesystem is prepared.
        self.assertRun('fatlabel %s BOOTSTRAP' % imgfile)

        return imgfile

    def losetup(self, imgfile):
        status, output = self.target.run('losetup --find --show %s' % imgfile)
        assert status == 0, 'Failed to setup loopback device. Output:\n%s' % output

        return output.strip()

    @OEHasPackage(['util-linux-losetup'])
    @OEHasPackage(['bootstrap-autorun'])
    @OETestDepends(['ssh.SSHTest.test_ssh'])
    def test_hotplug(self):
        image = self.mkimage([os.path.join(self.local_files_dir, 'autorun.sh')])
        self.addCleanup(self.target.run, 'rm %s' % image)

        loopdev = self.losetup(image)
        self.addCleanup(self.target.run, 'losetup --detach %s' % loopdev)

        self.addCleanup(self.target.run, 'rm /tmp/autorun.log')

        endtime = time.time() + 60
        while True:
            status, output = self.target.run('cat /tmp/autorun.log')
            if status == 0:
                break
            if time.time() >= endtime:
                status, output = self.target.run('journalctl -u bootstrap-autorun')
                self.fail('No output seen within timeout. bootstrap-autorun log:\n%s' % output)
            time.sleep(5)

        self.assertTrue('Autorun was here' in output, msg='Unexpected contents of autorun.log:\n%s' % output)
