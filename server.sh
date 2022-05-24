#!/bin/bash

java -jar bank-server/target/bank-server.jar \
--spring.profiles.active=retry-default,cdc-default,crdb,crdb-sleipner  \
"$@"
