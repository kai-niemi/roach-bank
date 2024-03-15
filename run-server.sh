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

fn_print_cyan "Database type, one of:"
fn_print_blue "     crdb-local - Use CockroachDB JDBC driver connecting to localhost (default)"
fn_print_blue "     crdb-cloud - Use CockroachDB JDBC driver connecting to CockroachDB Cloud"
fn_print_blue "   pgjdbc-local - Use PostgreSQL JDBC driver connecting to localhost"
fn_print_blue "   pgjdbc-cloud - Use PostgreSQL JDBC driver connecting to CockroachDB Cloud"
fn_print_blue "     psql-local - Use PostgreSQL JDBC driver connecting to PostgreSQL on localhost"

fn_print_cyan "Retry strategy, one of:"
fn_print_blue "   retry-client - Enables client-side retries with exponential backoff (default)"
fn_print_blue "   retry-driver - Enable JDBC driver level retries (requires crdb-local or crdb-cloud)"
fn_print_blue "retry-savepoint - Enables client-side retries using savepoints"
fn_print_blue "     retry-none - Disable retries"

fn_print_cyan "Optional:"
fn_print_blue "            jpa - Enables JPA repositories over JDBC (default)"
fn_print_blue "         outbox - Enables writing to a transactional outbox table"
fn_print_blue "          debug - Enables debug features for Thymeleaf"

PS3='Please select profile(s): '
options=(
"default"
"crdb-local" "crdb-cloud" "crdb-dev" "pgjdbc-local" "pgjdbc-cloud" "pgjdbc-dev" "psql-local" "psql-dev"
"retry-client" "retry-driver" "retry-savepoint" "retry-none"
"jpa" "outbox" "debug"
"<Start>" "<Quit>" )
profiles=default

select option in "${options[@]}"; do
  case $option in
    "default")
      profiles=$profiles,crdb-dev,retry-client,jpa
      break
      ;;
    "<Start>")
      break
      ;;
    "<Quit>")
      exit 0
      ;;
    *)
      profiles=$profiles,$option
      fn_print_cyan "You chose: $option"
      ;;
  esac
done

fn_print_blue java -jar ${jarfile} --spring.profiles.active=$profiles "$@"

java -jar ${jarfile} --spring.profiles.active=$profiles "$@"
