HOMEPAGE = "http://kubernetes.io"
SECTION = "support"
SUMMARY = "container orchestration engine"
DESCRIPTION = "Kubernetes is an open-source system for automating deployment, \
operations, and scaling of containerized applications."

SRC_URI = "\
	http://github.com/kubernetes/kubernetes/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "d2d4db19666e6a9458f6bd015347e982"
SRC_URI[sha256sum] = "77fbc5db607daa723e7b6576644d25e98924439954523808cf7ad2c992566398"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d6177d11bbe618cf4ac8a63a16d30f83"

inherit golang

GOPKGROOT = "k8s.io/kubernetes"
USE_GODEP = "yes"

PACKAGES =+ "${PN}-client ${PN}-mesos pause kubedns"

FILES_${PN}-client += "${bindir}/kubectl"
FILES_${PN}-mesos += "${bindir}/k8sm-* ${bindir}/km"
FILES_pause += "${bindir}/pause"
FILES_kubedns += "${bindir}/kube-dns"

# Upstream build scripts are just too painful to work with. Skip them
# entirely and just do our own build.
GO_PACKAGE = "\
	 ${GOPKGROOT}/cmd/kubectl \
	 ${GOPKGROOT}/cmd/kube-dns \
	 ${GOPKGROOT}/cmd/kube-proxy \
	 ${GOPKGROOT}/cmd/kube-apiserver \
	 ${GOPKGROOT}/cmd/kube-controller-manager \
	 ${GOPKGROOT}/cmd/kubelet \
	 ${GOPKGROOT}/cmd/kubemark \
	 ${GOPKGROOT}/cmd/hyperkube \
	 ${GOPKGROOT}/federation/cmd/federation-apiserver \
	 ${GOPKGROOT}/federation/cmd/federation-controller-manager \
	 ${GOPKGROOT}/plugin/cmd/kube-scheduler \
	 ${GOPKGROOT}/contrib/mesos/cmd/k8sm-scheduler \
	 ${GOPKGROOT}/contrib/mesos/cmd/k8sm-executor \
	 ${GOPKGROOT}/contrib/mesos/cmd/k8sm-controller-manager \
	 ${GOPKGROOT}/contrib/mesos/cmd/km \
	 "

do_configure () {
  :
}

do_compile_append () {
  ${CC} ${CFLAGS} ${LDFLAGS} -Os -Wall -o ${B}/bin/pause ${S}/build/pause/pause.c
}

BBCLASSEXTEND = "native nativesdk"
