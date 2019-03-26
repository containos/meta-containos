import re

from oeqa.runtime.case import OERuntimeTestCase
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.decorator.package import OEHasPackage

class DockerTest(OERuntimeTestCase):

    def assertVersionMatches(self, value, regex):
        cmd = 'docker version -f "{{%s}}"' % value
        (status, output) = self.target.run(cmd)
        self.assertEqual(status, 0, output)
        m = re.search(regex, output)
        self.assertTrue(m, msg = 'Unexpected value for %s:\n%s' % (value, output))

    @OEHasPackage(['docker'])
    @OETestDepends(['ssh.SSHTest.test_ssh'])
    def test_version(self):
        cmd = 'docker version'
        (status, output) = self.target.run(cmd)
        self.assertEqual(status, 0, output)

        # These are set by build-time flags, and easy to mis-build
        self.assertVersionMatches('.Server.Version', r'^\d+\.\d+\.\d+(-.*)?$')
        self.assertVersionMatches('.Server.GitCommit', r'^[0-9a-f]+$')

    @OEHasPackage(['docker'])
    @OETestDepends(['ssh.SSHTest.test_ssh'])
    def test_info(self):
        cmd = 'docker info'
        (status, output) = self.target.run(cmd)
        self.assertEqual(status, 0, output)

    @OEHasPackage(['docker'])
    @OETestDepends(['docker.DockerTest.test_info'])
    def test_run(self):
        cmd = 'docker run --rm hello-world'
        (status, output) = self.target.run(cmd)

        if status != 0:
            if 'systemd' in self.tc.td['DISTRO_FEATURES']:
                (_, logs) = self.target.run('journalctl -n 50 -u docker')
                self.logger.info(logs)

        self.assertEqual(status, 0, output)

        msg = 'Unexpected output from hello-world docker image:\n%s' % output
        self.assertTrue('Hello from Docker' in output, msg=msg)
