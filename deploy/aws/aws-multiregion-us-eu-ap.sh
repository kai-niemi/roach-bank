#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region US-EU-AP deployment"
# CRDB release version
releaseversion="v21.1.10"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('us' 'eu' 'ap')
# Array of regions localities (must match zone names)
regions=('us-east-1' 'eu-central-1' 'ap-southeast-1')
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
ap-southeast-1a,\
ap-southeast-1b,\
ap-southeast-1c,\
us-east-1a,\
eu-central-1a,\
ap-southeast-1a"
# AWS/GCE machine types
machinetypes="c5d.2xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common/functions"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0