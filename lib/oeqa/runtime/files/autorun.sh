#!/bin/sh

set -e -x

{
    # bootstrap_autorun.py test case looks for this string
    echo 'Autorun was here'
    echo "id: $(id)"
    echo "pwd: $(pwd)"
    env
} > /tmp/autorun.log.tmp

# bootstrap_autorun.py test case looks for this file
mv /tmp/autorun.log.tmp /tmp/autorun.log

if [ -e /tmp/autorun-test-should-fail ]; then
    exit 1
fi

exit 0
