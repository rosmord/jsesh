The files here are used in the last, non-automated phase of building a
JSesh distribution. 

I will probably try to automate everything at some point, this being said.

------------------------------------
Building a Mac Distribution:

a) run the jar installer to have everything where it belongs.

b) replace the original main.sh files for JSesh and SignInfo with those from
    jsesh-sh and signInfo-sh in this folder.
    (check that they have the +x flag)

b1) place the jre in the Contents folder of the application.

See JSesh-7.0.1-dist.pkgproj for a config file for the Packages software for Mac.

------------------------------------
Building a PC distribution : 
done with Launch4J to make an exe, and innoSetup to build the installer.


 
