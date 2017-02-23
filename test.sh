#!/bin/bash

# Test Runner

SHARE="/cs/scratch/${USER}"

#get the number of test times to capture
if [ -z $1 ]; then
    echo "expected number"
    exit 1
fi
N_TEST=$1
shift

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

average() {
    ELEMENTS=("$@")
    SUM="0"
    for item in "${ELEMENTS[@]}"; do
        SUM=$(echo "x=${SUM} + ${item}; if(x<1) print 0; x" | bc)
    done
    AVG=$(echo "scale=10; x=${SUM} / ${#ELEMENTS[@]}; if(x<1) print 0; x" | bc)
    echo "${AVG}"
}
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

declare -A FILETIMES
declare -A DSTTIMES
declare -A SCPTIMES
declare -A SFTPTIMES

#get a row of CSV output
measureAverageTimeDst() {
    N=$1
    shift
    FILE=$1
    shift
    ROW=()
    SHARED_FILE="${WORKING_DIR}/measure/${FILE}"
    COPIED_FILE="${SHARE}/${FILE}"

    #measure for dst
    echo "=========================================================="
    echo "getting average for dst"
    DATA=()
    for i in `seq "${N}"`; do
        ./cleanscratch.sh "${CLIENTS[@]}"
        DATA+=( $(measureTime "./dst.sh ${SHARED_FILE} logs ${CLIENTS[@]}") )
        ./verify.sh "${SHARED_FILE}" "${COPIED_FILE}" "${CLIENTS[@]}"
    done
    echo "averaging data: ${DATA[@]}"
    AVG=$(average "${DATA[@]}")
    DSTTIMES["${FILE}"]="${AVG}"
}

measureAverageTimeScp() {
    N=$1
    shift
    FILE=$1
    shift
    ROW=()
    SHARED_FILE="${WORKING_DIR}/measure/${FILE}"
    COPIED_FILE="${SHARE}/${FILE}"

    #measure for scp
    echo "=========================================================="
    echo "getting average for scp"
    DATA=()
    for i in `seq "${N}"`; do
        ./cleanscratch.sh "${CLIENTS[@]}"
        SUM_TIME="0"
        for client in "${CLIENTS[@]}"; do
            T=$(measureTime "scp ${SHARED_FILE} ${client}:${SHARE}/${FILE}")
            SUM_TIME=$(echo "x=${SUM_TIME} + ${T}; if(x<1) print 0; x" | bc)
        done
        DATA+=( "${SUM_TIME}" )
    done
    #./verify.sh "${SHARED_FILE}" "${COPIED_FILE}" "${CLIENTS[@]}"
    echo "averaging data: (${DATA[@]})"
    AVG=$(average "${DATA[@]}")
    SCPTIMES["${FILE}"]="${AVG}"
}

measureAverageTimeSftp() {
    N=$1
    shift
    FILE=$1
    shift
    ROW=()
    SHARED_FILE="${WORKING_DIR}/measure/${FILE}"
    COPIED_FILE="${SHARE}/${FILE}"

    #measure for sftp
    echo "=========================================================="
    echo "getting average for sftp"
    DATA=()
    for i in `seq "${N}"`; do
        ./cleanscratch.sh "${CLIENTS[@]}"
        SUM_TIME="0"
        for client in "${CLIENTS[@]}"; do
            T=$(measureTime "sftp ${client}:${SHARE} <<< $'put ${SHARED_FILE}'")
            SUM_TIME=$(echo "x=${SUM_TIME} + ${T}; if(x<1) print 0; x" | bc)
        done
        DATA+=( "${SUM_TIME}" )
    done
    #./verify.sh "${SHARED_FILE}" "${COPIED_FILE}" "${CLIENTS[@]}"
    echo "averaging data: ${DATA[@]}"
    AVG=$(average "${DATA[@]}")
    SFTPTIMES["${FILE}"]="${AVG}"
}

for file in $(ls measure); do
    measureAverageTimeDst "${N_TEST}" "${file}"
    measureAverageTimeScp "${N_TEST}" "${file}"
    measureAverageTimeSftp "${N_TEST}" "${file}"
done

echo "====================RESULTS======================="
echo "results in seconds, averaged over ${N_TEST} runs; for replication to ${#CLIENTS[@]} nodes"
for file in $(ls measure); do
    DST="${DSTTIMES[${file}]}"
    SCP="${SCPTIMES[${file}]}"
    SFTP="${SFTPTIMES[${file}]}"
    echo "${file}: "
    echo "DST : ${DST}"
    echo "SCP : ${SCP}"
    echo "SFTP: ${SFTP}"
    echo ""
done