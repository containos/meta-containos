# This is for building go code

inherit golang-base

DEPENDS += "go-native"

GO_GCFLAGS = ""
GO_ASMFLAGS = ""
GO_LDFLAGS = ""

B = "${WORKDIR}/gopath"

SRC_URI = "git://${GOPKGROOT}.git"
GO_PACKAGE = "${GOPKGROOT}"

do_compile() {
  mkdir -p $(dirname "${B}/src/${GOPKGROOT}")
  ln -sf "${S}" "${B}/src/${GOPKGROOT}"

  GOPATH="${B}:${GOPATH}" \
  go install -x -asmflags "${GO_ASMFLAGS}" -gcflags "${GO_GCFLAGS}" -ldflags "${GO_LDFLAGS}" "${GO_PACKAGE}"
}

FILES_${PN} = "${bindir}"
FILES_${PN}-staticdev = "${GO_LIBDIR}/src ${GO_LIBDIR}/pkg/${GOOS}_${GOARCH}/${GO_PACKAGE}.a"

do_install() {
  mkdir -p "${D}${GO_LIBDIR}" "${D}${bindir}"
  test -d "${B}/bin" && cp "${B}"/bin/* "${D}${bindir}"
  test -d "${B}/pkg" && cp -r "${B}/pkg" "${D}${GO_LIBDIR}"
}
