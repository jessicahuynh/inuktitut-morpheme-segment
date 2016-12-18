# Statistical morpheme segmentation of Inuktitut

This project uses the [Inuktitut Morphological Analyzer](http://www.inuktitutcomputing.ca/Uqailaut/info.php) developed by the National Research Council of Canada on the [Nunavut Hansard corpus](http://www.inuktitutcomputing.ca/NunavutHansard/info.php?lang=en) and the Inuktitut Bible to obtain a baseline and training data consisting of Inuktitut words and their morphemes.

Then, the project runs the following statistical methods to try to beat the baseline:
- find some methods

The test set will also be from the Nunavut Hansard and Inuktitut Bible.

## Project setup
- README, .gitignore, etc
- pipeline.sh: bash script that runs everything
- Uqailaut.jar: the morphological analyzer
- corpus/
    - NunavutHansard.txt: version 2.0 of the Nunavut Hansard (too large for GitHub, download from link above)
    - bible/
        - genesis.txt
        - exodus.txt
- data/
    - Corpus.java: constructs train and test sets for the different models
    - train/
    - test/
- writeup/
    - finalproject.tex: LaTeX file
    - finalproject.pdf: PDF of the final report
    - bib.bib: BibTeX bibliography

## How to run

## Results