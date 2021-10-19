#!/bin/bash

fn_deploy_server() {
  fn_echo_info_nl "Deploying app binaries to ${CLUSTER}:$1"
  fn_failcheck roachprod put ${CLUSTER}:$1 ../../bank-server/target/bank-server.jar
  fn_failcheck roachprod put ${CLUSTER}:$1 ../../bank-client/target/bank-client.jar
}

fn_start_server(){
  local c=$1
  local locality=$2

  fn_echo_info_nl "Starting bank server in locality $locality.."

if ((i > 1)); then
fn_failcheck roachprod run $CLUSTER:$c <<EOF
xxnohup ./bank-server.jar --roachbank.locality=$locality --spring.flyway.enabled=false > /dev/null 2>&1 &
EOF
else
fn_failcheck roachprod run $CLUSTER:$c <<EOF
nohup ./bank-server.jar --roachbank.locality=$locality > /dev/null 2>&1 &
EOF
fi

    local url="http://$(roachprod ip $CLUSTER:$c --external):8090"

    fn_echo_info_nl "Waiting for $url"

    until $(curl --output /dev/null --silent --head --fail $url); do
      printf '.'
      sleep 5
    done

    fn_open_url $url
}

#############################################################

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi

i=0;
for c in "${clients[@]}"
do
    fn_deploy_server $c
    fn_start_server $c ${localities[$i]}
    i=($i+1)
done

