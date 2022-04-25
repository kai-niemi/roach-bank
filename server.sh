#!/bin/bash

java -jar bank-server/target/bank-server.jar \
--spring.datasource.url=jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable \
"$@"
