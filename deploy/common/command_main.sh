#!/bin/bash

core_util.sh

case "$OSTYPE" in
  darwin*)
        rootdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
        selfname="$(basename "$(test -L "$0" && readlink "$0" || echo "$0")")"
        ;;
  *)
        rootdir="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
        selfname="$(basename "$(readlink -f "${BASH_SOURCE[0]}")")"
        ;;
esac

partitionsqlfile="${selfname%.*}.sql"

fn_echo_header
{
	echo -e "${lightblue}Cluster id:\t\t${default}$CLUSTER"
	echo -e "${lightblue}Node count:\t\t${default}$nodes"
	echo -e "${lightblue}CRDB nodes:\t\t${default}$crdbnodes"
	echo -e "${lightblue}CRDB version:\t\t${default}$releaseversion"
	echo -e "${lightblue}Client nodes:\t\t${default}${clients[*]}"
	echo -e "${lightblue}Cloud:\t\t${default}$cloud"
	echo -e "${lightblue}Machine types:\t\t${default}$machinetypes"
	echo -e "${lightblue}Zones:\t\t${default}$zones"
	echo -e "${lightblue}Partition SQL file (if exist):\t\t${default}${partitionsqlfile}"
} | column -s $'\t' -t

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  echo "Use: export CLUSTER='your-cluster-id'"
  exit 1
fi

if fn_prompt_yes_no "Create CRDB cluster?" Y; then
  command_create.sh
fi

if fn_prompt_yes_no "Deploy Bank servers?" Y; then
  command_bank_deploy.sh
fi

if fn_prompt_yes_no "Start Bank servers?" Y; then
  command_bank_start.sh
fi

if test -f "$partitionsqlfile"; then
  if fn_prompt_yes_no "Apply geo-partitioning?" Y; then
    command_partition.sh
  fi
fi
