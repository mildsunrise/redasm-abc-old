#!/bin/bash

PREFIX="/usr/local"
DNAME="redasm"
TARGET="target/redasm.jar"

if let UID!=0;
then {
    echo -e "You must run this script as root.";
    exit 5;
}; fi;

r="`dirname "$0"`"
d="$PREFIX/bin"

# Build redasm-abc
#mvn package

# Copy the target
cp "$r/$TARGET" "$d/$DNAME"

