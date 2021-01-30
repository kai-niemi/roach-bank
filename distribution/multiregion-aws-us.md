# Multi-Region Setup in AWS (US)

Tutorial for provisioning a multi-regional cluster with 
9 CockroachDB nodes on c5d.9xlarge instances. 
Uses the Geo-Partitioned Replicas and Duplicate Indexes topology patterns.

## Cluster Provisioning

Create 9 VMs for CockroachDB across 3 AWS regions, 3 per region with each VM in the same availability zone. 
Also create 3 extra VMs, 1 per region for a region-specific version of Roach Bank and HAProxy:

    roachprod create $CLUSTER --clouds=aws --aws-machine-type-ssd=c5d.9xlarge --geo --local-ssd --nodes=12 \
    --aws-zones=\
    us-west-2a,\
    us-west-2b,\
    us-west-2c,\
    us-east-2a,\
    us-east-2b, \
    us-east-2c,\
    us-east-1a,\
    us-east-1b,\
    us-east-1c,\
    us-west-2a,\
    us-east-2a,\
    us-east-1a

## CockroachDB Cluster Setup

### Stage binaries on each VM

    roachprod stage $CLUSTER release v20.2.2

### Start up nodes in all regions excluding VMs for clients

Node 1-9 are for CockroachDB, 10-12 for haproxy and Roach Bank.

    roachprod start $CLUSTER:1-9 --sequential

### (optional) Bring up the admin UI for observability

    roachprod admin --open --ips $CLUSTER:1

### Install haproxy on machines 10-12

    roachprod run ${CLUSTER}:10-12 'sudo apt-get -qq update'
    roachprod run ${CLUSTER}:10-12 'sudo apt-get -qq install -y haproxy'

### Generate HAProxy config on the VM in each region

us-west-2:

    roachprod run ${CLUSTER}:10 "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=us-west-2"

us-east-2:

    roachprod run ${CLUSTER}:11 "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=us-east-2"

us-east-1:

    roachprod run ${CLUSTER}:12 "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=us-east-1"

### (optional) Print haproxy.cfg for observability

    roachprod run ${CLUSTER}:10 'cat haproxy.cfg'

### Start HAProxy on the VM in each region

    roachprod run ${CLUSTER}:10 'haproxy -f haproxy.cfg &'
    roachprod run ${CLUSTER}:11 'haproxy -f haproxy.cfg &'
    roachprod run ${CLUSTER}:12 'haproxy -f haproxy.cfg &'

### Create the database on any node

    roachprod run $CLUSTER:1 './cockroach sql --insecure --host=`roachprod ip $CLUSTER:1` -e "CREATE DATABASE roach_bank;"'

### If needed, set an enterprise license to be able to access the Node Map and use geo-partitioning:

    roachprod run $CLUSTER:10 <<EOF
    ./cockroach sql --insecure --host=`roachprod ip $CLUSTER:1` -e "SET CLUSTER SETTING cluster.organization = '...'; SET CLUSTER SETTING enterprise.license = '...';"
    EOF

## Roach Bank Setup

This tutorial assumes Roach Bank is built on a workstation. It's also possible to build on each client VM.

### Clone project

    git clone git@github.com:kai-niemi/roach-bank.git
    cd roachbank

Add SSH key to your github account (if needed):

 - https://docs.github.com/en/github/authenticating-to-github/adding-a-new-ssh-key-to-your-github-account 

## Build deployment assembly 

A JDK is needed to build Roach Bank from source (Ubuntu).

    sudo apt-get -qq install -y openjdk-8-jdk

Build the project:

    chmod +x mvnw
    ./mvnw clean install -P dist
  
### Install a JDK or JRE on machines 10-12

    roachprod run ${CLUSTER}:10-12 'sudo apt-get -qq install -y openjdk-8-jre-headless'

### Copy binaries to machines 10-12

    roachprod put ${CLUSTER}:10-12 distribution/target/roach-bank.tar.gz

### Explode tar.tz on machines 10-12

    roachprod run ${CLUSTER}:10-12 'tar xvf roach-bank.tar.gz'

### Start Bank Server on machines 10-12

List the service URLs:
    
    echo "US west bank service: http://$(roachprod ip $CLUSTER:10 --external):8090"
    echo "US central bank service: http://$(roachprod ip $CLUSTER:11 --external):8090"
    echo "US east bank service: http://$(roachprod ip $CLUSTER:12 --external):8090"

First instance creates DB schema and loads initial account plan:

_(reducing pool size since there are 3 servers in total)_

    roachprod run ${CLUSTER}:10 'nohup ./roach-bank-server.jar --spring.datasource.hikari.maximum-pool-size=500 > /dev/null 2>&1 &'

Wait for the service to start:

    open http://$(roachprod ip $CLUSTER:10 --external):8090
    
Now start second and third instance:

_(disabling liquibase since schema is already created)_

    roachprod run ${CLUSTER}:11-12 'nohup ./roach-bank-server.jar --spring.liquibase.enabled=false --spring.datasource.hikari.maximum-pool-size=500 > /dev/null 2>&1 &'

Optionally wait for other services to start:

    open http://$(roachprod ip $CLUSTER:11 --external):8090
    open http://$(roachprod ip $CLUSTER:12 --external):8090

## Enable Geo-Partitioning

Last step is to setup geo-partitioning to drastically speed things up: 

    roachprod run $CLUSTER:10 './cockroach sql --insecure --database roach_bank --host=`roachprod ip $CLUSTER:1` < partition-aws-us.sql'

To observe P99 latency and range distribution, open the admin UI:

    roachprod adminurl $CLUSTER:1 --open

Optionally, execute SHOW RANGES on one of the tables: 

    roachprod run $CLUSTER:1 <<EOF
    ./cockroach sql --insecure --host=roachprod ip $CLUSTER:1 -e "USE roach_bank; SHOW RANGES FROM TABLE ACCOUNT;"
    EOF

## Disable Geo-Partitioning (optional)

If you later want to remove the partitions, execute:

    roachprod run $CLUSTER:1 './cockroach sql --insecure --database roach_bank --host=`roachprod ip $CLUSTER:1` < drop-partitions.sql'

## Generate load

After this stage, there will be 3 clients and 3 servers producing transactions concurrently from
different parts of the US.

See [Data Import](import.md) for larger data volumes.

## Generating load

### Generate money transfer across US west accounts: 

    roachprod run $CLUSTER:10 <<EOF 
    ./roach-bank-client.jar connect && transfer --regions us-west
    EOF
    
### Generate money transfer across US central accounts: 

    roachprod run $CLUSTER:11 <<EOF
    ./roach-bank-client.jar connect && transfer --regions us-central
    EOF

### Generate money transfers across US east accounts:
 
    roachprod run $CLUSTER:11 <<EOF
    ./roach-bank-client.jar connect && transfer --regions us-east
    EOF

-- the end