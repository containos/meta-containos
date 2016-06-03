HOMEPAGE = "http://kubernetes.io"
SECTION = "support"
SUMMARY = "container orchestration engine"
DESCRIPTION = "Kubernetes is an open-source system for automating deployment, \
operations, and scaling of containerized applications."

SRC_URI = "\
	http://github.com/kubernetes/kubernetes/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "1bb19f8400ae1e224c710d8e0a8579a1"
SRC_URI[sha256sum] = "20a3984f9c044f1a1da3088166b181f3c10380d3efd4bf3fbc64678fef279ced"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d6177d11bbe618cf4ac8a63a16d30f83"

inherit golang

GOPKGROOT = "k8s.io/kubernetes"
USE_GODEP = "yes"

PACKAGES =+ "${PN}-client ${PN}-mesos pause kube2sky"

FILES_${PN}-client += "${bindir}/kubectl"
FILES_${PN}-mesos += "${bindir}/k8sm-* ${bindir}/km"
FILES_pause += "${bindir}/pause"
FILES_kube2sky += "${bindir}/kube2sky"

# Upstream build scripts are just too painful to work with. Skip them
# entirely and just do our own build.
GO_PACKAGE = "\
	 ${GOPKGROOT}/cmd/kubectl \
	 ${GOPKGROOT}/cmd/kube-proxy \
	 ${GOPKGROOT}/cmd/kube-apiserver \
	 ${GOPKGROOT}/cmd/kube-controller-manager \
	 ${GOPKGROOT}/cmd/kubelet \
	 ${GOPKGROOT}/cmd/kubemark \
	 ${GOPKGROOT}/cmd/hyperkube \
	 ${GOPKGROOT}/cmd/linkcheck \
	 ${GOPKGROOT}/plugin/cmd/kube-scheduler \
	 ${GOPKGROOT}/contrib/mesos/cmd/k8sm-scheduler \
	 ${GOPKGROOT}/contrib/mesos/cmd/k8sm-executor \
	 ${GOPKGROOT}/contrib/mesos/cmd/k8sm-controller-manager \
	 ${GOPKGROOT}/contrib/mesos/cmd/km \
	 ${GOPKGROOT}/build/pause \
	 ${GOPKGROOT}/cluster/addons/dns/kube2sky \
	 "

do_configure () {
  :
}

BBCLASSEXTEND = "native nativesdk"
