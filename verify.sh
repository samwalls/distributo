#!/bin/bash

SHARE="/cs/scratch/${USER}/"
#bad, bad, magic variable...
COMPARE="/cs/studres/CS3102/Practicals/Practical\ 1/Files\ for\ replication\ experiments/"
TTY=$(tty)

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
    echo "diff'ing for ${client}============================"
    ssh -oStrictHostKeyChecking=no "${client}" "bash -c \"diff -rq ${SHARE} ${COMPARE}\"" > "${TTY}" 2> "${TTY}" < /dev/null
done