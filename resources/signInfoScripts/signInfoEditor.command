#!/bin/bash

DIR="$INSTALL_PATH"
CP="$DIR/JSesh.app/Contents/Resources/Java/jsesh.jar"
MAINCLASS=jsesh.signInfoEditor.ui.SignInfoEditorApplication

java -cp "$CP" $MAINCLASS


