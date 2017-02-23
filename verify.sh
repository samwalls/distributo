#!/bin/bash

SHARE="/cs/scratch/${USER}/"
TTY=$(tty)

#get the compare path
if [ -z $1 ]; then
    echo "expected path to compare share root with"
    exit 1
fi
COMPARE="$1"
shift

#get the list of clients to connect to
if [ -z $1 ]; then
    echo "expected list of clients to verify"
    exit 1
fi
CLIENTS=()

while [ ! -z $1 ]; do
    CLIENTS+=( $1 )
    shift
done

for client in "${CLIENTS[@]}"; do
    echo "diffing for ${client}============================"
    echo "comparing ${SHARE} to ${COMPARE}"
    ssh -oStrictHostKeyChecking=no "${client}" "bash -c \"diff -rq ${SHARE} ${COMPARE}\"" > "${TTY}" 2> "${TTY}" < /dev/null
done