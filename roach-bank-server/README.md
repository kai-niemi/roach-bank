# Roach Bank Server

Provides the main microservice implemented with Spring Boot and packaged as a self-contained, executable jar.

## Starting the Server

Start the server with default profiles:

    chmod +x roach-bank-server.jar
    ./roach-bank-server.jar
    
The default server URL is:

    http://localhost:8090

## Deploying the Server

Spring Boot apps are (besides the JVM) self-contained and easy to deploy. Here's an 
[example](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment-service) 
of starting a Unix/Linux service via `init.d` or `system.d`.

In short, simply add a symlink:

    sudo ln -s <path>/roach-bank-server.jar /etc/init.d/bank
    
Then you can control it via:

    ~$ /etc/init.d/bank
    Usage: /etc/init.d/bank {start|stop|force-stop|restart|force-reload|status|run}    
    
Server settings can be configured by using a .conf file. The file is expected to be next to the jar file 
and have the same name but suffixed with `.conf` rather than `.jar`. 

Example:    

    RUN_ARGS=--roachbank.datasource.url=jdbc:postgresql://192.168.1.99:26300/roach_bank?sslmode=disable
    PID_FOLDER=/tmp

## Configuration

The default server configuration can be found in [application.yml](src/main/resources/application.yml).
The config can be overridden at startup time through the command line and by activating Spring profiles, which are:

Database type, one of:

   * db-crdb - Enables CockroachDB features and db schema (default)
   * db-psql - Enables PostgreSQL features and db schema

Retry strategy, one of:

   * retry-backoff - Enables retryable transactions with exponential backoff for concurrency errors (default)
   * retry-savepoint - Enables retryable transactions using savepoints
   * retry-none - Enables default Spring declarative transaction management without any retrys

Change data capture and websocket push events, one of:

   * cdc-aop - Enables synthetic CDC events (via AOP) for websocket push (default)
   * cdc-kafka - Enables Kafka subscriptions of CDC events for websocket push (requires CRDB, CDC, Kafka)
   * cdc-http - Enables HTTP subscriptions of CDC events for websocket push (requires CRDB and CDC)
 
Optional:

   * dev - Enables debug features for Thymeleaf
   * jpa - Enables JPA repositories over JDBC (which is default)
   * sql-trace - Enables SQL tracing
   
Note: CDC requires a CockroachDB enterprise license.

Profiles are set during startup with following command line parameter:

    --spring.profiles.active=db-crdb,retry-backoff,cdc-aop

CockroachDB example:

    java -jar target/roach-bank-server.jar \
    --roachbank.datasource.url=jdbc:postgresql://localhost:26257/roach_bank?sslmode=disable \
    --spring.datasource.username=root \
    --spring.datasource.password= \
    --spring.profiles.active=db-crdb,retry-backoff,cdc-kafka  \
    --spring.kafka.bootstrap-servers=localhost:9092 \
    --server.port=8080

PostgreSQL example:

    java -jar target/roach-bank-server.jar \
    --roachbank.datasource.url=jdbc:postgresql://localhost:5432/roach_bank \
    --spring.datasource.username=root \
    --spring.datasource.password= \
    --spring.profiles.active=db-psql,retry-backoff,cdc-kafka  \
    --spring.kafka.bootstrap-servers=localhost:9092 \
    --server.port=8080
    
