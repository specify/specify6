#!/bin/sh

#Globals
current_path=`pwd`
ID_MAC="24"
ID_WINDOWS="176"
ID_WINDOWS_64="1873"
ID_LINUX="179"



EnvironWizard () {
[ -f cache/environ_tmp.txt ] && rm cache/environ_tmp.txt

n=0
while read linenumber
do
	n=`expr $n + 1`
	first_field[$n]="`echo $linenumber | cut -d\| -f1`"
	second_field[$n]="`echo $linenumber | cut -d\| -f2`"
	third_field[$n]="`echo $linenumber | cut -d\| -f3`"	
done < cache/environ.txt

m=0
while [ "$m" -lt "$n" ] 
do
	m=`expr $m + 1`
	while :
	do
	 	echo "Is ${first_field[$m]} the correct entry for ${second_field[$m]}?"
	 	echo "1) Yes"
	 	echo "2) No"
	 	read input
	 	
	 	case $input in
	 		1) echo "${first_field[$m]}|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt ; break ;;
	 		2) echo "Please enter the ${second_field[$m]}" ; read input3 ; echo "${input3}|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt ; break ;;
	 		*) echo "Please try again." ;;
 		esac	
	 done
done

[ -f cache/environ_tmp.txt ] && mv -f cache/environ_tmp.txt cache/environ.txt
}

View () {
while :
do
	#Dump out visual representation of all variables
	echo -e "\nEnter the number of the variable you would like to change the value of.  The chart is variable description, variable value. \n\n"

	n=0
	while read linenumber
	do
		n=`expr $n + 1`
		value_name[$n]="`echo $linenumber | cut -d\| -f2`"
		value_value[$n]="`echo $linenumber | cut -d\| -f1`"

		echo -e "$n)  ${value_name[$n]}:\t${value_value[$n]}"
	done < cache/environ.txt

	q=`expr $n + 1`
	echo -e "\n$q) Return to main menu"

	read input

	if [ "$input" -ge "1" -a "$input" -le "$n" ] ; then
		Edit $input
	elif [ "$input" = "$q" ] ; then
		break
	else
		echo -e "\n ***********Please try again*********** \n"
	fi
done
}

Edit () {
[ -f cache/environ_tmp.txt ] && rm cache/environ_tmp.txt

z=0
while read linenumber
do
	z=`expr $z + 1`

	first_field[$z]="`echo $linenumber | cut -d\| -f1`"
	second_field[$z]="`echo $linenumber | cut -d\| -f2`"
	third_field[$z]="`echo $linenumber | cut -d\| -f3`"
done < cache/environ.txt

m=0
while [ "$m" -lt "$z" ]
do
	m=`expr $m + 1`
	if [ "$1" = "$m" ] ; then
		echo "The old value of ${second_field[$m]} is: ${first_field[$m]}"
		echo "Please enter its new value:"
		read input2
		echo "$input2|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt
		echo "Your input has been written."
		echo -e "----------------------------------------------------\n\n"
	else
		echo "${first_field[$m]}|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt
	fi
done

mv -f cache/environ_tmp.txt cache/environ.txt
}

UpdateIP () {
Edit 2
}

Release () {
while :
do
	#Questions
	echo -e "Enter the number of the trunk/branch combination.\n"
	echo -e "1  Trunk-Internal"
	echo -e "2  Trunk-External"
	echo -e "3  Branch-Internal"
	echo -e "4  Branch-External"

	echo -e "\nChoose by typing the relevant number and pressing enter."

	read choice
	
	case $choice in 
		1) MakeProject trunk internal ; break ;;
		2) MakeProject trunk external ; break ;;
		3) MakeProject branch internal ; break ;;
		4) MakeProject branch external ; break ;;
		*) echo "Please try again." ;;
	esac
done
}

MakeProject () {
echo -e "\n\nPlease enter the version number of the $1, $2 release (e.g. 6.2.90):"
read version

while :
do
	echo -e "\nFor the $1, $2 release, put out:"
	echo -e "\n1) Plain\n2) EZDB\n3) Mobile"
	echo "Enter the number of the choice:"

	read choice

	case $choice in 
		1) BuildProject $1 $2 $version p ; break ;;
		2) BuildProject $1 $2 $version e ; break ;;
		3) BuildProject $1 $2 $version m ; break ;;
		*) echo "Please try again." ;;
	esac
done
}

