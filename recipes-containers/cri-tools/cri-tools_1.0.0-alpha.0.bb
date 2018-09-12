HOMEPAGE = "https://github.com/kubernetes-incubator/cri-tools"
SECTION = "console/tools"
SUMMARY = "CLI and validation tools for Kubelet Container Runtime Interface (CRI)."
DESCRIPTION = "cri-tools aims to provide a series of debugging and validation \
tools for Kubelet CRI."

UPSTREAM_CHECK_URI = "https://github.com/kubernetes-incubator/cri-tools/releases"
UPSTREAM_CHECK_REGEX = "v(?P<pver>(\d+[._a-z-]*)+)\.tar\.gz"

inherit golang

SRC_URI = "https://github.com/kubernetes-incubator/cri-tools/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz"

SRC_URI[md5sum] = "2a5fe9c72eae7a1fc5f7aad52b6cc500"
SRC_URI[sha256sum] = "2c8e45414d5804628f557171d622ca3e4a55dfc1f2de0cd33bcba98e863342d1"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e3fc50a88d0a364313df4b21ef20c29e"

GO_IMPORT = "github.com/kubernetes-incubator/cri-tools"
GO_INSTALL = "\
	${GO_IMPORT}/cmd/crictl \
	"

GO_LDFLAGS += "-X ${GO_IMPORT}/pkg/version.Version=${PV}"

BBCLASSEXTEND = "native nativesdk"
