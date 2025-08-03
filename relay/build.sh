#!/bin/bash
set -e

cd src
javac *.java
mv * ../build
cd ../build
mv *.java ../src
java Relay 5477
cd ..
