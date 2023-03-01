#!/bin/bash

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi

for c in "${clients[@]}"
do
    fn_failcheck roachprod put ${CLUSTER}:${c} ../../bank-client/target/bank-client.jar
    fn_failcheck roachprod put ${CLUSTER}:${c} run_server.sh

    i=($i+1)
done
