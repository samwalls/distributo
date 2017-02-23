#!/bin/bash

SHARE="/cs/scratch/${USER}"
TTY=$(tty)

#get the list of clients to connect to
if [ -z $1 ]; then
    echo "expected list of clients for which to clean the scratch folder: ${SHARE}"
    exit 1
fi
CLIENTS=()

while [ ! -z $1 ]; do
    CLIENTS+=( $1 )
    shift
done

for client in "${CLIENTS[@]}"; do
    echo "cleaning out client share space (${SHARE}) for ${client}"
    ssh -oStrictHostKeyChecking=no "${client}" "bash -c \"pkill -f distributo && rm ${SHARE}/*\"" > "${TTY}" 2> "${TTY}" < /dev/null
done