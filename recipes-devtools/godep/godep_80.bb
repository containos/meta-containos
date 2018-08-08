HOMEPAGE = "http://godoc.org/github.com/tools/godep"
SECTION = "devel"
SUMMARY = "dependency tool for go"

UPSTREAM_CHECK_URI = "https://github.com/tools/godep/releases"
UPSTREAM_CHECK_REGEX = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

inherit golang

SRC_URI = "\
	http://github.com/tools/godep/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "dde279ee6c36cc58ad66cc19eb1014b9"
SRC_URI[sha256sum] = "029adc1a0ce5c63cd40b56660664e73456648e5c031ba6c214ba1e1e9fc86cf6"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://License;md5=71eb66e9b353dd06ca5a81ce0f469e1a"

GO_IMPORT = "github.com/tools/godep"

BBCLASSEXTEND = "native nativesdk"
