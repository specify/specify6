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


#untar what was grabbed from the mac side
cd ${webloc}
tar -xvf ./file.tar
cd ${weblocMac}
mv *.dmg updates.xml.macfull updates.xml.macupdate ../
cd ..
rm ./file.tar
rm -r ${weblocMac}


#NOW:  everything is in place in the appropriate launcher directory
#use rod's new script to generate the correct updates.xml file


#if internal, run copy.sh; if external, do nothing (look up ftp commands?)

#~/install4j/bin/install4jc -r=$3 -d= -s 
