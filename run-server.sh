#!/bin/bash

FILE=bank-server/target/bank-server.jar
if [ ! -f "$FILE" ]; then
    chmod +x mvnw
    ./mvnw clean install
fi

fn_start_local(){
java -jar $FILE --spring.profiles.active=retry-client,cdc-none,crdb-local "$@"
}

fn_start_dev(){
java -jar $FILE --spring.profiles.active=retry-client,cdc-none,crdb-dev "$@"
}

fn_start_cloud(){
java -jar $FILE --spring.profiles.active=retry-client,cdc-none,crdb-cloud "$@"
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
    cloud)
        fn_start_cloud "$*"
        ;;
    *)
    if [ -n "${getopt}" ]; then
        echo -e "Unknown command: $0 ${getopt}"
    fi
    echo -e "Usage: $0 [command]"
    echo -e "Commands:"
    {
        echo -e "local"
        echo -e "dev"
        echo -e "cloud"
    } | column -s $'\t' -t
esac
