#!/bin/bash

BASE_DIR="/Users/Thomas/Entwicklung/GitHub/souffleur/server"

VERSION=`perl -lne 'print $1 if /VERSION = "(\S*)"/' < "$BASE_DIR/src/eu/thomaskuenneth/souffleur/Main.java"`

jpackage --vendor "Thomas Kuenneth" --name Souffleur --icon $BASE_DIR/artwork/Souffleur.icns --type dmg --app-version $VERSION --input $BASE_DIR/out/artifacts/server_jar --main-jar server.jar
