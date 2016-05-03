SUMMARY = "Some \"well-known\" rkt trusted keys"
DESCRIPTION = "\
  This package pre-seeds rkt with various \"well-known\" signing keys.\
  \
  Note this does not imply any greater trust beyond verifying that the\
  downloaded container image is unmodified.\
"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://../GPL-2;md5=94d55d512a9ba36caa9b7df079bae19f"

inherit allarch

SRC_URI = "\
    file://quay-io.gpg \
    file://coreos-com.gpg \
    file://GPL-2 \
    "

rkt_trusted_prefixdir = "${libdir}/rkt/trustedkeys/prefix.d"

FILES_${PN} += "${rkt_trusted_prefixdir}"

do_install() {
  install -d -m 0755 ${D}${rkt_trusted_prefixdir}/quay.io
  install -m 0644 ${WORKDIR}/quay-io.gpg ${D}${rkt_trusted_prefixdir}/quay.io/bff313cdaa560b16a8987b8f72abf5f6799d33bc

  install -d -m 0755 ${D}${rkt_trusted_prefixdir}/coreos.com/etcd
  install -m 0644 ${WORKDIR}/coreos-com.gpg ${D}${rkt_trusted_prefixdir}/coreos.com/etcd/8b86de38890ddb7291867b025210bd8888182190
}
