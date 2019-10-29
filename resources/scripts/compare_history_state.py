#!/usr/bin/env python3

import os
import os.path
import sys
import csv
import time
import argparse
import shutil
import subprocess as sub
import run_examples as runex
import gen_all_configs_table as gentables

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script

def extractHistorySliceFromCSlicerLog(cslicer_log):
    fr = open(cslicer_log)
    lines = fr.readlines()
    fr.close()
    commit_list = []
    commit_msg_list = []
    for i in range(len(lines)):
        if lines[i].startswith('TEST: ') or \
           lines[i].startswith('COMP: ') or \
           lines[i].startswith('HUNK: '):
            commit = lines[i].split()[1]
            commit_msg = lines[i].strip().split(' : ')[-1]
            commit_list.append(commit)
            commit_msg_list.append(commit_msg)
    commit_list.reverse()
    commit_msg_list.reverse()
    return commit_list, commit_msg_list

def extractHistorySliceFromDefinerLog(definer_log):
    fr = open(definer_log)
    lines = fr.readlines()
    fr.close()
    commit_list = []
    commit_msg_list = []
    for i in range(len(lines)):
        if lines[i].startswith('[OUTPUT] H*:'):
            commit = lines[i].split()[2]
            commit_msg = lines[i].strip().split(' : ')[-1]
            commit_list.append(commit)
            commit_msg_list.append(commit_msg)
    return commit_list, commit_msg_list

def genFileLevelSliceFromOrigSlice(orig_hist_slice):
    # cherry-pick all commits to another branch and do split there
    # return a list of commit messages
    pass

def 
