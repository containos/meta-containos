require containos-image-base.bb

DESCRIPTION = "Image suitable for debugging."

IMAGE_FEATURES += "tools-debug debug-tweaks dbg-pkgs doc-pkgs dev-pkgs tools-profile"
