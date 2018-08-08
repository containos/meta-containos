SUMMARY = "An Enhanced Seccomp (mode 2) Helper Library"
HOMEPAGE = "https://github.com/seccomp/libseccomp"
SECTION = "libs/security"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7c13b3376cea0ce68d2d2da0a1b3a72c"

UPSTREAM_CHECK_URI = "https://github.com/seccomp/libseccomp/releases"
UPSTREAM_CHECK_REGEX = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

SRC_URI = "https://github.com/seccomp/libseccomp/releases/download/v${PV}/libseccomp-${PV}.tar.gz \
          "

SRC_URI[md5sum] = "e6f3e84921ef9c2e9188681963f0943f"
SRC_URI[sha256sum] = "ff5bdd2168790f1979e24eaa498f8606c2f2d96f08a8dc4006a2e88affa4562b"

inherit autotools pkgconfig

PACKAGES += "${PN}-utils"

FILES_${PN} = "${libdir}/libseccomp-2.so.* \
               ${libdir}/libseccomp.so.* \
               ${sysconfdir}"

FILES_${PN}-utils = "${bindir}"
