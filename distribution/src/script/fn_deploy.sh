#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.
# DO NOT EDIT THIS FILE

# Bootstrap phase
#############################

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi

fn_echo_header
{
	echo -e "${lightblue}Cluster id:\t\t${default}$CLUSTER"
	echo -e "${lightblue}Node count:\t\t${default}$nodes"
	echo -e "${lightblue}CRDB nodes:\t\t${default}$crdbnodes"
	echo -e "${lightblue}Client nodes:\t\t${default}${clients[*]}"
	echo -e "${lightblue}Client localities:\t\t${default}${localities[*]}"
	echo -e "${lightblue}CRDB version:\t\t${default}$releaseversion"
	echo -e "${lightblue}Cloud:\t\t${default}$cloud"
	echo -e "${lightblue}Machine types:\t\t${default}$machinetypes"
	echo -e "${lightblue}Zones:\t\t${default}$zones"
} | column -s $'\t' -t

if ! fn_prompt_yes_no "Continue?" Y; then
	exit 0
fi

# First client
client1=${clients[0]}

#################################################
if [ "${cloud}" = "aws" ]; then
  fn_failcheck roachprod create $CLUSTER --clouds=aws --aws-machine-type-ssd=${machinetypes} --geo --local-ssd --nodes=${nodes} --aws-zones=${zones}
elif [ "${cloud}" = "gce" ]; then
  fn_failcheck roachprod create $CLUSTER --clouds=gce --gce-machine-type=${machinetypes} --geo --local-ssd --nodes=${nodes} --gce-zones=${zones}
else
  fn_failcheck roachprod create $CLUSTER --clouds=azure --azure-machine-type=${machinetypes} --geo --local-ssd --nodes=${nodes} --azure-locations=${zones}
fi

#################################################
fn_echo_info_nl "Stage binaries"
fn_failcheck roachprod stage $CLUSTER release $releaseversion

#################################################
fn_echo_info_nl "Start CockroachDB nodes $crdbnodes"
fn_failcheck roachprod start $CLUSTER:$crdbnodes --sequential
fn_failcheck roachprod admin --open --ips $CLUSTER:1

#################################################
i=0;
for c in "${clients[@]}"
do
  region=${regions[$i]}
  i=($i+1)

  fn_failcheck roachprod run ${CLUSTER}:$c 'sudo apt-get -qq update'
  fn_failcheck roachprod run ${CLUSTER}:$c 'sudo apt-get -qq install -y openjdk-8-jre-headless htop dstat haproxy'
  fn_failcheck roachprod run ${CLUSTER}:$c "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=$region"
  fn_failcheck roachprod run ${CLUSTER}:$c 'nohup haproxy -f haproxy.cfg > /dev/null 2>&1 &'

  fn_echo_info_nl "Deploying app binaries to ${CLUSTER}:$c"
  fn_failcheck roachprod put ${CLUSTER}:$c roach-bank.tar.gz
  fn_failcheck roachprod run ${CLUSTER}:$c "tar xvfz roach-bank.tar.gz --exclude='*.sh'"
done

#################################################
fn_echo_info_nl "Creating database roach_bank via $CLUSTER:$client1"
fn_failcheck roachprod run $CLUSTER:$client1 <<EOF
./cockroach sql --insecure --host=`roachprod ip $CLUSTER:1` -e "CREATE DATABASE roach_bank"
EOF

#################################################

i=0;
for c in "${clients[@]}"
do
  locality=${localities[$i]}
  i=($i+1)

  fn_echo_info_nl "Starting bank service in $locality.."

if ((i > 1)); then
fn_failcheck roachprod run $CLUSTER:$c <<EOF
nohup ./roach-bank-server.jar --roachbank.locality=$locality --spring.flyway.enabled=false > /dev/null 2>&1 &
EOF
else
fn_failcheck roachprod run $CLUSTER:$c <<EOF
nohup ./roach-bank-server.jar --roachbank.locality=$locality > /dev/null 2>&1 &
EOF
fi

  url="http://$(roachprod ip $CLUSTER:$c --external):8090"

  fn_echo_info_nl "Waiting for $url"

  until $(curl --output /dev/null --silent --head --fail $url); do
    printf '.'
    sleep 5
  done

  fn_open_url $url
done

#################################################
if [ "${partitionsqlfile}" ]; then
fn_echo_info_nl "Creating table partitions"
fn_failcheck roachprod run $CLUSTER:$client1 <<EOF
./cockroach sql --insecure --database roach_bank --host=`roachprod ip $CLUSTER:1` < ${partitionsqlfile}
EOF
else
fn_echo_info_nl "Skipping table partitions"
fi

fn_echo_info_nl "Done!"

fn_echo_info_nl "Command hints:"
for c in "${clients[@]}"
do
fn_echo_info_nl "roachprod run $CLUSTER:$c"
done
fn_echo_info_nl "./roach-bank-client.jar connect transfer balance --follower-reads"

fn_echo_info_nl "Admin URLs:"
for c in "${clients[@]}"
do
roachprod adminurl $CLUSTER:$c
done

exit 0