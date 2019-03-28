HOMEPAGE = "http://kubernetes.io"
SECTION = "support"
SUMMARY = "container orchestration engine"
DESCRIPTION = "Kubernetes is an open-source system for automating deployment, \
operations, and scaling of containerized applications."

UPSTREAM_CHECK_URI = "https://github.com/kubernetes/kubernetes/releases"
UPSTREAM_CHECK_REGEX = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

SRC_URI = "https://github.com/kubernetes/kubernetes/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
           file://docker.conf \
           file://0001-Increase-many-timeouts-10x.patch \
           "
SRC_URI[md5sum] = "cc454e5ef2034ddda1b1513bd3364bbd"
SRC_URI[sha256sum] = "27daae1122fa56222703fe62f5b4ddbcfe8e4eeceb30caf8984c9a80b40504e1"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit golang systemd

# Additional dependencies for `make generated_files`
DEPENDS += "coreutils-native rsync-native"

PACKAGES =+ "${PN}-client kubelet kubeadm kube-proxy pause gci-mounter hyperkube"

RDEPENDS_${PN}-dev += "bash"

# Dependencies "documented" here:
#  https://github.com/kubernetes/kubernetes/issues/26093
# FIXME: only need nsenter from util-linux
RDEPENDS_kubelet += "ethtool util-linux"
RDEPENDS_kubelet += "ebtables iptables iproute2-tc"
# busybox `ip` is sufficient
#RDEPENDS_kubelet += "iproute2"
# socat is used for port-forward feature
RDEPENDS_kubelet += "socat"
# FIXME: uses find -printf, which isn't supported by busybox find
RDEPENDS_kubelet += "findutils"

RDEPENDS_kubeadm += "kubelet cri-tools"

FILES_${PN}-client += "${bindir}/kubectl"
FILES_kubelet += "\
	${bindir}/kubelet \
	${systemd_system_unitdir}/kubelet.service \
	${systemd_system_unitdir}/kubelet.path \
	${systemd_system_unitdir}/docker.service.d/10-kubelet.conf \
	${systemd_system_unitdir}/kubelet.service.d/10-kubeadm.conf \
	${sysconfdir}/kubernetes/manifests \
	${sysconfdir}/modules-load.d/kubelet.conf \
        ${localstatedir}/lib/kubelet \
	"
FILES_kube-proxy += "${bindir}/kube-proxy"
FILES_kubeadm += "\
	${bindir}/kubeadm \
	"
FILES_pause += "${bindir}/pause"
FILES_gci-mounter += "${bindir}/mounter"
FILES_hyperkube += "${bindir}/hyperkube"
FILES_${PN}-staticdev += "${GOROOT_FINAL}/pkg"

SYSTEMD_PACKAGES = "kubelet"
SYSTEMD_SERVICE_kubelet = "kubelet.service"

# Upstream build scripts are too painful to work with. Skip them
# entirely and just do our own regular golang build.
GO_IMPORT = "k8s.io/kubernetes"
# This list is composed from hack/lib/golang.sh server_targets + client_targets
GO_INSTALL = "\
	 ${GO_IMPORT}/cmd/kube-proxy \
	 ${GO_IMPORT}/cmd/kube-apiserver \
	 ${GO_IMPORT}/cmd/kube-controller-manager \
	 ${GO_IMPORT}/cmd/cloud-controller-manager \
	 ${GO_IMPORT}/cmd/kubelet \
	 ${GO_IMPORT}/cmd/kubeadm \
	 ${GO_IMPORT}/cmd/hyperkube \
	 ${GO_IMPORT}/vendor/k8s.io/kube-aggregator \
	 ${GO_IMPORT}/vendor/k8s.io/apiextensions-apiserver \
	 ${GO_IMPORT}/plugin/cmd/kube-scheduler \
	 ${GO_IMPORT}/cluster/gce/gci/mounter \
	 ${GO_IMPORT}/cmd/kubectl \
	 "

# go binaries don't use GNU_HASH. Known, disable warning
# "QA Issue: No GNU_HASH in the elf binary: ..."
INSANE_SKIP_kubeadm += "ldflags"

# Note `-ldflags -linkmode=external` to work around
# https://github.com/golang/go/issues/19425
GO_LINKMODE = "--linkmode=external"

export KUBE_GO_PACKAGE = "${GO_IMPORT}"
GO_LDFLAGS += "$(bash -c '. ${S}/hack/lib/version.sh; kube::version::ldflags')"

do_configure_prepend() {
  mkdir -p ${S}/vendor/k8s.io
  for repo in ${S}/staging/src/k8s.io/*; do
    f=${repo##*/}
    test -e ${S}/vendor/k8s.io/$f || ln -s ../../staging/src/k8s.io/$f ${S}/vendor/k8s.io/$f
  done
}

do_compile_prepend() {
  set -x
  mkdir -p ${S}/pkg/generated

  GOOS="${BUILD_GOOS}" GOARCH="${BUILD_GOARCH}" \
  GOROOT="${STAGING_LIBDIR_NATIVE}/go" \
  GOTOOLDIR="${STAGING_LIBDIR_NATIVE}/go/pkg/tool/${BUILD_GOTUPLE}" \
  CC="${BUILD_CC}" \
  CPP="${BUILD_CPP}" \
  CGO_CFLAGS="${BUILD_CFLAGS}" \
  CGO_LDFLAGS="${BUILD_LDFLAGS}" \
  KUBE_VERBOSE=5 \
  make -C ${S} generated_files
}

do_compile_append() {
  install -d ${B}/build
  ${CC} ${CFLAGS} ${LDFLAGS} -Os -Wall -o ${B}/build/pause ${S}/build/pause/pause.c
}

do_install_append () {
  d=${D}${libdir}/go/src/${GO_IMPORT}
  rm -r $d/cluster $d/test $d/hack $d/staging

  install -D -m 0755 -t ${D}${bindir} ${B}/build/pause

  # Having this exist (but perhaps empty) silences a warning with
  #  kubelet --pod-manifest-path=/etc/kubernetes/manifests
  install -d -m 0755 ${D}${sysconfdir}/kubernetes/manifests

  install -D -m 0644 -t ${D}${systemd_system_unitdir} ${S}/build/debs/kubelet.service
  install -D -m 0644 ${S}/build/debs/10-kubeadm.conf ${D}${systemd_system_unitdir}/kubelet.service.d/10-kubeadm.conf
  install -D -m 0644 ${WORKDIR}/docker.conf ${D}${systemd_system_unitdir}/docker.service.d/10-kubelet.conf

  install -d -m 0755 ${D}${localstatedir}/lib/kubelet

  install -d -m 0755 ${D}${sysconfdir}/modules-load.d
  echo br-netfilter > ${D}${sysconfdir}/modules-load.d/kubelet.conf
}

do_install_ptest_base_append() {
	# Unused by tests, and require bash
	rm -f ${D}${PTEST_PATH}/${GO_IMPORT}/pkg/kubectl/cmd/testdata/edit/*.sh
}

BBCLASSEXTEND = "native nativesdk"
