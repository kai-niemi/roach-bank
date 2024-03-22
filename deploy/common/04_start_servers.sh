#!/bin/bash

fn_start_server(){
  local c=$1

  fn_echo_info_nl "Starting bank server $c.."

  fn_failcheck roachprod run $CLUSTER:$c 'chmod +x *.sh'
  fn_failcheck roachprod run $CLUSTER:$c './run_server.sh > /dev/null 2>&1 &'

  local ip
  ip=$(roachprod ip $CLUSTER:$c --external)

  local url
  url="http://$ip:8090"

  fn_echo_info_nl "Waiting for server to start: $url"

  until curl --output /dev/null --silent --head --fail "$url"; do
    printf '.'
    sleep 5
  done

  fn_open_url "$url"
}

#############################################################

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi

i=0;
for c in "${clients[@]}"
do
    fn_start_server $c
    i=($i+1)
done

