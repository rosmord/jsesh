#!/bin/bash

# A shell script to start JSesh sign info.

# remove when script is ok.
#LOG=/tmp/$$.log
#exec > $LOG 2>&1
#set -x

# JRE is not known by java_home...
# find it here:
JRE=/Library/Internet\ Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java
found=0

JAVA_MIN_VERSION=001008

if [ -f "$JRE" ] ; then
	# extract version. we really want the first two parts of the version
	version=$("$JRE" -version 2>&1 | awk -F '"' '/version/ {print $2}')
	# format on 3 digits by version part. Should be ok for a while.
	version1=$(echo "$version" | awk -F. '{printf("%03d%03d",$1,$2);}')
	echo >&2 "JRE version: $version1"
        if [ $version1 -ge $JAVA_MIN_VERSION ] ; then
		found=1
	fi	
fi

#Check which of those properties are really needed...
PROPERTIES="-Dapple.awt.textantialiasing=true -Dapple.laf.useScreenMenuBar=true"
PROPERTIES="$PROPERTIES -Dapple.awt.antialiasing=true -Dapple.awt.showGrowBox=true -Dapple.awt.graphics.UseQuartz=true"

if [ $found -eq 1 ] ; then
    # Allows drag and drop to application icon:
    export CFProcessPath="$0"
    # find relevant folders
    BINDIR=$(dirname "$0")
    CONTENTS="$BINDIR/.."
    LIB="$CONTENTS/../../JSesh.app/Contents/lib"
    # Starts java
    # removed -Djava.library.path="$LIB" because not needed anymore...
    APPCLASS=jsesh.utilitySoftwares.signInfoEditor.ui.SignInfoEditorApplication
    "$JRE" -Xmx512m  -cp "$LIB/*" -Xdock:name="Sign Info" $APPCLASS
else
    osascript -e 'tell app "Finder" to display dialog "Java not found\nPlease download at least jdk 1.8"'
    open "https://www.java.com/download"
fi
