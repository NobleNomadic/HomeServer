#!/bin/bash
set -e

cd src
javac *.java
mv * ../build
cd ../build
mv *.java ../src
java Server 8080
cd ..
