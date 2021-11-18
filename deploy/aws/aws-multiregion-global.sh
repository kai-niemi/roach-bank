#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 4-region deployment"
# CRDB release version
releaseversion="v21.2.0"
# Number of node instances in total including clients
nodes="16"
# Nodes hosting CRDB
crdbnodes="1-12"
# Array of client nodes (must match size of regions)
clients=(13 14 15 16)
# Array of client localities (must match partition names)
localities=('eu' 'sa' 'ap' 'us')
# Array of regions localities (must match zone names)
regions=('eu-central-1' 'sa-east-1' 'ap-northeast-1' 'us-east-1')
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
us-east-1a,\
us-east-1a,\
us-east-1a,\
eu-central-1a,\
sa-east-1a,\
ap-northeast-1a,\
us-east-1a"
# AWS/GCE machine types
machinetypes="c5.2xlarge"
#machinetypes="c5d.2xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0