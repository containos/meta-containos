FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
  file://network-mac-for-usb \
  file://eth-dhcp \
  file://timesyncd.conf \
  file://var-lib.mount \
  file://kubelet.conf \
  file://kubelet.path \
  file://system.conf \
  "

inherit systemd

systemd_networkdir = "${systemd_unitdir}/network"
systemd_timesyncdconfdir = "${systemd_unitdir}/timesyncd.conf.d"
systemd_systemconfdir = "${systemd_unitdir}/system.conf.d"

SYSTEMD_PACKAGES += "${PN}"
SYSTEMD_SERVICE_${PN} += "kubelet.path ${EXTRA_SYSTEMD_SERVICES}"

EXTRA_SYSTEMD_SERVICES = ""
EXTRA_SYSTEMD_SERVICES_bananapi += "var-lib.mount"

do_install_append() {
  set -x

  ln -s ../run/systemd/resolve/resolv.conf ${D}${sysconfdir}/resolv.conf
  ln -s ../usr/share/zoneinfo/UTC ${D}${sysconfdir}/localtime
  ln -sf ../proc/self/mounts ${D}${sysconfdir}/mtab

  install -d ${D}${systemd_systemconfdir}
  install -m 644 ${WORKDIR}/system.conf ${D}${systemd_systemconfdir}/50-containos.conf

  install -d ${D}${systemd_networkdir}
  install -m 644 ${WORKDIR}/network-mac-for-usb ${D}${systemd_networkdir}/90-mac-for-usb.link
  install -m 644 ${WORKDIR}/eth-dhcp ${D}${systemd_networkdir}/90-dhcp.network

  install -D -m 644 ${WORKDIR}/timesyncd.conf ${D}${systemd_timesyncdconfdir}/90-fallback.conf

  for f in ${SYSTEMD_SERVICE_base-files}; do
    install -D -m 644 -t ${D}/${systemd_system_unitdir} ${WORKDIR}/$f
  done

  install -D -m 644 ${WORKDIR}/kubelet.conf ${D}/${systemd_system_unitdir}/kubelet.service.d/50-containos.conf
}
