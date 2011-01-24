#!/bin/sh

#build.sh (tr or br) (int or ext) (version #) (r or e) (this ip) (mac ip) (m or no)
#	       1	  2	      3           4        5         6     7
#help stuff (simply type "./build.sh help" to access)
[ "$1" = "help" ] && { echo "build.sh (tr or br) (int or ext) (version #) (r or e) (this ip) (mac ip) (m  OR no)"; exit 0; }

#no args are passed->choice-mode
if [ "$#" = 0 ] ; then 
	echo "Answer the following questions by typing the number next to the answer you want."
	echo "-----------------------------------------"

	###NormalORNot
	while [ "$special_task" = "" ]
	do
		echo "Is there anything special you want to do?"
		echo "1) No, just the normal update process."
		echo "2) Yes, dump only the mobile versions"
		
		read input
		case $input in 
			1) echo "Ok, continuing with the normal build process..." ; break ;;
			2) para4="r" ; para7="m" ; special_task='a' ;;
			*) echo "Please answer again" ;;
		esac
	done

	###Trunk or Branch?
	while [ "$para1" = "" ]
	do
		echo "Release from the Trunk or the Branch?"
		echo "1) Trunk"
		echo "2) Branch"

		read input
		case $input in 
			1) para1="tr" ;;
			2) para1="br" ;;
			*) echo "Please answer again" ;;
		esac
	done

	###Internal or External? Both?
	while [ "$para2" = "" ]
	do
		echo "Release is internal or external?"
		echo "1) Internal"
		echo "2) External"
		echo "3) Both Internal and External"

		read input
		case $input in 
			1) para2="int" ;;
			2) para2="ext" ;;
			3) para2="both" ;;
			*) echo "Please answer again" ;;
		esac
	done	

	###Version number
	echo "What is the version number of this release?  e.g. 6.2.81"
	read input
	para3="$input"

	###Include Embedded Mysql Libraries?
	while [ "$para4" = "" ]
	do
		echo "Include embedded MySQL libraries?"
		echo "1) Yes"
		echo "2) No"

		read input
		case $input in 
			1) para4="e" ;;
			2) para4="r" ;;
			*) echo "Please answer again" ;;
		esac
	done

	###This computer's IP address
	while [ "$para5" = "" ]
	do
		if [ -f thisip.txt ] ; then 
			echo "Is this your computer's IP address?"
			echo | cat thisip.txt
			echo "1) Yes"
			echo "2) No"
			read input 
		else
			input="2"
		fi
	
		if [ "$input" = "1" ] ; then
			para5=`cat thisip.txt`
		elif [ "$input" = "2" ] ; then
			echo -n "Enter the IP address of this machine: "
			read input

			#Write new ip save
			echo "----"
			echo "Writing this ip address to a save file."
			echo "$input" > thisip.txt
			echo "----"

			para5="$input"
		else
			echo "Please answer again"
		fi		
	done

	###Mac computer's IP address
	while [ "$para6" = "" ]
	do
		if [ -f macip.txt ] ; then 
			echo "Is this the Mac's IP address?"
			echo | cat macip.txt
			echo "1) Yes"
			echo "2) No"
			read input 
		else
			input="2"
		fi
	
		if [ "$input" = "1" ] ; then
			para6=`cat macip.txt`
		elif [ "$input" = "2" ] ; then
			echo -n "Enter the IP address of the mac machine: "
			read input
		
			#Write new ip save
			echo "----"
			echo "Writing this ip address to a save file."
			echo "$input" > macip.txt
			echo "----"

			para6="$input"
		else
			echo "Please answer again"
		fi		
	done

	###Mobile version too?
	while [ "$para7" = "" ]
	do
		echo "Are you releasing the Mobile app too?"
		echo "1) Yes"
		echo "2) No"

		read input
		
		if [ "$input" = "1" ] ; then
			para7="m"
			break
		elif [ "$input" = "2" ] ; then
			para7="no"
			break
		fi

		echo "Please answer again"
	done

	./build.sh $para1 $para2 $para3 $para4 $para5 $para6 $para7
	exit 0
