#!/bin/bash

db_url="jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable"
spring_profile="--spring.profiles.active=retry-client,pgjdbc-local"

nohup java -jar bank-server.jar \
--spring.datasource.url="${db_url}" \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.profiles.active="${spring_profile}" \
--roachbank.default-account-limit=10 \
> /dev/null 2>&1 &
