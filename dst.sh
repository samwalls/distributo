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

# now we have a jar, pass extra arguments to it an run
java -jar "target/${JAR}" "$@"