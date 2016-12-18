#!/bin/bash

NUNAVUT_HANSARD="./corpus/NunavutHansard.txt"
BIBLE_DIR="./corpus/bible/"

DATA_DIR="./data/"
TEST_DIR="test/"
TRAIN_DIR="train/"
MODEL_DIR="./models"

# create necessary directories
mkdir $DATA_DIR
mkdir "$DATA_DIR$TEST_DIR"
mkdir "$DATA_DIR$TRAIN_DIR"
mkdir $MODEL_DIR

# making data sets
echo "Creating data sets"
javac scripts/Corpus.java -Xlint:unchecked
java scripts.Corpus $NUNAVUT_HANSARD $BIBLE_DIR $DATA_DIR
echo "Done with making data sets"

# build, train, decode, and evaluate
echo "Running python scripts/run_hmm.py"
python scripts/run_hmm.py

