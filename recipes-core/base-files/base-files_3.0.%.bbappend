FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
  file://network-mac-for-usb \
  file://eth-dhcp \
  file://timesyncd.conf \
  file://data.mount \
  file://var-lib-docker.mount \
  file://var-lib-etcd.mount \
  file://kubelet.conf \
  "

inherit systemd

systemd_networkdir = "${base_libdir}/systemd/network"
systemd_timesyncdconfdir = "${base_libdir}/systemd/timesyncd.conf.d"

SYSTEMD_PACKAGES += "${PN}"
SYSTEMD_SERVICE_${PN} += "data.mount"
SYSTEMD_SERVICE_${PN}_bananapi += "data.mount var-lib-docker.mount var-lib-etcd.mount"

# don't use a static hostname
hostname = ""

do_install_append() {
  set -x

  ln -s ../run/systemd/resolve/resolv.conf ${D}${sysconfdir}/resolv.conf
  ln -s ../usr/share/zoneinfo/UTC ${D}${sysconfdir}/localtime
  ln -sf ../proc/self/mounts ${D}${sysconfdir}/mtab

  install -d ${D}${systemd_networkdir}
  install -m 644 ${WORKDIR}/network-mac-for-usb ${D}${systemd_networkdir}/90-mac-for-usb.link
  install -m 644 ${WORKDIR}/eth-dhcp ${D}${systemd_networkdir}/90-dhcp.network

  install -D -m 644 ${WORKDIR}/timesyncd.conf ${D}${systemd_timesyncdconfdir}/90-fallback.conf

  for f in ${SYSTEMD_SERVICE_${PN}}; do
    install -D -m 644 -t ${D}/${systemd_system_unitdir} ${WORKDIR}/$f
  done

  install -D -m 644 ${WORKDIR}/kubelet.conf ${D}/${systemd_system_unitdir}/kubelet.service.d/50-containos.conf
}
