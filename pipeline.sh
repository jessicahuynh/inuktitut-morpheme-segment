#!/bin/bash

NUNAVUT_HANSARD="./corpus/NunavutHansard-sm.txt"
BIBLE_DIR="./corpus/bible/"

DATA_DIR="./data/"
TEST_DIR="test/"
TRAIN_DIR="train/"
MODEL_DIR="./models"

MODEL_BIBLE="$MODEL_DIR/bible.bin"
MODEL_HANSARD="$MODEL_DIR/hansard.bin"

# create necessary directories
mkdir $DATA_DIR
mkdir "$DATA_DIR$TEST_DIR"
mkdir "$DATA_DIR$TRAIN_DIR"
mkdir $MODEL_DIR

# making data sets
echo "Creating data sets"
python scripts/make_small.py
javac scripts/Corpus.java -Xlint:unchecked
java scripts.Corpus $DATA_DIR $NUNAVUT_HANSARD
echo "Done with making data sets"

# build, train, decode, and evaluate
echo "Building models"
morfessor -t "data/train/NunavutHansard-sm-text" -s $MODEL_HANSARD

echo "Running python scripts/run_hmm.py"
python scripts/run_hmm.py $MODEL_HANSARD "data/train/NunavutHansard-sm-annotation" "data/test/NunavutHansard-sm-text" "data/test/NunavutHansard-sm-gold"

