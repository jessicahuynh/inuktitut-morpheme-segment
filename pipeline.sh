#!/bin/bash

NUNAVUT_HANSARD="./corpus/NunavutHansard.txt"
BIBLE_DIR="./corpus/bible/"
DATA_DIR="./data/"

# create necessary directories
mkdir $DATA_DIR
mkdir "$DATA_DIR/test/"
mkdir "$DATA_DIR/train/"

# making data sets
echo "Creating data sets"
javac data/Corpus.java -Xlint:unchecked
java data.Corpus $NUNAVUT_HANSARD $BIBLE_DIR $DATA_DIR
echo "Done with making data sets"
