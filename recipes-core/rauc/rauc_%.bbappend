FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://system.conf"

RAUC_KEYRING_BASENAME = "${@os.path.basename(d.getVar("RAUC_KEYRING_FILE"))}"

do_install_append() {
	if [ "${RAUC_KEYRING_BASENAME}" != keyring.pem ]; then
		ln -s ${RAUC_KEYRING_BASENAME} ${D}${sysconfdir}/rauc/keyring.pem
	fi
}
