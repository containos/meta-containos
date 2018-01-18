SUMMARY = "Simple tools for container-based distros"

SRC_URI += "file://toolbox"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "\
        file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
	"

RDEPENDS_${PN} += "docker-client"

do_install() {
   install -D -m 755 -t ${D}${bindir} ${WORKDIR}/toolbox
}
