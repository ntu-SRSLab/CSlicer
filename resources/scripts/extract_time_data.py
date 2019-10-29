#!/usr/bin/python3

import os
import os.path
import sys
import csv
import argparse
import subprocess as sub
import run_examples as runex

def parseArgs(argv):
    '''
    Parse the args of the script.
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument('--repo', help='Repo path', required=True)
    parser.add_argument('--branch', help='Branch name', required=True)
    parser.add_argument('--log', help='Select log file', required=True)

    if (len(argv) == 0):
        parser.print_help()
        exit(1)
    opts = parser.parse_args(argv)
    return opts

def extract_definer_time(example):
    for f in os.listdir(runex.DEMO_RESULTS_DIR):
        if f == example + '.log':
            fr = open(runex.DEMO_RESULTS_DIR + '/' + f, 'r')
            lines = fr.readlines()
            fr.close()
            for i in range(len(lines)):
                if lines[i].startswith('[STATS] total.time :'):
                    definer_time = lines[i].strip().split(': ')[-1]
                    return float(definer_time)

def extract_cslicer_orig_time(example):
    for f in os.listdir(runex.CSLICER_ORIG_OUTPUT_DIR):
        if f == example + '.orig.log':
            fr = open(runex.CSLICER_ORIG_OUTPUT_DIR + '/' + f, 'r')
            lines = fr.readlines()
            fr.close()
            for i in range(len(lines)):
                if lines[i].startswith('[STATS] total.time :'):
                    cslicer_orig_time = lines[i].strip().split(': ')[-1]
                    return float(cslicer_orig_time)

def extract_cslicer_split_time(example):
    # split time
    for f in os.listdir(runex.SPLIT_LOGS_DIR):
        if f == example + '.logs':
            fr = open(runex.SPLIT_LOGS_DIR + '/' + f, 'r')
            lines = fr.readlines()
            fr.close()
            split_time = lines[-1].strip()
            break
    # run time
    for f in os.listdir(runex.CSLICER_SPLIT_OUTPUT_DIR):
        if f == example + '.split.log':
            fr = open(runex.CSLICER_SPLIT_OUTPUT_DIR + '/' + f, 'r')
            lines = fr.readlines()
            fr.close()
            for i in range(len(lines)):
                if lines[i].startswith('[STATS] total.time :'):
                    split_run_time = lines[i].strip().split(': ')[-1]
                    break
    return float(split_time) + float(split_run_time)

if __name__ == '__main__':
    for example in runex.examples:
        example_id = example.replace('-', '')
        definer_time = extract_definer_time(example)
        cslicer_orig_time = extract_cslicer_orig_time(example)
        cslicer_split_time = extract_cslicer_split_time(example)
        fr = open(runex.NUMBERS_TEX_PATH, 'r')
        number_lines = fr.readlines()
        fr.close()
        if definer_time != None:
            number_lines += '\\DefMacro{' + example_id + 'DefinerTime}{' + \
                            '{0:.2f}'.format(definer_time) + '}\n'
        else:
            number_lines += '\\DefMacro{' + example_id + 'DefinerTime}{' + \
                            'TimeOut' + '}\n'
        number_lines += '\\DefMacro{' + example_id + 'CSlicerOrigTime}{' + \
                        '{0:.2f}'.format(cslicer_orig_time) + '}\n'
        number_lines += '\\DefMacro{' + example_id + 'CSlicerSplitTime}{' + \
                        '{0:.2f}'.format(cslicer_split_time) + '}\n'
        fw = open(runex.NUMBERS_TEX_PATH, 'w')
        fw.write(''.join(number_lines))
        fw.close()
