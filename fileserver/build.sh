#!/bin/bash
set -e

cd src
javac *.java
mv * ../build
cd ../build
mv *.java ../src
java FileServer 5400
cd ..
