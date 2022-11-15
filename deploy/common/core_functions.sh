#!/bin/bash

core_util.sh(){
  source "${functionsdir}/core_util.sh"
}

01_create_cluster.sh(){
  source "${functionsdir}/01_create_cluster.sh"
}

02_deploy_servers.sh(){
  source "${functionsdir}/02_deploy_servers.sh"
}

03_deploy_clients.sh(){
  source "${functionsdir}/03_deploy_clients.sh"
}

04_start_servers.sh(){
  source "${functionsdir}/04_start_servers.sh"
}

05_partition.sh(){
  source "${functionsdir}/05_partition.sh"
}

main.sh(){
  source "${functionsdir}/main.sh"
}

