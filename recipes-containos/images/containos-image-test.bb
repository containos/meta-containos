require containos-image-base.bb

DESCRIPTION = "Image used during auto-testing."

# NB: This combination allows ssh to root without a password (which is
# what we want for testing, but probably not otherwise!)
IMAGE_FEATURES += "debug-tweaks ssh-server-dropbear"

# kubernetes.py needs kubeadm package
TESTIMAGEDEPENDS_append = " kubernetes:do_build"

# Extra rootfs space for qemu images, so they can install docker
# images, etc. (in KB)
IMAGE_ROOTFS_EXTRA_SPACE_qemuall = "5242880"
