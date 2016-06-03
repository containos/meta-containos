HOMEPAGE = "http://godoc.org/github.com/tools/godep"
SECTION = "devel"
SUMMARY = "dependency tool for go"

inherit golang

SRC_URI = "\
	http://github.com/tools/godep/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "4da68819c7436e1b08533811a4915ad3"
SRC_URI[sha256sum] = "c2a2b37c20620d19cf341c479442067434049171e9cc3e226cdddde34aa6b3d9"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://License;md5=71eb66e9b353dd06ca5a81ce0f469e1a"

GOPKGROOT = "github.com/tools/godep"

BBCLASSEXTEND = "native nativesdk"
