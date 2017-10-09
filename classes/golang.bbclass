# This is for building go-based code

inherit golang-base

# x32 ABI is not supported on go compiler so far
COMPATIBLE_HOST_linux-gnux32 = "null"
# ppc32 is not supported in go compilers
COMPATIBLE_HOST_powerpc = "null"

# FIXME: go-cross shouldn't exist. go >=1.5 is capable of cross
# compiling from go-native directly
DEPENDS += "go-cross-${TARGET_ARCH}"
DEPENDS_class-native += "go-native"

export CGO_ENABLED = "1"
export CGO_CFLAGS = "${CFLAGS}"
export CGO_CPPFLAGS = "${CPPFLAGS}"
export CGO_CXXFLAGS = "${CXXFLAGS}"
export CGO_LDFLAGS = "${LDFLAGS}"

GO = "go"

GO_FLAGS ?= ""
GO_GCFLAGS ?= ""
GO_ASMFLAGS ?= ""
GO_LDFLAGS ?= ""

# FIXME: remove $TARGET_SYS from this path - go already prefixes
# arch-specific libs
export GOROOT = "${STAGING_LIBDIR_NATIVE}/${TARGET_SYS}/go"
GOROOT_class-native = "${STAGING_LIBDIR_NATIVE}/go"

# FIXME: remove $TARGET_SYS from this path - go already prefixes
# arch-specific libs
GOROOT_FINAL = "${libdir}/${TARGET_SYS}/go"

# go requires a particular source directory location
GOPATH_TOP = "${WORKDIR}/gopath"
S = "${GOPATH_TOP}/src/${GO_IMPORT}"
export GOPATH = "${GOPATH_TOP}:${STAGING_LIBDIR}/${TARGET_SYS}/go"
UNPACK_STRIP_PREFIX = "${BP}"

SRC_URI ??= "git://${GO_IMPORT}.git"

GO_INSTALL ?= "${GO_IMPORT}/..."

do_unpack[dirs] += "${S}"
base_do_unpack_append() {
    s = d.getVar("S")
    unpackdir = os.path.join(d.getVar("WORKDIR"), d.getVar("UNPACK_STRIP_PREFIX"))
    if s != unpackdir:
        bb.utils.mkdirhier(os.path.dirname(s))
        bb.utils.remove(s, recurse=True)
        import shutil
        shutil.move(unpackdir, s)
}

go_do_compile() {
  if [ "${GO_INSTALL}" != "" ]; then
    ${GO} install -x -asmflags "${GO_ASMFLAGS}" -gcflags "${GO_GCFLAGS}" -ldflags "${GO_LDFLAGS}" ${GO_FLAGS} ${GO_INSTALL}
  fi
}

do_compile() {
  go_do_compile
}

# go binaries don't use GNU_HASH. Known, disable "QA Issue: No GNU_HASH in the elf binary: ..." warnings.
INSANE_SKIP_${PN} += "ldflags"

FILES_${PN}-staticdev += "${GOROOT_FINAL}/src ${GOROOT_FINAL}/pkg"

go_do_install() {
  for file in ${GOPATH_TOP}/bin/${GOOS}_${GOARCH}/* ${GOPATH_TOP}/bin/*; do
    if [ -f "$file" ]; then
      install -D -m 0755 -t ${D}${bindir}/ $file
    fi
  done

  if [ -d ${GOPATH_TOP}/pkg/${GOOS}_${GOARCH}/${GO_IMPORT} ]; then
    install -d ${D}${GOROOT_FINAL}/pkg/${GOOS}_${GOARCH}/${GO_IMPORT}
    tar -C ${GOPATH_TOP}/pkg/${GOOS}_${GOARCH}/${GO_IMPORT} -cf - --exclude=./vendor . | \
      tar -C ${D}${GOROOT_FINAL}/pkg/${GOOS}_${GOARCH}/${GO_IMPORT} -xvf -
    chown -R root:root ${D}${GOROOT_FINAL}/pkg
  fi
}

do_install() {
  go_do_install
}

inherit sanity

python () {
  if not d.getVar("GO_IMPORT", False):
    raise_sanity_error("%s: GO_IMPORT should be set" % d.getVar("P", True), d)
}
