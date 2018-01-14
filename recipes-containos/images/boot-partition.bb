SUMMARY = "Perform approximate equivalent of wic's bootimg-partition.py"
DESCRIPTION = "This image provides essential boot files."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# list of files from $DEPLOY_DIR_IMAGE to place in boot partition.
# FIXME: do_image below doesn't support semicolon-based rename feature.
IMAGE_BOOT_FILES ??= ""

IMGDEPLOYDIR = "${WORKDIR}/deploy-${PN}-image-complete"

do_rootfs[depends] += "virtual/bootloader:do_deploy"

do_rootfs[cleandirs] += "${S}"
fakeroot do_rootfs() {
	for f in ${IMAGE_BOOT_FILES}; do
		cp ${DEPLOY_DIR_IMAGE}/$f ${S}/$f
	done
}

addtask rootfs after do_prepare_recipe_sysroot

do_image[cleandirs] += "${IMGDEPLOYDIR}"
fakeroot do_image() {
	tar -cf ${IMGDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.tar -C ${S} .
}

addtask do_image after do_rootfs

do_image_complete() {
	ln -s ${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.tar ${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}.tar
}

do_image_complete[dirs] = "${TOPDIR}"
do_image_complete[umask] = "022"
SSTATETASKS += "do_image_complete"
SSTATE_SKIP_CREATION_task-image-complete = '1'
do_image_complete[sstate-inputdirs] = "${IMGDEPLOYDIR}"
do_image_complete[sstate-outputdirs] = "${DEPLOY_DIR_IMAGE}"
do_image_complete[stamp-extra-info] = "${MACHINE}"
addtask do_image_complete after do_image before do_build
python do_image_complete_setscene () {
    sstate_setscene(d)
}
addtask do_image_complete_setscene
