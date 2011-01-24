#!/bin/bash



while :
do

	if [ $# -gt 0 ]
	then
		if [ $1 = "1" -o $1 = "2" -o $1 = "3" -o $1 = "4" ]
		then
			choice=$1
		else
			echo "EXIT:  FAILED TO PASS 1,2,3,4; EXACTLY 1 ARG TO SCRIPT."
			exit 0
		fi
	else

	echo "What sort of release you be puttin' up to specify6-prod??"
	echo "1) internal w/ full launcher set"
	echo "2) external w/ limited launcher set"
	echo "3) like 1, but with ezdb"
	echo "4) like 2, but with ezdb"

	read choice

	fi

	case $choice in 
		1) tar -cvf ~/web_launch/file-int.tar ~/web_launch/Specify*.sh ~/web_launch/Specify*.dmg ~/web_launch/Specify*.exe ~/web_launch/updates.xml ; scp ~/web_launch/file-int.tar NHM\\johnd@specify6-prod.nhm.ku.edu:./file-int.tar ; break ;;
		2) tar -cvf ~/web_launch_ext/file-ext.tar ~/web_launch_ext/Specify*.sh ~/web_launch_ext/Specify*.dmg ~/web_launch_ext/Specify*.exe ~/web_launch_ext/updates.xml ; scp ~/web_launch_ext/file-ext.tar NHM\\johnd@specify6-prod.nhm.ku.edu:./file-ext.tar ; break ;;
		3) tar -cvf ~/web_launch-embed/file-int-embed.tar ~/web_launch-embed/Specify*.sh ~/web_launch-embed/Specify*.dmg ~/web_launch-embed/Specify*.exe ~/web_launch-embed/updates-embed.xml ; scp ~/web_launch-embed/file-int-embed.tar NHM\\johnd@specify6-prod.nhm.ku.edu:./file-int-embed.tar ; break ;;
		4) tar -cvf ~/web_launch_ext-embed/file-ext-embed.tar ~/web_launch_ext-embed/Specify*.sh ~/web_launch_ext-embed/Specify*.dmg ~/web_launch_ext-embed/Specify*.exe ~/web_launch_ext-embed/updates-embed.xml ; scp ~/web_launch_ext-embed/file-ext-embed.tar NHM\\johnd@specify6-prod.nhm.ku.edu:./file-ext-embed.tar ; break ;;
		*) echo "Your answer is not a number between 1 and 4. Please try again.";;
	esac
 
done
