SUMMARY = "A console-only image that fully supports the target device hardware."

LICENSE = "MIT"

inherit core-image

# FIXME: re-enable this once dockerd works with a readonly /etc
#IMAGE_FEATURES += "read-only-rootfs"

# Bugfix.  testimage.bbclass fails to append this for _qemuall
TESTIMAGEDEPENDS_qemuall += "${@bb.utils.contains('IMAGE_PKGTYPE', 'ipk', 'opkg-utils-native:do_populate_sysroot', '', d)}"

# Needs more than a tiny amount of ram
QB_MEM = "-m 2048"

# Extra rootfs space for qemu images, so they can install docker
# images, etc. (in KB)
IMAGE_ROOTFS_EXTRA_SPACE_qemuall = "5242880"

WKS_FILE_DEPENDS_BOOTLOADERS += "virtual/bootloader"
do_image_wic[depends] += " \
	virtual/bootloader:do_deploy \
	"

WKS_FILE = "sdimage-dualboot.wks.in"
WIC_CREATE_EXTRA_ARGS += "--debug"
WIC_CREATE_EXTRA_ARGS += "--no-fstab-update"

read_only_rootfs_hook_append () {
	set -x
	if [ -f ${IMAGE_ROOTFS}/usr/lib/tmpfiles.d/tmp.conf ]; then
		sed -i '\!q /var/tmp !d' ${IMAGE_ROOTFS}/usr/lib/tmpfiles.d/tmp.conf
	fi
	if [ -f ${IMAGE_ROOTFS}/usr/lib/tmpfiles.d/home.conf ]; then
		sed -i '\!q /srv !d' ${IMAGE_ROOTFS}/usr/lib/tmpfiles.d/home.conf
	fi
	if [ -f ${IMAGE_ROOTFS}/usr/lib/tmpfiles.d/var.conf ]; then
		sed -i '\!d /var/log !d' ${IMAGE_ROOTFS}/usr/lib/tmpfiles.d/var.conf
	fi
}
