#!/bin/bash

fn_start_server(){
  local c=$1
  local locality=$2

  fn_echo_info_nl "Starting server in locality $locality.."

fn_failcheck roachprod run $CLUSTER:$c <<EOF
nohup ./bank-server.jar --roachbank.locality=$locality > /dev/null 2>&1 &
EOF

  local url="http://$(roachprod ip $CLUSTER:$c --external):8090"

  fn_echo_info_nl "Waiting for server to start $url"

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
    fn_start_server $c ${localities[$i]}
    i=($i+1)
done

