#!/bin/bash

BASE_DIR="/mnt/c/Users/tkuen/Entwicklung/GitHub/souffleur/server"

VERSION=`perl -lne 'print $1 if /VERSION = "(\S*)"/' < "$BASE_DIR/src/eu/thomaskuenneth/souffleur/Main.java"`

jpackage --linux-menu-group "Thomas Kuenneth" --vendor "Thomas Kuenneth" --name Souffleur --icon $BASE_DIR/artwork/Souffleur.png --type deb --app-version $VERSION --input $BASE_DIR/out/artifacts/server_jar --main-jar server.jar
