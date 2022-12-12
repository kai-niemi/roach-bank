#!/bin/bash

FILE=bank-server/target/bank-server.jar
if [ ! -f "$FILE" ]; then
    chmod +x mvnw
    ./mvnw clean install
fi

fn_start_local(){
java -jar $FILE \
--spring.profiles.active=retry-none,cdc-none,crdb-local  \
"$@"
}

fn_start_dev(){
java -jar $FILE \
--spring.profiles.active=retry-driver,cdc-none,crdb-sleipner  \
"$@"
}

fn_start_custom(){
java -jar $FILE \
--spring.profiles.active=retry-none,cdc-none,crdb-local  \
"$@"
}

########################################

getopt=$1
shift

case "${getopt}" in
    local)
        fn_start_local "$*"
        ;;
    dev)
        fn_start_dev "$*"
        ;;
    custom)
        fn_start_custom "$*"
        ;;
    *)
    if [ -n "${getopt}" ]; then
        echo -e "Unknown command: $0 ${getopt}"
    fi
    echo -e "Usage: $0 [command]"
    echo -e "Commands"
    {
        echo -e "local"
        echo -e "dev"
        echo -e "custom"
    } | column -s $'\t' -t
esac
