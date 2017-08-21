HOMEPAGE = "https://github.com/opencontainers/runc"
SECTION = "console/tools"
SUMMARY = "CLI tool for spawning and running containers according to the OCI specification"

inherit go pkgconfig

SRC_URI = "git://github.com/docker/runc.git;branch=17.03.x"
SRCREV = "54296cf40ad8143b62dbcaa1d90e520a2136ddfe"
PV = "1.0.0-rc3+git${SRCPV}"

RPROVIDES_${PN} += "runc"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${UNPACK}/LICENSE;md5=435b266b3899aa8a959f17d41c56def8"

GO_IMPORT = "github.com/opencontainers/runc"
GO_INSTALL = ""

PACKAGECONFIG ?= "seccomp ambient ${@bb.utils.filter('DISTRO_FEATURES','selinux',d)}"
PACKAGECONFIG[seccomp] = "seccomp,,libseccomp"
PACKAGECONFIG[apparmor] = "apparmor,,libapparmor"
PACKAGECONFIG[ambient] = "ambient,,"
PACKAGECONFIG[selinux] = "selinux,,"

EXTRA_OEMAKE += "BUILDTAGS='${PACKAGECONFIG_CONFARGS}'"
EXTRA_OEMAKE += "BINDIR=${D}${bindir}"

# go.bbclass uses S oddly :-(
S = "${WORKDIR}/gopath"
B = "${S}/src/${GO_IMPORT}"
UNPACK = "${WORKDIR}/git"

export GOPATH = "${S}:${STAGING_LIBDIR}/${TARGET_SYS}/go"

do_compile[dirs] += "${B}"
do_compile_prepend() {
  tar -C ${UNPACK} -cf - . | tar -C ${B} -xpf -
}

do_compile () {
  oe_runmake
}

do_install () {
  oe_runmake install

  ln -s runc ${D}${bindir}/docker-runc
}
