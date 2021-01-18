#!/bin/bash
# Maven+gitflow release script

set -e

case "$OSTYPE" in
  darwin*)
        default="\x1B[0m"
        cyan="\x1B[36m"
        creeol="\r\033[K"
        ;;
  *)
        default="\e[0m"
        cyan="\e[36m"
        creeol="\r\033[K"
        ;;
esac

fn_print_info(){
  echo -en "${creeol}[${cyan} INFO ${default}] $@"
	echo -en "\n"
}

##########################
# Filter functions
##########################

# Remove any version suffixes (such as '-SNAPSHOT')
fn_filter_version(){
    local v=$1
    local cleaned=$(echo ${v} | sed -e 's/[^0-9][^0-9]*$//')
    local last_num=$(echo ${cleaned} | sed -e 's/[0-9]*\.//g')
    local next_num=$(($last_num))
    echo ${v} | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/$next_num/"
}

# Advances the last number of the given version string by one
fn_advance_version(){
    local v=$1
    local cleaned=$(echo ${v} | sed -e 's/[^0-9][^0-9]*$//')
    local last_num=$(echo ${cleaned} | sed -e 's/[0-9]*\.//g')
    local next_num=$(($last_num+1))
    echo ${v} | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/$next_num/"
}

##########################
# Release metadata
##########################

fn_print_info "Extracting pom.xml project version"

# The current version
pomVersion=$(echo 'VERSION=${project.version}' | mvn help:evaluate | grep '^VERSION=' | sed 's/^VERSION=//g')
# The version to be released
releaseVersion="$(fn_filter_version ${pomVersion}).RELEASE"
# The next development version
developmentVersion="$(fn_advance_version ${pomVersion}).BUILD-SNAPSHOT"

fn_print_info "Git branch: $(git rev-parse --abbrev-ref HEAD)"
fn_print_info "POM version is ${pomVersion}"
fn_print_info "Release version: ${releaseVersion}"
fn_print_info "Next development version: ${developmentVersion}"

echo -en "\n"

while true; do
    read -p "Confirm releasing version '${releaseVersion}' of this project [y/N]" yn
    case ${yn} in
    [Yy]* ) break;;
    [Nn]* ) echo Exiting; exit 1;;
    * ) echo "Please answer yes or no.";;
esac
done

mvn --batch-mode gitflow:release \
    -DreleaseVersion=${releaseVersion} \
    -DdevelopmentVersion=${developmentVersion}
