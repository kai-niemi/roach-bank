#!/bin/bash

FILE=bank-client/target/bank-client.jar
if [ ! -f "$FILE" ]; then
    chmod +x mvnw
    ./mvnw clean install
fi

java -jar $FILE "$@"