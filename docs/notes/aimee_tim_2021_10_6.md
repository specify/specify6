# Aimee/Tim notes 2021-10-6

## Conversion 

- scripts and tools 
- Github - Most scripts in Sp 6 repo
- Strategies
  - First handle tree - separate out taxonomy and geography 
    - Taxonomy synonyms is a problem
    - Parasite host relationship can be a problem (insects)
  - Generic: Flatten  file first, use uploader
  - Parsing agents etc, lists of agents with one-many,  diff tools,
    one-offs for individual projects
  - Sometimes put everything in without resolving dupes, remove dupes later
  - Only some collections think of collection events as one to many
    with  collection object (ex fish).  If no CE, one to one is default
  - Details are the problem - format dates, lat/lon, etc
  - Try to get user to do prep work
- Stages
- Sizes - 95% are 3K - 300K, 50K average

## Specify 6

- Go through code repo
- object diagram, process diagram
  - We will whiteboard, and discuss 
- Schema update process
  - Hibernate does most of the work (adding new things)
  - Modifying existing or dropping things must be done with SQL code
- Report writer
  - Designer based on 2007 code, not touched > 10 years in Sp6, getting
    to work in 7
  - Updated Jasper engine but that’s it
- Hibernate
  - Mixed with SQL b/c Hibernate is so heavy
- Most difficult: Data entry form 
  - complex and recursive
  - Business rules applied at diff times in the process, Tim goes here first
  - Other ways to fix outside of it
- Missing Sp7 elements 
  - Schema config
  - Permissions systems
  - Wizard to create/configure databases
  - Workbench is done!
- Other stuff
  - Export JVM issue
    - JVM args must be built into Windows exe
    - Could experiment with different args/vals on Linux to determine
      best way, then implement in Windows version
  - Barcelona
  - U of Washington bad data
  - Others?
  
## Other considerations

- Explain commented code
- Ben can help with building app(s), signing, certificates, etc
- Tim immediate priorities:
  - Conversion script - minor doc and to github
  - Add package-level explanatory docstrings to files
  - Entry points for other stand-alone apps
    - clear in install4j configuration - which is in packaging directory,
    top level of source tree
    - Could also look for all the ‘main’ methods
    - Primary apps (maybe 8 total)
      - DB creation wizard
      - Data exporter
      - Security wizard