SUMMARY = "Run autorun.sh script from external storage"

SRC_URI += "\
        file://bootstrap-autorun.service \
        file://bootstrap-autorun.path \
        file://run-autorun.sh \
        "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "\
	file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
	"

inherit systemd allarch

RRECOMMENDS_${PN} += "docker-client"

SYSTEMD_PACKAGES += "${PN}"
SYSTEMD_SERVICE_${PN} = "${BPN}.path"
FILES_${PN} += "${systemd_system_unitdir}"

do_install() {
	install -D -m 644 -t ${D}/${systemd_system_unitdir} ${WORKDIR}/bootstrap-autorun.service ${WORKDIR}/bootstrap-autorun.path
	install -D -m 755 -t ${D}/${libdir}/${BPN} ${WORKDIR}/run-autorun.sh
}
