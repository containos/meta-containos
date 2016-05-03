require containos-image-base.bb

DESCRIPTION = "Image suitable for testing."

IMAGE_FEATURES += "tools-debug ptest-pkgs"

DISTRO_EXTRA_RDEPENDS += "\
    ptest-runner \
    "
