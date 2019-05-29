FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
        file://tmpfiles.conf \
        "

tmpfilesdir = "${libdir}/tmpfiles.d"

FILES_${PN} += "${tmpfilesdir}"

do_install_append() {
	install -D -m 644 ${WORKDIR}/tmpfiles.conf ${D}/${tmpfilesdir}/${BP}.conf
}
