#!/bin/bash

set -e

case "$OSTYPE" in
  darwin*)
        default="\x1B[0m"
        cyan="\x1B[36m"
        lightblue="\x1B[94m"
        magenta="\x1B[35m"
        creeol="\r\033[K"
        ;;
  *)
        default="\e[0m"
        cyan="\e[36m"
        lightblue="\e[94m"
        magenta="\e[35m"
        creeol="\r\033[K"
        ;;
esac

fn_print_cyan(){
  echo -en "${creeol}${cyan}$@${default}"
	echo -en "\n"
}

fn_print_blue(){
  echo -en "${creeol}${lightblue}$@${default}"
	echo -en "\n"
}

########################################

basedir=.
jarfile=${basedir}/bank-server/target/bank-server.jar

if [ ! -f "$jarfile" ]; then
    ./mvnw clean install
fi

########################################

profiles=default

fn_print_cyan "Datasource option, one of:"
fn_print_blue "1) pgjdbc-local - Use pgJDBC driver connecting to localhost"
fn_print_blue "2) pgjdbc-cloud - Use pgJDBC driver connecting to CockroachDB Cloud"
fn_print_blue "3) pgjdbc-dev - Use pgJDBC driver connecting to dev host (default)"
fn_print_blue "4) psql-local - Use pgJDBC driver connecting to PostgreSQL on localhost"
fn_print_blue "5) crdb-local - Use CockroachDB JDBC driver connecting to localhost"
fn_print_blue "6) crdb-cloud - Use CockroachDB JDBC driver connecting to CockroachDB Cloud"
fn_print_blue "7) crdb-dev - Use CockroachDB JDBC driver connecting to dev host"
fn_print_blue "8) psql-dev - Use pgJDBC driver connecting to dev host"

PS3='Please select datasource: '
options=(
"pgjdbc-local" "pgjdbc-cloud" "pgjdbc-dev" "psql-local" "crdb-local" "crdb-cloud" "crdb-dev" "psql-dev"
"<Default>" "<Quit>" )

select option in "${options[@]}"; do
  case $option in
    "<Default>")
      profiles=$profiles,pgjdbc-dev
      break
      ;;
    "<Quit>")
      exit 0
      ;;
    *)
      profiles=$profiles,$option
      fn_print_cyan "Selected profile: $profiles"
      break
      ;;
  esac
done

########################################

fn_print_cyan "Retry option, one of:"
fn_print_blue "1) retry-client - Enables client-side retries with exponential backoff (default)"
fn_print_blue "2) retry-driver - Enable JDBC driver level retries (requires crdb-local or crdb-cloud)"
fn_print_blue "3) retry-savepoint - Enables client-side retries using savepoints"
fn_print_blue "4) retry-none - Disable retries"

PS3='Please select retry option: '
options=(
"retry-client" "retry-driver" "retry-savepoint" "retry-none"
"<Default>" "<Quit>" )

select option in "${options[@]}"; do
  case $option in
    "<Default>")
      profiles=$profiles,retry-client
      break
      ;;
    "<Quit>")
      exit 0
      ;;
    *)
      profiles=$profiles,$option
      fn_print_cyan "Selected profile: $profiles"
      break
      ;;
  esac
done

########################################

fn_print_cyan "Optional, any of:"
fn_print_blue "1) jpa - Enables JPA repositories over JDBC (default)"
fn_print_blue "2) outbox - Enables writing to a transactional outbox table"
fn_print_blue "3) debug - Enables debug features for Thymeleaf"
fn_print_blue "4) verbose - Enables verbose logging"

PS3='Please select profile(s): '
options=(
"jpa" "outbox" "debug" "verbose"
"<Start>" "<Quit>" )

select option in "${options[@]}"; do
  case $option in
    "<Start>")
      break
      ;;
    "<Quit>")
      exit 0
      ;;
    *)
      profiles=$profiles,$option
      fn_print_cyan "Selected profiles: $profiles"
      fn_print_blue "(Press enter)"
      ;;
  esac
done

fn_print_blue java -jar ${jarfile} --spring.profiles.active=$profiles "$@"

java -jar ${jarfile} --spring.profiles.active=$profiles "$@"
