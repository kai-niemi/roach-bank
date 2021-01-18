# Roach Bank Client 

Main bank Hypermedia client implemented with Spring Shell and packaged as a 
self-contained, executable jar. Can be used for generating import files
and workloads, including:

 - Generate custom account plan import CSVs and DMLs
 - Retrieve a report of accounts and transactions
 - Generate workloads:
   - Query account balances 
   - Transfer funds between accounts
   - Batch account creation 

## Usage

Start the shell with:

    chmod +x roach-bank-client.jar
    ./roach-bank-client.jar

First specify the endpoint URL (or reverse proxy / loadbalancer):

    connect [url]
     
Default URL is `http://localhost:8090/api`      

Transfer funds between random accounts in 'us' regions:

    transfer --regions us

Transfer amounts between 5 and 15 in local currency between accounts in three regions for 90 minutes:

    transfer --regions us,eu,ap --amount-range 5.00-15:00 --duration 90m

Query the balance of random top accounts per region for 5min and 30s:

    balance --duration 5m30s --follower-reads

Type `help` for additional guidance.

## Configuration

All parameters in `application.yaml` can be overridden via CLI. See 
[Common Application Properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
for details.