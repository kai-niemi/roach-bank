#!/bin/bash

db_url="jdbc:cockroachdb://localhost:26257/roach_bank?sslmode=disable&retryTransientErrors=true&implicitSelectForUpdate=true"
#db_url="jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable"
spring_profile="--spring.profiles.active=retry-driver,crdb-local"
#spring_profile="--spring.profiles.active=retry-client,crdb-local,verbose"

java -jar bank-server.jar \
--spring.datasource.url="${db_url}" \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.profiles.active="${spring_profile}" \
--roachbank.default-account-limit=100 \
--roachbank.select-for-update=false "$*"

#nohup java -jar bank-server.jar \
#--spring.datasource.url="${db_url}" \
#--spring.datasource.username=root \
#--spring.datasource.password= \
#--spring.profiles.active="${spring_profile}" \
#--roachbank.default-account-limit=10 \
#--roachbank.select-for-update=false \
#> /dev/null 2>&1 &