BuildProject () {
#Dump the environment into variables inside the script
z=0
while read linenumber
do
	z=`expr $z + 1`

	first_field[$z]="`echo $linenumber | cut -d\| -f1`"
	second_field[$z]="`echo $linenumber | cut -d\| -f2`"
	third_field[$z]="`echo $linenumber | cut -d\| -f3`"
done < cache/environ.txt

m=0
while [ "$m" -lt "$z" ]
do
	m=`expr $m + 1`
	declare "${third_field[$m]}"="${first_field[$m]}"
	declare "${third_field[$m]}desc"="${second_field[$m]}"
	declare "${third_field[$m]}var"="${third_field[$m]}"
done

while :
do
	echo -e "\n\nDo you want to include 64-bit installers for Windows?\n\n1) Yes\n2) No\n\n"
	
	read choice
	case $choice in
		1) is64="1" ; break ;;
		2) is64="0" ; break ;;
		*) echo "Please Try again." ;;
	esac
done

while :
do
	echo -e "\n\nSkip straight to the build process (skipping svn update/update location/changes to resources files/changes to install4jfiles/ant builds)?\n\n1) No\n2) Yes\n\n"
	
	read choice
	case $choice in
		1) skip="0" ; break ;;
		2) skip="1" ; break ;;
		*) echo "Please try again." ;;
	esac
done

#Do this block if skip=0
if [ "$skip" = "0" ] ; then
	#Resolve:  if the release is external, is it going to be public?
	if [ "$2" = "external" ] ; then
		while :
		do
			echo -e "\nYou have selected an external release with the limited launcher set designed for public releases.  Sometimes external releases are either in-house or public.\n\nIs this a public release?\n\n1) Yes\n2) No\n\n"
		
			read choice
			case $choice in
				1) public_rel="1" ; echo -e "\n\n---------------------------\n\n" ; break ;;
				2) public_rel="0" ; echo -e "\n\n---------------------------\n\n" ; break ;;
				*) echo "Please try again." ;;
			esac
		done
	else
		public_rel="0"
	fi

	#If public, grab the update location based on its p e d def.  if nonpublic, choose the update location based on its p e d def.
	if [ "$public_rel" = "1" -a "$4" = "e" ] ; then
		update_location="$addr_e_pub"
	elif [ "$public_rel" = "1" -a "$4" = "p" ] ; then
		update_location="$addr_p_pub"
	elif [ "$public_rel" = "0" -a "$4" = "e" ] ; then
		while :
		do
			echo -e "Please select the nonpublic update location for this in-house release from the list below:\n\n1) $addr_e_tr\n2) $addr_e_br\n\n"
			read choicex
			case $choicex in
				1) update_location=$addr_e_tr ; break ;;
				2) update_location=$addr_e_br ; break ;; 
				*) echo "Please try again." ;;
			esac		
		done
	elif [ "$public_rel" = "0" -a "$4" = "p" ] ; then
		while :
		do
			echo -e "Please select the nonpublic update location for this in-house release from the list below:\n\n1) $addr_p_tr\n2) $addr_p_br\n"
			read choicey
			case $choicey in
				1) update_location="$addr_p_tr" ; break ;;
				2) update_location="$addr_p_br" ; break ;; 
				*) echo "Please try again." ;;
			esac		
		done
	fi
fi

#bUILDING pROJECT
echo -e "The update location for this release is the following:  $update_location \n\n-----------------------------------\nBeginning Release\n-----------------------------------\n\n"
#Mash up variable variables

#???
copysh="$2"
#???

#Internal/External
if [ "$2" = "internal" ] ; then
	pwebloc="$intdirlocal"
	pweblocMac="$intdirmac"
	pPappend1=""
	is_int=1
else
	pwebloc="$extdirlocal"
	pweblocMac="$extdirmac"
	pPappend1="-ext"
	is_int=0
fi

#Trunk/Branch
if [ "$1" = "trunk" ] ; then
	projloc="$trunkdirlocal"
	projlocMac="$trunkdirmac"
else
	projloc="$branchdirlocal"
	projlocMac="$branchdirmac"
fi

#Define version number
version="$3"

