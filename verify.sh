#!/bin/bash

SHARE="/cs/scratch/${USER}/"
TTY=$(tty)

#get the compare path
if [ -z $1 ]; then
    echo "expected file to compare"
    exit 1
fi
LOCAL="$1"
shift

if [ -z $1 ]; then
    echo "expected file to compare"
    exit 1
fi
REMOTE="$1"
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
    echo "comparing for ${client}: ${LOCAL} to ${REMOTE}"
    ssh -oStrictHostKeyChecking=no "${client}" "bash -c \"diff -rq ${LOCAL} ${REMOTE}\"" > "${TTY}" 2> "${TTY}" < /dev/null
done