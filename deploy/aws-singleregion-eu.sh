#!/bin/bash
# Script for setting up a single-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB single region deployment (AWS)"
# CRDB release version
releaseversion="v24.2.3"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of regions localities (must match zone names)
regions=('eu-central-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes size)
zones="\
eu-central-1a,\
eu-central-1b,\
eu-central-1c,\
eu-central-1a,\
eu-central-1b,\
eu-central-1c,\
eu-central-1a,\
eu-central-1b,\
eu-central-1c,\
eu-central-1a,\
eu-central-1b,\
eu-central-1c"

# AWS/GCE machine types
# https://aws.amazon.com/ec2/instance-types/m6i/
machinetypes="m6i.4xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="./common"

source "${functionsdir}/core_functions.sh"

main.sh