from oeqa.runtime.case import OERuntimeTestCase
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.decorator.package import OEHasPackage

class DockerTest(OERuntimeTestCase):

    @OEHasPackage(['docker'])
    @OETestDepends(['ssh.SSHTest.test_ssh'])
    def test_run(self):
        cmd = 'docker run --rm hello-world'
        (status, output) = self.target.run(cmd)

        self.assertEqual(status, 0, output)

        msg = 'Unexpected output from hello-world docker image:\n%s' % output
        self.assertTrue('Hello from Docker' in output, msg=msg)
