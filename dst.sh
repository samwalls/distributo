#!/bin/bash

# Distributo shell wrapper
# This wrapper is mainly built for use in the CS labs at St Andrews University; if you don't have a very similar setup
# this might not be useful.
#

USAGE="Usage: dst <file> <path> <logpath> <client>+"

GROUP="230.1.1.1"

if [ -z $1 ]; then
    echo "$USAGE"
    exit 1
fi

#get the file to transfer
FILE="$1"
shift

if [ -z $1 ]; then
    echo "$USAGE"
    exit 1
fi

#get the location to download files to
SHARE="$1"
shift

if [ -z $1 ]; then
    echo "expected log path"
    echo "$USAGE"
    exit 1
fi

#get the location to write log output to
LOG="$1"
shift

if [ -z $1 ]; then
    echo "expected list of clients to copy file to"
    echo "$USAGE"
    exit 1
fi

#get the list of clients to connect to
CLIENTS=()

while [ ! -z $1 ]; do
    CLIENTS+=( $1 )
    shift
done

#for client in "${CLIENTS[@]}"; do
#    echo "$client"
#done

#might be useful if necessary
#get a list of key-value pairs: the client address paired with the place
#declare -A CLIENTS
#while [ ! -z $1 ]; do
#    CLIENTS[$1]="$2"
#    shift 2
#done

#for key in ${!CLIENTS[@]}; do
#    echo ${key} ${CLIENTS[${key}]}
#done

# find the latest distributo version
if [ -d target ]; then
    JARS=$(ls target | grep ^distributo | sort -r --version-sort)
    if [ -z "${JARS}" ]; then
        echo "no distributo jar found - has it been built?"
        exit 1
    fi
    JAR=$(echo "${JARS}"  | awk 'NR==1 {print;exit}')
    if [ -z "${JARS}" ]; then
        echo "no distributo jar found - has it been built?"
        exit 1
    fi
else
    echo "no distributo jar found - has it been built?"
    exit 1
fi

DST="java -jar target/${JAR}"

#delete all space in the share root
function cleanScratch {
    for client in "${CLIENTS[@]}"; do
        echo "cleaning out client share space for ${client}"
        nohup ssh "${client}" "$(rm ${SHARE}*)" > "${LOG}/${client}.log" 2> "${LOG}/${client}-error.log" < /dev/null &
    done
}

#start all clients
function clientStart {
    #setup all our clients to receive the file
    for client in "${CLIENTS[@]}"; do
        #run the ssh in the background, sending all output to the logfile
        nohup ssh "${client}" "cd $(pwd) && ${DST} receive -s ${SHARE} -g ${GROUP} -c $(hostname)" > "${LOG}/${client}.log" 2> "${LOG}/${client}-error.log" < /dev/null &
    done
}

echo "===================DISTRIBUTO====================="
echo "sharing ${FILE} to clients:"
for client in "${CLIENTS[@]}"; do
    echo "- ${client}"
done
echo "at ${SHARE}"
echo "sending log output to ${LOG}"
echo "=================================================="

cleanScratch

#start listening for clients, and start the clients
clientStart &
${DST} send -f "${FILE}" -g "${GROUP}"