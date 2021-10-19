#!/bin/bash

java -jar bank-server/target/bank-server.jar \
--spring.datasource.url=jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable \
"$@"

#java -jar server/target/bank-server.jar \
#--spring.datasource.url=jdbc:postgresql://192.168.1.99:26300/roach_bank?sslmode=disable \
#--roachbank.health.admin-endpoint=http://192.168.1.99 \
#"$@"
