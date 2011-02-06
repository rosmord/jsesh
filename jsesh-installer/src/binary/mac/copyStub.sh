#!/bin/bash
mkdirIfNotE () {
    if [ ! -d "$1" ] ; then
	mkdir "$1"
    fi
}

# ok, we need to fix the Info.plist files a bit... we want them
# to contain a list
# of strings like <string>$APP_PACKAGE/../lib/antlr-2.7.7.jar</string>
# where the original files contain <MY_CLASSPATH/>


dest="$1"
cd "$dest"

ls ./lib/ | sed 's%\(.*\)%<string>$APP_PACKAGE/../lib/\1</string>%' >/tmp/dep_list.txt

cat  Info.plist.jsesh /tmp/dep_list.txt Info.plist.end > "$dest/JSesh.app/Contents/Info.plist"

# cleanup
#rm Info.plist.end Info.plist.jsesh


# Actual stub copy...
# Avoid any License problems...
stub=/System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub

mkdirIfNotE "$dest/JSesh.app/Contents/MacOS"
cp $stub "$dest/JSesh.app/Contents/MacOS"

