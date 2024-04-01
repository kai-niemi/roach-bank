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

PS3='Please select datasource: '
options=("<Skip>" "pgjdbc-dev" "pgjdbc-cloud" "crdb-dev" "crdb-local" "crdb-cloud" "psql-dev" "psql-local")

select option in "${options[@]}"; do
  case $option in
    "<Skip>")
      break
      ;;
    *)
      db_option=$option
      fn_print_cyan "Selected profile: $option"
      break
      ;;
  esac
done

########################################

PS3='Please select retry strategy: '
options=("<Skip>" "retry-client" "retry-driver" "retry-savepoint" "retry-none")

select option in "${options[@]}"; do
  case $option in
    "<Skip>")
      retry_option="retry-client"
      break
      ;;
    *)
      retry_option=$option
      fn_print_cyan "Selected profile: $option"
      break
      ;;
  esac
done

########################################

PS3='Please select optional profile(s): '
options=("<Skip>" "demo" "jpa" "outbox" "debug" "verbose" )

select option in "${options[@]}"; do
  case $option in
    "<Skip>")
      break
      ;;
    *)
      if [ -n "${extra_option}" ]; then
        extra_option=$extra_option,$option
      else
        extra_option=$option
      fi
      fn_print_cyan "Selected profiles: $option"
      fn_print_blue "(Select <Skip> to start)"
      ;;
  esac
done

########################################

function join_by {
  local d=${1-} f=${2-}
  if shift 2; then
    printf %s "$f" "${@/#/$d}"
  fi
}

profiles=$(join_by , $db_option $retry_option $extra_option)

echo java -jar ${jarfile} --spring.profiles.active=$profiles "$@"

sleep 3

java -jar ${jarfile} --spring.profiles.active=$profiles "$@"
