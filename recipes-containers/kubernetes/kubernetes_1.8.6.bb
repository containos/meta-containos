HOMEPAGE = "http://kubernetes.io"
SECTION = "support"
SUMMARY = "container orchestration engine"
DESCRIPTION = "Kubernetes is an open-source system for automating deployment, \
operations, and scaling of containerized applications."

SRC_URI = "\
	https://github.com/kubernetes/kubernetes/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
        file://git-archive.patch \
	file://docker.conf \
	"
SRC_URI[md5sum] = "c35ab1148b9906d1920a5d208a1c329e"
SRC_URI[sha256sum] = "ac46cadd3e0221582936cf901f426104793fe98eff7f06dfb886a765e4d57d0d"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit golang systemd

# Additional dependencies for `make generated-files`
DEPENDS += "coreutils-native rsync-native"

PACKAGES =+ "${PN}-client kubelet kubeadm kube-proxy pause hyperkube"

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

RDEPENDS_kubeadm += "kubelet"

FILES_${PN}-client += "${bindir}/kubectl ${bindir}/kubefed"
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
FILES_hyperkube += "${bindir}/hyperkube"
FILES_${PN}-staticdev += "${GOROOT_FINAL}/pkg"

SYSTEMD_PACKAGES = "kubelet"
SYSTEMD_SERVICE_kubelet = "kubelet.service"

# Upstream build scripts are too painful to work with. Skip them
# entirely and just do our own regular golang build.
GO_IMPORT = "k8s.io/kubernetes"
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
	 ${GO_IMPORT}/cmd/kubectl \
	 ${GO_IMPORT}/federation/cmd/kubefed \
	 "

# go binaries don't use GNU_HASH. Known, disable warning
# "QA Issue: No GNU_HASH in the elf binary: ..."
INSANE_SKIP_kubeadm += "ldflags"

# Note `-ldflags -linkmode=external` to work around
# https://github.com/golang/go/issues/19425
GO_LINKMODE = "--linkmode=external"

do_configure_prepend() {
  # upstream tarball includes uncleaned binaries (?!)
  rm -v -f ${S}/cluster/gce/gci/mounter/mounter
}

do_compile_prepend() {
  mkdir -p ${B}/src/${GO_IMPORT}/pkg/generated

  GOOS="${BUILD_GOOS}" GOARCH="${BUILD_GOARCH}" \
  CC="${BUILD_CC}" \
  CPP="${BUILD_CPP}" \
  CGO_CFLAGS="${BUILD_CFLAGS}" \
  CGO_LDFLAGS="${BUILD_LDFLAGS}" \
  KUBE_VERBOSE=5 \
  make -C ${B}/src/${GO_IMPORT} generated_files
}

do_compile_append() {
  install -d ${B}/build/pause
  ${CC} ${CFLAGS} ${LDFLAGS} -Os -Wall -o ${B}/build/pause/pause ${S}/build/pause/pause.c
}

do_install_append () {
  b=${B}/src/${GO_IMPORT}

  d=${D}${libdir}/go/src/${GO_IMPORT}
  rm -r $d/cluster $d/test $d/hack $d/staging

  install -D -m 0755 -t ${D}${bindir} ${B}/build/pause/pause

  # Having this exist (but perhaps empty) silences a warning with
  #  kubelet --pod-manifest-path=/etc/kubernetes/manifests
  install -d -m 0755 ${D}${sysconfdir}/kubernetes/manifests

  install -D -m 0644 -t ${D}${systemd_system_unitdir} $b/build/debs/kubelet.service
  install -D -m 0644 $b/build/debs/kubeadm-10.conf ${D}${systemd_system_unitdir}/kubelet.service.d/10-kubeadm.conf
  install -D -m 0644 ${WORKDIR}/docker.conf ${D}${systemd_system_unitdir}/docker.service.d/10-kubelet.conf

  install -d -m 0755 ${D}${localstatedir}/lib/kubelet

  install -d -m 0755 ${D}${sysconfdir}/modules-load.d
  echo br-netfilter > ${D}${sysconfdir}/modules-load.d/kubelet.conf
}

BBCLASSEXTEND = "native nativesdk"
