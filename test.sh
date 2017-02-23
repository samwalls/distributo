#!/bin/bash

# Test Runner

#get the list of clients to connect to
if [ -z $1 ]; then
    echo "expected list of clients to test with"
    exit 1
fi
CLIENTS=()

while [ ! -z $1 ]; do
    CLIENTS+=( $1 )
    shift
done

mkdir -p results
mkdir -p logs

WORKING_DIR=$(pwd)
if [ ! -d measure/ ] || [ -z "$(ls -A ${WORKING_DIR}/measure/)" ]; then
    echo "expected directory measure to contain test data"
    exit 1
fi

TTY=$(tty)

#attempt the test over all files in measure/
measureTime() {
    CMD="$1"
    shift
    ARGS="$@"
    T_START=$(date +"%s.%N")
    bash -c "${CMD} ${ARGS}" > "${TTY}" 2> "${TTY}"
    T_FINISH=$(date +"%s.%N")
    T_TOTAL=$(echo "x=${T_FINISH} - ${T_START}; if(x<1) print 0; x" | bc)
    echo "${T_TOTAL}"
}

echo "===================TESTING========================"
echo "Running tests for ${#CLIENTS[@]} clients:"
for client in "${CLIENTS[@]}"; do
    echo "- ${client}"
done
echo "=================================================="

./cleanscratch.sh "${CLIENTS[@]}"
for file in $(ls measure); do
    T=$(measureTime "./dst.sh measure/${file} logs ${CLIENTS[@]}")
    echo "${T}"
done
./verify.sh "$(pwd)/measure/" "${CLIENTS[@]}"