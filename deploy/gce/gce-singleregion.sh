#!/bin/bash
# Script for setting up a single-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB single region deployment (GCE)"
# CRDB release version
releaseversion="v21.2.6"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of client localities (must match partition names)
localities=('us-east4' 'us-east4' 'us-east4')
# Array of regions localities (must match zone names)
regions=('us-east4' 'us-east4' 'us-east4')
# AWS/GCE cloud (aws|gce)
cloud="gce"
# AWS/GCE region zones (must align with nodes size)
zones="\
us-east4-a,\
us-east4-a,\
us-east4-a,\
us-east4-b,\
us-east4-b,\
us-east4-b,\
us-east4-c,\
us-east4-c,\
us-east4-c,\
us-east4-a,\
us-east4-b,\
us-east4-c"
# AWS/GCE machine types
machinetypes="n2-standard-8"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0