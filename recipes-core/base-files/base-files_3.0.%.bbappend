FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
  file://network-mac-for-usb \
  file://eth-dhcp \
  file://timesyncd.conf \
  "

systemd_networkdir = "${base_libdir}/systemd/network"
systemd_timesyncdconfdir = "${base_libdir}/systemd/timesyncd.conf.d"

do_install_append() {
  ln -s /run/systemd/resolve/resolv.conf ${D}${sysconfdir}/resolv.conf

  install -d ${D}${systemd_networkdir}
  install -m 644 ${WORKDIR}/network-mac-for-usb ${D}${systemd_networkdir}/90-mac-for-usb.link
  install -m 644 ${WORKDIR}/eth-dhcp ${D}${systemd_networkdir}/90-dhcp.network

  install -d ${D}${systemd_timesyncdconfdir}
  install -m 644 ${WORKDIR}/timesyncd.conf ${D}${systemd_timesyncdconfdir}/90-fallback.conf
}
