HOMEPAGE = "http://godoc.org/github.com/tools/godep"
SECTION = "devel"
SUMMARY = "dependency tool for go"

inherit go

SRC_URI = "\
	http://github.com/tools/godep/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
	"
SRC_URI[md5sum] = "4e5165d136074cc323898edacb3db38c"
SRC_URI[sha256sum] = "e68c7766c06c59327a4189fb929d390e1cc7a0c4910e33cada54cf40f40ca546"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://License;md5=71eb66e9b353dd06ca5a81ce0f469e1a"

GO_IMPORT = "github.com/tools/godep"

# go.bbclass uses S oddly :-(
S = "${WORKDIR}/gopath"
B = "${S}/src/${GO_IMPORT}"
UNPACK = "${WORKDIR}/${BPN}-${PV}"

do_compile[dirs] += "${B}"
do_compile_prepend() {
  tar -C ${UNPACK} -cf - . | tar -C ${B} -xpf -
}

do_install () {
  install -D -m 755 -t ${D}${bindir} ${S}/bin/godep
}

BBCLASSEXTEND = "native nativesdk"
