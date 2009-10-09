#!/bin/sh
#run bash ./configure_environment.sh

backupsToServer () {

#Variable Declaration!
z=0
while read linenumber
do
	z=`expr $z + 1`
	first_field[$z]="`echo $linenumber | cut -d\| -f1`"
	third_field[$z]="`echo $linenumber | cut -d\| -f3`"
done < cache/environ.txt
m=0
while [ "$m" -lt "$z" ]
do
	m=`expr $m + 1`
	declare "${third_field[$m]}"="${first_field[$m]}"
done

while :
do
	echo "What do you want to do?"
	echo "1) Backup InstallerFiles from Trunk to $backupserver"
	echo "2) Backup InstallerFiles from Branch to $backupsever"
	echo "3) Backup InstallerFiles from Trunk and Branch to $backupserver"
done


mkdir backups


}

buildproject () {
#Internal/External 
copysh="$2"
if [ "$2" = "internal" ] ; then
	pwebloc="$intdirlocal"
	pweblocMac="$intdirmac"
	pPappend1=""
else
	pwebloc="$extdirlocal"
	pweblocMac="$extdirmac"
	pPappend1="-ext"
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
	elif [ "$1" = "m" ] ; then
		mode="-mobile"
		Pappend1="${pPappend1}${mode}"
		webloc="${pwebloc}${mode}"
		weblocMac="${pweblocMac}${mode}"
	else
		Pappend1="${pPappend1}"
		webloc="${pwebloc}"
		weblocMac="${pweblocMac}"		
	fi

	#Cleaning the machines
	rm -rf $webloc
	mkdir $webloc
	ssh ${username}@${macip} rm -rf $weblocMac

		#verify that ssh connects properly
		if [ "$?" = "255" ] ; then
			echo -e "----------------------------------------------\nThere was an error in connecting to the mac computer with ssh:  Please verify that you are using the correct ip address.\n----------------------------------------------"
			exit 1
		fi

	ssh ${username}@${macip} mkdir $weblocMac

	#copy over send script for internal builds
	[ "$copysh" = "internal" ] && cp ./copy.sh ${webloc}/copy.sh


	#building winlin projects
	$i4jlocal/install4jc --release="${version}" --destination="${webloc}" -s "${projloc}/${winlinbase}-full${Pappend1}.install4j"
	mv ${webloc}/updates.xml ${webloc}/updates.xml.${winlinbase}full
	$i4jlocal/install4jc --release="${version}" --destination="${webloc}" -s "${projloc}/${winlinbase}-update${Pappend1}.install4j"
	mv ${webloc}/updates.xml ${webloc}/updates.xml.${winlinbase}update

	#building mac projects
	ssh ${username}@${macip} ${i4jmac}/install4jc --release="${version}" --destination="${weblocMac}" -s "./${projlocMac}/${macbase}-full${Pappend1}.install4j"
	ssh ${username}@${macip} mv ${weblocMac}/updates.xml ${weblocMac}/updates.xml.${macbase}full

	ssh ${username}@${macip} ${i4jmac}/install4jc --release="${version}" --destination="${weblocMac}" -s "./${projlocMac}/${macbase}-update${Pappend1}.install4j"
	ssh ${username}@${macip} mv ${weblocMac}/updates.xml ${weblocMac}/updates.xml.${macbase}update

	#Transfer
	ssh ${username}@${macip} tar -cvf file.tar ${weblocMac}/*
	scp ${username}@${macip}:./file.tar  ./
	ssh ${username}@${macip} rm file.tar

	mv ./file.tar ../file.tar
	cd ..
	tar -xvf ./file.tar
	rm file.tar
	cd release_command_center

	component=$version
	len=${#component}
	
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

	$i4jU/Install4JUpdater ../${webloc}/updates.xml.${winlinbase}full ../${webloc}/updates.xml.${winlinbase}update ../${webloc}/updates.xml.${macbase}full ../${webloc}/updates.xml.${macbase}update ../${webloc}/updates${mode}.xml 6.0.0 $compOne $compTwo $compThree

	echo -e "\nThis release is finished.\n"
	
	shift 1
done

}

makeproject () {
o=0
i="$#"

echo "pre makeproject i=$i, o=$o"

while [ "$o" -lt "$i" ]
do
	o=`expr $o + 2`

	echo "pre makeproject i=$i, o=$o"

	echo -e "\n\nPlease enter the version number of the $1, $2 release (e.g. 6.2.90):"
	read version

	while :
	do
		echo -e "\nFor the $1, $2 release, put out:"
		echo -e "1) Plain + EZDB + Mobile\n2) Plain + EZDB\n3) Plain + Mobile\n4) EZDB + Mobile\n5) Plain\n6) EZDB\n7) Mobile"
		echo "Enter the number of the choice:"
	
		read choice
	
		case $choice in 
			1) buildproject $1 $2 $version p e m ; break ;;
			2) buildproject $1 $2 $version p e ; break ;;
			3) buildproject $1 $2 $version p m ; break ;;
			4) buildproject $1 $2 $version e m ; break ;;
			5) buildproject $1 $2 $version p ; break ;;
			6) buildproject $1 $2 $version e ; break ;;
			7) buildproject $1 $2 $version m ; break ;;
			*) echo "Please try again." ;;
		esac
	done

	shift 2
		echo "pre makeproject i=$i, o=$o"
done
}

Release () {

#Variable Declaration!
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

echo -e "\nVariable Declaration\n--------------------------"

echo "$thisipvar  =  $thisipdesc  =  $thisip"
echo "$macipvar  =  $macipdesc  =  $macip"
echo "$winlinbasevar  =  $winlinbasedesc  =  $winlinbase"
echo "$macbasevar  =  $macbasedesc  =  $macbase"
echo "$intdirlocalvar  =  $intdirlocaldesc  =  $intdirlocal"
echo "$extdirlocalvar  =  $extdirlocaldesc  =  $extdirlocal"
echo "$intdirmacvar  =  $intdirmacdesc  =  $intdirmac"
echo "$extdirmacvar  =  $extdirmacdesc  =  $extdirmac"
echo "$trunkdirlocalvar  =  $trunkdirlocaldesc  =  $trunkdirlocal"
echo "$trunkdirmacvar  =  $trunkdirmacdesc  =  $trunkdirmac"
echo "$branchdirlocalvar  =  $branchdirlocaldesc  =  $branchdirlocal"
echo "$branchdirmacvar  =  $branchdirmacdesc  =  $branchdirmac"
echo "$intservervar  =  $intserverdesc  =  $intserver"
echo "$extservervar  =  $extserverdesc  =  $extserver"
echo -e "\n\n"

while :
do
	#Questions
	echo -e "Enter the number of the trunk/branch combination.\n"
	echo -e "1  Trunk-Internal"
	echo -e "2  Trunk-External"
	echo -e "3  Branch-Internal"
	echo -e "4  Branch-External"
	echo -e "5  Trunk-Internal\tTrunk-External"
	echo -e "6  Trunk-Internal\tBranch-Internal"
	echo -e "7  Trunk-Internal\tBranch-External"
	echo -e "8  Trunk-External\tBranch-Internal"
	echo -e "9  Trunk-External\tBranch-External"
	echo -e "10  Branch-Internal\tBranch-External"
	echo -e "\nChoose by typing the relevant number and pressing enter."

	read choice
	
	case $choice in 
		1) makeproject trunk internal ; break ;;
		2) makeproject trunk external ; break ;;
		3) makeproject branch internal ; break ;;
		4) makeproject branch external ; break ;;
		5) makeproject trunk internal trunk external ; break ;;
		6) makeproject trunk internal branch internal ; break ;;
		7) makeproject trunk internal branch external ; break ;;
		8) makeproject trunk external branch internal ; break ;;
		9) makeproject trunk external branch external ; break ;;
		10) makeproject branch internal branch external ; break ;;
		*) echo "Please try again." ;;
	esac
done
}

UpdateIPAuto () {

	echo "This command doesn't do anything"
	exit

}

UpdateIP () {
echo -e "\nThis brief wizard udpates IP addresses for the update process."
echo "----------------------------------------------------"

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
	if [ "${third_field[$m]}" = "macip" ] ; then
		echo "The old value of ${second_field[$m]} is: ${first_field[$m]}"
			echo -e "Is this value correct?\n1) Yes\n2) No"
			read input1
			if [ "$input1" = "2" ] ; then
				echo "Please enter its new value:"
				read input2
				echo "$input2|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt
				echo "Your input has been written."
			else
				echo "Your old value has been saved"
				echo "${first_field[$m]}|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt
			fi
		echo -e "----------------------------------------------------\n\n"
	else
		echo "${first_field[$m]}|${second_field[$m]}|${third_field[$m]}" >> cache/environ_tmp.txt
	fi
done

mv -f cache/environ_tmp.txt cache/environ.txt

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

View () {
while :
do
	#Dump out visual representation of all variables
	echo -e "Enter the number of the variable you would like to change the value of.  The chart is variable description, variable value. \n\n"
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

WizardNew () {
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

while :
do
	echo "--------------------"
	echo "Type the number of the task you would like to perform"
	echo "1) Run the environment configuration wizard"
	echo "2) View an editable list of all variables"
	echo "3) Update IP addresses manually"
	echo "4) Automatically update the IP addresses"
	echo "5) Run a release"
	echo "6) Exit"
	echo "--------------------"
	
	read input
	
	case $input in
		1) WizardNew ;;
		2) View ;;
		3) UpdateIP ;;
		4) UpdateIPAuto ;;
		5) Release ;;
		6) break ;;
		*) echo "Please answer again." ;;
	esac
done

exit 1

