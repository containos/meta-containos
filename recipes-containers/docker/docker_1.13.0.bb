HOMEPAGE = "https://dockerproject.com/"
SECTION = "console/tools"
SUMMARY = "Linux container runtime"
DESCRIPTION = "Docker is an open source project to pack, ship and run any \
application as a lightweight container."

inherit go systemd useradd ptest

SRC_URI = "\
	https://github.com/moby/moby/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
        file://run-ptest \
	"
SRC_URI[md5sum] = "23e13f2e102a8746240909d435e2d22f"
SRC_URI[sha256sum] = "0b8cd2f66bd99d781e9a228dc00f14399dd9bd11078ee51a777d4e321c2095cf"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=aadc30f9c14d876ded7bedc0afd2d3d7"

S = "${WORKDIR}/moby-${PV}"
GO_IMPORT = "github.com/docker/docker"
GO_INSTALL = ""

DEPENDS += "containerd"
RDEPENDS_${PN} += "containerd"

DEPENDS += "\
	bash-native \
	sqlite \
	lvm2 \
	btrfs-tools \
	"

RRECOMMENDS_${PN} += "${PN}-client"

# NB: we use 'ps' from busybox, not procps
# NB: we don't provide xfsprogs
RDEPENDS_${PN} += "\
	iptables (>= 1.4) \
	busybox \
	e2fsprogs-mke2fs (>= 1.4.12) e2fsprogs-tune2fs (>= 1.4.12) \
	xz (>= 4.9) \
	"
RDEPENDS_${PN}-client += "\
	git (>= 1.7) \
	"

# Promote RDEPENDS to DEPENDS to a) help bitbake and b) verify that
# the RDEPENDS *can* actually be built
DEPENDS += "\
	iptables \
	busybox \
	e2fsprogs \
	xz \
	"

export DOCKER_GITCOMMIT = "v${PV}"
export DOCKER_BUILDTAGS = "\
	${@bb.utils.contains('DISTRO_FEATURES','selinux','selinux','',d)} \
	"

# seccomp should probably be a DISTRO_FEATURE...
DOCKER_BUILDTAGS += "seccomp"
DEPENDS += "libseccomp"

SYSTEMD_SERVICE_${PN} = "docker.service docker.socket"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "--system docker"

PACKAGES =+ "${PN}-client"

FILES_${PN}-client += "${bindir}/docker ${datadir}/bash-completion"

do_compile_append () {
  ( cd ${B}/src/${GO_IMPORT} && ./hack/make.sh dynbinary )
}

do_install () {
  set -x
  d=${B}/src/${GO_IMPORT}/bundles/latest

  install -d -m 755 ${D}${sysconfdir}/docker

  install -D -m 755 -t ${D}${bindir} $d/dynbinary-daemon/dockerd $d/dynbinary-client/docker

  install -D -m 644 -t ${D}${datadir}/bash-completion/completions ${S}/contrib/completion/bash/docker

  for f in ${SYSTEMD_SERVICE_${PN}}; do
    install -D -m 644 ${S}/contrib/init/systemd/$f ${D}${systemd_system_unitdir}/$f
  done
}

BBCLASSEXTEND = "native nativesdk"
