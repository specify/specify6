#!/bin/bash
#NOTE The http:// is already included:  only include the replacement after that.
NOTE="This script is used as follows:  \"...sh [-g] replacement_url filename [filename filename filename...]; if -g is used it becomes ...sh -g  path/to/replacement/list replacement_url path_to_i4jdir"



if [ "$1" != "-g" ]; then
	#test filenames for existence
	#set pattern
	#shift
	#test amount of vars
	#create array

	NAMEOFI4JPROJECTS="/home/specify/release_command_center/cache/list_of_install4j_filenames.txt"

	pattern="$1"
	shift 1
	NUM_FILES="$#"

	n=0
	while [ "$n" -lt "$NUM_FILES" ]
	do
		n=`expr $n + 1`
		filename[$n]=$1
		shift 1
	done
elif [ "$1" = "-g" ]; then
	#set pattern
	#read in filenames
	#test for existence
	#create array

#	if [ ! -a "$NAMEOFI4JPROJECTS" ]; then
#		echo "The filepath to the list of install4j files was invalid."
#		exit 0
#	fi

	
	NAMEOFI4JPROJECTS="$2"
	shift 2	
	pattern="$1"

	n=0
	while read linenumber
	do
		n=`expr $n + 1`
		filename[$n]="$2/$linenumber"
		#echo "${filename[$n]}"
	done < $NAMEOFI4JPROJECTS
fi

echo "Number of files to mod"
echo ${#filename[@]}


NUM_ELEMENTS=${#filename[@]}
n=0
while [ "$n" -lt "$NUM_ELEMENTS" ]
do
	n=`expr $n + 1`

	if [ ! -f "${filename[$n]}" ]; then
		echo "The ${n} th filename given was invalid: $1."
		exit 0
	fi
done

n=0
while [ "$n" -lt "$NUM_ELEMENTS" ]
do
	n=`expr $n + 1`
	sed -e "s#<string>http:\/\/.*<#<string>http:\/\/$pattern<#" ${filename[$n]} > ${filename[$n]}.new
	#sed -ne "#<string>http:\/\/#s#http:\/\/.*<#http:\/\/$pattern<#g" ${filename[$n]} > ${filename[$n]}.new	#.tmpdump.txt
	mv ${filename[$n]}.new ${filename[$n]}
	echo "The file ${filename[$n]} was successfully modified."
done
echo -e "\n\nThe files were modified correctly."

exit 0
#done
