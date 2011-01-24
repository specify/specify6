#!/bin/bash

ARGS=2
E_BADARGS=65

if [ $# -ne "$ARGS" ]
then
	echo "Usage: `basename $0` new-pattern filename"
	exit $E_BADARGS
fi

pattern=$1


if [ -f "$2" ]
then
	file_name=$2
else
	echo "File \"$2\" does not exist."
	exit $E_BADARGS
fi

sed -e "s#UPDATE_PATH=http:\/\/.*#UPDATE_PATH=http:\/\/$pattern#g" $file_name > tmpdump.txt
mv tmpdump.txt $file_name

exit 0
