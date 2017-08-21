FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI += "file://docker.cfg"

do_configure() {
    cat ${WORKDIR}/docker.cfg >> ${B}/.config

    kernel_do_configure
}
