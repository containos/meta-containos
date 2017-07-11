HOMEPAGE = "https://containerd.io/"
SECTION = "console/tools"
SUMMARY = "An open and reliable container runtime"
DESCRIPTION = "containerd is a daemon to control runC, built for \
performance and density."

inherit go

SRC_URI = "\
	https://github.com/containerd/containerd/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "cfc64cbcbed580889dd0efad80f363dd"
SRC_URI[sha256sum] = "88e099af66b50abe7f2159f13bdab793fa5199d8d5b9a9ef7a68171abb4359be"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.code;md5=aadc30f9c14d876ded7bedc0afd2d3d7"

S = "${WORKDIR}/${BP}"

GO_IMPORT = "github.com/docker/containerd"
GO_INSTALL = ""
GIT_COMMIT = "v${PV}"
EXTRA_OEMAKE += "GIT_COMMIT=${GIT_COMMIT} LDFLAGS=${GO_LDFLAGS}"

DEPENDS += "protobuf-native"

do_compile_append () {
  ( cd ${B}/src/${GO_IMPORT} && oe_runmake all )
}

do_install () {
  set -x
  d=${B}/src/${GO_IMPORT}/bin

  install -D -m 755 $d/containerd ${D}${bindir}/docker-containerd
  install -D -m 755 $d/ctr ${D}${bindir}/docker-containerd-ctr
  install -D -m 755 $d/containerd-shim ${D}${bindir}/docker-containerd-shim
}

BBCLASSEXTEND = "native nativesdk"
