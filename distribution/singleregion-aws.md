# Single-Region 9x Setup in AWS

Tutorial for provisioning a cluster across a single region using
9 CockroachDB nodes on c5d.9xlarge instances. 

## Cluster Provisioning
  
Create 9 VMs for CockroachDB across one AWS region, 3 per availability zone. 
Also create one extra VM for a version of Roach Bank and HAProxy:

    roachprod create $CLUSTER --clouds=aws --aws-machine-type-ssd=c5d.9xlarge --geo --local-ssd --nodes=12 \
    --aws-zones=\
    eu-central-1a,\
    eu-central-1a,\
    eu-central-1a,\
    eu-central-1b,\
    eu-central-1b,\
    eu-central-1b,\
    eu-central-1c,\
    eu-central-1c,\
    eu-central-1c,\
    eu-central-1a,\
    eu-central-1b,\
    eu-central-1c

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

### Generate HAProxy config

    roachprod run ${CLUSTER}:10-12 "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=eu-central-1"

### (optional) Print haproxy.cfg for observability

    roachprod run ${CLUSTER}:10 'cat haproxy.cfg'

### Start HAProxy 

    roachprod run ${CLUSTER}:10-12 'nohup haproxy -f haproxy.cfg > /dev/null 2>&1 &'
    
### Create the database on any node

    roachprod run $CLUSTER:1 './cockroach sql --insecure --host=`roachprod ip $CLUSTER:1` -e "CREATE DATABASE roach_bank;"'

### If needed, set an enterprise license to be able to access the Node Map and use geo-partitioning:

    roachprod run $CLUSTER:1 <<EOF
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

    chmod +x mvnw
    ./mvnw clean install -P dist

The binaries are now available here:

    tar -tvf distribution/target/roach-bank.tar.gz
    
### Install a JDK or JRE on machines 10-12

    roachprod run ${CLUSTER}:10-12 'sudo apt-get -qq install -y openjdk-8-jdk'

_Alternatively just the JRE (not needed if above is used)_

    roachprod run ${CLUSTER}:10-12 'sudo apt-get -qq install -y openjdk-8-jre-headless'

### Copy binaries to machines 10-12

    roachprod put ${CLUSTER}:10-12 distribution/target/roach-bank.tar.gz

### Explode tar.tz on machines 10-12

    roachprod run ${CLUSTER}:10-12 'tar xvf roach-bank.tar.gz'

### Start Bank Server on machines 10-12

List the service URLs:
    
    echo "1st bank service: http://$(roachprod ip $CLUSTER:10 --external):8090"
    echo "2nd bank service: http://$(roachprod ip $CLUSTER:11 --external):8090"
    echo "3rd bank service: http://$(roachprod ip $CLUSTER:12 --external):8090"

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

## Generate load

See [Data Import](../docs/import.md) for larger data volumes.

Transfer money across US accounts on first client:

    roachprod run $CLUSTER:10 <<EOF
    ./roach-bank-client.jar connect && transfer --regions eu_west && balance --follower-reads
    EOF

Transfer money across EU accounts on second client:

    roachprod run $CLUSTER:11 <<EOF
    ./roach-bank-client.jar connect && transfer --regions eu_south && balance --follower-reads
    EOF

Transfer money across AUS accounts on third client:

    roachprod run $CLUSTER:12 <<EOF
    ./roach-bank-client.jar connect && transfer --regions eu_north && balance --follower-reads
    EOF
