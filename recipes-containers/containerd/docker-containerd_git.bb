HOMEPAGE = "https://containerd.io/"
SECTION = "console/tools"
SUMMARY = "An open and reliable container runtime"
DESCRIPTION = "containerd is a daemon to control runC, built for \
performance and density."

inherit golang

SRC_URI = "git://github.com/docker/containerd.git;branch=docker-1.13.x"
SRCREV = "a7f9a195fe4909d9194cadc9e1147c4ccc10467b"
PV = "0.2.5+git${SRCPV}"

RPROVIDES_${PN} += "containerd"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.code;md5=aadc30f9c14d876ded7bedc0afd2d3d7"

S = "${WORKDIR}/git"

GO_IMPORT = "github.com/docker/containerd"
GO_INSTALL = "\
           ${GO_IMPORT}/ctr \
           ${GO_IMPORT}/containerd \
           ${GO_IMPORT}/containerd-shim \
           "
GIT_COMMIT = "v${PV}"
GO_LDFLAGS += "-X github.com/docker/containerd.GitCommit=${GIT_COMMIT}"

RDEPENDS_${PN} += "runc"

DEPENDS += "protobuf-native"

# doesn't use a standard vendor/ dir structure :(
GOPATH .= ":${S}/vendor"

do_install_append () {
  set -x

  ln -s containerd ${D}${bindir}/docker-containerd
  ln -s containerd-shim ${D}${bindir}/docker-containerd-shim
  ln -s ctr ${D}${bindir}/docker-containerd-ctr
}

BBCLASSEXTEND = "native nativesdk"
