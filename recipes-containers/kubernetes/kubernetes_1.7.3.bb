HOMEPAGE = "http://kubernetes.io"
SECTION = "support"
SUMMARY = "container orchestration engine"
DESCRIPTION = "Kubernetes is an open-source system for automating deployment, \
operations, and scaling of containerized applications."

SRC_URI = "\
	https://github.com/kubernetes/kubernetes/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	file://docker.conf \
	"
SRC_URI[md5sum] = "2b5c6c0e701f503c0e2aa709e4949921"
SRC_URI[sha256sum] = "7f84e08c2944865247c3fee26b010452e4a315a1cdd7983ff59d151f17167b13"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${UNPACK}/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit go systemd

# go.bbclass uses S oddly :-(
S = "${WORKDIR}/gopath"
B = "${S}/src/${GO_IMPORT}"
UNPACK = "${WORKDIR}/${BPN}-${PV}"

DEPENDS += "godep-native"

# Additional dependencies for `make generated-files`
DEPENDS += "coreutils-native rsync-native"

PACKAGES =+ "${PN}-client kubelet kubeadm kube-proxy pause hyperkube"

RDEPENDS_${PN}-dev += "bash"

# Dependencies "documented" here:
#  https://github.com/kubernetes/kubernetes/issues/26093
# FIXME: only need nsenter from util-linux
RDEPENDS_kubelet += "ethtool util-linux"
RDEPENDS_kubelet += "cni-plugins iptables iproute2-tc"
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
	 ${systemd_system_unitdir}/docker.service.d/10-kubelet.conf \
	 "
FILES_kube-proxy += "${bindir}/kube-proxy"
FILES_kubeadm += "\
	 ${bindir}/kubeadm \
	 ${systemd_system_unitdir}/kubelet.service.d/10-kubeadm.conf \
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
	 ${GO_IMPORT}/cmd/kubelet \
	 ${GO_IMPORT}/cmd/kube-proxy \
	 ${GO_IMPORT}/cmd/kube-apiserver \
	 ${GO_IMPORT}/cmd/kube-controller-manager \
	 ${GO_IMPORT}/cmd/cloud-controller-manager \
	 ${GO_IMPORT}/cmd/kubectl \
	 ${GO_IMPORT}/cmd/kubeadm \
	 ${GO_IMPORT}/cmd/hyperkube \
	 ${GO_IMPORT}/vendor/k8s.io/kube-aggregator \
	 ${GO_IMPORT}/vendor/k8s.io/apiextensions-apiserver \
	 ${GO_IMPORT}/plugin/cmd/kube-scheduler \
	 ${GO_IMPORT}/federation/cmd/kubefed \
	 "

# go binaries don't use GNU_HASH. Known, disable warning
# "QA Issue: No GNU_HASH in the elf binary: ..."
INSANE_SKIP_${PN} += "ldflags"
INSANE_SKIP_kubeadm += "ldflags"

do_configure () {
  :
}

do_compile[dirs] += "${B}"
do_compile () {
  set -x

  # upstream tarball includes uncleaned binaries (?!)
  rm -v -f ${UNPACK}/cluster/gce/gci/mounter/mounter

  tar -C ${UNPACK} -cf - . | tar -C ${B} -xpf -

  mkdir -p ${S}/pkg/generated

  GOOS="${BUILD_GOOS}" GOARCH="${BUILD_GOARCH}" \
  CC="${BUILD_CC}" \
  CPP="${BUILD_CPP}" \
  CGO_CFLAGS="${BUILD_CFLAGS}" \
  CGO_LDFLAGS="${BUILD_LDFLAGS}" \
  make generated_files

  # Note `-ldflags -linkmode=external` to work around
  # https://github.com/golang/go/issues/19425
  GOPATH=${S}:${STAGING_LIBDIR}/${TARGET_SYS}/go godep go install -ldflags "-linkmode=external -extld '${HOST_PREFIX}gcc' -extldflags '${HOST_CC_ARCH}${TOOLCHAIN_OPTIONS} ${LDFLAGS}'" -v ${GO_INSTALL}

  ${CC} ${CFLAGS} ${LDFLAGS} -Os -Wall -o ${B}/build/pause/pause ${B}/build/pause/pause.c
}

do_install_append () {
  rm -r ${D}${GOSRC_FINAL}/${GO_IMPORT}/_output

  if [ -e "${D}${GOROOT_FINAL}/bin" ]; then
     install -d -m 0755 "${D}${bindir}"
     find ${D}${GOROOT_FINAL}/bin ! -type d -print0 | xargs -r0 mv --target-directory=${D}${bindir}
     rmdir -p ${D}${GOROOT_FINAL}/bin || true
  fi

  # -linkmode=external workaround above results in embedded RPATHs -> remove
  chrpath -d ${D}${bindir}/*

  install -D -m 0755 -t ${D}${bindir} ${B}/build/pause/pause

  install -D -m 0644 -t ${D}${systemd_system_unitdir} ${B}/build/debs/kubelet.service
  install -D -m 0644 ${B}/build/debs/kubeadm-10.conf ${D}${systemd_system_unitdir}/kubelet.service.d/10-kubeadm.conf
  install -D -m 0644 ${WORKDIR}/docker.conf ${D}${systemd_system_unitdir}/docker.service.d/10-kubelet.conf
}

BBCLASSEXTEND = "native nativesdk"
