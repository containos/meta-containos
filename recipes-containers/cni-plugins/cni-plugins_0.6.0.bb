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

GO_IMPORT = "github.com/containernetworking/plugins"
GO_INSTALL = "\
	${GO_IMPORT}/plugins/meta/... \
	${GO_IMPORT}/plugins/main/... \
	${GO_IMPORT}/plugins/ipam/... \
	${GO_IMPORT}/plugins/sample/... \
	"

FILES_${PN}-staticdev += "${libexecdir}/cni/sample"

# go binaries don't use GNU_HASH. Known, disable warning
# "QA Issue: No GNU_HASH in the elf binary: ..."
INSANE_SKIP_${PN} += "ldflags"
INSANE_SKIP_${PN}-staticdev += "ldflags"

do_install () {
  install -D -m 755 -t ${D}/opt/cni/bin $(find ${S}/bin -type f)
}

BBCLASSEXTEND = "native nativesdk"
