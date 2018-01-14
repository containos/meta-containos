FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "\
	file://tmpfiles.conf \
	file://fw_env.config \
	"

tmpfilesdir = "${libdir}/tmpfiles.d"
FILES_${PN} += "${tmpfilesdir}"

do_install_append () {
	install -d ${D}${tmpfilesdir}
	install -m 0644 ${WORKDIR}/tmpfiles.conf ${D}${tmpfilesdir}/${BP}.conf

	install -d ${D}${sysconfdir}
	install -m 0644 ${WORKDIR}/fw_env.config ${D}${sysconfdir}/fw_env.config
}
