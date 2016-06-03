DESCRIPTION = "\
  Go is an open source programming language that makes it easy to build simple, \
  reliable, and efficient software. \
  "
HOMEPAGE = "https://golang.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=591778525c869cdde0ab5a1bf283cd81"

SRC_URI = "http://golang.org/dl/go${PV}.src.tar.gz"

SRC_URI[md5sum] = "e67833ea37fbc002fbe38efe6c1bcd98"
SRC_URI[sha256sum] = "a96cce8ce43a9bf9b2a4c7d470bc7ee0cb00410da815980681c8353218dcf146"

SRC_URI += "\
    file://cc-args.patch \
    "

S = "${WORKDIR}/go"
B = "${S}/src"

## TODO: consider setting GO_EXTLINK_ENABLED
export CC_FOR_TARGET = "${TARGET_PREFIX}gcc ${TARGET_CC_ARCH}${TOOLCHAIN_OPTIONS} --sysroot=${STAGING_DIR_TARGET}"
export CXX_FOR_TARGET = "${TARGET_PREFIX}g++ ${TARGET_CC_ARCH}${TOOLCHAIN_OPTIONS} --sysroot=${STAGING_DIR_TARGET}"
TOOLCHAIN_DEPS = "virtual/${TARGET_PREFIX}gcc virtual/${TARGET_PREFIX}g++"
TOOLCHAIN_DEPS_class-native = ""
DEPENDS += "${TOOLCHAIN_DEPS}"

inherit golang-base

export CGO_ENABLED = "1"

DEPENDS += "go-initial-native bash-native"
export GOROOT_BOOTSTRAP = "${STAGING_LIBDIR_NATIVE}/go-bootstrap"

# "Stripping golang binaries causes crashes"
#  https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=717172
# Also, without this package.bbclass tries to strip the
# non-native-arch binaries in pkg/tool/* and fails (including these
# other executables is probably a packaging error, fwiw)
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"

# make.bash compiles+installs executables to GOBIN
export GOBIN = "${S}/bin"

# executables look for GOROOT_FINAL at run-time
export GOROOT_FINAL = "${libdir}/${BPN}"

do_compile() {
  bash -x ./make.bash
}

do_test() {
  GOROOT=${S} \
  PATH=$GOBIN/${GOOS}_${GOARCH}:$GOBIN:$PATH \
  CC=$CC_FOR_TARGET \
  CXX=$CXX_FOR_TARGET \
  bash -x ./run.bash --no-rebuild
}

# TODO(gus): Work out when/if to run testsuite.
# run.bash tests only work on native arch, so aren't actually much use
# as a ptest.
# addtask test after do_compile before do_install

do_install() {
  set -x
  if [ -d "${GOBIN}/${GOOS}_${GOARCH}" ]; then
    bin_staging_dir="${GOBIN}/${GOOS}_${GOARCH}"
  else
    bin_staging_dir="${GOBIN}"
  fi
  install -d "${D}${bindir}"
  install -m 0755 "$bin_staging_dir/go" "$bin_staging_dir/gofmt" "${D}${bindir}/"

  install -d "${D}${GOROOT_FINAL}"
  for dir in lib pkg src; do
    cp -a "${S}/${dir}" "${D}${GOROOT_FINAL}/"
  done
}

BBCLASSEXTEND = "native nativesdk"
