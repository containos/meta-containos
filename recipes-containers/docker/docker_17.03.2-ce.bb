HOMEPAGE = "https://dockerproject.com/"
SECTION = "console/tools"
SUMMARY = "Linux container runtime"
DESCRIPTION = "Docker is an open source project to pack, ship and run any \
application as a lightweight container."

UPSTREAM_CHECK_URI = "https://github.com/moby/moby/releases"
UPSTREAM_CHECK_REGEX = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

inherit golang systemd useradd pkgconfig ptest

SRC_URI = "https://github.com/moby/moby/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
           file://run-ptest \
           file://0001-Extend-timeout-while-waiting-for-containerd-to-start.patch \
           "
SRC_URI[md5sum] = "a064b84fda9a903ce23b2c8818d32337"                            
SRC_URI[sha256sum] = "fb7052a4c565d2fd7ad4f63dc17c5e3d9b0896e18f5cafdc2c78f9c5974a86b3"                                                                         

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=aadc30f9c14d876ded7bedc0afd2d3d7"

GO_IMPORT = "github.com/docker/docker"
GO_INSTALL = "\
           ${GO_IMPORT}/cmd/docker \
           ${GO_IMPORT}/cmd/dockerd \
           "

S = "${WORKDIR}/moby-${PV}"

RDEPENDS_${PN} += "containerd"

DEPENDS += "\
	bash-native \
	sqlite \
	lvm2 \
	btrfs-tools \
	libdevmapper \
	"

RRECOMMENDS_${PN} += "${PN}-client"

# NB: we use 'ps' from busybox, not procps
# NB: we don't provide xfsprogs
RDEPENDS_${PN} += "\
	iptables (>= 1.4) \
	busybox \
	e2fsprogs-mke2fs (>= 1.4.12) e2fsprogs-tune2fs (>= 1.4.12) \
	xz (>= 4.9) \
	kernel-module-bridge \
	kernel-module-br-netfilter \
	kernel-module-xt-conntrack \
	kernel-module-xt-addrtype \
	kernel-module-xt-nat \
	kernel-module-nf-nat-masquerade-ipv4 \
	kernel-module-ip-vs \
	kernel-module-xt-redirect \
	kernel-module-xfrm-user \
	kernel-module-overlay \
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

# go sqlite bindings use pthread symbols without adding linker directive
CGO_LDFLAGS += "-lpthread"

PACKAGECONFIG ?= "seccomp pkcs11 \
              ${@bb.utils.filter('DISTRO_FEATURES','selinux',d)} \
              ${@bb.utils.contains('DISTRO_FEATURES','systemd','journald','',d)} \
              "
PACKAGECONFIG[journald] = "journald,,systemd"
PACKAGECONFIG[seccomp] = "seccomp,,libseccomp"
PACKAGECONFIG[apparmor] = "apparmor,,libapparmor"
PACKAGECONFIG[selinux] = "selinux,,"
PACKAGECONFIG[pkcs11] = "pkcs11,,libtool"

DOCKER_BUILDTAGS = "daemon autogen sqlite_omit_load_extension"
DOCKER_BUILDTAGS += "${PACKAGECONFIG_CONFARGS}"
GOBUILDFLAGS += "-tags '${DOCKER_BUILDTAGS}'"

SYSTEMD_SERVICE_${PN} = "docker.service docker.socket"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "--system docker"

PACKAGES =+ "${PN}-client"

FILES_${PN}-client += "${bindir}/docker ${datadir}/bash-completion"

do_configure_append() {
  cd ${S} && \
    GITCOMMIT="${DOCKER_GITCOMMIT}" \
    VERSION="$(cat ./VERSION)" \
    IAMSTATIC="${@not bool(d.getVar('GO_DYNLINK'))}" \
    BUILDTIME="$(date --rfc-3339 ns 2> /dev/null | sed -e 's/ /T/')" \
    bash -x ./hack/make/.go-autogen
}

do_install_append () {
  set -x

  install -D -m 644 -t ${D}${datadir}/bash-completion/completions ${S}/contrib/completion/bash/docker

  install -d -m 700 ${D}${localstatedir}/lib/docker

  for f in ${SYSTEMD_SERVICE_${PN}}; do
    install -D -m 644 ${S}/contrib/init/systemd/$f ${D}${systemd_system_unitdir}/$f
  done

  # We use systemd >= 226, so enable this option
  sed -i -e 's/^# *TasksMax=infinity/TasksMax=infinity/' ${D}${systemd_system_unitdir}/docker.service
}

BBCLASSEXTEND = "native nativesdk"
