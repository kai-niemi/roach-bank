#!/bin/bash
# Script for setting up a multi-region Roach Bank cluster using roachprod in either AWS or GCE.

# Configuration
########################

title="CockroachDB 3-region EU deployment"
# CRDB release version
releaseversion="v21.2.9"
# Number of node instances in total including clients
nodes="7"
# Nodes hosting CRDB
crdbnodes="1-6"
# Array of client nodes (must match size of regions)
clients=(7)
# Array of regions localities (must match zone names)
regions=('eu-west-1' 'eu-west-2' 'eu-central-1')
# AWS/GCE cloud (aws|gce)
cloud="aws"
# AWS/GCE region zones (must align with nodes count)
zones="\
eu-west-1a,\
eu-west-1a,\
eu-west-2a,\
eu-west-2a,\
eu-central-1a,\
eu-central-1a,\
eu-central-1a"
# AWS/GCE machine types
machinetypes="c5d.large"

# DO NOT EDIT BELOW THIS LINE
#############################

functionsdir="../common"

source "${functionsdir}/core_functions.sh"

command_main.sh