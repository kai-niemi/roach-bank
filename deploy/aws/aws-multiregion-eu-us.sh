#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region EU-US deployment"
# CRDB release version
releaseversion="v22.2.2"
# Number of node instances in total including clients
nodes="8"
# Nodes hosting CRDB
crdbnodes="1-7"
# Array of client nodes (must match size of regions)
clients=(8)
# Array of regions localities (must match zone names)
regions=('eu-central-1' 'eu-west-1' 'us-east-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
eu-central-1a,\
eu-central-1b,\
eu-central-1c,\
eu-west-1a,\
us-east-1a,\
us-east-1b,\
us-east-1c,\
eu-central-1a"
# AWS/GCE machine types
machinetypes="c5d.2xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

main.sh