fi 



#Define Project Paths (Trunk or Branch)
#Define project file extension for external projects
if [ "$1" = "tr" ] ; then
	projloc='../workspace/SpTrunk/packaging'
	projlocMac='Documents/workspace/Specify6/packaging'
else
	projloc='../workspace/SpBranch/packaging'
	projlocMac='Documents/workspace/SpecifyBranch7180/packaging'
fi

#Define Destination
if [ "$2" = "ext" ] ; then
	webloc='../web_launch_ext'
	weblocStr='web_launch_ext'
	weblocMac='./web_launch_ext'
	Pappend1='-ext'
else
	webloc='../web_launch'
	weblocStr='web_launch'
	weblocMac='./web_launch'
	Pappend1=''
fi

#Define regular or embedded projects. (no extension or -embedded)
[ "$4" = "r" ] && Pappend2='' || Pappend2='-embed'

#Define mobile projects (no extension or -mobile)
[ "$7" = "m" ] && Pappend3='-mobile' || Pappend3=''

#Clean out the correct launcher directory
rm -r $webloc
mkdir $webloc
#If 
[ "$2" = "int" ] && cp ./copy.sh ${webloc}/copy.sh

#Generate the windows and linux full and update install4j project files
../install4j/bin/install4jc --release="${3}" --destination="${webloc}" -s "${projloc}/winlin-full${Pappend1}${Pappend2}${Pappend3}.install4j"
mv ${webloc}/updates.xml ${webloc}/updates.xml.winlinfull
../install4j/bin/install4jc --release="${3}" --destination="${webloc}" -s "${projloc}/winlin-update${Pappend1}${Pappend2}${Pappend3}.install4j"
mv ${webloc}/updates.xml ${webloc}/updates.xml.winlinupdate



#Clean out the launcher directory on the mac
ssh specify@${6} rm -r $weblocMac
ssh specify@${6} mkdir $weblocMac

#Generate the installer files on the mac
ssh specify@${6} /Applications/install4j/bin/install4jc --release="${3}" --destination="${weblocMac}" -s "${projlocMac}/mac-full${Pappend1}${Pappend2}${Pappend3}.install4j"
ssh specify@${6} mv ${weblocMac}/updates.xml ${weblocMac}/updates.xml.macfull
ssh specify@${6} /Applications/install4j/bin/install4jc --release="${3}" --destination="${weblocMac}" -s "${projlocMac}/mac-update${Pappend1}${Pappend2}${Pappend3}.install4j"
ssh specify@${6} mv ${weblocMac}/updates.xml ${weblocMac}/updates.xml.macupdate

#tar them up
ssh specify@${6} tar -cvf file.tar ${weblocMac}

#copy them back over to the linux side
scp specify@${6}:./file.tar ${webloc}
ssh specify@${6} rm ./file.tar

#untar what was grabbed from the mac side
#cd ${webloc}
#tar -xvf ./file.tar
#mv ${weblocmac}/*.* .
#rm ./file.tar
cd ${webloc}
tar -xvf ./file.tar
cd ${weblocMac}
mv *.dmg updates.xml.macfull updates.xml.macupdate ../
cd ..
rm ./file.tar
rm -r ${weblocMac}

pwd

#^^the above code has put everything in place in webloc
#now, split up the release version number and generate the final updates.xml file w/ java script. 
component=$3
compOne=`expr substr $component 1 1`
compTwo=`expr substr $component 3 1`
compThree=`expr substr $component 5 2`

../Install4JUpdater/bin/Install4JUpdater ../${webloc}/updates.xml.winlinfull ../${webloc}/updates.xml.winlinupdate ../${webloc}/updates.xml.macfull ../${webloc}/updates.xml.macupdate ../${webloc}/updates.xml 6.0.0 $compOne $compTwo $compThree  


