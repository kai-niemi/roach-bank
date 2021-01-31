# Roach Bank Client 

Main bank client used for generating import files and workloads, including:

 - Account import CSVs and DMLs
 - Report of accounts and transactions
 - Query account balances 
 - Transfer funds between accounts
 - Create accounts 

It's an interactive REST client implemented using Spring Shell and Spring Hateoas,
packaged as a self-contained, executable jar. 

## Usage

Start the client with:

    chmod +x roach-bank-client.jar
    ./roach-bank-client.jar

First specify the endpoint URL (or reverse proxy / loadbalancer):

    connect [url]
     
Default URL is `http://localhost:8090/api`      

Type `help` for additional guidance.

## Workload commands

Get help for a command:

    help balance

Transfer funds between all accounts:

    transfer

Transfer funds between random accounts in 'us' regions:

    transfer --regions us_west,us_central,us_east

Another transfer example:

    transfer --regions us_east,eu_west,ap --amount-range 5.00-15:00 --duration 90m

Query the balance of random top accounts per region:

    balance --duration 5m30s --follower-reads

## Generate CSV import files

Generate 50M accounts for all regions:

    gen-csv --accounts 50000000

Generate 250M accounts without legs for US regions:

    gen-csv --no-legs --regions us_west,us_central,us_east --accounts 250000000

## Configuration

All parameters in `application.yaml` can be overridden via CLI. See 
[Common Application Properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
for details.