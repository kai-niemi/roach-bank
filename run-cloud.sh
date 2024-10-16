#!/bin/bash

java -jar bank-server/target/bank-server.jar \
--spring.datasource.url="jdbc:postgresql://kai-odin-qmg.aws-eu-north-1.cockroachlabs.cloud:26257/roach_bank?sslmode=require&sslrootcert=$HOME/Library/CockroachCloud/certs/277cd25a-97f3-467b-8bf1-1e7f6fa69840/kai-odin-ca.crt" \
--spring.datasource.username=craig \
--spring.datasource.password=cockroach \
--spring.profiles.active=retry-client \
--server.port=8090 $*