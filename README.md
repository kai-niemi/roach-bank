[![Java CI](https://github.com/kai-niemi/roach-bank/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/kai-niemi/roach-batch/actions/workflows/maven.yml)
[![coverage](.github/badges/jacoco.svg)](https://github.com/kai-niemi/roach-bank/actions/workflows/maven.yml)
[![branches coverage](.github/badges/branches.svg)](https://github.com/kai-niemi/roach-bank/actions/workflows/maven.yml)

# Roach Bank

Roach Bank represents a full-stack, financial accounting ledger demo running on [CockroachDB](https://www.cockroachlabs.com/)
and PostgreSQL. It's designed to demonstrate the safety and liveness properties of a globally deployed, 
system-of-record type of workload.

# Introduction

The concept of the ledger is to move funds between accounts using balanced, multi-legged transactions 
at a high frequency. As a financial system, it needs to conserve money at all times and also provide 
an audit trail of all transactions performed towards the accounts.

This is visualized (below) using a single page to display accounts as rectangles with their current
balance.

![frontend](docs/diagram_frontend.png)

## Key Invariants

There are two key invariants that must hold true at all times, regardless of observer and activities 
such as infrastructure failure and conflicting operations if updating the same accounts 
concurrently:

* The total balance of all accounts must be constant
* All user accounts must have a positive balance

The system must deny forward progress if an operation would result in any invariant being compromised. 
For example, if a variation of the total balances is observed at any given time it means these rules 
have been breached and money has either been invented or destroyed. Because it's a stateless service, 
these invariants are safeguarded by the ACID transactional guarantees of the CockroachDB database.

## Double-entry Bookkeeping

For auditability, this ledger follows the [double-entry bookkeeping](https://en.wikipedia.org/wiki/Double-entry_bookkeeping)
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

Real accounting doesn't use negative numbers, but for simplicty this ledger does. A positive value means 
increasing value (credit), and a negative value means decreasing value (debit). A transaction is 
considered balanced when the sum of the legs with the same currency equals zero.

# Deployment

See the [Deployment Guide](deploy/README.md) on how to deploy the ledger to a single or multi-region 
AWS, GCE or Azure cluster. 

When deployed in a multi-regional topology (like US-EU), the accounts and transactions needs to be 
pinned/domiciled to each region for best performance. This is done through the [regional-by-row](https://www.cockroachlabs.com/docs/stable/multiregion-overview.html#regional-by-row-tables)
topology in CockroachDB. This will provide low read and write latencies in each region, and also for 
an entire region to be brought down without affecting forward progress in any of the other regions.

# Design and Implementation

See the [Design Notes](docs/DESIGN.md) for a complete overview of used architectural mechanisms.
The ledger is based on a common [Spring Boot](https://spring.io/projects/spring-boot) microservice
stack using Spring Boot, Spring Data JDBC/JPA, Spring Hateoas, HikariCP, Flyway and more. Kafka
is optional to use to drive account balance push events.

Architecture overview:

![architecture](docs/diagram_architecture.png)

# Project Setup

## Prerequisites

- Java 17
    - https://openjdk.org/projects/jdk/17/
    - https://www.oracle.com/java/technologies/downloads/#java17
- Maven 3+ (optional, embedded wrapper available)
    - https://maven.apache.org/

Install the JDK (Ubuntu example):

    sudo apt-get install openjdk-17-jdk

Confirm the installation by running:

    java --version

## Subprojects

- [api](bank-api/README.md) - API artifacts and message models
- [client](bank-client/README.md) - Interactive service endpoint shell client for generating load
- [server](bank-server/README.md) - Main service implementation

## Supported Databases

Supported databases are CockroachDB 22.1+ and PostgreSQL 10+. 
The database type can be selected at start-up time by activating 
the appropriate profile (see `run-server.sh`). 

Creation of the table schema and initial account plan is automatic. 

### CockroachDB Notes

A CockroachDB enterprise (trial) license is required for some demo features like 
geo-partitioning and follower-reads.

Create the database:

    cockroach sql --insecure --host=localhost -e "CREATE database roach_bank;"
    
Set an enterprise license (optional):

    cockroach sql --insecure --host=localhost -e "SET CLUSTER SETTING cluster.organization = '...'; SET CLUSTER SETTING enterprise.license = '...';"
 
### PostgreSQL Notes

Create the database:

    CREATE database roach_bank;
    CREATE extension pgcrypto;

## Building and running from codebase

The application is built with [Maven 3.1+](https://maven.apache.org/download.cgi).
Tanuki's Maven wrapper is included (mvnw). All 3rd party dependencies are available in public Maven repos.

To build and deploy to your local Maven repo, execute:

    ./mvnw clean install

### Starting locally

Quick start:

    chmod +x run-server.sh
    chmod +x run-client.sh
    ./run-server.sh
    ./run-client.sh

See [server](bank-server/README.md) and [client](bank-client/README.md) for more details.
