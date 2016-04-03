# This package only exists as a bootstrap dependency for go>=1.5
EXCLUDE_FROM_WORLD = "1"

DESCRIPTION = "\
  Go is an open source programming language that makes it easy to build simple, \
  reliable, and efficient software. \
  "
HOMEPAGE = "https://golang.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=591778525c869cdde0ab5a1bf283cd81"

SRC_URI = "http://golang.org/dl/go${PV}.src.tar.gz"

SRC_URI[md5sum] = "dfb604511115dd402a77a553a5923a04"
SRC_URI[sha256sum] = "9947fc705b0b841b5938c48b22dc33e9647ec0752bae66e50278df4f23f64959"

S = "${WORKDIR}/go"
B = "${S}/src"

inherit golang-base

export CC_FOR_TARGET = "${TARGET_PREFIX}gcc ${TARGET_CC_ARCH}${TOOLCHAIN_OPTIONS} --sysroot=${STAGING_DIR_TARGET}"
export CXX_FOR_TARGET = "${TARGET_PREFIX}g++ ${TARGET_CC_ARCH}${TOOLCHAIN_OPTIONS} --sysroot=${STAGING_DIR_TARGET}"
DEPENDS_class-cross += "virtual/${TARGET_PREFIX}gcc virtual/${TARGET_PREFIX}g++"
export CGO_ENABLED = "1"
## TODO: consider setting GO_EXTLINK_ENABLED

DEPENDS += "bash-native"

# "Stripping golang binaries causes crashes"
#  https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=717172
INHIBIT_PACKAGE_STRIP = "1"

# make.bash compiles+installs executables to GOBIN
export GOBIN = "${S}/bin"

# Executables look for GOROOT_FINAL at run-time.  Other packages will
# need to look below this GOROOT to find the bootstrap go environment.
export GOROOT_FINAL = "${libdir}/go-bootstrap"

do_compile() {
  bash -x ./make.bash
}

do_install() {
  set -x
  install -d "${D}${GOROOT_FINAL}"
  for dir in bin include lib pkg src; do
    cp -a "${S}/${dir}" "${D}${GOROOT_FINAL}/"
  done
}

BBCLASSEXTEND = "cross native nativesdk"
