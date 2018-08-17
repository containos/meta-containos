#!/bin/sh

set -e

slotcp() {
    flags=""
    case "$1" in
	-r)
	    flags="$1"; shift
	    ;;
    esac

    for arg; do
	if [ -e "$arg" ]; then
	    if [ "x$flags" = "x-r" ]; then
		rm -r $RAUC_SLOT_MOUNT_POINT"$arg"
	    fi
	    mkdir -p $RAUC_SLOT_MOUNT_POINT$(dirname "$arg")
	    cp -dP $flags "$arg" "$RAUC_SLOT_MOUNT_POINT$arg"
	fi
    done
}

case "$1" in
    slot-post-install)
	test "$RAUC_SLOT_CLASS" = rootfs || exit 0

	slotcp /etc/hostname
	slotcp /etc/machine-id

	slotcp /etc/dropbear/dropbear_rsa_host_key
	slotcp /etc/ssh/ssh_host*

	slotcp -r /etc/kubernetes

	# This provides no value afaics, but prevents docker from
	# modifying an otherwise readonly filesystem
	# FIXME: symlink this to /run or somewhere equally harmless instead
	slotcp /etc/docker/key.json
	;;
    *)
	exit 1
	;;
esac

exit 0
