#!/bin/bash
#this script will take as arguments an updates.xml location, a path to an install4j list, a path to a Specify eclipse project and change the udpate path in all relevant install4j projects and resources_XX.properties files. 


webserver_update_path="$1"
path_to_i4j_list="$2"
path_to_projectname_dir_lin="$3"
path_to_projectname_dir_mac="$4"
path_to_scripts="./"

ls $path_to_packaging_dir_lin/src | grep resources > ./.tmpdump2.txt

n=0
while read linenumber
do
	n=`expr $n + 1`
	filename[$n]="$path_to_packaging_dir_lin/src/$linenumber"
	#echo "${filename[$n]}"
done < ./.tmpdump2.txt

$path_to_scripts/replace_install4j_update_path_2.sh -g $webserver_update_path $path_to_projectname_dir_lin/packaging
#and ssh version

NUM_ELEMENTS=${#filename[@]}
n=0
while [ "$n" -lt "$NUM_ELEMENTS" ]
do
	$path_to_scripts/replace_resources_update_path.sh $webserver_update_path ${filename[$n]}
	#and ssh version
done
