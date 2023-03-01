#!/bin/bash

db_url="jdbc:cockroachdb://localhost:26257/roach_bank?sslmode=disable&retryTransientErrors=true&implicitSelectForUpdate=true"
#db_url="jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable"
spring_profile="--spring.profiles.active=retry-driver,cdc-none,crdb-local"
#spring_profile="--spring.profiles.active=retry-client,cdc-none,crdb-local,verbose"

java -jar bank-server.jar \
--spring.datasource.url="${db_url}" \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.profiles.active="${spring_profile}" \
--roachbank.accountsPerCityLimit=10 \
--roachbank.updateRunningBalance=false \
--roachbank.selectForUpdate=false "$*"

#nohup java -jar bank-server.jar \
#--spring.datasource.url="${db_url}" \
#--spring.datasource.username=root \
#--spring.datasource.password= \
#--spring.profiles.active="${spring_profile}" \
#--roachbank.accountsPerCityLimit=10 \
#--roachbank.updateRunningBalance=false \
#--roachbank.selectForUpdate=false \
#> /dev/null 2>&1 &
