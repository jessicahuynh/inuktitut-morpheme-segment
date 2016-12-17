#!/bin/bash

NUNAVUT_HANSARD="./corpus/SentenceAligned.txt"
DECOMPOSITIONS="./data/morphemes.txt"

# create necessary directories
mkdir "data/"

# get morpheme data
echo "Decomposing Hansard corpus to $DECOMPOSITIONS from $NUNAVUT_HANSARD"
javac decompose/HansardDecompose.java -Xlint:unchecked
java decompose.HansardDecompose $NUNAVUT_HANSARD $DECOMPOSITIONS
echo "Done with morphemes from Nunavut Hansard"
