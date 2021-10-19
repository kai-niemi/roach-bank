# Roch Bank

Roach Bank represents a full-stack, financial accounting ledger demo running on [CockroachDB](https://www.cockroachlabs.com/). 
It's designed to demonstrate the safety and liveness properties of a globally deployed, system-of-record type 
of workload.

# Introduction

Each Roach Bank instance provides a single page that displays the top accounts in the system, grouped 
by currency and region (city) represented by colored rectangles. In a multi-region deployment, the 
displayed cities are filtered by region.

![frontend](docs/diagram_frontend.png)
                
The demo concept is to move funds between accounts using balanced, multi-legged transactions at a high
frequency. As a financial ledger, it needs to conserve money at all times and also provide an audit trail 
of all transactions performed towards accounts. 

## Key Invariants

In Roach Bank, there are two key invariants that must hold true at all times, regardless of observer
and activities such as infrastructure failure and conflicting operations if updating the same accounts 
concurrently.

* The total balance of all accounts must be constant
* All user accounts must have a positive balance

The system must refuse forward progress if an operation would result in any invariant being compromised. 
For instance, if a variation of the total balances is observed at any given time it means these rules 
have been breached and money has either been invented or destroyed. Because it's a stateless service, 
these invariants are safeguarded by the ACID transactional guarantees of the database.

## Double-entry Bookkeeping

Roach Bank follows the [double-entry bookkeeping](https://en.wikipedia.org/wiki/Double-entry_bookkeeping)
principle for monetary transactions. This principle was originally formalized and published by the italian 
mathematician Luca Pacioli during the 15:th century. It involves making at least two account entries for 
every transaction. A debit in one account and a corresponding credit in another account. The sum of all 
debits must equal the sum of all credits, providing a simple method for error detection.

    Account | Credit(+) | Debit(-) |
    A         100               
    B                     -50
    C          25
    D*                    -25 \
                               -75 (coalesced)
    D*                    -50 /
    ------------------------------------------
    Σ         125    +   -125 = 0 

Real accounting doesn't use negative numbers, but in Roach Bank a positive value means increasing value (credit),
and a negative value decreasing value (debit). A transaction is balanced when the sum of the legs with
the same currency equals zero.

## Deployment

See the [Deployment Guide](deploy/README.md) on how to deploy it to a single
or multi-region AWS or GCE cluster.

Roach Bank can run anywhere, but it's intended to be globally deployed across multiple 
regions in a single cloud, multi-cloud or on-prem. 

When deployed in a multi-regional topology (like US-EU), the accounts and transactions needs 
to be pinned/domiciled to each region for best performance. This is done through the 
geo-partitioned replicas topology (SQL scripts are provided). It will provide both 
local read and write latencies and also for one entire region to be brought down without 
affecting forward progress in any of the other regions.

## Implementation

See the [Design Notes](docs/DESIGN.md) for a complete overview of the different architectural
mechanisms used.

Roach Bank provides a backend service with a single page web front-end, and a Hypermedia API 
for workload-generating clients. The clients issue transfer requests to the service API, 
which in turn executes the SQL transactions and publishes push event for the frontend. 

As an option, CDC can be used to push change events to either Kafka or an HTTP endpoint, 
which are translated to websocket push events. These push events signals account balance updates
and drives the frontend updates. Push events are regionally scoped.

A regionally scoped load balancer also sits between the service and CockroachDB nodes.

![architecture](docs/diagram_architecture.png)

Roach Bank is based on a fairly common [Spring Boot](https://spring.io/projects/spring-boot) microservice 
stack using frameworks like Spring Data, Spring Hateoas, HikariCP, Flyway and more. 

## Project Setup

How to build the service.

### Subprojects

- [api](bank-api/README.md) - API artifacts and message models
- [client](bank-client/README.md) - Interactive service endpoint shell client for generating load
- [server](bank-server/README.md) - Main service implementation

### Prerequisites

- JDK8+ with 1.8 language level 
- [Maven 3](https://maven.apache.org/download.cgi) for building the project (optional, embedded)  

OpenJDK installation on Ubuntu:

    sudo apt-get -qq install -y openjdk-8-jdk

### Supported Databases

Both CockroachDB 20.2+ and PostgreSQL 9.1+ are supported. The database type can be selected 
at start-up time by activating the appropriate profile (see below). Table schema and
initial data (account plan) creation is automatic through Flyway. 

#### CockroachDB Notes

A CockroachDB enterprise (trial) license is required for some demo features like 
geo-partitioning and follower-reads.

Create the database:

    cockroach sql --insecure --host=localhost -e "CREATE database roach_bank;"
    
Set an enterprise license (optional):

    cockroach sql --insecure --host=localhost -e "SET CLUSTER SETTING cluster.organization = '...'; SET CLUSTER SETTING enterprise.license = '...';"
 
#### PostgreSQL Notes

Create the database:

    CREATE database roach_bank;

If an error message says `function gen_random_uuid() does not exist` then run:

    CREATE extension pgcrypto;

## Building and running from codebase

The application is built with [Maven 3.1+](https://maven.apache.org/download.cgi).
Tanuki's Maven wrapper is included (mvnw). All 3rd party dependencies are available in public Maven repos.

To build and deploy to your local Maven repo, execute:

    ./mvnw clean install

### Starting locally

See [server](bank-server/README.md) and [client](bank-client/README.md) for details.

Quick start:

    chmod +x server.sh
    chmod +x client.sh
    ./server.sh
    ./client.sh
