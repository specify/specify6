# Specify 6

The Specify Software Project is funded by the Advances in
Biological Informatics Program, U.S. National Science Foundation
(NSF/BIO: 1565098).
  
Specify 6 Copyright © 2017 University of Kansas Center for
Research. Specify comes with ABSOLUTELY NO WARRANTY.  This is
free software licensed under GNU General Public License 2
(GPL2).

 
    Specify Software Project
    Biodiversity Institute
    University of Kansas
    1345 Jayhawk Blvd.
    Lawrence, KS USA 66045
 


## Building Specify 6

Building Specify 6 requires the **JDK v1.8** and **Apache Ant v1.9**.

The following build targets are available:

* `ant compile-nonmac` - Compile Java source code to target platforms
  other than Mac OSX.
  
* `ant compile-mac` - Compile Java source code targeting Mac OSX.

* `ant make-jar-nonmac` - Assemble JAR file for non Mac targets.

* `ant make-jar-mac` - Assemble JAR file for Mac targets.

* `ant run-specify-nonmac` - Execute the main Specify application for
  non Mac systems.
  
* `ant run-specify-mac` - Execute the main Specify application on Mac
  systems.
  
All build outputs will be found in the `build/` directory.
  
## Packaging Specify 6

Packaging Specify 6 requires **Install4J  v5.1** (evaluation or
licensed).

Developers within the Biodiversity Institute network should
use the floating license on *specify6-prod.nhm.ku.edu* by running
`/path/to/install4j/bin/install4jc
--license=FLOAT:specify6-prod.nhm.ku.edu` after installing Install4J.

The path to Install4J must be provided to the Ant
build through the `-Dinstall4j.dir=/path/to/install4j` parameter. 

The following Ant build targets are available for packaging:

* `ant package-internal-nonmac` - Produce internal style installers
  and updaters for Windows and Linux 64 and 32 bit systems.
  
* `ant package-internal-mac` - Produce internal style installers and
  updaters for Mac systems.
  
* `ant package-external-nonmac` - Produce release style installers
  and updaters for Windows and Linux 64 and 32 bit systems.
  
* `ant package-external-mac` - Produce release style installers and
  updaters for Mac systems.

* `ant package-internal` - Produce internal style installers and
  updaters for all systems including *updates.xml* auto-updating
  information.

* `ant package-external` - Produce release style installers and
  updaters for all systems including *updates.xml* auto-updating
  information.

* `ant package-all` - Produce internal and release style installers and
  updaters for all systems including *updates.xml* auto-updating
  information.
  
All packaging products will be found in the `packages/`
directory. Internal style packages will be in `packages/internal`, and
release style packages will be in `packages/external`.

