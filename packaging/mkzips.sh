#!/bin/sh
tar -cvf windows.tar Specify SpecifyBuilder.exe SpecifyDBInit.exe Specify.exe SpecifyLocalizer.exe
gzip windows.tar

tar -cvf linux.tar Specify specifybuilder.sh specifyschema.sh specify.sh specifydbinit.sh
gzip linux.tar

tar -cvf mac.tar Specify.app SpecifyBuilder.app SpecifyDBInit.app SpecifySchema.app
gzip mac.tar

tar -cvf mac_brief.tar Specify.app SpecifyBuilder.app
gzip mac_brief.tar

