#!/bin/bash

#file-ext.tar
#file-ext-embed.tar
#file-int.tar
#file-int-embed.tar

n=0

if [ -f "file-ext.tar" ]
then
	tar -xvf file-ext.tar --strip=2 home/specify/
	n=`n+1`
	mv web_launch_ext /var/www/spinternal/ext
fi

if [ -f "file-ext-embed.tar" ]
then
	tar -xvf file-ext-embed.tar --strip=2 home/specify/
	n=`n+1`
	mv web_launch_ext-embed /var/www/spinternal/ext_embed
fi

if [ -f "file-int.tar" ]
then
	tar -xvf file-int.tar --strip=2 home/specify/
	n=`n+1`
	mv web_launch /var/www/spinternal/int
fi

if [ -f "file-int-embed.tar" ]
then
	tar -xvf file-int-embed.tar --strip=2 home/specify/
	n=`n+1`
	mv web_launch /var/www/spinternal/int_embed
fi


if [ "$n" = 0 ] 
then
	echo "The script found no archives to untar."
fi

