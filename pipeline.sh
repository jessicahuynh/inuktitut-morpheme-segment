#!/bin/bash

NUNAVUT_HANSARD="./corpus/SentenceAligned.txt"

# get data from corpus
echo "Decomposing Hansard corpus to ./data/hansard.txt from $NUNAVUT_HANSARD"
javac -classpath ./Uqailaut.jar decompose/HansardDecompose.java # decomposes Hansard corpus
java -classpath . decompose.HansardDecompose $NUNAVUT_HANSARD