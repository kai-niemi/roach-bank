#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 6-region EU-US deployment"
# CRDB release version
releaseversion="v23.2.3"
# Number of node instances in total including clients
nodes="19"
# Nodes hosting CRDB
crdbnodes="1-18"
# Array of client nodes (must match size of regions)
clients=(19)
# Array of regions localities (must match zone names)
regions=('eu-central-1' 'eu-west-1' 'eu-west-2' 'us-east-1' 'us-east-2' 'us-west-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
eu-central
-1a,\
eu-central-1b,\
eu-central-1c,\
eu-west-1a,\
eu-west-1b,\
eu-west-1c,\
eu-west-2a,\
eu-west-2b,\
eu-west-2c,\
us-east-1a,\
us-east-1b,\
us-east-1c,\
us-east-2a,\
us-east-2b,\
us-east-2c,\
us-west-1a,\
us-west-1b,\
us-west-1c,\
eu-central-1a"
# AWS/GCE machine types
machinetypes="c5d.large"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

main.sh