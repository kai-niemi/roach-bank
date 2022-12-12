#!/bin/bash

java -jar bank-server/target/bank-server.jar \
--spring.profiles.active=retry-none,cdc-none,crdb,crdb-sleipner  \
"$@"
