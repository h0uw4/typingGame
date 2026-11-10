#!/bin/bash
# Compile and run (requires JDK 17+)
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name '*.java') && java -cp out:res typingboss.Main
