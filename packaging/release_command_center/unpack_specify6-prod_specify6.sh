#!/bin/bash
#decompile from server

ARGS=1

if [ $# -ne 1 ] ; then
	echo "Only 1 argument available:  name of wblaunch dir that you want to move."
	exit 0;
fi

if [ -d $1 ] ; then
	directory=$1

fi
