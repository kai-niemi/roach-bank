#!/bin/bash

roach-bank-server/target/roach-bank-server.jar \
--roachbank.datasource.url=jdbc:postgresql://192.168.1.99:26300/roach_bank?sslmode=disable \
--roachbank.health.admin-endpoint=http://192.168.1.99 \
"$@"
