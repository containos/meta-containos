require containos-image-base.bb

DESCRIPTION = "Image used during auto-testing."

# NB: This combination allows ssh to root without a password (which is
# what we want for testing, but probably not otherwise!)
IMAGE_FEATURES += "debug-tweaks ssh-server-dropbear"
