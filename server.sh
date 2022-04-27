#!/bin/bash

java -jar bank-server/target/bank-server.jar \
--spring.profiles.active=crdb,retry-backoff,cdc-none,crdb-dev  \
"$@"
