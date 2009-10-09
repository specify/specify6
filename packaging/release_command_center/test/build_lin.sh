#!/bin/sh
#build.sh (tr or br) (int or ext) (version #) (r or e) (this ip) (mac ip) (m)
#	       1	  2	      3           4        5         6     7
#Define Project Paths (Trunk or Branch)
#Define project file extension for external projects
if [ "$1" = "tr" ] ; then
	projloc='../workspace/SpTrunk/packaging'
	projlocMac='Documents/workspace/Specify6/packaging'
	Pappend1=''
else
	projloc='../workspace/SpBranch/packaging'
	projlocMac='Documents/workspace/SpecifyBranch7180/packaging'
	Pappend1='-ext'
fi

#Define Destination
if [ "$2" = "ext" ] ; then
	webloc='../web_launch_ext'
	weblocMac='./web_launch_ext'
else
	webloc='../web_launch'
	weblocMac='./web_launch'
fi

#Define regular or embedded projects. (no extension or -embedded)
[ "$4" = "r" ] && Pappend2='' || Pappend2='-embed'

#Clean out the correct launcher directory
rm -r $webloc
mkdir $webloc
#If 
[ "$2" = "int" ] && cp ./copy.sh ${webloc}/copy.sh

#Generate the windows and linux full and update install4j project files
../install4j/bin/install4jc --release="${3}" --destination="${webloc}" -s "${projloc}/winlin-full${Pappend1}${Pappend2}.install4j"
mv ${webloc}/updates.xml ${webloc}/updates.xml.winlinfull
../install4j/bin/install4jc --release="${3}" --destination="${webloc}" -s "${projloc}/winlin-update${Pappend1}${Pappend2}.install4j"
mv ${webloc}/updates.xml ${webloc}/updates.xml.winlinupdate
