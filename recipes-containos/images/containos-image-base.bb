SUMMARY = "A console-only image that fully supports the target device hardware."

# FIXME: re-enable this once dockerd works with a readonly /etc
#IMAGE_FEATURES += "read-only-rootfs"

LICENSE = "MIT"

inherit core-image

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
