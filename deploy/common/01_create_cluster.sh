#!/bin/bash

fn_create_cluster() {
  if [ "${cloud}" = "aws" ]; then
    echo roachprod create $CLUSTER --clouds=aws --aws-machine-type-ssd=${machinetypes} --geo --local-ssd --nodes=${nodes} --aws-zones=${zones}
    fn_failcheck roachprod create $CLUSTER --clouds=aws --aws-machine-type-ssd=${machinetypes} --geo --local-ssd --nodes=${nodes} --aws-zones=${zones}
  elif [ "${cloud}" = "gce" ]; then
    echo roachprod create $CLUSTER --clouds=gce --gce-machine-type=${machinetypes} --geo --local-ssd --nodes=${nodes} --gce-zones=${zones}
    fn_failcheck roachprod create $CLUSTER --clouds=gce --gce-machine-type=${machinetypes} --geo --local-ssd --nodes=${nodes} --gce-zones=${zones}
  else
    echo roachprod create $CLUSTER --clouds=azure --azure-machine-type=${machinetypes} --geo --local-ssd --nodes=${nodes} --azure-locations=${zones}
    fn_failcheck roachprod create $CLUSTER --clouds=azure --azure-machine-type=${machinetypes} --geo --local-ssd --nodes=${nodes} --azure-locations=${zones}
  fi
}

fn_stage_cluster() {
  fn_echo_info_nl "Stage binaries $releaseversion"
  fn_failcheck roachprod stage $CLUSTER release $releaseversion
}

fn_start_cluster() {
  fn_echo_info_nl "Start CockroachDB nodes $crdbnodes"
  fn_failcheck roachprod start $CLUSTER:$crdbnodes --sequential
  fn_failcheck roachprod admin --open --ips $CLUSTER:1
}

fn_stage_lb() {
  i=0;
  for c in "${clients[@]}"
  do
    region=${regions[$i]}
    i=($i+1)

    fn_echo_info_nl "Stage client ${CLUSTER}:$c"

    fn_failcheck roachprod run ${CLUSTER}:$c 'sudo apt-get -qq update'
    fn_failcheck roachprod run ${CLUSTER}:$c 'sudo apt-get -qq install -y openjdk-17-jre-headless htop dstat haproxy'
    fn_failcheck roachprod run ${CLUSTER}:$c "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=$region"
    fn_failcheck roachprod run ${CLUSTER}:$c 'nohup haproxy -f haproxy.cfg > /dev/null 2>&1 &'
  done
}

fn_create_db() {
fn_echo_info_nl "Creating database via $CLUSTER:1"

fn_failcheck roachprod run $CLUSTER:1 <<EOF
./cockroach sql --insecure --host=`roachprod ip $CLUSTER:1` -e "CREATE DATABASE roach_bank"
EOF
}

##################################################################

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  export CLUSTER="your-cluster-id"
fi

if fn_prompt_yes_no "1.1/5: Create new cluster?" Y; then
fn_create_cluster
fi

if fn_prompt_yes_no "1.2/5: Stage cluster?" Y; then
fn_stage_cluster
fi

if fn_prompt_yes_no "1.1/5: Start cluster?" Y; then
fn_start_cluster
fi

if fn_prompt_yes_no "1.1/5: Stage LB?" Y; then
fn_stage_lb
fi

if fn_prompt_yes_no "1.1/5: Create DB?" Y; then
fn_create_db
fi

