#!/bin/sh

set -e
HOOK="$1"; shift

if [ "$2" = debug ]; then
    shift
    set -x
fi

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

case "$RAUC_SLOT_CLASS:$HOOK" in
    rootfs:slot-post-install)
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