#Do this block iff skip=0
if [ "$skip" = "0" ] ; then
	#SVNUPDATE
	svn update ${projloc}
	ssh ${username}@${macip} svn update ${projlocMac}

	#first ssh command was last executed, so verify that ssh connects properly
	if [ "$?" = "255" ] ; then
		echo -e "----------------------------------------------\nThere was an error in connecting to the mac computer with ssh:  Please verify that you are using the correct ip address.\n----------------------------------------------"
		exit 1
	fi

	#INSTALL4jCHANGE
	#RESOURCESCHANGE
	if [ "$is_int" -ge "1" ] ; then
		if [ "$4" = "p" ] ; then
			list_loc="int-plain"
		elif [ "$4" = "e" ] ; then
			list_loc="int-embed"
		else
			list_loc="int-embed"
		fi
	else
		if [ "$4" = "p" ] ; then
			list_loc="ext-plain"
		elif [ "$4" = "e" ] ; then
			list_loc="ext-embed"
		else
			list_loc="int-embed"
		fi
	fi
	
	./replace_install4j_update_path_2.sh -g ./i4jlists/${list_loc} $update_location ${projloc}
	./replace_resources_update_path.sh $update_location ${projloc}/../src/resources_en.properties
	./replace_resources_update_path.sh $update_location ${projloc}/../src/resources_pt.properties
	#the new version number property placeholder NOTE version already is set to var: ${version}
	#end the new version number property placeholder
	tar -cvf file.tar replace_install4j_update_path_2.sh replace_resources_update_path.sh i4jlists/${list_loc}
	scp file.tar ${username}@${macip}:${projlocMac}
	ssh ${username}@${macip} tar -xvf ${projlocMac}/file.tar
	ssh ${username}@${macip} chmod +x ./replace_install4j_update_path_2.sh ./replace_resources_update_path.sh
	ssh ${username}@${macip} ./replace_install4j_update_path_2.sh -g ./i4jlists/${list_loc} $update_location ${projlocMac}
	ssh ${username}@${macip} ./replace_resources_update_path.sh $update_location ${projlocMac}/../src/resources_en.properties
	ssh ${username}@${macip} ./replace_resources_update_path.sh $update_location ${projlocMac}/../src/resources_pt.properties
	ssh ${username}@${macip} rm -r ./replace_install4j_update_path_2.sh ./replace_resources_update_path.sh ./i4jlists ${projlocMac}/file.tar
	rm file.tar

	#ANT
	ant -f ${projloc}/../build.xml installer-linux
	ssh ${username}@${macip} ant -f ${projlocMac}/../build.xml installer-linux
fi

