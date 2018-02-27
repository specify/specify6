#!/bin/bash

if [ "$1" = "-h" -o "$1" = "--help" ] ; then
	echo -e "....sh operatingPath Cversion [mediaID filename_full] [] ... [] (repeat same two as needed with respective order)."
	echo -e 'Note that each "set" above (of two arguments) produces two entries in the updates.xml output file because there is a "full" update entry and an "update" update entry. Note that the operatingPath is the relative path from this script to that of the web_launch directory that the script is producing an updates.xml file for (Do not include the last slash).'
	exit
fi

CreateUpdateEntry () {
n=0
m="$#"

while [ "$n" -lt "$m" ]
do
	#parsing

		#mediaID
			mediaid="$1"

		#Cversion
			#current (version to be updated to) version = c1.c2.c3 and is represented by (y.x.?)
			#update version = u1.u2.u3
			#full version = f1.f2.f3
			#(n.m.q) <=> n.m.q
			
			#break the current version number up into components
			c1=`expr substr $version 1 1`
			c2=`expr substr $version 3 1`
			c3=`expr substr $version 5 2`

			#set component 1
			u1="$c1"
			f1="$c1"
			
			#set comonents 2 and 3

				#if the current version number is of the form y.x.00
				#then f2=u2=x-1
				#and
				#then f3=98;u3=99
				if [ "$c3" = "00" ] ; then
					u2=`expr $c2 - 1`
					f2=`expr $c2 - 1`
					
					u3="99"
					f3="98"

				#if the current version number is of the form y.x.01
				#then the update version (u1.u2.u3) is of the form y.x.00
				#and
				#then the full version (f1.f2.f3) is of the form y.x-1.99
				elif [ "$c3" = "01" ] ; then
					u2="$c2"
					f2=`expr $c2 - 1`					

					u3="00"
					f3="99"

				#if the current version number is of the form y.x.0z where z is 2,...,9
				#then the update version is of the form y.x.0(z-1)
				#and
				#then the full version is of the form y.x.0(z-2)
				elif [ `expr substr $version 5 1` = "0" ] ; then
					u2="$c2"
					f2="$c2"

					z=`expr substr $version 6 1`	
					u3=`expr $z - 1`
					f3=`expr $z - 2`

					u3=0$u3
					f3=0$f3				

				#if the current version number is fo the form y.x.z where z is 10,...,99
				#then the update version is of the form y.x.(z-1)
				#and
				#then the full version is of the form y.x.(z-2)
				elif [ "$c3" -ge 10 -a "$c3" -le 99 ] ; then
					u2="$c2"
					f2="$c2"

					u3=`expr $c3 - 1`
					f3=`expr $c3 - 2`			
				else
					echo -e "The version number passed to this script, $version, doesn't match any of the conditions defined in the script"
				fi
			
			updateversion="$u1.$u2.$u3"
			fullversion="$f1.$f2.$f3"

		#filename
			filename="$2"
			echo "$filename"
		#filesize
			filesize=`du -b $operatingpath/$filename | cut -f1`

	#entry output
	echo -e "\t<entry" >> tmp.xml
	echo -e "\t\ttargetMediaFileId=\"$mediaid\"" >> tmp.xml
	echo -e "\t\tupdatableVersionMin=\"1.0.0\"" >> tmp.xml
	echo -e "\t\tupdatableVersionMax=\"$updateversion\"" >> tmp.xml
	echo -e "\t\tfileName=\"$filename\"" >> tmp.xml
	echo -e "\t\tnewVersion=\"$version\"" >> tmp.xml
	echo -e "\t\tnewMediaFileId=\"$mediaid\"" >> tmp.xml
	echo -e "\t\tfileSize=\"$filesize\"" >> tmp.xml
	echo -e "\t\tbundledJre=\"\"" >> tmp.xml
	echo -e "\t\tarchive=\"false\"" >> tmp.xml
	echo -e "\t>" >> tmp.xml
	echo -e "\t</entry>" >> tmp.xml

	#loop maintenance
	mod_len=2
	n=`expr $n + $mod_len`
	shift $mod_len
done
}
#Globals
version="$2"
operatingpath="$1"
shift 2
echo "$@"


#output of updates.xml
[[ -f tmp.xml ]] && rm tmp.xml

echo -e '<?xml version="1.0" encoding="UTF-8"?>' >> tmp.xml
echo -e '<updateDescriptor>' >> tmp.xml
CreateUpdateEntry "$@"
echo -e '</updateDescriptor>' >> tmp.xml
