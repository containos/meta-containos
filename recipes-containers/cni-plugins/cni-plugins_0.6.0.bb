HOMEPAGE = "https://github.com/containernetworking/cni"
SECTION = "support"
SUMMARY = "CNI reference plugins"

SRC_URI = "\
	https://github.com/containernetworking/plugins/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "1d4bfa3b2eeb584ae4b70bf7fa6326aa"
SRC_URI[sha256sum] = "8589670f7f9b211a351dfcd211d4fe0b961d77283a7415443dc188f3dbf05668"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e3fc50a88d0a364313df4b21ef20c29e"

inherit golang

S = "${WORKDIR}/plugins-${PV}"

# cni plugins are considered externally-installed
bindir = "/opt/cni/bin"

GO_IMPORT = "github.com/containernetworking/plugins"
GO_INSTALL = "\
	${GO_IMPORT}/plugins/meta/flannel \
	${GO_IMPORT}/plugins/meta/portmap \
	${GO_IMPORT}/plugins/meta/tuning \
	${GO_IMPORT}/plugins/main/bridge \
	${GO_IMPORT}/plugins/main/vlan \
	${GO_IMPORT}/plugins/main/ptp \
	${GO_IMPORT}/plugins/main/macvlan \
	${GO_IMPORT}/plugins/main/ipvlan \
	${GO_IMPORT}/plugins/main/loopback \
	${GO_IMPORT}/plugins/ipam/dhcp \
	${GO_IMPORT}/plugins/ipam/host-local \
	"

FILES_${PN}-staticdev += "${libexecdir}/cni/sample"

BBCLASSEXTEND = "native nativesdk"
