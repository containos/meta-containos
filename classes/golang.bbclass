# This is for building go-based code
#
# Just like go.bbclass, only with ${S} pointing to the source
# directory directly rather than $GOROOT

# go.bbclass doesn't set these?!
export GOOS = "${TARGET_GOOS}"
export GOARCH = "${TARGET_GOARCH}"

# go.bbclass includes "-ldflags='...'" in value for GO_LDFLAGS, which
# makes it impossible to append more LDFLAGS :(
GO_LDFLAGS = "${GO_RPATH} ${GO_LINKMODE} -extldflags '${GO_EXTLDFLAGS}'"
export GOBUILDFLAGS = '-v -ldflags="${GO_LDFLAGS}"'

# Note: Needs to be explicit package names, and not a ./... wildcard.
# See comment in do_configure below for why.
GO_INSTALL ?= "${GO_IMPORT}"

SRC_URI ??= "git://${GO_IMPORT}.git"

inherit go

golang_do_configure() {
	# NB: go developers hate symlinks.  The "foo/..." wildcard
	# will deliberately _not_ follow symlinks, but all explicit
	# paths (imports and command line args) _will_.	 So the
	# following works for everything, except unfortunately not
	# "..." wildcards :(
	mkdir -p $(dirname ${B}/src/${GO_IMPORT})
	ln -snf ${S} ${B}/src/${GO_IMPORT}
}

# Undo go_do_unpack changes
python golang_do_unpack() {
    src_uri = (d.getVar('SRC_URI') or "").split()
    if len(src_uri) == 0:
        return

    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.unpack(d.getVar('WORKDIR'))
    except bb.fetch2.BBFetchException as e:
        bb.fatal(str(e))
}

golang_do_install() {
	install -d ${D}${libdir}/go/src/${GO_IMPORT}
        tar -C ${B} -cf - pkg | tar -C ${D}${libdir}/go --no-same-owner -xf -

        ( cd ${S} && \
          find . -path ./vendor -prune -o \
        	-type f -name \*.go -print > ${WORKDIR}/gosrc.list )
        tar -C ${S} -cf - --exclude-vcs --verbatim-files-from -T ${WORKDIR}/gosrc.list | \
        	tar -C ${D}${libdir}/go/src/${GO_IMPORT} --no-same-owner -xf -

	for file in ${B}/${GO_BUILD_BINDIR}/*; do
        	if [ -f $file ]; then
                	install -D -m 0755 -t ${D}${bindir}/ $file
                fi
        done
}

EXPORT_FUNCTIONS do_configure do_unpack do_install

inherit sanity

python () {
  if not d.getVar("GO_IMPORT", False):
    raise_sanity_error("%s: GO_IMPORT should be set" % d.getVar("P", True), d)
  if "..." in d.getVar("GO_INSTALL", True):
    raise_sanity_error("%s: GO_INSTALL may not use ... wildcard" % d.getVar("P", True), d)
}
