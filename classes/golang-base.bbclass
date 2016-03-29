export GOOS = "${TARGET_OS}"
GOOS_linux-gnu = "linux"
GOOS_linux-gnueabi = "linux"

export GOARCH = "${TARGET_ARCH}"
GOARCH_x86 = "386"
GOARCH_x86-64 = "amd64"
GOARCH_aarch64 = "arm64"

export GOARM
GOARM_armv5 = "5"
GOARM_armv6 = "6"
GOARM_armv7a = "7"

export GO386 = "${@bb.utils.contains('TUNE_FEATURES','pentium4','sse2',bb.utils.contains('TUNE_FEATURES','i586','387','',d),d)}"

export CGO_ENABLED = "1"
export CGO_CFLAGS = "${CFLAGS}"
export CGO_CPPFLAGS = "${CPPFLAGS}"
export CGO_CXXFLAGS = "${CXXFLAGS}"
export CGO_LDFLAGS = "${LDFLAGS}"

GO_LIBDIR = "${libdir}/go"
export GOPATH = "${GO_LIBDIR}"
