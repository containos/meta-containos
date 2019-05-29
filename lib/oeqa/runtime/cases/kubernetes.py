import os
import re
import time
import urllib

from oeqa.runtime.case import OERuntimeTestCase
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.decorator.package import OEHasPackage

class KubeletTest(OERuntimeTestCase):

    local_files_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'files')

    @classmethod
    def setUpClass(cls):
        src = os.path.join(cls.local_files_dir, 'testpod.yaml')

        cls.tc.target.run('mkdir -p /etc/kubernetes/pki')

        files = [
            'kubernetes/kubelet.conf',
            'kubernetes/pki/ca.crt',
        ]
        for path in files:
            src = os.path.join(cls.local_files_dir, path)
            dst = '/etc/%s' % path
            cls.tc.target.copyTo(src, dst)

        cls.tc.target.run('systemctl restart kubelet')

    @classmethod
    def tearDownClass(cls):
        cls.tc.target.run('find /etc/kubernetes -type f -print | xargs rm -v')
        cls.tc.target.run('systemctl restart kubelet')

    @OEHasPackage(['kubelet'])
    def test_healthy(self):
        count = 0
        while True:
            try:
                resp = urllib.request.urlopen('http://%s:10255/healthz' % self.target.ip, timeout=10)
                self.assertEqual(resp.getcode(), 200, 'Kubelet healthz response: %s' % resp)
                break
            except urllib.error.URLError:
                if count > 10:
                    raise
                count += 1
                time.sleep(5)

    @OEHasPackage(['kubelet'])
    @OEHasPackage(['docker'])
    @OETestDepends(['ssh.SSHTest.test_ssh'])
    @OETestDepends(['kubernetes.KubeletTest.test_healthy'])
    def test_cgroup_driver(self):
        cmd = 'docker info -f "{{.CgroupDriver}}"'
        (status, output) = self.target.run(cmd)
        self.assertEqual(status, 0, output)
        docker_driver = output

        cmd = '%s ww | grep [k]ubelet' % self.tc.target_cmds['ps']
        (status, output) = self.target.run(cmd)
        self.assertEqual(status, 0, msg='No kubelet running; ps output:\n%s' % output)
        matches = re.findall('--cgroup-driver(?:=| +)(\S+)', output)
        self.assertEqual(len(matches), 1, 'Failed to find expected --cgroup-driver arg in:\n%s' % output)
        kubelet_driver = matches[0]

        self.assertEqual(docker_driver, kubelet_driver, 'docker and kubelet cgroup driver should match')

    @OEHasPackage(['kubelet'])
    @OETestDepends(['docker.DockerTest.test_run'])
    @OETestDepends(['kubernetes.KubeletTest.test_healthy'])
    def test_manifest(self):
        src = os.path.join(self.local_files_dir, 'testpod.yaml')
        dst = '/etc/kubernetes/manifests/testpod.yaml'

        self.target.copyTo(src, dst)
        self.addCleanup(self.target.run, 'rm %s' % dst)

        endtime = time.time() + (60 * 5)
        while True:
            status, output = self.target.run('cat /tmp/testout/pod.log')
            if status == 0:
                break
            self.assertTrue(time.time() < endtime, msg='No pod output seen before timeout')
            time.sleep(10)

        self.assertTrue('FOO=bar' in output, msg='Expected FOO=bar in:\n%s' % output)
        self.assertTrue('pod=testpod' in output, msg='Expected pod=testpod in:\n%s' % output)


class KubeadmTest(OERuntimeTestCase):

    def getMachine(self):
        return self.td.get('MACHINE', '')

    @OEHasPackage(['kubelet'])
    @OETestDepends(['kubernetes.KubeletTest.test_manifest'])
    def test_init(self):
        if self.getMachine().startswith('qemu'):
            # Boo :(  TODO: Find some better solution.
            self.skipTest('Too slow on emulated machine')

        self.addCleanup(self.target.run, 'kubeadm reset')

        # --ignore-prefilight-errors=CRI is a workaround for kubernetes/kubeadm#733
        status, output = self.target.run('kubeadm init --ignore-preflight-errors=CRI')
        self.assertEqual(status, 0, output)
