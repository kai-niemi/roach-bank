#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 6-region EU-US deployment"
# CRDB release version
releaseversion="v22.2.2"
# Number of node instances in total including clients
nodes="10"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10)
# Array of regions localities (must match zone names)
regions=('eu-west-1' 'eu-west-2' 'eu-central-1' 'us-west-2' 'us-east-2' 'us-east-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
eu-west-1a,\
eu-west-1b,\
eu-west-2a,\
eu-west-2b,\
eu-central-1a,\
eu-central-1b,\
us-west-2a,\
us-east-2a,\
us-east-1a,\
eu-west-1a"
# AWS/GCE machine types
machinetypes="c5d.xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh