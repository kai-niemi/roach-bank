#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region deployment"
# CRDB release version
releaseversion="v21.2.6"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('eu' 'sa' 'ap')
# Array of regions localities (must match zone names)
regions=('eu-central-1' 'sa-east-1' 'ap-northeast-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
eu-central-1a,\
eu-central-1a,\
eu-central-1a,\
sa-east-1a,\
sa-east-1a,\
sa-east-1a,\
ap-northeast-1a,\
ap-northeast-1a,\
ap-northeast-1a,\
eu-central-1a,\
sa-east-1a,\
ap-northeast-1a"
# AWS/GCE machine types
machinetypes="c5d.4xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0