#!/bin/sh

ftp -v ftp.specifysoftware.org <<END_SCRIPT
cd update.specifysoftware.org
cd test
put ~/web_launch/updates.xml ./updates.xml.test
quit
END_SCRIPT
