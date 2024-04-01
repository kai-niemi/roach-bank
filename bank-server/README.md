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
The config can be overridden at startup time through the command line and by activating Spring profiles, 
which are:

Database type, one of:

* crdb-local - Use CockroachDB JDBC driver connecting to localhost (default)
* crdb-cloud - Use CockroachDB JDBC driver connecting to CockroachDB Cloud
* pgjdbc-cloud - Use pgJDBC connecting to CockroachDB Cloud
* psql-local - Use pgJDBC driver connecting to PostgreSQL on localhost 

Default is pgJDBC against local CockroachDB.

Retry strategy, one of:

* retry-driver - Enable JDBC driver level retries (requires crdb-local or crdb-cloud)
* retry-savepoint - Enables client-side retries using savepoints
* retry-none - Disable retries

Default is client-side retries with exponential backoff.

Other:

* demo - Slimmed account plan
* jpa - Enables JPA repositories over JDBC
* outbox - Enables writing transfer requests to a transactional outbox table
* dev - Enables debug features for Thymeleaf 
* verbose - Enables verbose debug logging 
   
Profiles are set during startup with following command line parameter:

    java -jar target/bank-server.jar \
    --spring.datasource.url=jdbc:cockroachdb://localhost:26257/roach_bank?sslmode=disable \
    --spring.datasource.username=root \
    --spring.datasource.password= \
    --spring.profiles.active=retry-none,crdb-local  \
    --server.port=8090

PostgreSQL example:

    java -jar target/bank-server.jar \
    --spring.datasource.url=jdbc:cockroachdb://localhost:5432/roach_bank \
    --spring.datasource.username=postgres \
    --spring.datasource.password=root \
    --spring.profiles.active=retry-none,psql-local  \
    --server.port=8090

### Custom Account Plan

Create an `application.yml` in the base directory where the server is 
started from:
    
    bank:
      name: "Custom"
      default-account-limit: 10
      report-query-timeout: 60
      select-for-update: true
      clear-at-startup: false
      account-plan:
        accounts-per-city: 15000
        initial-balance: "10000.00"
        currency: USD
      regions:
        - name: eu-central-1
          primary: true
          cities: "london,amsterdam,rotterdam,berlin,hamburg,frankfurt"
        - name: eu-north-1
          cities: "stockholm,copenhagen,helsinki,oslo,riga,tallinn"
          secondary: true
        - name: us-east-1
          cities: "new york,boston,washington dc,miami,charlotte,atlanta"
      region-mapping:
        aws-eu-central-1: eu-central-1
        aws-eu-north-1: eu-north-1
        aws-us-east-1: us-east-1

See [application.yml](src/main/resources/application.yml) for the default
config with comments.