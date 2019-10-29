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

def countSliceLines(log_file, repo):
    print (repo)
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    commits = []
    for i in range(len(lines)):
        if lines[i].startswith('TEST: ') or \
           lines[i].startswith('COMP: ') or \
           lines[i].startswith('HUNK: '):
            sha = lines[i].split()[1]
            commits.append(sha)
    total_num_of_insertions = 0
    total_num_of_deletions = 0
    os.chdir(repo)
    for sha in commits:
        print (sha)
        p = sub.Popen('git --no-pager log --stat ' + sha + ' -1', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
        p.wait()
        commit_messages = p.stdout.readlines()
        #for msg in commit_messages:
        #    print (msg.decode("utf-8"))
        last_line = commit_messages[-1].decode("utf-8")[:-1]
        if 'insertion' in last_line:
            num_of_insertions = int(last_line.split('insertion')[0].split(',')[1].strip())
            print (num_of_insertions)
            total_num_of_insertions += num_of_insertions
        else:
            num_of_insertions = 0
        if 'deletion' in last_line:
            num_of_deletions = int(last_line.split('deletion')[0].split(',')[-1].strip())
            print (num_of_deletions)
            total_num_of_deletions += num_of_deletions
        else:
            num_of_deletions = 0
    total_num_of_edits = total_num_of_insertions + total_num_of_deletions
    return total_num_of_edits

if __name__ == '__main__':
    for example in runex.examples:
        example_id = example.replace('-', '')
        cslicer_orig_log = runex.CSLICER_ORIG_OUTPUT_DIR + '/' + example + '.orig.log'
        cslicer_split_log = runex.CSLICER_SPLIT_OUTPUT_DIR + '/' + example + '.split.log'
        repo = runex.REPOS_BACKUP_DIR + '/' + example + '-repo'
        orig_num_of_edits = countSliceLines(cslicer_orig_log, repo)
        split_num_of_edits = countSliceLines(cslicer_split_log, repo)
        edits_reduction = float(orig_num_of_edits - split_num_of_edits) / orig_num_of_edits * 100
        fr = open(runex.NUMBERS_TEX_PATH, 'r')
        number_lines = fr.readlines()
        fr.close()
        number_lines += '\\DefMacro{' + example_id + 'OrigNumOfEdits}{' + \
                        str(orig_num_of_edits) + '}\n'
        number_lines += '\\DefMacro{' + example_id + 'SplitNumOfEdits}{' + \
                        str(split_num_of_edits) + '}\n'
        number_lines += '\\DefMacro{' + example_id + 'EditsReduction}{' + \
                        '{0:.2f}'.format(edits_reduction) + '}\n'
        fw = open(runex.NUMBERS_TEX_PATH, 'w')
        fw.write(''.join(number_lines))
        fw.close()