#NORMALPROCESS
#p e d definitions
shift 3
n=0
m="$#"
while [ "$n" -lt "$m" ]
do
n=`expr $n + 1`

	if [ "$1" = "e" ] ; then
		mode="-embed"
		Pappend1="${pPappend1}${mode}"
		webloc="${pwebloc}${mode}"
		weblocMac="${pweblocMac}${mode}"
		trac_type="e"
	elif [ "$1" = "m" ] ; then
		mode="-mobile"
		Pappend1="${pPappend1}${mode}"
		webloc="${pwebloc}${mode}"
		weblocMac="${pweblocMac}${mode}"
		trac_type="m"
	else
		mode=""
		Pappend1="${pPappend1}"
		webloc="${pwebloc}"
		weblocMac="${pweblocMac}"	
		trac_type="p"	
	fi

	#Cleaning the machines
	rm -rf $webloc
	mkdir $webloc
	ssh ${username}@${macip} rm -rf $weblocMac
	ssh ${username}@${macip} mkdir $weblocMac

	#Copy over send script for internal builds
	[ "$copysh" = "internal" ] && cp ./copy.sh ${webloc}/copy.sh

	#64bit prep
	if [ "$is64" = "1" ] ; then
		ID_WINLIN="${ID_LINUX},${ID_WINDOWS},${ID_WINDOWS_64}"
	else
		ID_WINLIN="${ID_LINUX},${ID_WINDOWS}"
	fi
	
	#Building winlin projects
	$i4jlocal/install4jc --release="${version}" --destination="${webloc}" --build-ids=${ID_WINLIN} "${projloc}/${winlinbase}-update${Pappend1}.install4j"
	mv ${webloc}/updates.xml ${webloc}/updates.xml.winlinupdate
	if [ "$is64" = "1" ] ; then
		temp=`ls ${webloc} | grep exe | awk '{ ORS="|"; print; }' | cut -d'|' -f1 | cut -d'.' -f1`
		temp2=`ls ${webloc} | grep exe | awk '{ ORS="|"; print; }' | cut -d'|' -f2`
		mv ${webloc}/$temp2 ${webloc}/${temp}_64.exe
		sed -e "/64/s/fileName=\"[^\"]*\"/fileName=\"${temp}_64.exe\"/" ${webloc}/updates.xml.winlinupdate > updates.tmp
		mv updates.tmp ${webloc}/updates.xml.winlinupdate
	fi

	$i4jlocal/install4jc --release="${version}" --destination="${webloc}" --build-ids=${ID_WINLIN} "${projloc}/${winlinbase}-full${Pappend1}.install4j"
	mv ${webloc}/updates.xml ${webloc}/updates.xml.winlinfull
	if [ "$is64" = "1" ] ; then
		temp=`ls ${webloc} | grep exe | awk '{ ORS="|"; print; }' | cut -d'|' -f1 | cut -d'.' -f1`
		temp2=`ls ${webloc} | grep exe | awk '{ ORS="|"; print; }' | cut -d'|' -f4`
		mv ${webloc}/$temp2 ${webloc}/${temp}_64.exe
		sed -e "/64/s/fileName=\"[^\"]*\"/fileName=\"${temp}_64.exe\"/" ${webloc}/updates.xml.winlinfull > updates.tmp
		mv updates.tmp ${webloc}/updates.xml.winlinfull
	fi

	#Building mac projects
	ssh ${username}@${macip} ${i4jmac}/install4jc --release="${version}" --destination="${weblocMac}" --build-ids=${ID_MAC} "./${projlocMac}/${macbase}-update${Pappend1}.install4j"
	ssh ${username}@${macip} mv ${weblocMac}/updates.xml ${weblocMac}/updates.xml.macupdate

	ssh ${username}@${macip} ${i4jmac}/install4jc --release="${version}" --destination="${weblocMac}" --build-ids=${ID_MAC} "./${projlocMac}/${macbase}-full${Pappend1}.install4j"
	ssh ${username}@${macip} mv ${weblocMac}/updates.xml ${weblocMac}/updates.xml.macfull

	#Transfer
	ssh ${username}@${macip} tar -cvf file.tar ${weblocMac}/*
	scp ${username}@${macip}:./file.tar  ./
	ssh ${username}@${macip} rm file.tar

	mv ./file.tar ../file.tar
	cd ..
	tar -xvf ./file.tar
	rm file.tar
	cd $current_path

	component=$version
	len=${#component}
	
	echo -e $version
	
	if [ "$len" -eq 6 ] ; then
		r=1
		t=5
	else
		r=2
		t=6
	fi

	compOne=`expr substr $component 1 1`
	compTwo=`expr substr $component 3 $r`
	compThree=`expr substr $component $t 2`

	$i4jU/Install4JUpdater ../${webloc}/updates.xml.winlinfull ../${webloc}/updates.xml.winlinupdate ../${webloc}/updates.xml.macfull ../${webloc}/updates.xml.macupdate ../${webloc}/updates${mode}.xml 1.0.0 $compOne $compTwo $compThree
	
	if [ "$1" = "m" ] ; then
		while : 
		do
			echo -e "\n\nDo you want to package the mobile product automatically?\n\n1) Yes\n2) No\n\n"
			read choice
			
			if [ "$choice" = "1" ] ; then
				name_of_dir="SpecifyWorkBench"
				rm ${webloc}/*update*
				mkdir ${webloc}/${name_of_dir}
				tar -xvf ${webloc}/*.tgz
				mv SpecifyMobile ${webloc}/${name_of_dir}/MacOSX
				tar -xvf ${webloc}/*.gz
				mv SpecifyMobile ${webloc}/${name_of_dir}/Linux
				unzip ${webloc}/*.zip
				mv SpecifyMobile ${webloc}/${name_of_dir}/Windows
				cp spdatabase ${webloc}/${name_of_dir}/spdatabase
				chmod -R 775 ${webloc}/${name_of_dir}
				zip -r ${name_of_dir}.zip ${webloc}/${name_of_dir}
				mv ${name_of_dir}.zip ${webloc}
				break
			elif [ "$choice" = "2" ] ; then
				break
			else
				echo -e "\n\nPlease try again.\n\n"
			fi
		done
	fi
	
	#Generate the TRACLINE
	SVN_VERSION=`svn update ${projloc} | cut -d' ' -f3 | cut -d . -f1`
	RELEASE_DATE=`date '+%m.%d.%Y'`
	if [ "$public_rel" = "0" ] ; then
		trac_append="; RFT"
	fi
	#remember $version, $trac_type
	TRACLINE="|| ${SVN_VERSION} || ${version} || ${RELEASE_DATE} ||  || ${trac_type}${trac_append} ||"


	echo -e "\nThis release is finished.\nThe output for the track svn is: \n\n${TRACLINE}"
done
}

#-----
#End Functions
#-----

while :
do
	echo "--------------------"
	echo "Type the number of the task you would like to perform"
	echo "1) Run the environment configuration wizard"
	echo "2) View an editable list of all variables"
	echo "3) Update IP addresses manually"
	echo "4) Create a release"
	echo "5) Exit"
	echo "--------------------"
	
	read input
	
	case $input in
		1) EnvironWizard ;;
		2) View ;;
		3) UpdateIP ;;
		4) Release ;;
		5) break ;;
		*) echo "Please answer again." ;;
	esac
done

exit 1
