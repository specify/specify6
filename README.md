# Specify 6
![Build Status](https://github.com/specify/specify6/workflows/Specify%206%20CI/badge.svg)

Specify Software is a product of the Specify Collections Consortium
that is funded by its member institutions. Consortium Founding Members
include: University of Michigan, University of Florida, Denmark
Consortium of Museums, and the University of Kansas. The Consortium
operates under the non-profit U.S. tax status of the University of
Kansas Center for Research. Specify was supported previously by
multiple awards from the U.S. National Science Foundation.

Specify 6 Copyright © 2023 University of Kansas Center for
Research. Specify comes with ABSOLUTELY NO WARRANTY.  This is free
software licensed under GNU General Public License 2 (GPL2).


    Specify Collections Consortium
    Biodiversity Institute
    University of Kansas
    1345 Jayhawk Blvd.
    Lawrence, KS USA 66045



## Building Specify 6

Building Specify 6 requires the **JDK v1.8** and **Apache Ant v1.9.3**.

The following build targets are available:

* `ant compile-nonmac` - Compile Java source code to target platforms
  other than Mac OSX.

* `ant compile-mac` - Compile Java source code targeting Mac OSX.

* `ant make-jar-nonmac` - Assemble JAR file for non Mac targets.

* `ant make-jar-mac` - Assemble JAR file for Mac targets.

* `ant generate-datamodel` - Generate
  `config/specify_datamodel.xml`. This target is depended upon by the
  packaging targets and will be run automatically for packaging. It
  needs to be run manually, however, to permit Specify to run from
  within an IDE.

* `ant run-specify-nonmac` - Execute the main Specify application for
  non Mac systems.

* `ant run-specify-mac` - Execute the main Specify application on Mac
  systems.

All build outputs will be found in the `build/` directory except for
`config/specify_datamodel.xml`.

## Packaging Specify 6

Packaging Specify 6 requires **Install4J  v8.0** (evaluation or
licensed).

The path to Install4J must be provided to the Ant
build through the `-Dinstall4j.dir=/path/to/install4j` parameter unless
Install4J is at the default location `$HOME/install4j8.0.11`.

The following Ant build targets are available for packaging:

* `ant package-nonmac` - Produce installers
  for Windows and Linux 64 and 32 bit systems.

* `ant package-mac` - Produce installers for Mac systems.

* `ant package-all` - Produce installers for all systems 
  plus the *updates.xml* auto-updating information.

All packaging products will be found in the `packages/`
directory.

### Code Signing

The Windows and Mac installers can be code signed by passing the
following properties to Ant:

* `code.signing` set to "true" to enable code signing.

* `win.pkcs12` with the path to the Windows code signing certificate
  with root and intermediate certificates.

* `win-keystore-password` with the encryption password for the above.

* `mac.pkcs12` with the path to the Mac code signing key.

* `mac-keystore-password` with the encryption password for the above.

For example:

```sh
ant package-all \
    -Dcode.signing=true \
    -Dwin.pkcs12=/mnt/biteme/BI/Specify/Specify6/Certificates/WindowsCertificates/certwithroot.pfx \
    -Dwin-keystore-password=SECRET \
    -Dmac.pkcs12=/mnt/biteme/BI/Specify/Specify6/Certificates/MacOsCertificates/SpecifyMacOSCert.p12 \
    -Dmac-keystore-password=SECRET
```
## Automated Builds

This repository is equipped with automated builds using *GitHub
actions*. All commits pushed to the repository will result in test
builds that can be found under the [actions
tab](https://github.com/specify/specify6/actions) with downloadable
package artifacts for testing that persist for 90 days. If a commit is
tagged and pushed the repository, a GitHub *draft prerelease* with the
name of the tag will be automatically created and have the build
output packages attached.

## Proposed Release Process (starting with v6.7.00)

A process for producing final releases could be as follows:

1. Tag the commit to be released with the version number plus `rcX`,
   where `X` is the release candidate number.

1. The automated build system will produce a prerelease as described
   above.

1. The external packages can be downloaded and given final testing.

1. If all is well, retag the commit with the plain version number.

1. Using the GitHub UI, remove the prerelease status from the
   resulting automatic build prerelease.

1. Download the external installer packages and `updates.xml`.

1. In the account specify@files.specifysoftware.org create a new
   directory named `XYZ` where *X*, *Y*, and *Z* come from the version
   number `X.Y.Z`.

1. Upload the installer packages and `updates.xml` to the
   newly created directory.

1. Upload the `readme.html` for the new version to the new directory
   with the filename `relnotes.html`.

1. Take a copy of `index.html` from a previous version folder and
   update it with the current version number and place that in the new
   directory.

1. To make the release live for auto updates, copy the contents of the
   new version directory to the home directory[![analytics](http://www.google-analytics.com/collect?v=1&t=pageview&dl=https%3A%2F%2Fgithub.com%2Fspecify%2F/specify6&uid=readme&tid=UA-169822764-4)]()
