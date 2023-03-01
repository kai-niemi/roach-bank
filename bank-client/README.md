# Roach Bank Client 

Main bank client used for generating load and more, including:

- Create accounts
- Transfer funds between accounts
- Query account balances
- Report of accounts and transactions
- Generate account import CSV files

## Usage

Start the client with:

    chmod +x bank-client.jar
    ./bank-client.jar

First connect to the ledger service endpoint:

    connect [url]
     
Default URL is `http://localhost:8090/api`      

Type `help` for additional guidance.

## Workload commands

Get help for a command:

    help balance

Transfer funds between all accounts in the local region:

    transfer

List regions and cities:

    regions

Transfer funds between accounts in specified regions:

    transfer --regions <..>

Query the balance of random accounts in the local region:

    balance --duration 5m30s --follower-reads

## Configuration

All parameters in `application.yaml` can be overridden via CLI. See 
[Common Application Properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
for details.