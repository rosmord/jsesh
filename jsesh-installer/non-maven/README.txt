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

c) run disk utilities and create a DMG image large enough for the project
    copy the file back1.png in the ".background" folder on this disk
    
    open .background
    edit display options, make background the background
    copy the JSesh application folder in the DMG
    make a symbolic link to application
    fix folder size (120x120, text 16pt is fine)

d) eject the dmg
e) from disk utility, menu "convert". Choose your dmg; make it read only and
    compressed.

f) done !

------------------------------------
Building a PC distribution : 
done with Launch4J to make an exe, and innoSetup to build the installer.


 
