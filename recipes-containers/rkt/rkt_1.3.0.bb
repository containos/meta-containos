HOMEPAGE = "https://coreos.com/rkt/"
SECTION = "console/tools"
SUMMARY = "CLI for running app containers on Linux"
DESCRIPTION = "rkt (pronounced "rock-it") is a CLI for running app containers \
on Linux. rkt is designed to be secure, composable, and standards-based."

inherit golang-base autotools systemd useradd

SRC_URI = "\
        http://github.com/coreos/rkt/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
        file://cross-compile.patch \
        file://appc-arm-arch.patch \
	"
SRC_URI[md5sum] = "14a89e3a0fcf62480ac0c2c128b48c5a"
SRC_URI[sha256sum] = "0468c5b1079da8df65db9ebd0b712f64509acf6c8d6b34b780b6e787271d9b30"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=136e4f49dbf29942c572a3a8f6e88a77"

STAGE1_DEFAULT_LOCATION = "${libexecdir}/stage1-host.aci"

EXTRA_OECONF += "\
 --with-stage1-flavors=host,fly \
 --with-stage1-default-location=${STAGE1_DEFAULT_LOCATION} \
 "
PACKAGECONFIG[trousers] = "--enable-tpm,--disable-tpm,trousers,"

DEPENDS = "go-native \
    systemd \
    acl \
    "

RRECOMMENDS_${PN} += "ca-certificates"
RSUGGESTS_${PN} += "rkt-pubkeys"

# dlopened inside stage1
RDEPENDS_${PN} += "libacl"

# 'host' stage1 requires bash, systemd >= 222
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES','systemd','systemd','',d)}"

# 'host' copies various files from the host at runtime.
# See stage1/init/init.go:installAssets() for details.
# Current list is: bash systemctl systemd{,-shutdown,-journald} ldd
RDEPENDS_${PN} += "systemd (>= 222) bash ldd"

SYSTEMD_SERVICE_${PN} = "\
 rkt-gc.service rkt-gc.timer \
 rkt-metadata.service rkt-metadata.socket \
 "

# rkt doesn't support building outside srcdir
B = "${S}"

EXTRA_OEMAKE += "V=info CC_FOR_BUILD=${BUILD_CC} BUILDDIR=${B}/build-rkt"

systemd_tmpfilesdir = "${libdir}/tmpfiles.d"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "--system rkt"

FILES_${PN} += "${systemd_tmpfilesdir}"

do_install() {
  set -x

  install -d -m 755 ${D}/${bindir} ${D}${libexecdir} \
    ${D}${systemd_tmpfilesdir} ${D}${systemd_system_unitdir}

  install -m 755 ${B}/build-rkt/bin/rkt ${D}/${bindir}/
  install -m 644 ${B}/build-rkt/bin/stage1-host.aci ${D}/${STAGE1_DEFAULT_LOCATION}

  for f in ${SYSTEMD_SERVICE_${PN}}; do
    install -m 644 ${B}/dist/init/systemd/$f ${D}/${systemd_system_unitdir}/
  done
  install -m 644 ${B}/dist/init/systemd/tmpfiles.d/rkt.conf ${D}/${systemd_tmpfilesdir}


  if ${@bb.utils.contains('DISTRO_FEATURES','systemd','false','true',d)}; then
    # TODO: without systemd/tmpfiles.d/, this needs to be invoked on boot somehow
    install -d -m ${D}${datadir}
    install -D -m 755 ${B}/dist/scripts/setup-data-dir.sh ${D}${datadir}/
  fi
}
