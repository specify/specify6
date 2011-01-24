#!/bin/sh	


tar -xvf ./Specify_unix_Mobile.tar.gz
mv SpecifyMobile Linux
tar -xvf ./Specify_macos_Mobile.tgz
mv SpecifyMobile MacOSX
unzip ./Specify_windows_Mobile.zip
mv SpecifyMobile Windows
tar -xvf ./spdatabase.tar
mkdir SpecifyWorkBench
mv Linux SpecifyWorkBench
mv MacOSX SpecifyWorkBench
mv Windows SpecifyWorkBench
mv spdatabase SpecifyWorkBench
zip SpecifyWorkBench
