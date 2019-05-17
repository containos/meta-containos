inherit bundle

SUMMARY = "ContainOS update bundle"

SRC_URI += "file://hook.sh"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "\
        file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
	"

RAUC_BUNDLE_COMPATIBLE ?= "${MACHINE}"
RAUC_BUNDLE_VERSION = "${DISTRO_VERSION}"
RAUC_BUNDLE_SLOTS = "rootfs"

RAUC_BUNDLE_HOOKS[file] = "hook.sh"

# FIXME: should be containos-image-base once I solve bootstrap auth
RAUC_SLOT_rootfs = "containos-image-test"
RAUC_SLOT_rootfs[fstype] = "ext4"
RAUC_SLOT_rootfs[type] = "image"
RAUC_SLOT_rootfs[hooks] = "post-install"

RAUC_SLOT_kernel ?= "virtual/kernel"
RAUC_SLOT_kernel[type] = "kernel"
RAUC_SLOT_kernel[file] = "${KERNEL_IMAGETYPE}-${MACHINE}.bin"

RAUC_SLOT_boot ?= "boot-partition"
RAUC_SLOT_boot[type] = "image"
RAUC_SLOT_boot[fstype] = "tar"

RAUC_SLOT_bootloader ?= "virtual/bootloader"
RAUC_SLOT_bootloader[type] = "boot"
RAUC_SLOT_bootloader[file] = "${UBOOT_IMAGE}"
RAUC_SLOT_bootloader[hooks] = "install"

RAUC_SLOT_dtb ?= "kernel-devicetree"
RAUC_SLOT_dtb[type] = "file"
RAUC_SLOT_dtb[file] = "${KERNEL_DEVICETREE}"
RAUC_SLOT_dtb[hooks] = "install"

do_bundle_prepend() {
	set -x
}

do_deploy_prepend() {
	set -x
}

# Built explicitly, does not need to be part of world.
EXCLUDE_FROM_WORLD = "1"
