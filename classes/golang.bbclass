# This is for building go-based code
#
# Just like go.bbclass, only with ${S} pointing to the source
# directory directly rather than $GOROOT

inherit go

# go.bbclass doesn't set these?!
export GOOS = "${TARGET_GOOS}"
export GOARCH = "${TARGET_GOARCH}"

do_configure[cleandirs] = "${B}/src/${GO_IMPORT}"
golang_do_configure() {
	# This _should_ be as easy as:
        #     ln -snf ${S} ${B}/src/${GO_IMPORT}
	# ... except go refuses to follow symlinks when expanding
        # package patterns :(

        if [ ${B} != ${S} ]; then
        	tar -C ${S} -cf - --exclude-vcs . | tar -C ${B}/src/${GO_IMPORT} -xf -
        fi
}

SRC_URI ??= "git://${GO_IMPORT}.git"

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

        ( cd ${B}/src/${GO_IMPORT} && \
          find . -path ./vendor -prune -o \
        	-type f -name \*.go -print > ${WORKDIR}/gosrc.list )
        tar -C ${B}/src/${GO_IMPORT} -cf - --exclude-vcs --verbatim-files-from -T ${WORKDIR}/gosrc.list | \
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
}
