Demo running instructions.

## Starting the Server

    ./roach-bank-server.jar \
    --spring.datasource.hikari.maximum-pool-size=300 \
    --spring.datasource.url=jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable \
    --spring.datasource.username=root \
    --spring.datasource.password= \
    --spring.liquibase.enabled=true \
    --spring.liquibase.drop-first=false \
    --spring.profiles.active=db-crdb,retry-backoff,cdc-kafka  \
    --spring.kafka.bootstrap-servers=localhost:9092 \
    --server.port=8090

Example of changing the connection pool size:

    --spring.datasource.hikari.maximum-pool-size=300

## Starting the Client

    ./roach-bank-client.jar

Connect to localhost:

    connect

Command examples:

    help
    transfer --regions us_west
    transfer --regions us_east,eu_west --amount-range 5.00-15:00 --duration 90m
    balance --duration 15m30s --follower-reads

## Global Load Testing

In a multi-region setup, try to target the regions that are local to each client:

Client that runs 20 threads per each region in US, across a set of 500 random accounts:

    roachprod run cluster-name:10
    ./roach-bank-client.jar connect
    transfer --regions us_west,us_central,us_east

Corresponding for EU:

    roachprod run cluster-name:11
    ./roach-bank-client.jar connect
    transfer --regions eu_west,eu_central,eu_south

And AP:
    
    roachprod run cluster-name:12
    ./roach-bank-client.jar connect
    transfer --regions apac

Type `help` for additional guidance.
