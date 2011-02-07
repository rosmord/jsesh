#!/bin/bash

mkdirIfNotE () {
    if [ ! -d "$1" ] ; then
	mkdir "$1"
    fi
}

# prepare an application.
#
prepareApp() {
    app="$1"
    head="Info.plist.$app"
    plistFile="$dest/$app.app/Contents/Info.plist"
    cat  "$head" /tmp/dep_list.txt Info.plist.end > "$plistFile"

    # Actual stub copy...
    # Avoid any License problems...
    stub=/System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub

    mkdirIfNotE "$dest/$app.app/Contents/MacOS"
    cp $stub "$dest/$app.app/Contents/MacOS"
    # cleanup
    rm "$head"
}


dest="$1"
cd "$dest"


# ok, we need to fix the Info.plist files a bit... we want them
# to contain a list
# of strings like <string>$APP_PACKAGE/../lib/antlr-2.7.7.jar</string>
# where the original files contain <MY_CLASSPATH/>

ls ./lib/ | sed 's%\(.*\)%<string>$APP_PACKAGE/../lib/\1</string>%' >/tmp/dep_list.txt

prepareApp JSesh
prepareApp SignInfo


# cleanup
rm Info.plist.end




