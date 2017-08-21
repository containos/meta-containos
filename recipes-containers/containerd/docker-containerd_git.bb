HOMEPAGE = "https://containerd.io/"
SECTION = "console/tools"
SUMMARY = "An open and reliable container runtime"
DESCRIPTION = "containerd is a daemon to control runC, built for \
performance and density."

inherit go

SRC_URI = "git://github.com/docker/containerd.git;branch=docker-1.13.x"
SRCREV = "a7f9a195fe4909d9194cadc9e1147c4ccc10467b"
PV = "0.2.5+git${SRCPV}"

RPROVIDES_${PN} += "containerd"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${UNPACK}/LICENSE.code;md5=aadc30f9c14d876ded7bedc0afd2d3d7"

GO_IMPORT = "github.com/docker/containerd"
GO_INSTALL = ""
GIT_COMMIT = "v${PV}"
EXTRA_OEMAKE += "GIT_COMMIT=${GIT_COMMIT} LDFLAGS=${GO_LDFLAGS}"

RDEPENDS_${PN} += "runc"

DEPENDS += "protobuf-native"

# go.bbclass uses S oddly :-(
S = "${WORKDIR}/gopath"
B = "${S}/src/${GO_IMPORT}"
UNPACK = "${WORKDIR}/git"

export GOPATH = "${S}:${STAGING_LIBDIR}/${TARGET_SYS}/go"

do_compile[dirs] += "${B}"
do_compile_prepend() {
  tar -C ${UNPACK} -cf - . | tar -C ${B} -xpf -
}

do_compile_append () {
  ( cd ${B} && oe_runmake LDFLAGS= all )
}

do_install () {
  set -x
  d=${B}/bin

  install -D -m 0755 -t ${D}${bindir} $d/containerd $d/containerd-shim $d/ctr

  ln -s containerd ${D}${bindir}/docker-containerd
  ln -s containerd-shim ${D}${bindir}/docker-containerd-shim
  ln -s ctr ${D}${bindir}/docker-containerd-ctr
}

BBCLASSEXTEND = "native nativesdk"
