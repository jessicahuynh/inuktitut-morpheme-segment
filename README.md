# Statistical morpheme segmentation of Inuktitut

This project uses the [Inuktitut Morphological Analyzer](http://www.inuktitutcomputing.ca/Uqailaut/info.php) developed by the National Research Council of Canada on the [Nunavut Hansard corpus](http://www.inuktitutcomputing.ca/NunavutHansard/info.php?lang=en) and the Inuktitut Bible to obtain a baseline and training data consisting of Inuktitut words and their morphemes.

Then, the project uses the Morfessor package (which implements HMMs specifically for segmentation), using the recursive algorithm for training, to try and get the best segmentations (decoding done with Viterbi).

The test set is a portion of the Inuktitut Bible.

## Project setup
- README, .gitignore, etc
- pipeline.sh: bash script that runs everything
- Uqailaut.jar: the morphological analyzer
- corpus/
    - NunavutHansard.txt: version 2.0 of the Nunavut Hansard (too large for GitHub, download from link above)
    - NunavutHansard-sm.txt: first 100k lines of the Nunavut Hansard
    - bible/
        - genesis.txt
- scripts/
    - Corpus.java
    - run_hmm.py
    - make_small.py
- data/
    - train/
        - genesis-annotation
        - genesis-text
        - NunavutHansard-sm-annotation
        - NunavutHansard-sm-text
        - NunavutHansard-text
    - test/
        - genesis-gold
        - genesis-text
        - NunavutHansard-sm-text
        - NunavutHansard-text
- models/
    - bible.bin
    - bible-segmentation
    - hansard.bin
- results/: decoding using the various models on the test set (genesis-gold)
- writeup/
    - finalproject.tex: LaTeX file
    - finalproject.pdf: PDF of the final report
    - bib.bib: BibTeX bibliography

## How to run
    bash ./pipeline.sh

Ideally you should have Java 1.8 and Python 3 (although it should work with Python 2.7).

## Results
The data is too sparse for really good results. Generally, increasing the weight assigned to the annotated corpus increases F-measure and recall at the cost of precision over the unsupervised method.

In the future I would leave enough time to train and test on the Nunavut Hansard, train with online and online+batch training, and train with Viterbi in addition to the recursive algorithm.

The results are such that, at least with this size dataset, I wouldn't recommend statistical methods over a rule-based one, especially since Inuktitut morphology is so regular.