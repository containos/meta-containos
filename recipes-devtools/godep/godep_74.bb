HOMEPAGE = "http://godoc.org/github.com/tools/godep"
SECTION = "devel"
SUMMARY = "dependency tool for go"

UPSTREAM_CHECK_URI = "https://github.com/tools/godep/releases"
UPSTREAM_CHECK_REGEX = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

inherit golang

SRC_URI = "\
	http://github.com/tools/godep/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "4e5165d136074cc323898edacb3db38c"
SRC_URI[sha256sum] = "e68c7766c06c59327a4189fb929d390e1cc7a0c4910e33cada54cf40f40ca546"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://License;md5=71eb66e9b353dd06ca5a81ce0f469e1a"

GO_IMPORT = "github.com/tools/godep"

BBCLASSEXTEND = "native nativesdk"
