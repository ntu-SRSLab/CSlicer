#!/usr/bin/python3

import os
import os.path
import sys
import csv
import argparse
import subprocess as sub
import run_examples as runex

if __name__ == '__main__':
    examples  = runex.examples
    # cslicer-split-cslicer
    print ('EXP: cslicer-split-cslicer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --cslicer-split-cslicer-one ' + example, \
                shell=True)
    # cslicer-split-definer
    print ('EXP: cslicer-split-definer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --cslicer-split-definer-one ' + example, \
                shell=True)
    # split-cslicer
    print ('EXP: split-cslicer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --split-cslicer-one ' + example, \
                shell=True)
    # split-definer
    print ('EXP: split-definer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --split-definer-one ' + example, \
                shell=True)
    # cslicer-definer-split-cslicer
    print ('EXP: cslicer-definer-split-cslicer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --cslicer-definer-split-cslicer-one ' + \
                example, shell=True)
    # cslicer-definer-split-definer
    print ('EXP: cslicer-definer-split-definer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --cslicer-definer-split-definer-one ' + \
                example, shell=True)
    # cslicer
    print ('EXP: cslicer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --cslicer-one ' + example, shell=True)
    # definer
    print ('EXP: definer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --definer-one ' + example, shell=True)
    # definer-split-cslicer
    print ('EXP: definer-split-cslicer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --definer-split-cslicer-one ' + example, \
                shell=True)
    # definer-split-definer
    print ('EXP: definer-split-definer')
    for example in examples:
        sub.run('timeout 600 python3 run_examples.py --definer-split-definer-one ' + example, \
                shell=True)
    
