#!/bin/bash

fn_deploy_server() {
  fn_echo_info_nl "Deploying bank binaries to ${CLUSTER}:$1"
  fn_failcheck roachprod put ${CLUSTER}:$1 ../../bank-server/target/bank-server.jar
}

fn_deploy_client() {
  fn_echo_info_nl "Deploying bank binaries to ${CLUSTER}:$1"
  fn_failcheck roachprod put ${CLUSTER}:$1 ../../bank-client/target/bank-client.jar
}

#############################################################

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi


if [ "$1" == "server" ]; then
for c in "${clients[@]}"
  do
      fn_deploy_server $c
  done
else
  for c in "${clients[@]}"
  do
      fn_deploy_client $c
  done
fi
