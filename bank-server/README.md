# Roach Bank Server

Main bank service implemented with Spring Boot and packaged as a self-contained, executable jar.
See the [Deployment Guide](../deploy/README.md) on how to deploy it to a single or multi-region 
AWS, GCE or Azure cluster.

# Local deployment guide

## Starting the Server

Start the server with default profiles:

    chmod +x bank-server.jar
    ./bank-server.jar
    
The default server URL is:

    http://localhost:8090

## Deploying the Server

Spring Boot apps are (besides the JVM) self-contained and easy to deploy. Here's an 
[example](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment-service) 
of starting a Unix/Linux service via `init.d` or `system.d`.

In short, simply add a symlink:

    sudo ln -s <path>/bank-server.jar /etc/init.d/bank
    
Then you can control it via:

    ~$ /etc/init.d/bank
    Usage: /etc/init.d/bank {start|stop|force-stop|restart|force-reload|status|run}    
    
Server settings can be configured by using a .conf file. The file is expected to be next to the jar file 
and have the same name but suffixed with `.conf` rather than `.jar`. 

Example:    

    RUN_ARGS="--spring.datasource.url=jdbc:cockroachdb://192.168.1.99:26257/roach_bank?sslmode=disable --server.port=8088"
    PID_FOLDER=/tmp

## Configuration

The default server configuration can be found in [application.yml](src/main/resources/application.yml).
The config can be overridden at startup time through the command line and by activating Spring profiles, which are:

Database type, one of:

* crdb-local - Use CockroachDB JDBC driver connecting to localhost (default)
* crdb-cloud - Use CockroachDB JDBC driver connecting to CockroachDB Cloud
* pgjdbc-local - Use PostgreSQL JDBC driver connecting to localhost
* pgjdbc-cloud - Use PostgreSQL JDBC driver connecting to CockroachDB Cloud
* psql-local - Use PostgreSQL JDBC driver connecting to PostgreSQL on localhost 

Retry strategy, one of:

* retry-client - Enables client-side retries with exponential backoff (default)
* retry-driver - Enable JDBC driver level retries (requires crdb-local or crdb-cloud)
* retry-savepoint - Enables client-side retries using savepoints
* retry-none - Disable retries

Change data capture and websocket push events, one of:

* cdc-none - Enables synthetic CDC events (via AOP) for websocket push (default)
* cdc-kafka - Enables Kafka subscriptions of CDC events for websocket push (requires CRDB, CDC, Kafka)
* cdc-http - Enables HTTP subscriptions of CDC events for websocket push (requires CRDB and CDC)
 
Optional:

* jpa - Enables JPA repositories over JDBC
* outbox - Enables writing transfer requests to a transactional outbox table
* dev - Enables debug features for Thymeleaf 
   
Note: Some features including CDC requires a CockroachDB enterprise license (trial).

Profiles are set during startup with following command line parameter:

    java -jar target/bank-server.jar \
    --spring.datasource.url=jdbc:cockroachdb://localhost:26257/roach_bank?sslmode=disable \
    --spring.datasource.username=root \
    --spring.datasource.password= \
    --spring.profiles.active=retry-none,cdc-none,crdb-local  \
    --spring.kafka.bootstrap-servers=localhost:9092 \
    --server.port=8090

PostgreSQL example:

    java -jar target/bank-server.jar \
    --spring.datasource.url=jdbc:cockroachdb://localhost:5432/roach_bank \
    --spring.datasource.username=postgres \
    --spring.datasource.password=root \
    --spring.profiles.active=retry-none,cdc-none,psql-local  \
    --spring.kafka.bootstrap-servers=localhost:9092 \
    --server.port=8090
    
