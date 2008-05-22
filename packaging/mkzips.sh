#!/bin/sh
tar -cvf windows.tar Specify SpecifyBuilder.exe SpecifyDBInit.exe Specify.exe SpecifyLocalizer.exe iReports4Specify.exe
gzip windows.tar

tar -cvf linux.tar Specify specifybuilder.sh specifyschema.sh specify.sh specifydbinit.sh ireportlaunch.sh
gzip linux.tar

tar -cvf mac.tar Specify.app SpecifyBuilder.app SpecifyDBInit.app SpecifySchema.app iReports4Specify.app
gzip mac.tar

tar -cvf mac_brief.tar Specify.app SpecifyBuilder.app
gzip mac_brief.tar

