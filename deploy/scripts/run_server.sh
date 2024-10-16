#!/bin/bash

db_url="jdbc:postgresql://localhost:26257/roach_bank??ssl=true&sslmode=require"
spring_profile="retry-client,pgjdbc-local"

nohup java -jar bank-server.jar \
--spring.datasource.url="${db_url}" \
--spring.datasource.username=root \
--spring.datasource.password=cockroach \
--spring.profiles.active="${spring_profile}" \
--roachbank.default-account-limit=10 \
> /dev/null 2>&1 &
