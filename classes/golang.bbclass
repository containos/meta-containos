# This is for building go-based code

inherit golang-base

DEPENDS += "go-native"

GO_FLAGS ?= ""
GO_GCFLAGS ?= ""
GO_ASMFLAGS ?= ""
GO_LDFLAGS ?= ""

B = "${WORKDIR}/gopath"

SRC_URI ??= "git://${GOPKGROOT}.git"
GO_PACKAGE ?= "${GOPKGROOT}"

USE_GODEP ?= "no"
GO ?= "${@['go', 'godep go'][d.getVar('USE_GODEP', True) == 'yes']}"
GODEP_DEPENDS = "${@['', 'godep-native'][d.getVar('USE_GODEP', True) == 'yes']}"
DEPENDS += "${GODEP_DEPENDS}"

do_compile() {
  mkdir -p $(dirname "${B}/src/${GOPKGROOT}")
  ln -sf "${S}" "${B}/src/${GOPKGROOT}"

  # godep wants to be run from dir with $PWD/Godeps/_workspace (ie: ${S})
  (
  cd ${B}/src/${GOPKGROOT}
  GOPATH="${B}:${GOPATH}" \
  ${GO} install -x -asmflags "${GO_ASMFLAGS}" -gcflags "${GO_GCFLAGS}" -ldflags "${GO_LDFLAGS}" ${GO_FLAGS} ${GO_PACKAGE}
  )
}

# go binaries don't use GNU_HASH. Known, disable "QA Issue: No GNU_HASH in the elf binary: ..." warnings.
INSANE_SKIP_${PN} += "ldflags"

FILES_${PN}-staticdev += "${GO_LIBDIR}/src ${GO_LIBDIR}/pkg/${GOOS}_${GOARCH}/${GOPKGROOT}"

do_install() {
  set -x
  find ${B} -type f

  for dir in "${B}/bin" "${B}/Godeps/_workspace"; do
    for file in "$dir/${GOOS}_${GOARCH}"/* "$dir"/*; do
      if [ -f "$file" ]; then
        install -d "${D}${bindir}/"
	install -m 0755 "$file" "${D}${bindir}/"
      fi
    done
  done

  if [ -d "${B}/pkg/${GOOS}_${GOARCH}/${GOPKGROOT}" ]; then
    install -d "${D}${GO_LIBDIR}/pkg/${GOOS}_${GOARCH}/${GOPKGROOT}/"
    cp -r "${B}/pkg/${GOOS}_${GOARCH}/${GOPKGROOT}"/* "${D}${GO_LIBDIR}/pkg/${GOOS}_${GOARCH}/${GOPKGROOT}/"
  fi
}

inherit sanity

python () {
  if not d.getVar("GOPKGROOT", False):
    raise_sanity_error("%s: GOPKGROOT should be set" % d.getVar("P", True), d)
}
