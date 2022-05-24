#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region EU deployment"
# CRDB release version
releaseversion="v21.2.10"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clients=(10 11 12)
# Array of regions localities (must match zone names)
regions=('europe-west1' 'europe-west2' 'europe-west3')
# AWS/GCE cloud (aws|gce)
cloud="gce"
# AWS/GCE region zones (must align with nodes count)
zones="\
europe-west1-b,\
europe-west1-c,\
europe-west1-d,\
europe-west2-a,\
europe-west2-b,\
europe-west2-c,\
europe-west3-a,\
europe-west3-b,\
europe-west3-c,\
europe-west1-b,\
europe-west2-a,\
europe-west3-a"
# AWS/GCE machine types
machinetypes="n2-standard-4"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh

exit 0