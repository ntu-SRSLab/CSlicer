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

def extract_definer_progress(example, output_dir):
    for f in os.listdir(output_dir):
        if f == example + '.log':
            fr = open(output_dir + '/' + f, 'r')
            lines = fr.readlines()
            fr.close()

            stats = []
            for i in range(len(lines)):
                if lines[i].startswith('[DEBUG] |H*| ='):
                    stats_line = [int(s) for s in lines[i].split() if
                                  s.isdigit()]
                    stats.append((stats_line[0], stats_line[2]))
            return stats
    
if __name__ == '__main__':
    for example in runex.examples:
        example_id = example.replace('-', '')
        
        definer_progress_csd = extract_definer_progress(
            example, runex.CSLICER_SPLIT_DEFINER_OUTPUT_DIR)
        with open(runex.DEFINER_PROGRESS_DIR + '/' + example + '-csd.dat', 'w') as fw:
            fw.write('h,t\n')
            fw.writelines([','.join(map(str, progress)) + '\n' for progress
                           in definer_progress_csd])

        definer_progress_sd = extract_definer_progress(
            example, runex.SPLIT_DEFINER_OUTPUT_DIR)
        with open(runex.DEFINER_PROGRESS_DIR + '/' + example + '-sd.dat', 'w') as fw:
            fw.write('h,t\n')
            fw.writelines([','.join(map(str, progress)) + '\n' for progress
                           in definer_progress_sd])

