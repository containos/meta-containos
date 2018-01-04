FILESEXTRAPATHS_prepend := "${THISDIR}/linux:"
SRC_URI += "file://docker.cfg"

do_configure() {
    cat ${WORKDIR}/docker.cfg >> ${B}/.config

    kernel_do_configure
}
