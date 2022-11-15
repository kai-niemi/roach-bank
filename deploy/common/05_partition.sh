#!/bin/bash

fn_echo_info_nl "Executing ${partitionsqlfile}"

roachprod put $CLUSTER:1 ${partitionsqlfile}

fn_failcheck roachprod run $CLUSTER:1 <<EOF
./cockroach sql --insecure --database roach_bank --host=`roachprod ip $CLUSTER:1` < ${partitionsqlfile}
EOF
