HOMEPAGE = "https://github.com/opencontainers/runc"
SECTION = "console/tools"
SUMMARY = "CLI tool for spawning and running containers according to the OCI specification"

UPSTREAM_CHECK_URI = "https://github.com/docker/runc/releases"
# no tags :(
UPSTREAM_VERSION_UNKNOWN = "1"

inherit golang pkgconfig

SRC_URI = "git://github.com/docker/runc.git;branch=17.03.x"
SRCREV = "54296cf40ad8143b62dbcaa1d90e520a2136ddfe"
PV = "17.03+git${SRCPV}"

RPROVIDES_${PN} += "runc"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=435b266b3899aa8a959f17d41c56def8"

S = "${WORKDIR}/git"

GO_IMPORT = "github.com/opencontainers/runc"

PACKAGECONFIG ?= "seccomp ambient ${@bb.utils.filter('DISTRO_FEATURES','selinux',d)}"
PACKAGECONFIG[seccomp] = "seccomp,,libseccomp"
PACKAGECONFIG[apparmor] = "apparmor,,libapparmor"
PACKAGECONFIG[ambient] = "ambient,,"
PACKAGECONFIG[selinux] = "selinux,,"

BUILDTAGS = "${PACKAGECONFIG_CONFARGS}"
GOBUILDFLAGS += "-tags '${BUILDTAGS}'"

GOPATH .= ":${S}/Godeps/_workspace"

do_install_append () {
  ln -s runc ${D}${bindir}/docker-runc
}
