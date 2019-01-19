#!/bin/sh

WORKDIR=/run/device-autorun

notify() {
    local event=$1

    set -- /sys/class/leds/*:usr/trigger
    if [ -e $1 ]; then
        t=$1
    else
        t=/dev/null
    fi

    case $event in
        start)
            echo heartbeat > $t
            ;;
        failure)
            # in case 'timer' is not supported
            echo none > $t
            echo timer > $t
            ;;
        success)
            echo none > $t
            ;;
    esac
}

onexit() {
    code=$?
    set +e

    if [ $code -eq 0 ]; then
        notify success
        echo "SUCCESS"
    else
        notify failure
        echo "FAILED"
    fi

    cd /
    umount $WORKDIR/mnt
}
trap onexit EXIT

device=$1; shift

set -e

notify start

# filesystem may contain credentials and other secrets, so hide behind
# a restricted mount point
mkdir -p $WORKDIR/mnt
chown 0700 $WORKDIR

mount -t auto $device $WORKDIR/mnt

cd $WORKDIR/mnt
sh ./autorun.sh >autorun.log 2>&1
