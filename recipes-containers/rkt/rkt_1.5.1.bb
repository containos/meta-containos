HOMEPAGE = "https://coreos.com/rkt/"
SECTION = "console/tools"
SUMMARY = "CLI for running app containers on Linux"
DESCRIPTION = "rkt (pronounced "rock-it") is a CLI for running app containers \
on Linux. rkt is designed to be secure, composable, and standards-based."

inherit golang-base autotools systemd useradd ptest

SRC_URI = "\
        http://github.com/coreos/rkt/archive/v${PV}.tar.gz;downloadfilename=${BP}.tar.gz \
        file://appc-arm-arch.patch \
        file://run-ptest \
	"
SRC_URI[md5sum] = "8bc1068b3cbc79038cc6d16908b086e9"
SRC_URI[sha256sum] = "2a9320b0fe41551e88e8477e4223ef9e2b7ebaf9947829235b5e936dbd61f5d4"

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
#  Current list is: bash systemctl systemd{,-shutdown,-journald} ldd
RDEPENDS_${PN} += "systemd (>= 222) bash ldd"
# go-systemd dlopens libsystemd.so at run-time
RDEPENDS_${PN} += "libsystemd (>= 222)"
# go-iptables calls out to iptables
RDEPENDS_${PN} += "iptables"

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

FILES_${PN} += "${datadir}/bash-completion"

do_compile_append () {
  oe_runmake GOARCH= CC=${BUILD_CC} bash-completion manpages
}

do_install () {
  set -x

  install -D -m 755 ${B}/build-rkt/bin/rkt ${D}${bindir}/rkt
  install -d ${D}${libexecdir}/
  install -m 644 -t ${D}${libexecdir}/ ${B}/build-rkt/bin/stage1-*.aci

  for f in ${SYSTEMD_SERVICE_${PN}}; do
    install -D -m 644 ${B}/dist/init/systemd/$f ${D}${systemd_system_unitdir}/$f
  done
  install -D -m 644 ${B}/dist/init/systemd/tmpfiles.d/rkt.conf ${D}${systemd_tmpfilesdir}/rkt.conf

  install -D -m 644 ${B}/dist/bash_completion/rkt.bash ${D}${datadir}/bash-completion/completions/rkt

  install -d ${D}${mandir}/man1
  install -m 644 -t ${D}${mandir}/man1 ${B}/dist/manpages/*.1

  if ${@bb.utils.contains('DISTRO_FEATURES','systemd','false','true',d)}; then
    # TODO: without systemd/tmpfiles.d/, this needs to be invoked on boot somehow
    install -D -m 755 ${B}/dist/scripts/setup-data-dir.sh ${D}${datadir}/setup-data-dir.sh
  fi
}