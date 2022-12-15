nohup java -jar bank-server.jar \
--spring.datasource.url="jdbc:cockroachdb://localhost:26257/roach_bank?sslmode=disable&retryTransientErrors=true&implicitSelectForUpdate=true" \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.profiles.active=retry-driver,cdc-none,crdb-local,verbose \
--roachbank.accountsPerCityLimit=10 \
--roachbank.loadAccountByReference=false \
--roachbank.loadAccountWithSFU=false \
> /dev/null 2>&1 &