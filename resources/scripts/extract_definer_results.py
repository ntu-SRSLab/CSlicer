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

def extract_definer_results(example):
    for dir_path, subpaths, files in os.walk(runex.DOSC_META_DIR):
        for f in files:
            if f == example + '.yml':
                fr = open(dir_path + '/' + f, 'r')
                lines = fr.readlines()
                fr.close()
                for i in range(len(lines)):
                    if lines[i].startswith('history slice'):
                        start_index = i + 1
                    elif lines[i].startswith('developer labeled commits:'):
                        end_index = i - 1
                history_slice_len = end_index - start_index + 1
                return history_slice_len

if __name__ == '__main__':
    for example in runex.examples:
        example_id = example.replace('-', '')
        definer_history_slice_len = extract_definer_results(example)
        fr = open(runex.NUMBERS_TEX_PATH, 'r')
        number_lines = fr.readlines()
        fr.close()
        number_lines += '\\DefMacro{' + example_id + 'DefinerHistSliceSize}{' + \
                        str(definer_history_slice_len) + '}\n'
        fw = open(runex.NUMBERS_TEX_PATH, 'w')
        fw.write(''.join(number_lines))
        fw.close()
