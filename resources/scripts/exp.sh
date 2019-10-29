#!/bin/bash

./run_examples.py --clean-touchset


# echo "EXP: cslicer-standalone"
# time ./run_examples.py --cslicer
# ./run_examples.py --clean-touchset
echo "EXP: definer-standalone"
time ./run_examples.py --definer
./run_examples.py --clean-touchset

# echo "EXP: split-cslicer"
# time ./run_examples.py --split-cslicer
# ./run_examples.py --clean-touchset
# echo "EXP: split-definer"
# time ./run_examples.py --split-definer
# ./run_examples.py --clean-touchset

# echo "EXP: cslicer-split-cslicer"
# time ./run_examples.py --cslicer-split-cslicer
# ./run_examples.py --clean-touchset
# echo "EXP: cslicer-split-definer"
# time ./run_examples.py --cslicer-split-definer
# ./run_examples.py --clean-touchset

# echo "EXP: definer-split-cslicer"
# time ./run_examples.py --definer-split-cslicer
# ./run_examples.py --clean-touchset
# echo "EXP: definer-split-definer"
# time ./run_examples.py --definer-split-definer
# ./run_examples.py --clean-touchset

# echo "EXP: cslicer-definer-split-cslicer"
# time ./run_examples.py --cslicer-definer-split-cslicer
# ./run_examples.py --clean-touchset
# echo "EXP: cslicer-definer-split-definer"
# time ./run_examples.py --cslicer-definer-split-definer
# ./run_examples.py --clean-touchset

# echo "EXP: cslicer-definer"
# time ./run_examples.py --cslicer-definer
# ./run_examples.py --clean-touchset


# --- ASEJ ---
# echo "EXP2: LEARNING"
# time ./run_examples.py --definer-asej-exp "learning"
# ./run_examples.py --clean-touchset

# echo "EXP2: BASIC"
# time ./run_examples.py --definer-asej-exp "basic"
# ./run_examples.py --clean-touchset

# echo "EXP3: NEG"
# time ./run_examples.py --definer-asej-exp "neg"
# ./run_examples.py --clean-touchset

# echo "EXP3: NOPOS"
# time ./run_examples.py --definer-asej-exp "nopos"
# ./run_examples.py --clean-touchset

# echo "EXP3: LOW3"
# time ./run_examples.py --definer-asej-exp "low3"
# ./run_examples.py --clean-touchset


# --- ISSTA 19 ---

# echo "EXP: cslicer-definer-split-definer"
# time ./run_examples.py --cslicer-definer-split-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: cslicer-definer-split-cslicer"
# time ./run_examples.py --cslicer-definer-split-cslicer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: definer-split-definer"
# time ./run_examples.py --definer-split-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: split-definer-cslicer-definer"
# time ./run_examples.py --split-definer-cslicer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: definer-definer"
# time ./run_examples.py --definer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: split-definer-definer"
# time ./run_examples.py --split-definer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: definer-cslicer-split-definer"
# time ./run_examples.py --definer-cslicer-split-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: cslicer-split-definer-definer"
# time ./run_examples.py --cslicer-split-definer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: definer-split-cslicer-definer"
# time ./run_examples.py --definer-split-cslicer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: split-cslicer-definer-definer"
# time ./run_examples.py --split-cslicer-definer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: definer-cslicer-definer"
# time ./run_examples.py --definer-cslicer-definer --share-suffix
# ./run_examples.py --clean-touchset

# echo "EXP: cslicer-definer-definer"
# time ./run_examples.py --cslicer-definer-definer --share-suffix
# ./run_examples.py --clean-touchset

