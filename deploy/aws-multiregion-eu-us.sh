#!/bin/bash

# Configuration
########################

title="CockroachDB 3-region EU-US deployment"
# CRDB release version
releaseversion="v23.2.2"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of regions localities (must match zone names)
regions=('us-east-1' 'eu-central-1' 'eu-north-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
us-east-1a,\
us-east-1b,\
us-east-1c,\
eu-central-1a,\
eu-central-1b,\
eu-central-1c,\
eu-north-1a,\
eu-north-1b,\
eu-north-1c,\
us-east-1a,\
eu-central-1a,\
eu-north-1a"
# AWS/GCE machine types
machinetypes="m6i.2xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="./common"

source "${functionsdir}/core_functions.sh"

main.sh