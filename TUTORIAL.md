# Multi-region Tutorial

This page describes how to set up a multi-region demo, or large scale load test of roach-bank.

# Prerequisites

## CockroachDB Cluster

This tutorial assumes you already have a 9 node CockroachDB cluster with 8-32 vCPUs per node 
across 3 different regions, and one client VM per region.  

It does not cover any actual CockroachDB cluster setup, which can be either self-hosted or cloud. 
For that, see:
- https://www.cockroachlabs.com/docs/stable/manual-deployment
- https://www.cockroachlabs.com/docs/cockroachcloud

Tutorial cluster topology:

| Region       | Primary | Secondary | Zones                                     | Nodes | Clients |
|--------------|:--------|:----------|-------------------------------------------|-------|:--------|
| eu-central-1 | x       |           | eu-central-1a,eu-central-1b,eu-central-1c | 4-6   | 11      |
| eu-west-1    |         |           | eu-west-1a,eu-west-1b,eu-west-1c          | 1-3   | 10      |
| eu-north-1   |         | x         | eu-north-1a,eu-north-1b,eu-north-1c       | 7-9   | 12      |

The _Nodes_ column refers to the CockroachDB node numbers. 

The _Clients_ column refers to the VMs/pods/machines hosting the bank client, bank server,
a CockroachDB binary and a HAProxy load balancer (if applicable).

## Client Binaries

Required client dependencies on hosts 10-12:

- CockroachDB Binary
- HAProxy Load Balancer (if applicable) 
- Java 17 Runtime / JDK
- [bank-client](bank-client/README.md) JAR
- [bank-server](bank-server/README.md) JAR

It's up to you how to deploy these artifacts (download, scp, etc).

# Setup

## Client Setup

Create a `application-demo.yml` file on the primary client hosts (10, 11 or 12) 
in the same directory as the JAR files. 

Paste in the following account plan YAML:

```yaml
bank:
  name: "Demo Plan"
  default-account-limit: 100
  report-query-timeout: 60
  select-for-update: true
  clear-at-startup: true
  account-plan:
    accounts-per-city: 5000
    initial-balance: "150000.00"
    currency: USD
  regions:
    - name: eu-central-1
      primary: true
      cities: "berlin,hamburg,munich,frankfurt,dusseldorf,leipzig,dortmund,essen,stuttgart,zurich,krakov,zagraeb,zaragoza,lodz,athens,bratislava,prague,sofia,bucharest,vienna,warsaw,budapest"
    - name: eu-west-1
      cities: "dublin,belfast,liverpool,manchester,glasgow,birmingham,leeds,london,amsterdam,rotterdam,antwerp,hague,ghent,brussels,lyon,lisbon,toulouse,paris,cologne,seville,marseille,rome,milan,naples,turin,valencia,palermo,madrid,barcelona,sintra,lisbon"
    - name: eu-north-1
      cities: "stockholm,copenhagen,helsinki,oslo,riga,tallinn"
  region-mapping:
    aws-eu-central-1: eu-central-1
    aws-eu-west-1: eu-west-1
    aws-eu-north-1: eu-north-1
```

## Create the Database

SSH to the primary client (10, 11 or 12) and run:

    ./cockroach sql --certs-dir=certs --host=<ip> -e "CREATE DATABASE roach_bank; ALTER ROLE root WITH PASSWORD 'cockroach'"

## Start Bank Servers

SSH to each of the client hosts (10-12) and run:

    nohup java -jar bank-server.jar --profiles=demo > bank-stdout.log 2>&1 &

Optionally, you can tailor the startup parameters in the following manner:

    nohup java -jar bank-server.jar \
    --spring.datasource.url="jdbc:postgresql://localhost:26257/roach_bank??ssl=true&sslmode=require" \
    --spring.datasource.username=root \
    --spring.datasource.password=cockroach \
    --spring.profiles.active="demo" \
    > /dev/null 2>&1 &

## Start Bank Clients

SSH to each of the client hosts (10-12) and run:
    
    java -jar bank-client.jar

On the primary client, execute:

    $ connect
    $ enable-multi-region

_*) If the `enable-multi-region` command fail, see appendix A on how to manually 
partition the database for multi-region._

This command will add the database regions, set the survival goal to `REGION` and also set
table localities to `regional-by-row`. This will make the table rows home region
to be determined by the value of its city column (using the `region` computed
column as partition key).

Note that this can be a heavy operation so it may take a while to run and
for the cluster then re-balance the data as necessary. You can observe this
activity in the DB Console.

## Run Workloads

SSH to each of the client hosts (10-12) and run:

    java -jar bank-client.jar

On each of the clients, execute:

    $ transfer-funds 
    $ read-balance

You should notice that the client picks the cities for the local region.

# Appendix A:

Run the following SQL to add regions and table localities for multi-region.

```sql
ALTER DATABASE roach_bank PRIMARY REGION "eu-central-1";
ALTER DATABASE roach_bank ADD REGION "eu-north-1";
ALTER DATABASE roach_bank ADD REGION "eu-west-1";

ALTER TABLE region SET locality GLOBAL;

ALTER TABLE account ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-north-1'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','zurich','krakov','zagraeb','zaragoza','lodz','athens','bratislava','prague','sofia','bucharest','vienna','warsaw','budapest') THEN 'eu-central-1'
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow','birmingham','leeds','london','amsterdam','rotterdam','antwerp','hague','ghent','brussels','lyon','lisbon','toulouse','paris','cologne','seville','marseille','rome','milan','naples','turin','valencia','palermo','madrid','barcelona','sintra','lisbon') THEN 'eu-west-1'
        ELSE 'eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE account SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-north-1'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','zurich','krakov','zagraeb','zaragoza','lodz','athens','bratislava','prague','sofia','bucharest','vienna','warsaw','budapest') THEN 'eu-central-1'
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow','birmingham','leeds','london','amsterdam','rotterdam','antwerp','hague','ghent','brussels','lyon','lisbon','toulouse','paris','cologne','seville','marseille','rome','milan','naples','turin','valencia','palermo','madrid','barcelona','sintra','lisbon') THEN 'eu-west-1'
        ELSE 'eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction SET LOCALITY REGIONAL BY ROW AS region;

ALTER TABLE transaction_item ADD COLUMN region crdb_internal_region AS (
    CASE
        WHEN city IN ('stockholm','copenhagen','helsinki','oslo','riga','tallinn') THEN 'eu-north-1'
        WHEN city IN ('berlin','hamburg','munich','frankfurt','dusseldorf','leipzig','dortmund','essen','stuttgart','zurich','krakov','zagraeb','zaragoza','lodz','athens','bratislava','prague','sofia','bucharest','vienna','warsaw','budapest') THEN 'eu-central-1'
        WHEN city IN ('dublin','belfast','liverpool','manchester','glasgow','birmingham','leeds','london','amsterdam','rotterdam','antwerp','hague','ghent','brussels','lyon','lisbon','toulouse','paris','cologne','seville','marseille','rome','milan','naples','turin','valencia','palermo','madrid','barcelona','sintra','lisbon') THEN 'eu-west-1'
        ELSE 'eu-north-1'
        END
    ) STORED NOT NULL;

ALTER TABLE transaction_item SET LOCALITY REGIONAL BY ROW AS region;
```