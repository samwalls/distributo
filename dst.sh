#!/bin/bash

# Distributo shell wrapper
# This wrapper is mainly built for use in the CS labs at St Andrews University; if you don't have a very similar setup
# this might not be useful.
#

USAGE="Usage: dst <file> <logpath> <client>+"

TTY=$(tty)

GROUP="232.1.1.232"
CONTROL_PORT="9511"
GROUP_PORT="9512"

SHARE="/cs/scratch/${USER}/"

#get the file to transfer
if [ -z $1 ]; then
    echo "$USAGE"
    exit 1
fi
FILE="$1"
shift

#get the location to write log output to
if [ -z $1 ]; then
    echo "expected log path"
    echo "$USAGE"
    exit 1
fi
LOG="$1"
shift


#get the list of clients to connect to
if [ -z $1 ]; then
    echo "expected list of clients to copy file to"
    echo "$USAGE"
    exit 1
fi
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

#start all clients
function clientStart {
    #setup all our clients to receive the file
    for client in "${CLIENTS[@]}"; do
        #run the ssh in the background, sending log output to the logfile, and error output to the sender's terminal
        nohup ssh -oStrictHostKeyChecking=no "${client}" "cd $(pwd) && ${DST} receive -s ${SHARE} -g ${GROUP} -gp ${GROUP_PORT} -c $(hostname) -cp ${CONTROL_PORT}" > "${LOG}/${client}.log" 2> "${TTY}" < /dev/null &
    done
}

echo "===================DISTRIBUTO====================="
echo "sharing ${FILE} to ${#CLIENTS[@]} clients:"
for client in "${CLIENTS[@]}"; do
    echo "- ${client}"
done
echo "at ${SHARE}"
echo "sending receiver log output to ${LOG}"
echo "=================================================="

#start listening for clients, and start the clients
clientStart &
${DST} send -f "${FILE}" -g "${GROUP}" -t "${#CLIENTS[@]}" -cp "${CONTROL_PORT}" -gp "${GROUP_PORT}"