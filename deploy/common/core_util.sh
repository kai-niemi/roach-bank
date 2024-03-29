#!/bin/bash
# Colorful console experience

case "$OSTYPE" in
  darwin*)
        default="\x1B[0m"
        red="\x1B[31m"
        green="\x1B[32m"
        lightyellow="\x1B[93m"
        lightblue="\x1B[94m"
        cyan="\x1B[36m"
        lightcyan="\x1B[96m"
        creeol="\r\033[K"
        ;;
  *)
        default="\e[0m"
        red="\e[31m"
        green="\e[32m"
        lightyellow="\e[93m"
        lightblue="\e[94m"
        cyan="\e[36m"
        lightcyan="\e[96m"
        creeol="\r\033[K"
        ;;
esac

fn_sleep_time(){
  sleep 0.5
}

fn_echo_info_nl(){
  echo -en "${creeol}[${cyan} INFO ${default}] $*"
	fn_sleep_time
	echo -en "\n"
}

fn_echo_fail(){
  echo -en "${creeol}[${red} FAIL ${default}] $*"
	fn_sleep_time
}

fn_echo_fail_nl(){
  echo -en "${creeol}[${red} FAIL ${default}] $*"
	fn_sleep_time
	echo -en "\n"
}

fn_echo_dryrun_nl(){
  echo -en "[DRYRUN] $*"
	echo -en "\n"
}

fn_echo_warning(){
	echo -en "${lightyellow}Warning!${default} $*"
	fn_sleep_time
}

fn_echo_header(){
	echo -e ""
	echo -e "${lightyellow}${title} ${default}"
	echo -e "==========================================${default}"
}

fn_prompt_yes_no(){
	local prompt="$1"
	local initial="$2"

	if [ "${initial}" == "Y" ]; then
		prompt+=" [Y/n] "
	elif [ "${initial}" == "N" ]; then
		prompt+=" [y/N] "
	else
		prompt+=" [y/n] "
	fi

	while true; do
		read -e -p  "${prompt}" -r yn
		case "${yn}" in
			[Yy]|[Yy][Ee][Ss]) return 0 ;;
			[Nn]|[Nn][Oo]) return 1 ;;
		*) echo -e "Please answer yes or no." ;;
		esac
	done
}

fn_failcheck(){
    if [ "${dryrun}" == "on" ]; then
      fn_echo_dryrun_nl "$@"
      sleep 1
    else
      "$@"
      local status=$?
      if [ ${status} -ne 0 ]; then
          fn_echo_fail_nl "$@" >&2
          exit 1
      fi
      return ${status}
    fi
}

fn_open_url(){
  case "$OSTYPE" in
  darwin*)
        open "$@"
        ;;
  linux*)
        if [ -n $BROWSER ]; then
          $BROWSER "$@"
        else
          fn_echo_fail_nl "Could not detect web browser to use."
        fi
        ;;
  *)
        fn_echo_fail_nl "Unknown OS: $OSTYPE"
        exit 1
        ;;
  esac
}

