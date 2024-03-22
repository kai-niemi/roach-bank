#!/bin/bash

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi

for c in "${clients[@]}"
do
    fn_failcheck roachprod put ${CLUSTER}:${c} ../../bank-server/target/bank-server.jar
    fn_failcheck roachprod put ${CLUSTER}:${c} ../scripts/run_server.sh

    i=($i+1)
done
