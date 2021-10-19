#!/bin/bash

# Configuration
########################

title="CockroachDB 4-region AP-US deployment"
# CRDB release version
# Number of node instances in total including clients
nodes="16"
# Nodes hosting CRDB
crdbnodes="1-12"
# Array of client nodes (must match size of regions)
clients=(13 14 15 16)
# Array of client localities (must match partition names)
localities=('ap' 'us')
# Array of regions localities (must match zone names)
regions=('ap-southeast-1' 'us-west-2' 'us-east-2' 'us-east-1' )
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
ap-southeast-1a,\
ap-southeast-1b,\
ap-southeast-1c,\
us-west-2a,\
us-west-2b,\
us-west-2c,\
us-east-2a,\
us-east-2b,\
us-east-2c,\
us-east-1a,\
us-east-1b,\
us-east-1c,\
ap-southeast-1a,\
us-west-2a,\
us-east-2a,\
us-east-1a"
# AWS/GCE machine types
machinetypes="c5d.xlarge"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common/functions"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0