#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region US-EU deployment"
# CRDB release version
releaseversion="v21.2.5"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('us_east_1' 'us_east_2' 'eu_west_1')
# Array of regions localities (must match zone names)
regions=('us-east-1' 'us-east-2' 'eu-west-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
us-east-1a,\
us-east-1b,\
us-east-1c,\
us-east-2a,\
us-east-2b,\
us-east-2c,\
eu-west-1a,\
eu-west-1b,\
eu-west-1c,\
us-east-1a,\
us-east-2a,\
eu-west-1a"
# AWS/GCE machine types
machinetypes="c5d.2xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0