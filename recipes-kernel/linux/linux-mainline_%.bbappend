FILESEXTRAPATHS_prepend := "${THISDIR}/linux:"
SRC_URI += "\
   file://docker.cfg \
   file://rauc.cfg \
   file://containos.cfg \
   "

do_configure() {
    cat ${WORKDIR}/docker.cfg >> ${B}/.config
    cat ${WORKDIR}/rauc.cfg >> ${B}/.config
    cat ${WORKDIR}/containos.cfg >> ${B}/.config

    kernel_do_configure
}
