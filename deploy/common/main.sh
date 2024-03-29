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
} | column -s $'\t' -t

if [ -z "${CLUSTER}" ]; then
  fn_echo_warning "No \$CLUSTER id variable set!"
  echo "Use: export CLUSTER='your-cluster-id'"
  exit 1
fi

if fn_prompt_yes_no "1/5: Create CRDB cluster?" Y; then
  01_create_cluster.sh
fi

if fn_prompt_yes_no "2/5: Deploy Bank Servers?" Y; then
  02_deploy_servers.sh
fi

if fn_prompt_yes_no "3/5: Deploy Bank Clients?" Y; then
  03_deploy_clients.sh
fi

if fn_prompt_yes_no "4/5: Start Bank Servers?" Y; then
  04_start_servers.sh
fi

fn_echo_info_nl "Done!"