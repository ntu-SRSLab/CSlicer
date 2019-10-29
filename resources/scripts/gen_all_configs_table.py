#!/usr/bin/python3

import os
import os.path
import sys
import csv
import collections
import argparse
import subprocess as sub
import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import seaborn as sns
from goto import with_goto
from anytree import Node, RenderTree
import run_examples as runex

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-numbers.tex'
TREE_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/treeconfigs-numbers.tex' # ISSTA 19
TABLE0_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table0.tex'
TABLE1_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table1.tex'
TABLE2_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table2.tex'
TABLE3_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table3.tex'
TABLE4_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table4.tex'
TABLE5_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table5.tex'
# ISSTA 19
TABLE6_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table6.tex'
TABLE7_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table7.tex'
TABLE8_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table8.tex'
TABLE9_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table9.tex'
TABLE10_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table10.tex'
TABLE11_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table11.tex'
TABLE12_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-table12.tex'
# for true minimal exp
NUMBERSX_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/split-definer-numbers.tex'
SPLIT_DEFINER_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/split-definer-one-minimal-table.tex'
SPLIT_DEFINER_WITH_MEMORY_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/split-definer-true-minimal-table.tex'
# for asej exp 2
EFFECTIVENESS_EXP_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/effectiveness-exp-numbers.tex'
EFFECTIVENESS_EXP_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/effectiveness-exp-table.tex'
# for asej exp 3
PARTITION_EXP_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/partition-exp-numbers.tex'
PARTITION_EXP_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ASEJ2018_SemanticSlicing/tables/partition-exp-table.tex'
# ISSTA 19
SMALL_HISTORY_INFO_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/small_history-table.tex'
END_TO_END_TIME_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/end-to-end-time-numbers.tex'
END_TO_END_TIME_TABLE_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/end-to-end-time-table.tex'

CSLICER_ORIG_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/orig-configs'
REPOS_DIR = SCRIPT_DIR + '/../../resources/file-level/_repos'
OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output'
OUTPUT_NOOP_DIR = SCRIPT_DIR + '/../../resources/file-level/output-noop'
SHARE_PREFIX_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output-share-prefix'
SHARE_SUFFIX_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output-share-suffix'
ORIG_HISTORY_DIR = SCRIPT_DIR + '/../../resources/file-level/orig-history'

# FSE 19
SLICE_COMPARISON_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/fse19-slice-comparison-numbers.tex'
SLICE_COMPARISON_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/fse19-slice-comparison-table.tex'
OPTIMIZATION_COMPARISON_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/fse19-optimization-comparison-numbers.tex'
OPTIMIZATION_COMPARISON_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/fse19-optimization-comparison-table.tex'
# ASE 19
PRE_STUDY_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/ase19-pre-study-numbers.tex'
PRE_STUDY_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/ase19-pre-study-table.tex'
PLOTS_DIR = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/figures'
THEORY_CONFIG_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/ase19-optimal-configs-numbers.tex'
THEORY_CONFIG_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/ase19-optimal-configs-table.tex'


THEORY_CONFIGS = ['split-definer', 'split-cslicer-definer', 'cslicer-split-definer', \
                  'definer-split-definer', 'cslicer-definer-split-definer', \
                  'definer-split-cslicer-definer', 'cslicer-definer-split-cslicer-definer']

TREE_CONFIGS = ['definer-definer', 'split-definer-definer', 'cslicer-definer-definer', \
                'definer-split-definer', 'definer-cslicer-definer', \
                'split-cslicer-definer-definer', 'split-definer-cslicer-definer', \
                'cslicer-split-definer-definer', \
                'cslicer-definer-split-definer', 'definer-split-cslicer-definer', \
                'definer-cslicer-split-definer', 'cslicer-definer-split-cslicer-definer', \
                'cslicer-split-definer-cslicer-definer']

OLD_CONFIGS = ['cslicer', 'definer', 'split-cslicer', 'split-definer', \
               'cslicer-split-cslicer', 'cslicer-split-definer', 'definer-split-cslicer', \
               'cslicer-definer', 'split-cslicer-definer']

# ASE 19
SLICE_COMPARISON_CONFIGS = ['cslicer', 'definer', 'split-cslicer', \
                            'cslicer-definer', \
                            'definer-split-cslicer', \
                            'cslicer-definer-split-cslicer', 'split-definer']

# configs = ['split-definer', 'split-definer-with-memory']

configs = TREE_CONFIGS

def extractSliceInfoFromCSlicerLog(lines, standalone):
    num_of_commits_map_back, num_of_changed_lines, run_time, change_commit_ratio = \
                                                                        (None, None, None, None)
    map_back_commits_list = []
    for i in range(len(lines)):
        if 'change.commit.ratio :' in lines[i]:
            change_commit_ratio = lines[i].strip().split()[-1]
        if lines[i].startswith('Total Changed Lines: '):
            num_of_changed_lines = int(lines[i].strip().split()[-1])
        if lines[i].startswith('Total Run Time: '):
            run_time = float(lines[i].strip().split()[-1])
        if lines[i].startswith('TEST: ') or \
           lines[i].startswith('COMP: ') or \
           lines[i].startswith('HUNK: '):
            if standalone:
                map_back_sha = lines[i].split()[1]
            else:
                try:
                    map_back_sha = lines[i].split('[')[1].split()[0]
                except:
                    print ('error parsing commit msg')
                    return None, None, None
            if not map_back_sha in map_back_commits_list:
                map_back_commits_list.append(map_back_sha)
    map_back_commits_list.reverse()
    num_of_commits_map_back = len(map_back_commits_list)
    if num_of_commits_map_back == 0 or num_of_changed_lines == 0 or run_time == 0:
        return None, None, None
    return num_of_commits_map_back, num_of_changed_lines, run_time, change_commit_ratio

def extractSliceInfoFromDefinerLog(lines, standalone):
    num_of_commits_map_back, num_of_changed_lines, run_time = (None, None, None)
    map_back_commits_list = []
    for i in range(len(lines)):
        if lines[i].startswith('Total Changed Lines: '):
            num_of_changed_lines = int(lines[i].strip().split()[-1])
        if lines[i].startswith('Total Run Time: '):
            run_time = float(lines[i].strip().split()[-1])
        if lines[i].startswith('[OUTPUT] H*: '):
            if standalone:
                map_back_sha = lines[i].split()[2]
            else:
                try:
                    map_back_sha = lines[i].split('[')[2].split()[0]
                except:
                    print ('error parsing commit msg')
                    return None, None, None
            if not map_back_sha in map_back_commits_list:
                map_back_commits_list.append(map_back_sha)
    num_of_commits_map_back = len(map_back_commits_list)
    if num_of_commits_map_back == 0 or num_of_changed_lines == 0 or run_time == 0:
        return None, None, None
    return num_of_commits_map_back, num_of_changed_lines, run_time

# ASE 19
def extractSliceInfoFromLogFile_ASE(log_file, tool, level):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    if tool == 'cslicer':
        if level == 'commit':
            num_of_commits_map_back, num_of_changed_lines, x, change_commit_ratio = \
                                        extractSliceInfoFromCSlicerLog(lines, standalone=True)
        elif level == 'file':
            num_of_commits_map_back, num_of_changed_lines, x, change_commit_ratio = \
                                        extractSliceInfoFromCSlicerLog(lines, standalone=False)
    elif tool == 'definer':
        if level == 'commit':
            num_of_commits_map_back, num_of_changed_lines, x = \
                                        extractSliceInfoFromDefinerLog(lines, standalone=True)
        elif level == 'file':
            num_of_commits_map_back, num_of_changed_lines, x = \
                                        extractSliceInfoFromDefinerLog(lines, standalone=False)
    return num_of_commits_map_back, num_of_changed_lines

def extractSliceInfo(example, config):
    run_time = extractPhaseTime('Total', example, config)
    #print (run_time)
    for f in os.listdir(OUTPUT_DIR + '/' + config):
        if f.startswith(example) and f.endswith('.log'):
            fr = open(OUTPUT_DIR + '/' + config + '/' + f, 'r')
            lines = fr.readlines()
            fr.close()
            if config.endswith('cslicer'):
                if not 'split' in config:
                    num_of_commits_map_back, num_of_changed_lines, x, change_commit_ratio = \
                                        extractSliceInfoFromCSlicerLog(lines, standalone=True)
                else:
                    num_of_commits_map_back, num_of_changed_lines, x, change_commit_ratio = \
                                        extractSliceInfoFromCSlicerLog(lines, standalone=False)
            elif config.endswith('definer') or config.endswith('definer-with-memory'):
                if not 'split'in config:
                    num_of_commits_map_back, num_of_changed_lines, x = \
                                        extractSliceInfoFromDefinerLog(lines, standalone=True)
                else:
                    num_of_commits_map_back, num_of_changed_lines, x = \
                                        extractSliceInfoFromDefinerLog(lines, standalone=False)
            elif config == 'definer-learning' or config == 'definer-basic': # asej exp2
                    num_of_commits_map_back, num_of_changed_lines, run_time, \
                        = extractSliceInfoFromDefinerLog(lines, standalone=True)
            elif config == 'definer-neg' or config == 'definer-nopos' or \
                 config == 'definer-low3': # asej exp3
                    num_of_commits_map_back, num_of_changed_lines, run_time, \
                        = extractSliceInfoFromDefinerLog(lines, standalone=True)
            return num_of_commits_map_back, num_of_changed_lines, run_time

def extractPhaseTime(phase, example, config, output_dir=OUTPUT_DIR):
    log_file = output_dir + '/' + config + '/' + example + '.log'
    if not os.path.isfile(log_file):
        return 'TO'
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('[' + phase + ' Exec Time]:'):
            phase_time = lines[i].strip().split()[-1]
            if phase_time == 'NOT RUN':
                return '0'
            elif phase_time == 'TIME OUT':
                return 'TO'
            return phase_time

def isCSlicerLog(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(lines):
        if lines[i].startswith('DROP: '):
            return True
    return False

def isDefinerLog(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(lines):
        if lines[i].startswith('[OUTPUT] |H*| = '):
            return True
    return False

def splitSlice(example, log_file, downloads_dir=runex.DOWNLOADS_DIR):
    project = example.split('-')[0]
    repo_name = 'commons-'  + project.lower()
    repo_path = downloads_dir + '/' + repo_name
    if os.path.isdir(repo_path):
        sub.run('rm -rf ' + repo_path, shell=True)
    sub.run('cp -r ' + repo_path + '-fake ' + repo_path, shell=True)
    if isCSlicerLog(log_file):
        # extract info from cslicer orig config file
        start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            runex.extractInfoFromCSlicerConfigs(example)
    elif isDefinerLog(log_file):
        # extract info from config file
        start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                            runex.extractInfoFromDefinerConfigs(example)
        definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(log_file)
        end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                                'after-definer')
        splitCommitsByFile(example, repo_path, start, end, 'after-definer-split')
    # split commits by file, create separate branches
    # measure the overhead of splitting
    os.chdir(SCRIPT_DIR)
    print ('===> Splitting ...')
    sub.run('python3 split_commits.py --repo ' + repo_path + \
            ' --start ' + start + \
            ' --end ' + end + \
            ' --branch ' + branch, shell=True, \
            stdout=open(os.devnull, 'w'), stderr=sub.STDOUT)
    os.chdir(repo_path)
    sub.run('git checkout ' + branch, shell=True)
    p = sub.Popen('git --no-pager log --oneline -1', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    end = p.stdout.readlines()[0].decode("utf-8").split()[0]
    p = sub.Popen('git --no-pager log --oneline ' + start + '..' + end, shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    splitted_slice = p.stdout.readlines()
    splitted_slice_shas = []
    splitted_slice_msgs = []
    for i in range(len(splitted_slice)):
        sha = p.stdout.readlines()[0].decode("utf-8").split()[0]
        msg = ''.join(p.stdout.readlines()[0].decode("utf-8").split()[1:])
    splitted_slice_shas.append(sha)
    splitted_slice_msgs.append(msg)
    return splitted_slice_shas, splitted_slice_msgs

def extractSlice(log_file):
    if isCSlicerLog(log_file):
        return extractHistorySliceFromCSlicerLog(log_file)
    elif isDefinerLog(log_file):
        return extractHistorySliceFromDefinerLog(log_file)

def extractMsgAndFileNameSlice(history_slice):
    result_slice = [' '.join(commits.split()[2:]) for commit in history_slice]
    return result_slice

def compareSlice(is_split, log_file1, log_file2):
    slice_1 = extractSlice(log_file1)
    slice_2 = extractSlice(log_file2)
    if is_split:
        slice_1 = extractMsgAndFileNameSlice(slice_1)
        slice_2 = extractMsgAndFileNameSlice(slice_2)
    # TODO
    pass

# ISSTA 19
def genSplitLogFile(example, config, source_log, \
                    downloads_dir=runex.DOWNLOADS_DIR, output_dir=OUTPUT_DIR):
    start, end, _, _, repo_path, _, _ = runex.extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        sub.run('rm -rf ' + repo_path, shell=True)
    sub.run('cp -r ' + repo_path + '-fake ' + repo_path, shell=True)

    if runex.isCSlicerLog(source_log):
        shas, msgs = runex.extractHistorySliceFromCSlicerLog(source_log)
    elif runex.isDefinerLog(source_log):
        shas, msgs = runex.extractHistorySliceFromDefinerLog(source_log)
    
    end = runex.applyHistorySlice(repo_path, start, shas, msgs, 'beforesplit')
    runex.splitCommitsByFile(example, repo_path, start, end, 'aftersplit')

    runex.genSplitLogFile(example, config, start, repo_path, branch='aftersplit')
    
    return output_dir + '/' + config + '/' + example + '.log.split'

# ISSTA 19
CONFIGS_SHORTNAMES_MAP = collections.OrderedDict({'cdsd': 'cslicer-definer-split-definer',
                                                  'scdd': 'split-cslicer-definer-definer',
                                                  'dscd': 'definer-split-cslicer-definer',
                                                  'csdd': 'cslicer-split-definer-definer',
                                                  'dcsd': 'definer-cslicer-split-definer',
                                                  'sdcd': 'split-definer-cslicer-definer',
                                                  'dsd': 'definer-split-definer',
                                                  'cdd': 'cslicer-definer-definer',
                                                  'dcd': 'definer-cslicer-definer',
                                                  'sdd': 'split-definer-definer',
                                                  'dd': 'definer-definer',
                                            'cdscd': 'cslicer-definer-split-cslicer-definer',
                                            'csdcd': 'cslicer-split-definer-cslicer-definer'})

# ISSTA 19
def stateMatch(example, current_config, current_log_phase, cached_config, cached_log_phase, \
               output_dir=OUTPUT_DIR, orig_history_dir=ORIG_HISTORY_DIR, \
               short_names_map=CONFIGS_SHORTNAMES_MAP):
    # try:
    #     print ('CURRENT : ' + example + ' ' + current_config + ' ' + current_log_phase)
    #     print ('CACHED : ' + example + ' ' + cached_config + ' ' + cached_log_phase)
    # except TypeError:
    #     print ('CURRENT : ' + example + ' ' + current_config + ' None')
    #     print ('CACHED : ' + example + ' ' + cached_config + ' None')
    #     pass
    if current_log_phase == None:
        current_log_file = orig_history_dir + '/' + example + '.hist'
        cached_log_file = output_dir + '/' + short_names_map[cached_config] + '/' + example + \
                          '.log.' + cached_log_phase
        fr = open(current_log_file, 'r')
        orig_hist_lines = fr.readlines()
        fr.close()
        current_slice = [' '.join(cmt.strip().split()[1:]).replace('\"', '') \
                         for cmt in orig_hist_lines]
        if runex.isCommitLevel(cached_log_file):
            cached_slice = runex.extractSliceFromCommitLevelLog(cached_log_file)
        elif runex.isFileLevel(cached_log_file):
            cached_slice = runex.extractSliceFromFileLevelLog(cached_log_file)
        if current_slice == cached_slice:
            return True
        else:
            return False
    elif cached_log_phase == None:
        current_log_file = output_dir + '/' + short_names_map[current_config] + '/' + \
                           example + '.log.' + current_log_phase
        cached_log_file = orig_history_dir + '/' + example + '.hist'
        fr = open(cached_log_file, 'r')
        orig_hist_lines = fr.readlines()
        fr.close()
        cached_slice = [' '.join(cmt.strip().split()[1:]).replace('\"', '') \
                         for cmt in orig_hist_lines]
        if runex.isCommitLevel(current_log_file):
            current_slice = runex.extractSliceFromCommitLevelLog(current_log_file)
        elif runex.isFileLevel(current_log_file):
            current_slice = runex.extractSliceFromFileLevelLog(current_log_file)
        if current_slice == cached_slice:
            return True
        else:
            return False
    else:
        current_log_file = output_dir + '/' + short_names_map[current_config] + '/' + \
                           example + '.log.' + current_log_phase
        cached_log_file = output_dir + '/' + short_names_map[cached_config] + '/' + example + \
                          '.log.' + cached_log_phase
        if runex.isCommitLevel(current_log_file) and runex.isFileLevel(cached_log_file):
            return False
        if runex.isFileLevel(current_log_file) and runex.isCommitLevel(cached_log_file):
            return False
        if runex.isCommitLevel(current_log_file) and runex.isCommitLevel(cached_log_file):
            current_slice = runex.extractSliceFromCommitLevelLog(current_log_file)
            cached_slice = runex.extractSliceFromCommitLevelLog(cached_log_file)
            if current_slice == cached_slice:
                return True
            else:
                return False
        if runex.isFileLevel(current_log_file) and runex.isFileLevel(cached_log_file):
            current_slice = runex.extractSliceFromFileLevelLog(current_log_file)
            cached_slice = runex.extractSliceFromFileLevelLog(cached_log_file)
            if current_slice == cached_slice:
                return True
            else:
                return False
    return False

def getWhichPhasesSavedByPrefixSharing(examples=runex.examples, \
                                       shortnames_map=CONFIGS_SHORTNAMES_MAP):
    # CDSD->SCDD->DSCD->CSDD->DCSD->SDCD->DSD->CDD->DCD->SDD->DD->CDSCD->CSDCD
    seq_dict = collections.OrderedDict({})
    saved_phases_dict = collections.OrderedDict({})
    for example in examples:
        print (example)
        seq_dict[example] = []
        saved_phases_dict[example] = collections.OrderedDict({})
        # CDSD cannot be saved
        seq_dict[example].append('cdsd')
        # SCDD cannot be saved
        seq_dict[example].append('scdd')
        # DSCD cannot be saved
        seq_dict[example].append('dscd')
        # CSDD
        seq_dict[example].append('(c)sdd')
        saved_phases_dict[example][shortnames_map['csdd']] = ['CSlicer']
        # DCSD
        seq_dict[example].append('(d)csd')
        saved_phases_dict[example][shortnames_map['dcsd']] = ['Definer']
        # SDCD
        seq_dict[example].append('(s)dcd')
        saved_phases_dict[example][shortnames_map['sdcd']] = ['Split']
        # DSD
        seq_dict[example].append('(ds)d')
        saved_phases_dict[example][shortnames_map['dsd']] = ['Definer', 'Split']
        # CDD
        seq_dict[example].append('(cd)d')
        saved_phases_dict[example][shortnames_map['cdd']] = ['CSlicer', 'Definer']
        # DCD
        seq_dict[example].append('(dc)d')
        saved_phases_dict[example][shortnames_map['dcd']] = ['Definer', 'CSlicer']
        # SDD
        seq_dict[example].append('(sd)d')
        saved_phases_dict[example][shortnames_map['sdd']] = ['Split', 'Definer']
        # DD
        seq_dict[example].append('(d)d')
        saved_phases_dict[example][shortnames_map['dd']] = ['Definer']
        # CDSCD
        seq_dict[example].append('(cds)cd')
        saved_phases_dict[example][shortnames_map['cdscd']] = ['CSlicer', 'Definer', 'Split']
        # CSDCD
        seq_dict[example].append('(csd)cd')
        saved_phases_dict[example][shortnames_map['csdcd']] = ['CSlicer', 'Split', 'Definer']
        print ('->'.join(seq_dict[example]))
    return saved_phases_dict

# ISSTA 19
def getWhichPhasesSavedByStateMatching(examples=runex.examples, \
                                       shortnames_map=CONFIGS_SHORTNAMES_MAP):
    # CDSD->SCDD->DSCD->CSDD->DCSD->SDCD->DSD->CDD->DCD->SDD->DD->CDSCD->CSDCD
    seq_dict = collections.OrderedDict({})
    saved_phases_dict = collections.OrderedDict({})
    for example in examples:
        print (example)
        seq_dict[example] = []
        saved_phases_dict[example] = collections.OrderedDict({})
        # CDSD cannot be saved
        seq_dict[example].append('cdsd')
        # SCDD
        if stateMatch(example, current_config='scdd', current_log_phase='phase2', \
                      cached_config='cdsd', cached_log_phase='split'):
            seq_dict[example].append('scd(d)')
            saved_phases_dict[example][shortnames_map['scdd']] = ['Definer2']
        else:
            seq_dict[example].append('scdd')
        # DSCD
        if stateMatch(example, 'dscd', 'phase2', 'cdsd', 'split') or \
           stateMatch(example, 'dscd', 'phase2', 'scdd', 'phase2'):
            seq_dict[example].append('dsc(d)')
            saved_phases_dict[example][shortnames_map['dscd']] = ['Definer2']
        else:
            seq_dict[example].append('dscd')
        # CSDD
        if stateMatch(example, 'csdd', 'split', 'scdd', 'phase1'):
            seq_dict[example].append('cs(dd)')
            saved_phases_dict[example][shortnames_map['csdd']] = ['Definer', 'Definer2']
        elif stateMatch(example, 'csdd', 'phase2', 'cdsd', 'split') or \
             stateMatch(example, 'csdd', 'phase2', 'scdd', 'phase2') or \
             stateMatch(example, 'csdd', 'phase2', 'dscd', 'phase2'):
            seq_dict[example].append('csd(d)')
            saved_phases_dict[example][shortnames_map['csdd']] = ['Definer2']
        else:
            seq_dict[example].append('csdd')
        # DCSD
        if stateMatch(example, 'dcsd', 'phase2', 'cdsd', 'phase2'):
            seq_dict[example].append('dc(sd)')
            saved_phases_dict[example][shortnames_map['dcsd']] = ['Split', 'Definer2']
        elif stateMatch(example, 'dcsd', 'split', 'cdsd', 'split') or \
             stateMatch(example, 'dcsd', 'split', 'scdd', 'phase2') or \
             stateMatch(example, 'dcsd', 'split', 'dscd', 'phase2') or \
             stateMatch(example, 'dcsd', 'split', 'csdd', 'phase2'):
            seq_dict[example].append('dcs(d)')
            saved_phases_dict[example][shortnames_map['dcsd']] = ['Definer2']
        else:
            seq_dict[example].append('dcsd')
        # SDCD
        if stateMatch(example, 'sdcd', 'phase1', 'dscd', 'split'):
            seq_dict[example].append('sd(cd)')
            saved_phases_dict[example][shortnames_map['sdcd']] = ['CSlicer', 'Definer2']
        elif stateMatch(example, 'sdcd', 'phase2', 'cdsd', 'split') or \
             stateMatch(example, 'sdcd', 'phase2', 'scdd', 'phase2') or \
             stateMatch(example, 'sdcd', 'phase2', 'dscd', 'phase2') or \
             stateMatch(example, 'sdcd', 'phase2', 'csdd', 'phase2') or \
             stateMatch(example, 'sdcd', 'phase2', 'dcsd', 'split'):
            seq_dict[example].append('sdc(d)')
            saved_phases_dict[example][shortnames_map['sdcd']] = ['Definer2']
        else:
            seq_dict[example].append('sdcd')
        # DSD
        if stateMatch(example, 'dsd', None, 'cdsd', 'phase1'):
            seq_dict[example].append('(dsd)')
            saved_phases_dict[example][shortnames_map['dsd']] = ['Definer', 'Split', \
                                                                  'Definer2']
        elif stateMatch(example, 'dsd', 'phase1', 'cdsd', 'phase2') or \
             stateMatch(example, 'dsd', 'phase1', 'dcsd', 'phase2'):
            seq_dict[example].append('d(sd)')
            saved_phases_dict[example][shortnames_map['dsd']] = ['Split', 'Definer2']
        elif stateMatch(example, 'dsd', 'split', 'cdsd', 'split') or \
             stateMatch(example, 'dsd', 'split', 'scdd', 'phase2') or \
             stateMatch(example, 'dsd', 'split', 'dscd', 'phase2') or \
             stateMatch(example, 'dsd', 'split', 'csdd', 'phase2') or \
             stateMatch(example, 'dsd', 'split', 'dcsd', 'split') or \
             stateMatch(example, 'dsd', 'split', 'sdcd', 'phase2'):
            seq_dict[example].append('ds(d)')
            saved_phases_dict[example][shortnames_map['dsd']] = ['Definer2']
        else:
            seq_dict[example].append('dsd')
        # CDD
        if stateMatch(example, 'cdd', None, 'scdd', 'split'): # impossible
            seq_dict[example].append('(cdd)')
            saved_phases_dict[example][shortnames_map['cdd']] = ['CSlicer', 'Definer', \
                                                                  'Definer2']
        elif stateMatch(example, 'cdd', 'phase1', 'scdd', 'phase1') or \
             stateMatch(example, 'cdd', 'phase1', 'csdd', 'split'): # impossible
            seq_dict[example].append('c(dd)')
            saved_phases_dict[example][shortnames_map['cdd']] = ['Definer', 'Definer2']
        elif stateMatch(example, 'cdd', 'phase2', 'cdsd', 'split') or \
             stateMatch(example, 'cdd', 'phase2', 'scdd', 'phase2') or \
             stateMatch(example, 'cdd', 'phase2', 'dscd', 'phase2') or \
             stateMatch(example, 'cdd', 'phase2', 'csdd', 'phase2') or \
             stateMatch(example, 'cdd', 'phase2', 'dcsd', 'split') or \
             stateMatch(example, 'cdd', 'phase2', 'sdcd', 'phase2') or \
             stateMatch(example, 'cdd', 'phase2', 'dsd', 'split'):
            seq_dict[example].append('cd(d)')
            saved_phases_dict[example][shortnames_map['cdd']] = ['Definer2']
        else:
            seq_dict[example].append('cdd')
        # DCD
        if stateMatch(example, 'dcd', None, 'sdcd', 'split'): # impossible
            seq_dict[example].append('(dcd)')
            saved_phases_dict[example][shortnames_map['dcd']] = ['Definer', 'CSlicer', \
                                                                  'Definer2']
        elif stateMatch(example, 'dcd', 'phase1', 'dscd', 'split') or \
             stateMatch(example, 'dcd', 'phase1', 'sdcd', 'phase1'): # impossible
            seq_dict[example].append('d(cd)')
            saved_phases_dict[example][shortnames_map['dcd']] = ['CSlicer', 'Definer2']
        elif stateMatch(example, 'dcd', 'phase2', 'cdsd', 'split') or \
             stateMatch(example, 'dcd', 'phase2', 'scdd', 'phase2') or \
             stateMatch(example, 'dcd', 'phase2', 'dscd', 'phase2') or \
             stateMatch(example, 'dcd', 'phase2', 'csdd', 'phase2') or \
             stateMatch(example, 'dcd', 'phase2', 'dcsd', 'split') or \
             stateMatch(example, 'dcd', 'phase2', 'sdcd', 'phase2') or \
             stateMatch(example, 'dcd', 'phase2', 'dsd', 'split') or \
             stateMatch(example, 'dcd', 'phase2', 'cdd', 'phase2'):
            seq_dict[example].append('dc(d)')
            saved_phases_dict[example][shortnames_map['dcd']] = ['Definer2']
        else:
            seq_dict[example].append('dcd')
        # SDD
        if stateMatch(example, 'sdd', None, 'csdd', 'phase1'):
            seq_dict[example].append('(sdd)')
            saved_phases_dict[example][shortnames_map['sdd']] = ['Split', 'Definer', \
                                                                  'Definer2']
        elif stateMatch(example, 'sdd', 'split', 'scdd', 'phase1') or \
             stateMatch(example, 'sdd', 'split', 'csdd', 'split') or \
             stateMatch(example, 'sdd', 'split', 'cdd', 'phase1'):
            seq_dict[example].append('s(dd)')
            saved_phases_dict[example][shortnames_map['sdd']] = ['Definer', 'Definer2']
        elif stateMatch(example, 'sdd', 'phase1', 'cdsd', 'split') or \
             stateMatch(example, 'sdd', 'phase1', 'scdd', 'phase2') or \
             stateMatch(example, 'sdd', 'phase1', 'dscd', 'phase2') or \
             stateMatch(example, 'sdd', 'phase1', 'csdd', 'phase2') or \
             stateMatch(example, 'sdd', 'phase1', 'dcsd', 'split') or \
             stateMatch(example, 'sdd', 'phase1', 'sdcd', 'phase2') or \
             stateMatch(example, 'sdd', 'phase1', 'dsd', 'split') or \
             stateMatch(example, 'sdd', 'phase1', 'cdd', 'phase2') or \
             stateMatch(example, 'sdd', 'phase1', 'dcd', 'phase2'):
            seq_dict[example].append('sd(d)')
            saved_phases_dict[example][shortnames_map['sdd']] = ['Definer2']
        else:
            seq_dict[example].append('sdd')
        # DD
        if stateMatch(example, 'dd', None, 'scdd', 'phase1') or \
           stateMatch(example, 'dd', None, 'csdd', 'split') or \
           stateMatch(example, 'dd', None, 'cdd', 'phase1') or \
           stateMatch(example, 'dd', None, 'sdd', 'split'):
            seq_dict[example].append('(dd)')
            saved_phases_dict[example][shortnames_map['dd']] = ['Definer', 'Definer2']
        elif stateMatch(example, 'dd', 'phase1', 'cdsd', 'split') or \
             stateMatch(example, 'dd', 'phase1', 'scdd', 'phase2') or \
             stateMatch(example, 'dd', 'phase1', 'dscd', 'phase2') or \
             stateMatch(example, 'dd', 'phase1', 'csdd', 'phase2') or \
             stateMatch(example, 'dd', 'phase1', 'dcsd', 'split') or \
             stateMatch(example, 'dd', 'phase1', 'sdcd', 'phase2') or \
             stateMatch(example, 'dd', 'phase1', 'dsd', 'split') or \
             stateMatch(example, 'dd', 'phase1', 'cdd', 'phase2') or \
             stateMatch(example, 'dd', 'phase1', 'dcd', 'phase2') or \
             stateMatch(example, 'dd', 'phase1', 'sdd', 'phase1'):
            seq_dict[example].append('d(d)')
            saved_phases_dict[example][shortnames_map['dd']] = ['Definer2']
        else:
            seq_dict[example].append('dd')
        # CDSCD
        if stateMatch(example, 'cdscd', 'phase1', 'dscd', None):
            seq_dict[example].append('c(dscd)')
            saved_phases_dict[example][shortnames_map['cdscd']] = ['Definer', 'Split', \
                                                                    'CSlicer2', 'Definer2']
        elif stateMatch(example, 'cdscd', 'phase2', 'dscd', 'phase1'):
            seq_dict[example].append('cd(scd)')
            saved_phases_dict[example][shortnames_map['cdscd']] = ['Split', 'CSlicer2', \
                                                                    'Definer2']
        elif stateMatch(example, 'cdscd', 'split', 'dscd', 'split') or \
             stateMatch(example, 'cdscd', 'split', 'sdcd', 'phase1') or \
             stateMatch(example, 'cdscd', 'split', 'dcd', 'phase1'):
            seq_dict[example].append('cds(cd)')
            saved_phases_dict[example][shortnames_map['cdscd']] = ['CSlicer2', 'Definer2']
        elif stateMatch(example, 'cdscd', 'phase3', 'cdsd', 'split') or \
             stateMatch(example, 'cdscd', 'phase3', 'scdd', 'phase2') or \
             stateMatch(example, 'cdscd', 'phase3', 'dscd', 'phase2') or \
             stateMatch(example, 'cdscd', 'phase3', 'csdd', 'phase2') or \
             stateMatch(example, 'cdscd', 'phase3', 'dcsd', 'split') or \
             stateMatch(example, 'cdscd', 'phase3', 'sdcd', 'phase2') or \
             stateMatch(example, 'cdscd', 'phase3', 'dsd', 'split') or \
             stateMatch(example, 'cdscd', 'phase3', 'cdd', 'phase2') or \
             stateMatch(example, 'cdscd', 'phase3', 'dcd', 'phase2') or \
             stateMatch(example, 'cdscd', 'phase3', 'sdd', 'phase1') or \
             stateMatch(example, 'cdscd', 'phase3', 'dd', 'phase1'):
            seq_dict[example].append('cdsc(d)')
            saved_phases_dict[example][shortnames_map['cdscd']] = ['Definer2']
        else:
            seq_dict[example].append('cdscd')
        # CSDCD
        if stateMatch(example, 'csdcd', 'phase1', 'sdcd', None):
            seq_dict[example].append('c(sdcd)')
            saved_phases_dict[example][shortnames_map['csdcd']] = ['Split', 'Definer', \
                                                                    'CSlicer2', 'Definer2']
        elif stateMatch(example, 'csdcd', 'split', 'sdcd', 'split') or \
             stateMatch(example, 'csdcd', 'split', 'dcd', None):
            seq_dict[example].append('cs(dcd)')
            saved_phases_dict[example][shortnames_map['csdcd']] = ['Definer', 'CSlicer2', \
                                                                    'Definer2']
        elif stateMatch(example, 'csdcd', 'phase2', 'dscd', 'split') or \
             stateMatch(example, 'csdcd', 'phase2', 'sdcd', 'phase1') or \
             stateMatch(example, 'csdcd', 'phase2', 'dcd', 'phase1') or \
             stateMatch(example, 'csdcd', 'phase2', 'cdscd', 'split'):
            seq_dict[example].append('csd(cd)')
            saved_phases_dict[example][shortnames_map['csdcd']] = ['CSlicer2', 'Definer2']
        elif stateMatch(example, 'csdcd', 'phase3', 'cdsd', 'split') or \
             stateMatch(example, 'csdcd', 'phase3', 'scdd', 'phase2') or \
             stateMatch(example, 'csdcd', 'phase3', 'dscd', 'phase2') or \
             stateMatch(example, 'csdcd', 'phase3', 'csdd', 'phase2') or \
             stateMatch(example, 'csdcd', 'phase3', 'dcsd', 'split') or \
             stateMatch(example, 'csdcd', 'phase3', 'sdcd', 'phase2') or \
             stateMatch(example, 'csdcd', 'phase3', 'dsd', 'split') or \
             stateMatch(example, 'csdcd', 'phase3', 'cdd', 'phase2') or \
             stateMatch(example, 'csdcd', 'phase3', 'dcd', 'phase2') or \
             stateMatch(example, 'csdcd', 'phase3', 'sdd', 'phase1') or \
             stateMatch(example, 'csdcd', 'phase3', 'dd', 'phase1') or \
             stateMatch(example, 'csdcd', 'phase3', 'cdscd', 'phase3'):
            seq_dict[example].append('csdc(d)')
            saved_phases_dict[example][shortnames_map['csdcd']] = ['Definer2']
        else:
            seq_dict[example].append('csdcd')
        print ('->'.join(seq_dict[example]))
    return saved_phases_dict

# FSE 2019
def getOrigHistLen(example, orig_history_dir=ORIG_HISTORY_DIR):
    history_file = orig_history_dir + '/' + example + '.hist'
    fr = open(history_file, 'r')
    lines = fr.readlines()
    fr.close()
    return len(lines)

# FSE 2019
# Compare non-optimal configs with optimal configs
@with_goto
def genSliceComparisonNumbers(examples=runex.examples, configs=SLICE_COMPARISON_CONFIGS, \
                              output_dir=OUTPUT_DIR, tree_configs=TREE_CONFIGS, \
                              numbers_tex_file=SLICE_COMPARISON_NUMBERS_TEX_FILE):
    lines = ''
    total_orig_history_len = 0
    total_orig_num_of_changed_lines = 0
    for example in examples:
        orig_history_len, orig_num_of_changed_lines, _, _ = extractOrigHistoryInfo(example)
        lines += '\\DefMacro{' + example + 'OrigHistLen}{' + str(orig_history_len) + '}\n'
        lines += '\\DefMacro{' + example + 'OrigNumOfChangedLines}{' + \
                 str(orig_num_of_changed_lines) + '}\n'
        total_orig_history_len += orig_history_len
        total_orig_num_of_changed_lines += orig_num_of_changed_lines
        for config in configs:
            config_short_name = ''.join([phase[0].upper() for phase in config.split('-')])
            if config == 'split':
                num_of_commits = orig_history_len
                num_of_changed_lines = orig_num_of_changed_lines
                run_time = float(extractPhaseTime('Split', example, \
                                                  'split-cslicer-definer'))
                goto .afterparselog
            elif config == 'cslicer':
                log_file = output_dir + '/cslicer-split-definer/' + example + \
                           '.log.phase1'
                tool = 'cslicer'
                level = 'commit'
                run_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-split-definer'))
            elif config == 'definer':
                log_file = output_dir + '/definer-split-definer/' + example + '.log.phase1'
                tool = 'definer'
                level = 'commit'
                run_time = float(extractPhaseTime('Definer', example, 'definer-split-definer'))
            elif config == 'split-cslicer':
                log_file = output_dir + '/split-cslicer-definer/' + example + \
                           '.log.phase1'
                tool = 'cslicer'
                level = 'file'
                run_time = float(extractPhaseTime('Split', example, \
                                                  'split-cslicer-definer')) \
                           + float(extractPhaseTime('CSlicer', example, \
                                                   'split-cslicer-definer'))
            elif config == 'split-definer':
                log_file = output_dir + '/split-definer/' + example + '.log'
                tool = 'definer'
                level = 'file'
                run_time = float(extractPhaseTime('Total', example, 'split-definer'))
            elif config == 'cslicer-split':
                log_file = output_dir + '/cslicer-split-definer/' + example + \
                           '.log.phase1'
                tool = 'cslicer'
                level = 'commit'
                run_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-split-definer')) \
                           + float(extractPhaseTime('Split', example, \
                                                    'cslicer-split-definer'))
            elif config == 'cslicer-definer':
                log_file = output_dir + '/cslicer-definer-split-definer/' + example + \
                           '.log.phase2'
                tool = 'definer'
                level = 'commit'
                run_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-definer-split-definer')) \
                        + float(extractPhaseTime('Definer', example, \
                                                 'cslicer-definer-split-definer'))
            elif config == 'definer-split':
                log_file = output_dir + '/definer-split-definer/' + example + \
                           '.log.phase1'
                tool = 'definer'
                level = 'commit'
                run_time = float(extractPhaseTime('Definer', example, \
                                                  'definer-split-definer')) \
                        + float(extractPhaseTime('Split', example, \
                                                 'definer-split-definer'))
            elif config == 'cslicer-definer-split':
                log_file = output_dir + '/cslicer-definer-split-definer/' + example + \
                           '.log.phase2'
                tool = 'definer'
                level = 'commit'
                run_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-definer-split-definer')) \
                        + float(extractPhaseTime('Definer', example, \
                                                 'cslicer-definer-split-definer')) \
                        + float(extractPhaseTime('Split', example, \
                                                 'cslicer-definer-split-definer'))
            elif config == 'definer-split-cslicer':
                log_file = output_dir + '/definer-split-cslicer-definer/' + example + \
                           '.log.phase2'
                tool = 'cslicer'
                level = 'file'
                run_time = float(extractPhaseTime('Definer', example, \
                                                  'definer-split-cslicer-definer')) \
                        + float(extractPhaseTime('Split', example, \
                                                 'definer-split-cslicer-definer')) \
                        + float(extractPhaseTime('CSlicer', example, \
                                                 'definer-split-cslicer-definer'))
            elif config == 'cslicer-definer-split-cslicer':
                log_file = output_dir + '/cslicer-definer-split-cslicer-definer/' + example + \
                           '.log.phase3'
                tool = 'cslicer'
                level = 'file'
                run_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-definer-split-cslicer-definer')) \
                        + float(extractPhaseTime('Definer', example, \
                                                 'cslicer-definer-split-cslicer-definer')) \
                        + float(extractPhaseTime('Split', example, \
                                                 'cslicer-definer-split-cslicer-definer')) \
                        + float(extractPhaseTime('CSlicer2', example, \
                                                 'cslicer-definer-split-cslicer-definer'))
            else:
                log_file = output_dir + '/' + config + '/' + example + '.log'
            print (example, config)
            num_of_commits, num_of_changed_lines = \
                                    extractSliceInfoFromLogFile_ASE(log_file, tool, level)
            label .afterparselog
            cmt_reduction_rate = (orig_history_len - num_of_commits) / orig_history_len * 100
            line_reduction_rate = (orig_num_of_changed_lines - num_of_changed_lines) / \
                                  orig_num_of_changed_lines * 100
            lines += '\\DefMacro{' + example + config + 'ConfigShortName}{' + \
                     config_short_name + '}\n'
            lines += '\\DefMacro{' + example + config + 'NumOfCommits}{' + \
                     str(num_of_commits) + '}\n'
            lines += '\\DefMacro{' + example + config + 'NumOfChangedLines}{' + \
                     str(num_of_changed_lines) + '}\n'
            lines += '\\DefMacro{' + example + config + 'Runtime}{' + \
                     str(run_time) + '}\n'
            lines += '\\DefMacro{' + example + config + 'CommitReductionRate}{' + \
                     '{0:.2f}'.format(cmt_reduction_rate) + '}\n'
            lines += '\\DefMacro{' + example + config + 'LineReductionRate}{' + \
                     '{0:.2f}'.format(line_reduction_rate) + '}\n'
    avg_orig_history_len = total_orig_history_len / len (examples)
    avg_orig_num_of_changed_lines = total_orig_num_of_changed_lines / len (examples)
    lines += '\\DefMacro{AvgOrigHistLen}{' + '{0:.2f}'.format(avg_orig_history_len) + '}\n'
    lines += '\\DefMacro{AvgOrigNumOfChangedLines}{' + \
             '{0:.2f}'.format(avg_orig_num_of_changed_lines) + '}\n'
    fw = open(numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()
    # bold all the best numbers
    #boldBestNumbers()

def boldBestNumbers(examples=runex.examples, \
                    numbers_tex_file=SLICE_COMPARISON_NUMBERS_TEX_FILE):
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for example in examples:
        best_reduction_rate = 0
        best_reduction_rate_str = ''
        best_reduction_line_number = 0
        for i in range(len(lines)):
            if example in lines[i] and 'LinesReductionRate}{' in lines[i]:
                reduction_rate_str = lines[i].split('{')[2].split('}')[0]
                reduction_rate = float(reduction_rate_str)
                if reduction_rate > best_reduction_rate:
                    best_reduction_rate = reduction_rate
        for i in range(len(lines)):
            if example in lines[i] and 'LinesReductionRate}{' in lines[i]:
                reduction_rate_str = lines[i].split('{')[2].split('}')[0]
                reduction_rate = float(reduction_rate_str)
                if reduction_rate == best_reduction_rate:
                    best_reduction_rate_str = reduction_rate_str
                    best_reduction_line_number = i
                    lines[best_reduction_line_number] = \
                        lines[best_reduction_line_number].replace(best_reduction_rate_str, \
                                            '\\textbf{' + best_reduction_rate_str + '}')
    fw = open(numbers_tex_file, 'w')
    fw.write(''.join(lines))
    fw.close()

# ASE 2019
def extractNumOfTests(example, cslicer_orig_configs_dir=CSLICER_ORIG_CONFIGS_DIR):
    orig_config_file = cslicer_orig_configs_dir + '/' + example + '.properties'
    fr = open(orig_config_file, 'r')
    lines = fr.readlines()
    fr.close()
    num_of_test_classes = 0
    num_of_test_methods = 0
    for i in range(len(lines)):
        if lines[i].startswith('testScope'):
            num_of_test_classes = len(lines[i].strip().split(','))
            num_of_test_methods = len(lines[i].strip().split(',')) + lines[i].strip().count('+')
    return num_of_test_classes, num_of_test_methods

# ASE 2019
# Data of optimal configs
def genTheorySevenConfigNumbers(examples=runex.examples, configs=THEORY_CONFIGS, \
                                output_dir=OUTPUT_DIR, \
                                numbers_tex_file=THEORY_CONFIG_NUMBERS_TEX_FILE):
    lines = ''
    config_num_of_lines_dict = collections.OrderedDict({})
    for config in configs:
        config_num_of_lines_dict[config] = 0
    config_line_reduction_dict = collections.OrderedDict({})
    for config in configs:
        config_line_reduction_dict[config] = 0
    config_runtime_dict = collections.OrderedDict({})
    for config in configs:
        config_runtime_dict[config] = 0
    total_orig_history_len = 0
    total_orig_num_of_changed_lines = 0
    for example in examples:
        num_of_test_classes, num_of_test_methods = extractNumOfTests(example)
        lines += '\\DefMacro{' + example + 'NumOfTestClasses}{' + \
                 str(num_of_test_classes) + '}\n'
        lines += '\\DefMacro{' + example + 'NumOfTestMethods}{' + \
                 str(num_of_test_methods) + '}\n'
        orig_history_len, orig_num_of_changed_lines, _, _ = extractOrigHistoryInfo(example)
        lines += '\\DefMacro{' + example + 'OrigHistLen}{' + str(orig_history_len) + '}\n'
        lines += '\\DefMacro{' + example + 'OrigNumOfChangedLines}{' + \
                 str(orig_num_of_changed_lines) + '}\n'
        total_orig_history_len += orig_history_len
        total_orig_num_of_changed_lines += orig_num_of_changed_lines
        shortest_time = 0
        for config in configs:
            num_of_commits, num_of_changed_lines, run_time = extractSliceInfo(example, config)
            if shortest_time == 0 or float(run_time) < shortest_time:
                shortest_time = float(run_time)
        for config in configs:
            print (example, config)
            config_short_name = ''.join([phase[0].upper() for phase in config.split('-')])
            log_file = output_dir + '/' + config + '/' + example + '.log'
            num_of_commits, num_of_changed_lines, run_time = extractSliceInfo(example, config)
            cmt_reduction_rate = (orig_history_len - num_of_commits) / orig_history_len * 100
            lines_reduction_rate = (orig_num_of_changed_lines - num_of_changed_lines) / \
                                   orig_num_of_changed_lines * 100
            print (orig_num_of_changed_lines, num_of_changed_lines)
            config_num_of_lines_dict[config] += num_of_changed_lines
            config_line_reduction_dict[config] += lines_reduction_rate
            config_runtime_dict[config] += float(run_time)
            lines += '\\DefMacro{' + example + config + 'ConfigShortName}{' + \
                     config_short_name + '}\n'
            lines += '\\DefMacro{' + example + config + 'NumOfCommits}{' + \
                     str(num_of_commits) + '}\n'
            lines += '\\DefMacro{' + example + config + 'NumOfChangedLines}{' + \
                     str(num_of_changed_lines) + '}\n'
            if float(run_time) == shortest_time:
                lines += '\\DefMacro{' + example + config + 'Runtime}{\\textbf{' + \
                         '{0:.2f}'.format(float(run_time)) + '}}\n'
            else:
                lines += '\\DefMacro{' + example + config + 'Runtime}{' + \
                         '{0:.2f}'.format(float(run_time)) + '}\n'
            lines += '\\DefMacro{' + example + config + 'CommitReductionRate}{' + \
                     '{0:.2f}'.format(cmt_reduction_rate) + '}\n'
            lines += '\\DefMacro{' + example + config + 'LinesReductionRate}{' + \
                     '{0:.2f}'.format(lines_reduction_rate) + '}\n'
    shortest_avg_time = 0
    for config in configs:
        config_avg_run_time = config_runtime_dict[config] / len(examples)
        if shortest_avg_time == 0 or float(config_avg_run_time) < shortest_avg_time:
            shortest_avg_time = float(config_avg_run_time)
    for config in configs:
        config_avg_num_of_changed_lines = config_num_of_lines_dict[config] / len(examples)
        config_avg_line_reduction = config_line_reduction_dict[config] / len(examples)
        config_avg_run_time = config_runtime_dict[config] / len(examples)
        lines += '\\DefMacro{' + config + 'AvgNumOfChangedLines}{' + \
                     '{0:.2f}'.format(config_avg_num_of_changed_lines) + '}\n'
        lines += '\\DefMacro{' + config + 'AvgLinesReductionRate}{' + \
                     '{0:.2f}'.format(config_avg_line_reduction) + '}\n'
        if float(config_avg_run_time) == shortest_avg_time:
            lines += '\\DefMacro{' + config + 'AvgRuntime}{\\textbf{' + \
                     '{0:.2f}'.format(config_avg_run_time) + '}}\n'
        else:
            lines += '\\DefMacro{' + config + 'AvgRuntime}{' + \
                     '{0:.2f}'.format(config_avg_run_time) + '}\n'
    avg_orig_history_len = total_orig_history_len / len (examples)
    avg_orig_num_of_changed_lines = total_orig_num_of_changed_lines / len (examples)
    lines += '\\DefMacro{AvgOrigHistLen}{' + '{0:.2f}'.format(avg_orig_history_len) + '}\n'
    lines += '\\DefMacro{AvgOrigNumOfChangedLines}{' + \
             '{0:.2f}'.format(avg_orig_num_of_changed_lines) + '}\n'
    fw = open(numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

# ASE 2019  SCD, SD, CSD, CDSD, CDSCD, DSD, DSCD
def genTheorySevenConfigTable(debug, examples=runex.examples, configs=THEORY_CONFIGS, \
                              table_tex_file=THEORY_CONFIG_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by gen_all_configs_tables.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionSliceComparison{theorem:optimal:configs}}\n"
    if debug:
        table_lines += "\\begin{tabular}{lr|r|r|r|r|r|r|r|r|r|r}\n"
    else:
        table_lines += "\\begin{tabular}{lr|r|r|r|r|r|r|r}\n"
    table_lines += "\\toprule\n"
    if debug:
        table_lines += "\\multirow{2}{*}{\\TableHeadExampleId} & \\multirow{2}{*}{\\TableHeadLOCRed} & \\multicolumn{7}{c}{\\TableHeadRuntime} & \\multirow{2}{*}{Orig} & \\multirow{2}{*}{TestClass} & \\multirow{2}{*}{TestMethods} \\\\\n"
        table_lines += " & & \\multicolumn{1}{c}{\\TableHeadSD} & \\multicolumn{1}{c}{\\TableHeadSCD} & \\multicolumn{1}{c}{\\TableHeadCSD} & \\multicolumn{1}{c}{\\TableHeadDSD} & \\multicolumn{1}{c}{\\TableHeadCDSD} & \\multicolumn{1}{c}{\\TableHeadDSCD} & \\multicolumn{1}{c}{\\TableHeadCDSCD} & & & \\\\\n"
    else:
        table_lines += "\\multirow{2}{*}{\\TableHeadExampleId} & \\multirow{2}{*}{\\TableHeadLOCRed} & \\multicolumn{7}{c}{\\TableHeadRuntime} \\\\\n"
        table_lines += " & & \\multicolumn{1}{c}{\\TableHeadSD} & \\multicolumn{1}{c}{\\TableHeadSCD} & \\multicolumn{1}{c}{\\TableHeadCSD} & \\multicolumn{1}{c}{\\TableHeadDSD} & \\multicolumn{1}{c}{\\TableHeadCDSD} & \\multicolumn{1}{c}{\\TableHeadDSCD} & \\multicolumn{1}{c}{\\TableHeadCDSCD} \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        table_lines +=  '\\href{https://issues.apache.org/jira/browse/' + example + \
                        '}{\\UseMacro{' + example + '}}'
        table_lines += ' & \\UseMacro{' + example + 'split-definer' + 'LinesReductionRate}'
        for config in configs:
            table_lines += ' & \\UseMacro{' + example + config + 'Runtime}'
        if debug:
            table_lines += ' & \\UseMacro{' + example + 'OrigHistLen}'
            table_lines += ' & \\UseMacro{' + example + 'NumOfTestClasses}'
            table_lines += ' & \\UseMacro{' + example + 'NumOfTestMethods}'
        table_lines += ' \\\\\n'
    table_lines += '\\midrule\n'
    table_lines += 'Avg'
    table_lines += ' & \\UseMacro{' + 'split-definer' + 'AvgLinesReductionRate}'
    for config in configs:
        table_lines += ' & \\UseMacro{' + config + 'AvgRuntime}'
    table_lines += ' \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table*}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def plotSliceComparison(examples=runex.examples, configs=SLICE_COMPARISON_CONFIGS, \
                        numbers_tex_file=SLICE_COMPARISON_NUMBERS_TEX_FILE, \
                        plots_dir=PLOTS_DIR):
    # read numbers from tex file
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    avg_reduction_dict = collections.OrderedDict({})
    config_short_names_dict = collections.OrderedDict({})
    for config in configs:
        reduction_list = []
        for example in examples:
            for i in range(len(lines)):
                if example + config + 'LineReductionRate' in lines[i]:
                    reduction = float(lines[i].split('{')[2].split('}')[0])
                elif example + config + 'ConfigShortName' in lines[i]:
                    short_name = lines[i].split('{')[2].split('}')[0]
            reduction_list.append(reduction)
        config_avg_reduction = round(sum(reduction_list) / len(reduction_list), 2)
        avg_reduction_dict[config] = config_avg_reduction
        config_short_names_dict[config] = short_name
    sns.set()
    xticks = list(config_short_names_dict.values())
    xticks = ['GenSlice' if x == 'SD' else x for x in xticks]
    df = pd.DataFrame({"Reduction Rate (%)": list(avg_reduction_dict.values())}, index=xticks)
    print (df)
    fig = plt.figure(figsize=(8, 4))
    ax = fig.add_subplot(111)
    ax.set_ylabel("Reduction Rate (%)")
    df.plot(kind='bar', ax=ax, width=0.5, align='center', rot=0, legend=None)
    for i in range(len(ax.patches)):
        p = ax.patches[i]
        ax.text(p.get_x()-0.05, p.get_height() + 0.5, \
                str(list(avg_reduction_dict.values())[i]) + '%', fontsize=12)
    pos = df.index.get_loc('GenSlice')
    #ax.patches[pos].set_facecolor('forestgreen')
    plt.ylim(top=115)
    plt.subplots_adjust(top=0.95, bottom=0.1, left=0.1, right=0.95)
    #fig = plt.gcf()
    fig.savefig(plots_dir + '/ase19-slice-comparison.eps')
    plt.show()
    plt.clf() # MUST CLEAN

# FSE 2019
def genOptimizationComparisonNumbers(examples=runex.examples, configs=TREE_CONFIGS, \
                                     output_dir=OUTPUT_DIR, \
                                     numbers_tex_file=OPTIMIZATION_COMPARISON_NUMBERS_TEX_FILE):
    prefix_saved_phases = getWhichPhasesSavedByPrefixSharing(examples)
    suffix_saved_phases = getWhichPhasesSavedByStateMatching(examples)
    lines = ''
    all_examples_no_op_total_time = 0
    all_examples_prefix_total_time = 0
    all_examples_suffix_total_time = 0
    all_examples_prefix_saving_percentage = 0
    all_examples_suffix_saving_percentage = 0
    for example in examples:
        no_op_total_time = 0
        for config in configs:
            no_op_total_time += float(extractPhaseTime('Total', example, config))
        all_examples_no_op_total_time += no_op_total_time
        lines += '\\DefMacro{' + example + 'NoOpTotalTime}{' + \
                 '{0:.2f}'.format(no_op_total_time) + '}\n'
        # Prefix
        prefix_total_time = no_op_total_time
        for saved_config in prefix_saved_phases[example]:
            for phase in prefix_saved_phases[example][saved_config]:
                phase_time = float(extractPhaseTime(phase, example, saved_config))
                prefix_total_time -= phase_time
        prefix_saved_percentage = (no_op_total_time - prefix_total_time) / \
                                  no_op_total_time * 100
        all_examples_prefix_total_time += prefix_total_time
        all_examples_prefix_saving_percentage += prefix_saved_percentage
        lines += '\\DefMacro{' + example + 'PrefixTotalTime}{' + \
                 '{0:.2f}'.format(prefix_total_time) + '}\n'
        lines += '\\DefMacro{' + example + 'PrefixSavedPercentage}{' + \
                 '{0:.2f}'.format(prefix_saved_percentage) + '}\n'
        # Suffix
        suffix_total_time = no_op_total_time
        for saved_config in suffix_saved_phases[example]:
            for phase in suffix_saved_phases[example][saved_config]:
                phase_time = float(extractPhaseTime(phase, example, saved_config))
                suffix_total_time -= phase_time
        suffix_saved_percentage = (no_op_total_time - suffix_total_time) / \
                                  no_op_total_time * 100
        all_examples_suffix_total_time += suffix_total_time
        all_examples_suffix_saving_percentage += suffix_saved_percentage
        lines += '\\DefMacro{' + example + 'SuffixTotalTime}{' + \
                 '{0:.2f}'.format(suffix_total_time) + '}\n'
        lines += '\\DefMacro{' + example + 'SuffixSavedPercentage}{' + \
                 '{0:.2f}'.format(suffix_saved_percentage) + '}\n'
    avg_no_op_total_time = all_examples_no_op_total_time / len(examples)
    avg_prefix_total_time = all_examples_prefix_total_time / len(examples)
    avg_suffix_total_time = all_examples_suffix_total_time / len(examples)
    avg_prefix_saving_percentage = all_examples_prefix_saving_percentage / len(examples)
    avg_suffix_saving_percentage = all_examples_suffix_saving_percentage / len(examples)
    lines += '\\DefMacro{AvgNoOpTotalTime}{' + '{0:.2f}'.format(avg_no_op_total_time) + \
             '}\n'
    lines += '\\DefMacro{AvgPrefixTotalTime}{' + '{0:.2f}'.format(avg_prefix_total_time) + \
             '}\n'
    lines += '\\DefMacro{AvgSuffixTotalTime}{' + '{0:.2f}'.format(avg_suffix_total_time) + \
             '}\n'
    lines += '\\DefMacro{AvgPrefixSavingPercentage}{' + \
             '{0:.2f}'.format(avg_prefix_saving_percentage) + '}\n'
    lines += '\\DefMacro{AvgSuffixSavingPercentage}{' + \
             '{0:.2f}'.format(avg_suffix_saving_percentage) + '}\n'
    fw = open(numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

# FSE 2019  SCD, SD, CSD, CDSD, CDSCD, DSD, DSCD
def genOptimizationComparisonTable(examples=runex.examples, configs=TREE_CONFIGS, \
                                   table_tex_file=OPTIMIZATION_COMPARISON_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by gen_all_configs_tables.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionOptimizationComparison}\n"
    table_lines += "\\begin{tabular}{lrrrrr}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadExampleId & \\TableHeadNoOpTime & \\TableHeadPrefixTime & \\TableHeadSuffixTime & \\TableHeadPrefixSavePercentage & \\TableHeadSuffixSavePercentage \\\\\n"
    table_lines += "\\midrule\n"
    for example in ['COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', \
                    'COMPRESS-375', 'CSV-159', 'CSV-175', 'CSV-180', \
                    'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305', 'LANG-993', \
                    'LANG-1006','NET-527']:
        table_lines +=  example
        table_lines += '& \\UseMacro{' + example + 'NoOpTotalTime}'
        table_lines += '& \\UseMacro{' + example + 'PrefixTotalTime}'
        table_lines += '& \\UseMacro{' + example + 'SuffixTotalTime}'
        table_lines += '& \\UseMacro{' + example + 'PrefixSavedPercentage}'
        table_lines += '& \\UseMacro{' + example + 'SuffixSavedPercentage}'
        table_lines += ' \\\\\n'
    table_lines += "\\midrule\n"
    table_lines += 'Avg & \\UseMacro{AvgNoOpTotalTime} & \\UseMacro{AvgPrefixTotalTime} & \\UseMacro{AvgSuffixTotalTime} & \\UseMacro{AvgPrefixSavingPercentage} & \\UseMacro{AvgSuffixSavingPercentage} \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table*}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def countNumOfFeatures(changed_files):
    num_of_features = 0
    features = []
    for f in changed_files:
        if (f.startswith('Test') or f.endswith('Test.java')) and f.endswith('.java'):
            feature = f.replace('Test.java', '').replace('Test', '')
            features.append(feature)
            num_of_features += 1
    for f in changed_files:
        if not f.endswith('.java'):
            continue
        is_part_of_feature = False
        for feature in features:
            if f == feature + '.java':
                is_part_of_feature = True
        if not is_part_of_feature:
            num_of_features += 1
            return num_of_features
    return num_of_features

def extractOrigHistoryInfo(example):
    orig_config_file = CSLICER_ORIG_CONFIGS_DIR + '/' + example + '.properties'
    fr = open(orig_config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('startCommit'):
            start = lines[i].strip().split()[-1]
        elif lines[i].startswith('endCommit'):
            end = lines[i].strip().split()[-1]
    os.chdir(REPOS_DIR + '/' + example + '-repo')
    p = sub.Popen('git --no-pager log ' + start + '..' + end + ' --oneline', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    commits = p.stdout.readlines()
    total_num_of_insertions = 0
    total_num_of_deletions = 0
    for commit in commits:
        sha = commit.decode("utf-8")[:-1].strip().split(' ')[0]
        #print (sha)
        p = sub.Popen('git --no-pager log --stat ' + sha + ' -1', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
        p.wait()
        commit_messages = p.stdout.readlines()
        #for msg in commit_messages:
        #    print (msg.decode("utf-8"))
        last_line = commit_messages[-1].decode("utf-8")[:-1]
        if 'insertion' in last_line:
            num_of_insertions = int(last_line.split('insertion')[0].split(',')[1].strip())
            #print (num_of_insertions)
            total_num_of_insertions += num_of_insertions
        else:
            num_of_insertions = 0
        if 'deletion' in last_line:
            num_of_deletions = int(last_line.split('deletion')[0].split(',')[-1].strip())
            #print (num_of_deletions)
            total_num_of_deletions += num_of_deletions
        else:
            num_of_deletions = 0
    orig_num_of_changed_lines = total_num_of_insertions + total_num_of_deletions
    orig_history_len = len(commits)
    commit_to_num_of_changed_files_map = collections.OrderedDict({})
    commit_to_num_of_features_map = collections.OrderedDict({})
    for commit in commits:
        sha = commit.decode("utf-8")[:-1].strip().split(' ')[0]
        p = sub.Popen('git diff-tree --no-commit-id --name-only -r ' + sha, shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
        p.wait()
        changed_files = [l.decode("utf-8")[:-1].strip() for l in p.stdout.readlines()]
        commit_to_num_of_changed_files_map[commit] = len(changed_files)
        commit_to_num_of_features_map[commit] = countNumOfFeatures(changed_files)
    total_num_of_changed_files_per_commit = sum(commit_to_num_of_changed_files_map.values())
    total_num_of_features_per_commit = sum(commit_to_num_of_features_map.values())
    avg_num_of_changed_files_per_commit = round(total_num_of_changed_files_per_commit / \
                                                orig_history_len, 2)
    avg_num_of_features_per_commit = round(total_num_of_features_per_commit / \
                                           orig_history_len, 2)
    return orig_history_len, orig_num_of_changed_lines, avg_num_of_changed_files_per_commit, \
        avg_num_of_features_per_commit

def extractSplitHistoryInfo(example):
    split_cslicer_log_file = OUTPUT_DIR + '/split-cslicer/' + example + '.log'
    fr = open(split_cslicer_log_file, 'r')
    lines = fr.readlines()
    fr.close()
    split_history_len = 0
    for i in range(len(lines)):
        if lines[i].startswith('TEST: ') or lines[i].startswith('COMP: ') or \
           lines[i].startswith('HUNK: ') or lines[i].startswith('DROP: '):
            split_history_len += 1
    return split_history_len

def getSuffixTheoreticalTime(example, config, no_op_output_dir=OUTPUT_NOOP_DIR, \
                                   share_suffix_output_dir=SHARE_SUFFIX_OUTPUT_DIR):
    save_suffix_log_file = share_suffix_output_dir + '/' + config + '/' + example + '.log'
    no_op_log_file = no_op_output_dir + '/' + config + '/' + example + '.log'
    fr = open(save_suffix_log_file, 'r')
    lines = fr.readlines()
    fr.close()
    saved_phase_exec_time = 0
    for i in range(len(lines)):
        if ' Exec Time]: NOT RUN' in lines[i]:
            saved_phase = lines[i].split()[0].split('[')[1]
            saved_phase_exec_time += float(extractPhaseTime(saved_phase, example, config, \
                                                            output_dir=no_op_output_dir))
    no_op_exec_time = float(extractPhaseTime('Total', example, config, \
                                             output_dir=no_op_output_dir))
    theoretical_exec_time = no_op_exec_time - saved_phase_exec_time
    return str(theoretical_exec_time)

def genEndToEndTimeNumbers(examples=runex.examples, configs=configs, \
                           no_op_output_dir=OUTPUT_NOOP_DIR, \
                           share_prefix_output_dir=SHARE_PREFIX_OUTPUT_DIR, \
                           share_suffix_output_dir=SHARE_SUFFIX_OUTPUT_DIR, \
                           end_to_end_time_numbers_tex_file=END_TO_END_TIME_NUMBERS_TEX_FILE):
    lines = ''
    all_examples_no_op_total_time = 0
    all_examples_share_prefix_total_time = 0
    all_examples_share_suffix_total_time = 0
    all_examples_prefix_saving_percentage = []
    all_examples_suffix_saving_percentage = []
    for example in examples:
        no_op_end_to_end_time = 0
        share_prefix_end_to_end_time = 0
        share_suffix_end_to_end_time = 0
        for config in configs:
            no_op_exec_time = float(extractPhaseTime('Total', example, config, \
                                               output_dir=no_op_output_dir))
            share_prefix_exec_time =float(extractPhaseTime('Total', example, config, \
                                                      output_dir=share_prefix_output_dir))
            share_suffix_exec_time = float(extractPhaseTime('Total', example, config, \
                                                      output_dir=share_suffix_output_dir))
            # share_suffix_exec_time = float(getSuffixTheoreticalTime(example, config))
            no_op_end_to_end_time += no_op_exec_time
            share_prefix_end_to_end_time += share_prefix_exec_time
            share_suffix_end_to_end_time += share_suffix_exec_time
        lines += '\\DefMacro{' + example.replace('-', '') + 'NoOpEndToEndTime}{' + \
                 '{0:.2f}'.format(no_op_end_to_end_time) + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'SharePrefixEndToEndTime}{' + \
                 '{0:.2f}'.format(share_prefix_end_to_end_time) + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'ShareSuffixEndToEndTime}{' + \
                 '{0:.2f}'.format(share_suffix_end_to_end_time) + '}\n'
        all_examples_no_op_total_time += no_op_end_to_end_time
        all_examples_share_prefix_total_time += share_prefix_end_to_end_time
        all_examples_share_suffix_total_time += share_suffix_end_to_end_time
        prefix_saving_percentage = (no_op_end_to_end_time - share_prefix_end_to_end_time) / \
                                   no_op_end_to_end_time * 100
        suffix_saving_percentage = (no_op_end_to_end_time - share_suffix_end_to_end_time) / \
                                   no_op_end_to_end_time * 100
        lines += '\\DefMacro{' + example.replace('-', '') + 'SharePrefixSavingPercentage}{' + \
                 '{0:.2f}'.format(prefix_saving_percentage) + '\%}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'ShareSuffixSavingPercentage}{' + \
                 '{0:.2f}'.format(suffix_saving_percentage) + '\%}\n'
        all_examples_prefix_saving_percentage.append(prefix_saving_percentage)
        all_examples_suffix_saving_percentage.append(suffix_saving_percentage)
    lines += '\\DefMacro{AllExamplesNoOpEndToEndTime}{' + \
             '{0:.2f}'.format(all_examples_no_op_total_time) + '}\n'
    lines += '\\DefMacro{AllExamplesSharePrefixEndToEndTime}{' + \
             '{0:.2f}'.format(all_examples_share_prefix_total_time) + '}\n'
    lines += '\\DefMacro{AllExamplesShareSuffixEndToEndTime}{' + \
             '{0:.2f}'.format(all_examples_share_suffix_total_time) + '}\n'
    avg_no_op_total_time = all_examples_no_op_total_time / len(examples)
    avg_share_prefix_total_time = all_examples_share_prefix_total_time / len(examples)
    avg_share_suffix_total_time = all_examples_share_suffix_total_time / len(examples)
    lines += '\\DefMacro{AvgNoOpEndToEndTime}{' + \
             '{0:.2f}'.format(avg_no_op_total_time) + '}\n'
    lines += '\\DefMacro{AvgSharePrefixEndToEndTime}{' + \
             '{0:.2f}'.format(avg_share_prefix_total_time) + '}\n'
    lines += '\\DefMacro{AvgShareSuffixEndToEndTime}{' + \
             '{0:.2f}'.format(avg_share_suffix_total_time) + '}\n'
    avg_share_prefix_saving_percentage = sum(all_examples_prefix_saving_percentage) / \
                                         len(examples)
    avg_share_suffix_saving_percentage = sum(all_examples_suffix_saving_percentage) / \
                                         len(examples)
    lines += '\\DefMacro{AvgSharePrefixSavingPercentage}{' + \
             '{0:.2f}'.format(avg_share_prefix_saving_percentage) + '\%}\n'
    lines += '\\DefMacro{AvgShareSuffixSavingPercentage}{' + \
             '{0:.2f}'.format(avg_share_suffix_saving_percentage) + '\%}\n'
    fw = open(end_to_end_time_numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

def genPreStudyNumbers(examples, configs, output_dir=OUTPUT_DIR, \
                       numbers_tex_file=PRE_STUDY_NUMBERS_TEX_FILE):
    lines = ''
    for example in examples:
        definer_log_file = output_dir + '/definer-split-cslicer-definer/' + example + \
                           '.log.phase1'
        fr = open(definer_log_file, 'r')
        definer_log_lines = fr.readlines()
        fr.close()
        _, definer_hist_slice_size, _ = extractSliceInfoFromDefinerLog(definer_log_lines, \
                                                                 standalone=True)
        orig_history_len, orig_num_of_changed_lines, orig_avg_num_of_changed_files_per_commit, \
            orig_avg_num_of_features_per_commit = extractOrigHistoryInfo(example)
        definer_reduction_rate = (orig_num_of_changed_lines - definer_hist_slice_size) / \
                                 orig_num_of_changed_lines
        lines += '\\DefMacro{' + example + 'DefinerOrigHistLen}{' + \
                 str(orig_num_of_changed_lines) + '}\n'
        lines += '\\DefMacro{' + example + 'DefinerHistSliceSize}{' + \
                 str(definer_hist_slice_size) + '}\n'
        lines += '\\DefMacro{' + example + 'DefinerReductionRate}{' + \
                 '{0:.2f}'.format(definer_reduction_rate) + '}\n'
    fw = open(numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

def genPreStudyTable(examples, configs, output_dir=OUTPUT_DIR, \
                     table_tex_file=PRE_STUDY_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by gen_all_configs_tables.py\n"
    table_lines += "\\begin{table}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionPreStudy}\n"
    table_lines += "\\begin{tabular}{l|rrr}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadExampleId & \\TableHeadDefinerOrigHisLen & \\TableHeadDefinerHistSliceSize & \\TableHeadDefinerIsOptimal \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        table_lines += '\\UseMacro{' + example + '}'
        table_lines += ' & \\UseMacro{' + example + 'DefinerOrigHistLen}'
        table_lines += ' & \\UseMacro{' + example + 'DefinerHistSliceSize}'
        table_lines += ' & Yes'
        table_lines += ' \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def genNumbers(examples, configs, is_first_time=True, numbers_tex_file=NUMBERS_TEX_FILE):
    lines = []
    for example in examples:
        orig_history_len, orig_num_of_changed_lines, orig_avg_num_of_changed_files_per_commit, \
        orig_avg_num_of_features_per_commit = extractOrigHistoryInfo(example)
        split_history_len = extractSplitHistoryInfo(example)
        for config in configs:
            print (example, config)
            num_of_commits_map_back, num_of_changed_lines, run_time = \
                                                            extractSliceInfo(example, config)
            print (num_of_commits_map_back, num_of_changed_lines, run_time)
            if num_of_commits_map_back == None or \
               num_of_changed_lines == None \
               or run_time == None:
                num_of_commits_map_back, num_of_changed_lines, run_time = ('TO', 'TO', 'TO')
                commits_reduction, changed_lines_reduction = ('TO', 'TO')
            else:
                commits_reduction = (orig_history_len - num_of_commits_map_back) / orig_history_len * 100
                print (commits_reduction)
                #print (orig_num_of_changed_lines, num_of_changed_lines)
                changed_lines_reduction = (orig_num_of_changed_lines - num_of_changed_lines) / orig_num_of_changed_lines * 100

            example_id = example.replace('-', '')
            config_id = config.replace('-', '')
            lines += '\\DefMacro{' + example_id + config_id + 'NumOfCommitsMapBack}{' + \
                     str(num_of_commits_map_back) + '}\n'
            lines += '\\DefMacro{' + example_id + config_id + 'NumOfChangedLines}{' + \
                     str(num_of_changed_lines) + '}\n'
            if run_time == 'TO':
                lines += '\\DefMacro{' + example_id + config_id + 'RunTime}{' + \
                         'TO' + '}\n'
                lines += '\\DefMacro{' + example_id + config_id + 'CommitsReduction}{' + \
                         'TO' + '}\n'
                lines += '\\DefMacro{' + example_id + config_id + 'ChangedLinesReduction}{' + \
                         'TO' + '}\n'
            else:
                lines += '\\DefMacro{' + example_id + config_id + 'RunTime}{' + \
                         '{0:.2f}'.format(float(run_time)) + '}\n'
                lines += '\\DefMacro{' + example_id + config_id + 'CommitsReduction}{' + \
                         '{0:.2f}'.format(commits_reduction) + '\%}\n'
                lines += '\\DefMacro{' + example_id + config_id + 'ChangedLinesReduction}{' + \
                         '{0:.2f}'.format(changed_lines_reduction) + '\%}\n'
        if is_first_time and numbers_tex_file != NUMBERS_TEX_FILE:
            lines += '\\DefMacro{' + example_id + 'OrigHisLen}{' + \
                     str(orig_history_len) + '}\n'
            lines += '\\DefMacro{' + example_id + 'OrigNumOfChangedLines}{' + \
                     str(orig_num_of_changed_lines) + '}\n'
            lines += '\\DefMacro{' + example_id + 'OrigAvgNumOfChangedFilesPerCommit}{' + \
                     str(orig_avg_num_of_changed_files_per_commit) + '}\n'
            lines += '\\DefMacro{' + example_id + 'OrigAvgNumOfFeaturesPerCommit}{' + \
                     str(orig_avg_num_of_features_per_commit) + '}\n'
            lines += '\\DefMacro{' + example_id + 'SplitHisLen}{' + \
                     str(split_history_len) + '}\n'
    # Compute averages
    total_orig_history_len = 0
    total_orig_num_of_changed_lines = 0
    for example in examples:
        orig_history_len, orig_num_of_changed_lines, _, _ = extractOrigHistoryInfo(example)
        total_orig_history_len += orig_history_len
        total_orig_num_of_changed_lines += orig_num_of_changed_lines
    if is_first_time and numbers_tex_file != NUMBERS_TEX_FILE:
        avg_orig_history_len = total_orig_history_len / len(examples)
        avg_orig_num_of_changed_lines = total_orig_num_of_changed_lines / len(examples)
        lines += '\\DefMacro{AvgOrigHisLen}{' + \
                 str(int(avg_orig_history_len)) + '}\n'
        lines += '\\DefMacro{AvgOrigNumOfChangedLines}{' + \
                 str(int(avg_orig_num_of_changed_lines)) + '}\n'
    for config in configs:
        config_id = config.replace('-', '')
        total_num_of_commits_map_back = 0
        total_num_of_changed_lines = 0
        total_commits_reduction = 0
        total_changed_lines_reduction = 0
        total_run_time = 0
        num_of_not_timeout_examples = 0
        for example in examples:
            orig_history_len, orig_num_of_changed_lines, _, _ = extractOrigHistoryInfo(example)
            print (example, config)
            num_of_commits_map_back, num_of_changed_lines, run_time = \
                                                            extractSliceInfo(example, config)
            print (num_of_commits_map_back, num_of_changed_lines, run_time)
            if num_of_commits_map_back == None or \
               num_of_changed_lines == None \
               or run_time == None:
                num_of_commits_map_back, num_of_changed_lines, run_time = (0, 0, 0)
                commits_reduction, changed_lines_reduction = (0, 0)
            else:
                num_of_not_timeout_examples += 1
                commits_reduction = (orig_history_len - num_of_commits_map_back) \
                                    / orig_history_len * 100
                changed_lines_reduction = (orig_num_of_changed_lines - num_of_changed_lines) \
                                    / orig_num_of_changed_lines * 100
            total_num_of_commits_map_back += num_of_commits_map_back
            total_num_of_changed_lines += num_of_changed_lines
            print (commits_reduction)
            total_commits_reduction += commits_reduction
            total_changed_lines_reduction += changed_lines_reduction
            total_run_time += float(run_time)
        avg_num_of_commits_map_back = total_num_of_commits_map_back / \
                                      num_of_not_timeout_examples
        avg_num_of_changed_lines = total_num_of_changed_lines / \
                                   num_of_not_timeout_examples
        avg_commits_reduction = total_commits_reduction / \
                                num_of_not_timeout_examples
        avg_changed_lines_reduction = total_changed_lines_reduction/ \
                                      num_of_not_timeout_examples
        avg_run_time = total_run_time / num_of_not_timeout_examples
        lines += '\\DefMacro{' + config_id + 'AvgNumOfCommitsMapBack}{' + \
                         str(int(avg_num_of_commits_map_back)) + '}\n'
        lines += '\\DefMacro{' + config_id + 'AvgNumOfChangedLines}{' + \
                         str(int(avg_num_of_changed_lines)) + '}\n'
        lines += '\\DefMacro{' + config_id + 'AvgCommitsReduction}{' + \
                         '{0:.2f}'.format(avg_commits_reduction) + '\%}\n'
        lines += '\\DefMacro{' + config_id + 'AvgChangedLinesReduction}{' + \
                         '{0:.2f}'.format(avg_changed_lines_reduction) + '\%}\n'
        lines += '\\DefMacro{' + config_id + 'AvgRunTime}{' + \
                         '{0:.2f}'.format(avg_run_time) + '}\n'
    fw = open(numbers_tex_file, 'w')
    fw.write(''.join(lines))
    fw.close()

def genTable(examples, configs, table_id):
    if table_id == '0':
        configs = ['cslicer', 'definer']
    elif table_id == '1':
        configs = ['split-cslicer', 'split-definer']
    elif table_id == '2':
        configs = ['cslicer-split-cslicer', 'cslicer-split-definer']
    elif table_id == '3':
        configs = ['definer-split-cslicer', 'cslicer-definer']
    elif table_id == '4':
        configs = ['split-cslicer-definer', 'split-cslicer-definer']
    # elif table_id == '5':
    #     configs = ['split-cslicer-definer', 'split-cslicer-definer']
    if table_id == '6': # ISSTA 19
        configs = ['cslicer-definer-split-cslicer', 'cslicer-definer-split-definer']
    elif table_id == '7': # ISSTA 19
        configs = ['split-cslicer-definer-definer', 'definer-split-cslicer-definer']
    elif table_id == '8': # ISSTA 19
        configs = ['cslicer-split-definer-definer', 'definer-cslicer-split-definer']
    elif table_id == '9': # ISSTA 19
        configs = ['split-definer-cslicer-definer', 'definer-split-definer']
    elif table_id == '10': # ISSTA 19
        configs = ['cslicer-definer-definer', 'definer-cslicer-definer']
    elif table_id == '11': # ISSTA 19
        configs = ['split-definer-definer', 'definer-definer']
    elif table_id == '12': # ISSTA 19
        configs = ['cslicer-definer-split-cslicer-definer', \
                   'cslicer-split-definer-cslicer-definer']
    # for true minimal exp
    elif table_id == 'split-definer' or table_id == 'split-definer-with-memory':
        configs = [table_id]

    table_lines = []
    table_lines += "\\begin{table}\n"
    table_lines += "\\begin{scriptsize}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\vspace{-8pt}\n"
    # for true minimal exp
    if table_id == 'split-definer':
        table_lines += "\\caption{\\TableCaptionSplitDefiner}\n"
    elif table_id == 'split-definer-with-memory':
        table_lines += "\\caption{\\TableCaptionSplitDefinerWithMemory}\n"
    else:
        table_lines += "\\caption{\\TableCaptionAllConfigs}\n"
    table_lines += "\\makebox[\\linewidth][c]{"
    # for true minimal exp
    if table_id == 'split-definer' or table_id == 'split-definer-with-memory':
        table_lines += "\\begin{tabular}{l|rrrrr|rr}\n"
    else:
        table_lines += "\\begin{tabular}{l|rrR{1.2cm}rr|rrR{1.2cm}rr|R{1.5cm}R{1.5cm}}\n"
    table_lines += "\\toprule\n"
    if table_id == '0':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerStandalone} & \\multicolumn{5}{c}{\\TableHeadDefinerStandalone} & & \\\\\n"
    elif table_id == '1':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitCSlicer} & \\multicolumn{5}{c}{\\TableHeadSplitDefiner} & & \\\\\n"
    elif table_id == '2':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerSplitCSlicer} & \\multicolumn{5}{c}{\\TableHeadCSlicerSplitDefiner} & & \\\\\n"
    elif table_id == '3':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadDefinerSplitCSlicer} & \\multicolumn{5}{c}{\\TableHeadCSlicerDefiner} & & \\\\\n"
    elif table_id == '4':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitCSlicerDefiner} & \\multicolumn{5}{c}{\\TableHeadSplitCSlicerDefiner} & & \\\\\n"
    # elif table_id == '5':
    #     table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerDefiner} & \\multicolumn{5}{c}{\\TableHeadSplitCSlicerDefiner} & & \\\\\n"
    if table_id == '6': # ISSTA 19
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerDefinerSplitCSlicer} & \\multicolumn{5}{c}{\\TableHeadCSlicerDefinerSplitDefiner} & & \\\\\n"
    elif table_id == '7':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitCSlicerDefinerDefiner} & \\multicolumn{5}{c}{\\TableHeadDefinerSplitCSlicerDefiner} & & \\\\\n"
    elif table_id == '8':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerSplitDefinerDefiner} & \\multicolumn{5}{c}{\\TableHeadDefinerCSlicerSplitDefiner} & & \\\\\n"
    elif table_id == '9':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitDefinerCSlicerDefiner} & \\multicolumn{5}{c}{\\TableHeadDefinerSplitDefiner} & & \\\\\n"
    elif table_id == '10':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerDefinerDefiner} & \\multicolumn{5}{c}{\\TableHeadDefinerCSlicerDefiner} & & \\\\\n"
    elif table_id == '11':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitDefinerDefiner} & \\multicolumn{5}{c}{\\TableHeadDefinerDefiner} & & \\\\\n"
    elif table_id == '12':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadCSlicerDefinerSplitCSlicerDefiner} & \\multicolumn{5}{c}{\\TableHeadCSlicerSplitDefinerCSlicerDefiner} & & \\\\\n"
    # for true minimal exp
    elif table_id == 'split-definer':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitDefiner} & & \\\\\n"
    # for true minimal exp
    elif table_id == 'split-definer-with-memory':
        table_lines += "\\multirow{2}{*}{} & \\multicolumn{5}{c}{\\TableHeadSplitDefinerWithMemory} & & \\\\\n"
    table_lines += " & "
    for config in configs:
        table_lines += "\\TableHeadNumOfCommits & \\TableHeadNumOfChangedLines & \\TableHeadCommitsReduction & \\TableHeadChangedLinesReduction & \\TableHeadRunTime & "
    table_lines += "\\TableHeadOrigHisLen & \\TableHeadOrigNumOfChangedLines \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        example_id = example.replace('-', '')
        table_lines += example + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'NumOfCommitsMapBack}' + ' & ' + \
            '\\UseMacro{' + example_id + config_id + \
            'NumOfChangedLines}' + ' & ' + \
            '\\UseMacro{' + example_id + config_id + \
            'CommitsReduction}' + ' & ' + \
            '\\UseMacro{' + example_id + config_id + \
            'ChangedLinesReduction}' + ' & ' + \
            '\\UseMacro{' + example_id + config_id + \
            'RunTime}' + ' & '
        table_lines += '\\UseMacro{' + example_id + 'OrigHisLen}' + ' & ' + \
                       '\\UseMacro{' + example_id + 'OrigNumOfChangedLines}' + '\\\\\n'
    table_lines += "\\midrule\n"
    table_lines += '\TableRowAvg & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgNumOfCommitsMapBack}' + ' & ' + \
                       '\\UseMacro{' + config_id + 'AvgNumOfChangedLines}' + ' & ' + \
                       '\\UseMacro{' + config_id + 'AvgCommitsReduction}' + ' & ' + \
                       '\\UseMacro{' + config_id + 'AvgChangedLinesReduction}' + ' & ' + \
                       '\\UseMacro{' + config_id + 'AvgRunTime}' + ' & '
    table_lines += '\\UseMacro{AvgOrigHisLen}' + ' & ' + \
                   '\\UseMacro{AvgOrigNumOfChangedLines}' + '\\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{scriptsize}\n"
    table_lines += "\\end{table}\n"
    if table_id == '0':
        fw = open(TABLE0_TEX_FILE, 'w')
    elif table_id == '1':
        fw = open(TABLE1_TEX_FILE, 'w')
    elif table_id == '2':
        fw = open(TABLE2_TEX_FILE, 'w')
    elif table_id == '3':
        fw = open(TABLE3_TEX_FILE, 'w')
    elif table_id == '4':
        fw = open(TABLE4_TEX_FILE, 'w')
    # elif table_id == '5':
    #     fw = open(TABLE5_TEX_FILE, 'w')
    if table_id == '6':
        fw = open(TABLE6_TEX_FILE, 'w')
    elif table_id == '7':
        fw = open(TABLE7_TEX_FILE, 'w')
    elif table_id == '8':
        fw = open(TABLE8_TEX_FILE, 'w')
    elif table_id == '9':
        fw = open(TABLE9_TEX_FILE, 'w')
    elif table_id == '10':
        fw = open(TABLE10_TEX_FILE, 'w')
    elif table_id == '11':
        fw = open(TABLE11_TEX_FILE, 'w')
    elif table_id == '12':
        fw = open(TABLE12_TEX_FILE, 'w')
    # for true minimal exp
    elif table_id == 'split-definer':
        fw = open(SPLIT_DEFINER_TABLE_TEX_FILE, 'w')
    elif table_id == 'split-definer-with-memory':
        fw = open(SPLIT_DEFINER_WITH_MEMORY_TABLE_TEX_FILE, 'w')
    fw.write(''.join(table_lines))
    fw.close()

# for asej exp2
def genEffectivenessTable(examples, configs, table_tex_file=EFFECTIVENESS_EXP_TABLE_TEX_FILE):
    table_lines = []
    table_lines += "\\begin{table}\n"
    table_lines += "\\begin{tiny}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\vspace{-8pt}\n"
    table_lines += "\\caption{\\TableCaptionEffectivenessExp}\n"
    table_lines += "\\makebox[\\linewidth][c]{"
    table_lines += "\\begin{tabular}{l|rrr|rrr|rrr|r}\n"
    table_lines += "\\toprule\n"
    # row 1
    table_lines += "\\multirow{2}{*}{\\TableHeadExpTwoId} & " + \
                   "\\multicolumn{3}{c}{\\TableHeadExpTwoNumOfCommits} & " + \
                   "\\multicolumn{3}{c}{\\TableHeadExpTwoCommitsReduction} & " + \
                   "\\multicolumn{3}{c}{\\TableHeadExpTwoTime} & " + \
                   "\\multirow{2}{*}{\\TableHeadOrigHisLen} \\\\\n"
    # row 2
    table_lines += " & "
    for i in range(3):
        table_lines += "\\TableHeadExpTwoDefault & \\TableHeadExpTwoLearning & \\TableHeadExpTwoBasic & "
    table_lines += "\\\\\n"
    table_lines += "\\midrule\n"
    # rows 3-n
    for example in examples:
        example_id = example.replace('-', '')
        table_lines += example + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'NumOfCommitsMapBack}' + ' & '
        # for config in configs:
        #     config_id = config.replace('-', '')
        #     table_lines += '\\UseMacro{' + example_id + config_id + \
        #     'NumOfChangedLines}' + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'CommitsReduction}' + ' & '
        # for config in configs:
        #     config_id = config.replace('-', '')
        #     table_lines += '\\UseMacro{' + example_id + config_id + \
        #     'ChangedLinesReduction}' + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'RunTime}' + ' & '
        table_lines += '\\UseMacro{' + example_id + 'OrigHisLen}' + '\\\\\n'
    # last a few rows
    table_lines += "\\midrule\n"
    table_lines += '\\TableRowAvg & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgNumOfCommitsMapBack}' + ' & '
    # for config in configs:
    #     config_id = config.replace('-', '')
    #     table_lines += '\\UseMacro{' + config_id + 'AvgNumOfChangedLines}' + ' & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgCommitsReduction}' + ' & '
    # for config in configs:
    #     config_id = config.replace('-', '')
    #     table_lines += '\\UseMacro{' + config_id + 'AvgChangedLinesReduction}' + ' & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgRunTime}' + ' & '
    table_lines += '\\UseMacro{AvgOrigHisLen}' + '\\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{tiny}\n"
    table_lines += "\\end{table}\n"
    fw = open(table_tex_file, 'w')
    fw.write(''.join(table_lines))
    fw.close()

# for asej exp3
def genPartitionTable(examples, configs, table_tex_file=PARTITION_EXP_TABLE_TEX_FILE):
    table_lines = []
    table_lines += "\\begin{table}\n"
    table_lines += "\\begin{tiny}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\vspace{-8pt}\n"
    table_lines += "\\caption{\\TableCaptionPartitionExp}\n"
    table_lines += "\\makebox[\\linewidth][c]{"
    table_lines += "\\begin{tabular}{l|rrrr|rrrr|rrrr|r}\n"
    table_lines += "\\toprule\n"
    # row 1
    table_lines += "\\multirow{2}{*}{\\TableHeadExpThreeId} & \\multicolumn{4}{c}{\\TableHeadExpThreeNumOfCommits} & \\multicolumn{4}{c}{\\TableHeadExpThreeCommitsReduction} & \\multicolumn{4}{c}{\\TableHeadExpThreeTime} & \\multirow{2}{*}{\\TableHeadOrigHisLen} \\\\\n"
    # row 2
    #table_lines += " & "
    for i in range(3):
        table_lines += " & \\TableHeadExpThreeDefault & \\TableHeadExpThreeNeg & \\TableHeadExpThreeNoPos & \\TableHeadExpThreeLowThree"
    table_lines += " \\\\\n"
    table_lines += "\\midrule\n"
    # rows 3-n
    for example in examples:
        example_id = example.replace('-', '')
        table_lines += example + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'NumOfCommitsMapBack}' + ' & '
        # for config in configs:
        #     config_id = config.replace('-', '')
        #     table_lines += '\\UseMacro{' + example_id + config_id + \
        #     'NumOfChangedLines}' + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'CommitsReduction}' + ' & '
        # for config in configs:
        #     config_id = config.replace('-', '')
        #     table_lines += '\\UseMacro{' + example_id + config_id + \
        #     'ChangedLinesReduction}' + ' & '
        for config in configs:
            config_id = config.replace('-', '')
            table_lines += '\\UseMacro{' + example_id + config_id + \
            'RunTime}' + ' & '
        table_lines += '\\UseMacro{' + example_id + 'OrigHisLen}' + ' \\\\\n'
    # last a few rows
    table_lines += "\\midrule\n"
    table_lines += '\\TableRowAvg & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgNumOfCommitsMapBack}' + ' & '
    # for config in configs:
    #     config_id = config.replace('-', '')
    #     table_lines += '\\UseMacro{' + config_id + 'AvgNumOfChangedLines}' + ' & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgCommitsReduction}' + ' & '
    # for config in configs:
    #     config_id = config.replace('-', '')
    #     table_lines += '\\UseMacro{' + config_id + 'AvgChangedLinesReduction}' + ' & '
    for config in configs:
        config_id = config.replace('-', '')
        table_lines += '\\UseMacro{' + config_id + 'AvgRunTime}' + ' & '
    table_lines += '\\UseMacro{AvgOrigHisLen} ' + ' \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{tiny}\n"
    table_lines += "\\end{table}\n"
    fw = open(table_tex_file, 'w')
    fw.write(''.join(table_lines))
    fw.close()

def genSmallHistoryLenTable(examples, table_tex_file=SMALL_HISTORY_INFO_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by gen_all_configs_tables.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionSmallHistoryInfo}\n"
    table_lines += "\\begin{tabular}{l|rr|rr}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadExampleId & \\TableHeadNumOfFilesChanged & \\TableHeadOrigHisLen & \\TableHeadAvgNumOfChangedFilesPerCommit & \\TableHeadAvgNumOfFeaturesPerCommit \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        table_lines +=  example
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'SplitHisLen}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'OrigHisLen}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + \
                       'OrigAvgNumOfChangedFilesPerCommit}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + \
                       'OrigAvgNumOfFeaturesPerCommit}'
        table_lines += ' \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table*}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def genEndToEndTimeTable(examples=runex.examples, table_tex_file=END_TO_END_TIME_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by gen_all_configs_tables.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionEndToEndTime}\n"
    table_lines += "\\makebox[\\linewidth][c]{\\begin{tabular}{l|rrr|rr}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadExampleId & \\TableHeadTimeWithoutCache & \\TableHeadTimeWithPrefixCache & \\TableHeadTimeWithSuffixCache & \\TableHeadPrefixCacheSavingPercentage & \\TableHeadSuffixCacheSavingPercentage \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        table_lines +=  example
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'NoOpEndToEndTime}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'SharePrefixEndToEndTime}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'ShareSuffixEndToEndTime}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + \
                       'SharePrefixSavingPercentage}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + \
                       'ShareSuffixSavingPercentage}'
        table_lines += ' \\\\\n'
    table_lines += "\\midrule\n"
    table_lines += 'Avg & \\UseMacro{AvgNoOpEndToEndTime} & \\UseMacro{AvgSharePrefixEndToEndTime} & \\UseMacro{AvgShareSuffixEndToEndTime} & \\UseMacro{AvgSharePrefixSavingPercentage} & \\UseMacro{AvgShareSuffixSavingPercentage} \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table*}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

if __name__ == '__main__':
    examples = runex.examples
    # genNumbers(examples, configs=OLD_CONFIGS, numbers_tex_file=NUMBERS_TEX_FILE)
    # genTable(examples, configs, '0')
    # genTable(examples, configs, '1')
    # genTable(examples, configs, '2')
    # genTable(examples, configs, '3')
    # genTable(examples, configs, '4')
    # genTable(examples, configs, '5')

    # ISSTA 19
    # genNumbers(examples, configs, numbers_tex_file=TREE_NUMBERS_TEX_FILE)
    # genTable(examples, configs, '6')
    # genTable(examples, configs, '7')
    # genTable(examples, configs, '8')
    # genTable(examples, configs, '9')
    # genTable(examples, configs, '10')
    # genTable(examples, configs, '11')
    # genTable(examples, configs, '12')
    # genEndToEndTimeNumbers()
    # genEndToEndTimeTable()

    # FSE 19 paper
    #getWhichPhasesSavedByStateMatching()
    #genSliceComparisonNumbers()
    #genSliceComparisonTable()
    #getWhichPhasesSavedByPrefixSharing()
    #genOptimizationComparisonNumbers()
    #genOptimizationComparisonTable()

    #genSmallHistoryLenTable(examples)

    # ASE 19
    genTheorySevenConfigNumbers()
    #genTheorySevenConfigTable(debug=False)
    #genPreStudyNumbers(runex.definer_optimal_examples, configs)
    #genPreStudyTable(runex.definer_optimal_examples, configs)
    #plotSliceComparison()
    #plotOptimizationTime()

    # for true minimal exp
    # configs = ['split-definer', 'split-definer-with-memory']
    # genNumbers(examples, configs, numbers_tex_file=NUMBERSX_TEX_FILE)
    # genTable(examples, configs, 'split-definer')
    # genTable(examples, configs, 'split-definer-with-memory')

    # for asej exp2
    # configs = ['definer', 'definer-learning', 'definer-basic']
    # genNumbers(examples, configs, is_first_time=False, \
    #            numbers_tex_file=EFFECTIVENESS_EXP_NUMBERS_TEX_FILE)
    # genEffectivenessTable(examples, configs)

    # for asej exp3
    # configs = ['definer-neg', 'definer-nopos', 'definer-low3']
    # genNumbers(examples, configs, is_first_time=False, \
    #            numbers_tex_file=PARTITION_EXP_NUMBERS_TEX_FILE)
    # configs = ['definer', 'definer-neg', 'definer-nopos', 'definer-low3']
    # genPartitionTable(examples, configs)


# --- deprecated ---
# ISSTA 19
@with_goto
def stateMatch_OLD(example, saved, new, current_config, \
               configs_shortnames_map=CONFIGS_SHORTNAMES_MAP, output_dir=OUTPUT_DIR):
    print ('Comparing ' + str(saved) + ' vs ' + str(new))
    current_config = configs_shortnames_map[current_config]
    # find out which config to query
    short_name_list = list(configs_shortnames_map.keys())
    for i in range(len(short_name_list)):
        if short_name_list[i].startswith(saved):
            saved_config = configs_shortnames_map[short_name_list[i]]
    # saved
    if 's' in saved and 's' != saved:
        phase = len(saved) - 1 # if the compared prefix includes s, then get saved-1
        if saved.endswith('s'):
            if len(saved) == 3:
                print ('NOT SURE: ' + saved + ' vs ' + new)
                return False
            else:
                saved_log = genSplitLogFile(example, saved_config, source_log=output_dir + \
                                '/' + saved_config + '/' + example + '.log.phase' + str(phase))
        else:
            saved_log = output_dir + '/' + saved_config + '/' + example + '.log.phase' + \
                        str(phase)
    else:
        phase = len(saved)
        saved_log = output_dir + '/' + saved_config + '/' + example + '.log.phase' + str(phase)
    if not os.path.isfile(saved_log):
        return False
    # new
    if new == None:
        new_log = None
        goto .new_is_none
    if 's' in new and 's' != new:
        phase = len(new) - 1
        if new.endswith('s'):
            if len(new) == 3:
                print ('NOT SURE: ' + new + ' vs ' + new)
                return False
            else:
                new_log = genSplitLogFile(example, current_config, source_log=output_dir + \
                            '/' + current_config + '/' + example + '.log.phase' + str(phase))
        else:
            new_log = output_dir + '/' + current_config + '/' + example + '.log.phase' + \
                      str(phase)
    else:
        phase = len(new)
        new_log = output_dir + '/' + current_config + '/' + example + '.log.phase' + str(phase)
    if not os.path.isfile(new_log):
        return False
    # compare saved log with new log
    label .new_is_none
    if compareSavedLogWithNewLog(saved_log, new_log, example):
        return True
    else:
        return False

# --- deprecated ---
# ISSTA 19
def compareSavedLogWithNewLog_OLD(cached_log, current_log, example):
    print ('CACHED LOG IS: ' + str(cached_log))
    print ('CURRENT LOG IS: ' + str(current_log))
    start, end, _, _, repo_path, _, _ = runex.extractInfoFromCSlicerConfigs(example)
    if current_log == None:
        current_slice = runex.getOriginalHistory(start, end, repo_path)
        if runex.isCommitLevel(cached_log):
            cached_slice = runex.extractSliceFromCommitLevelLog(cached_log)
        elif runex.isFileLevel(cached_log):
            cached_slice = runex.extractSliceFromFileLevelLog(cached_log)
        else:
            cached_slice = runex.extractHistorySliceFromSplitLog(cached_log)
        if current_slice == cached_slice:
            return True
        else:
            return False
    if runex.isCommitLevel(current_log) and runex.isFileLevel(cached_log):
        return False
    if runex.isFileLevel(current_log) and runex.isCommitLevel(cached_log):
        return False
    if runex.isCommitLevel(current_log) and runex.isCommitLevel(cached_log):
        current_slice = runex.extractSliceFromCommitLevelLog(current_log)
        cached_slice = runex.extractSliceFromCommitLevelLog(cached_log)
        if current_slice == cached_slice:
            return True
        else:
            return False
    if runex.isFileLevel(current_log) and runex.isFileLevel(cached_log):
        current_slice = runex.extractSliceFromFileLevelLog(current_log)
        cached_slice = runex.extractSliceFromFileLevelLog(cached_log)
        if current_slice == cached_slice:
            return True
        else:
            return False

# --- deprecated ---
def computeRunTime(example, config, output_dir=OUTPUT_DIR):
    if not os.path.isfile(output_dir + '/' + config + '/' + example + '.log'):
        return 'TO'
    fr = open(output_dir + '/' + config + '/' + example + '.log', 'r')
    lines = fr.readlines()
    fr.close()
    # full time
    if config == 'cslicer-definer-split-cslicer' or \
       config == 'split-cslicer-definer-definer' or \
       config == 'definer-split-cslicer-definer' or \
       config == 'cslicer-split-definer-definer' or \
       config == 'definer-cslicer-split-definer' or \
       config == 'split-definer-cslicer-definer':
        for i in range(len(lines)):
            if lines[i].startswith('[Total Exec Time]:'):
                total_time = float(lines[i].strip().split()[-1])
                #return '{:.2f}'.format(total_time)
                return total_time
    elif config == 'cslicer-definer-split-definer':
        cslicer_time = extractPhaseTime('CSlicer', example, 'cslicer-definer-split-cslicer')
        definer_time = extractPhaseTime('Definer', example, 'cslicer-definer-split-cslicer')
        split_time = extractPhaseTime('Split', example, 'cslicer-definer-split-cslicer')
        definer2_time = extractPhaseTime('Definer2', example, config)
        if 'TIME OUT' in [cslicer_time, definer_time, split_time, definer2_time]:
            return 'TO'
        else:
            total_time = float(cslicer_time) + float(definer_time) + float(split_time) + \
                         float(definer2_time)
            #return '{:.2f}'.format(total_time)
            return total_time
    elif config == 'definer-split-definer':
        definer_time = extractPhaseTime('Definer', example, 'definer-split-cslicer-definer')
        split_time = extractPhaseTime('Split', example, config)
        definer2_time = extractPhaseTime('Definer2', example, config)
        if 'TIME OUT' in [definer_time, split_time, definer2_time]:
            return 'TO'
        else:
            total_time = float(definer_time) + float(split_time) + float(definer2_time)
            #return '{:.2f}'.format(total_time)
            return total_time
    elif config == 'cslicer-definer-definer':
        cslicer_time = extractPhaseTime('CSlicer', example, 'cslicer-definer-split-cslicer')
        definer_time = extractPhaseTime('Definer', example, config)
        definer2_time = extractPhaseTime('Definer2', example, config)
        if 'TIME OUT' in [cslicer_time, definer_time, definer2_time]:
            return 'TO'
        else:
            total_time = float(cslicer_time) + float(definer_time) + float(definer2_time)
            #return '{:.2f}'.format(total_time)
            return total_time
    elif config == 'definer-cslicer-definer':
        definer_time = extractPhaseTime('Definer', example, 'definer-cslicer-split-definer')
        cslicer_time = extractPhaseTime('CSlicer', example, 'definer-cslicer-split-definer')
        definer2_time = extractPhaseTime('Definer2', example, config)
        if 'TIME OUT' in [definer_time, cslicer_time, definer2_time]:
            return 'TO'
        else:
            total_time = float(definer_time) + float(cslicer_time) + float(definer2_time)
            #return '{:.2f}'.format(total_time)
            return total_time
    elif config == 'split-definer-definer':
        split_time = extractPhaseTime('Split', example, 'split-definer-cslicer-definer')
        definer_time = extractPhaseTime('Definer', example, 'split-definer-cslicer-definer')
        definer2_time = extractPhaseTime('Definer2', example, config)
        if 'TIME OUT' in [split_time, definer_time, definer2_time]:
            return 'TO'
        else:
            total_time = float(split_time) + float(definer_time) + float(definer2_time)
            #return '{:.2f}'.format(total_time)
            return total_time
    elif config == 'definer-definer':
        definer_time = extractPhaseTime('Definer', example, 'definer-split-cslicer-definer')
        definer2_time = extractPhaseTime('Definer2', example, config)
        if 'TIME OUT' in [definer_time, definer2_time]:
            return 'TO'
        else:
            total_time = float(definer_time) + float(definer2_time)
            #return '{:.2f}'.format(total_time)
            return total_time

# --- deprecated ---
@with_goto
def genEndToEndTimeNumbers_Old(examples=runex.examples, \
                           end_to_end_time_numbers_tex_file=END_TO_END_TIME_NUMBERS_TEX_FILE):
    lines = ''
    data_dict = genDataDictFromDataNumbersTex()
    all_examples_total_cached_time = []
    all_examples_total_suffix_time = []
    all_examples_total_time = []
    all_examples_cached_saving_percentage = []
    all_examples_suffix_saving_percentage = []
    # CDSC->(CDS)D->SCDD->DSCD->(C)SDD->(D)CSD->(S)DCD->(DS)D->(CD)D->(DC)D->(SD)D->(D)D
    for example in examples:
        # prefix-cache
        cdsc_cached_time = float(extractPhaseTime('Total', example, \
                                                  'cslicer-definer-split-cslicer'))
        cdsd_cached_time = float(extractPhaseTime('Definer2', example, \
                                                  'cslicer-definer-split-definer'))
        scdd_cached_time = float(extractPhaseTime('Total', example, \
                                                  'cslicer-definer-split-definer'))
        dscd_cached_time = float(extractPhaseTime('Total', example, \
                                                  'definer-split-cslicer-definer'))
        csdd_cached_time = float(extractPhaseTime('Split', example, \
                                                  'cslicer-split-definer-definer')) \
                         + float(extractPhaseTime('Definer', example, \
                                                  'cslicer-split-definer-definer')) \
                         + float(extractPhaseTime('Definer2', example, \
                                                  'cslicer-split-definer-definer'))
        dcsd_cached_time = float(extractPhaseTime('CSlicer', example, \
                                                  'definer-cslicer-split-definer')) \
                         + float(extractPhaseTime('Split', example, \
                                                  'definer-cslicer-split-definer')) \
                         + float(extractPhaseTime('Definer2', example, \
                                                  'definer-cslicer-split-definer'))
        sdcd_cached_time = float(extractPhaseTime('Definer', example, \
                                                  'split-definer-cslicer-definer')) \
                         + float(extractPhaseTime('CSlicer', example, \
                                                  'split-definer-cslicer-definer')) \
                         + float(extractPhaseTime('Definer2', example, \
                                                  'split-definer-cslicer-definer'))
        dsd_cached_time = float(extractPhaseTime('Definer2', example, 'definer-split-definer'))
        cdd_cached_time = float(extractPhaseTime('Definer2', example, 'cslicer-definer-definer'))
        dcd_cached_time = float(extractPhaseTime('Definer2', example, 'definer-cslicer-definer'))
        sdd_cached_time = float(extractPhaseTime('Definer2', example, 'split-definer-definer'))
        dd_cached_time = float(extractPhaseTime('Definer2', example, 'definer-definer'))
        total_cached_time = cdsc_cached_time + cdsd_cached_time + scdd_cached_time + \
                            dscd_cached_time + csdd_cached_time + dcsd_cached_time + \
                            sdcd_cached_time + dsd_cached_time + cdd_cached_time + \
                            dcd_cached_time + sdd_cached_time + dd_cached_time
        lines += '\\DefMacro{' + example.replace('-', '') + 'CachedEndToEndTime}{' + \
                 '{0:.2f}'.format(total_cached_time) + '}\n'
        all_examples_total_cached_time.append(total_cached_time)
        if example != 'CSV-159':
            total_suffix_time = 0
            goto .noop
        # suffix-cache + state-matching
        cdsc_suffix_time = float(extractPhaseTime('Total', example, \
                                                  'cslicer-definer-split-cslicer'))
        cdsd_suffix_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-definer-split-cslicer')) \
                         + float(extractPhaseTime('Definer', example, \
                                                  'cslicer-definer-split-cslicer')) \
                         + float(extractPhaseTime('Split', example, \
                                                  'cslicer-definer-split-cslicer')) \
                         + float(extractPhaseTime('Definer2', example, \
                                                  'cslicer-definer-split-definer'))
        scdd_suffix_time = float(extractPhaseTime('Total', example, \
                                                  'cslicer-definer-split-definer'))
        dscd_suffix_time = float(extractPhaseTime('Total', example, \
                                                  'definer-split-cslicer-definer'))
        csdd_suffix_time = float(extractPhaseTime('CSlicer', example, \
                                                  'cslicer-split-definer-definer')) \
                         + float(extractPhaseTime('Split', example, \
                                                  'cslicer-split-definer-definer')) \
                         + float(extractPhaseTime('Definer', example, \
                                                  'cslicer-split-definer-definer'))
        dcsd_suffix_time = float(extractPhaseTime('Definer', example, \
                                                  'definer-cslicer-split-definer')) \
                         + float(extractPhaseTime('CSlicer', example, \
                                                  'definer-cslicer-split-definer'))
        sdcd_suffix_time = float(extractPhaseTime('Split', example, \
                                                  'split-definer-cslicer-definer')) \
                         + float(extractPhaseTime('Definer', example, \
                                                  'split-definer-cslicer-definer')) \
                         + float(extractPhaseTime('CSlicer', example, \
                                                  'split-definer-cslicer-definer'))
        dsd_suffix_time = float(extractPhaseTime('Definer', example, \
                                                 'definer-split-cslicer-definer'))
        cdd_suffix_time = float(extractPhaseTime('CSlicer', example, \
                                                 'cslicer-definer-split-cslicer')) \
                        + float(extractPhaseTime('Definer', example, \
                                                 'cslicer-definer-definer')) \
                        + float(extractPhaseTime('Definer2', example, \
                                                 'cslicer-definer-definer'))
        dcd_suffix_time = float(extractPhaseTime('Definer', example, \
                                                 'definer-cslicer-split-definer')) \
                        + float(extractPhaseTime('CSlicer', example, \
                                                 'definer-cslicer-split-definer'))
        sdd_suffix_time = float(extractPhaseTime('Split', example, \
                                                 'split-cslicer-definer-definer')) \
                        + float(extractPhaseTime('Definer', example, \
                                                 'split-definer-cslicer-definer'))
        dd_suffix_time = float(extractPhaseTime('Definer', example, \
                                                'definer-split-cslicer-definer'))
        total_suffix_time = cdsc_suffix_time + cdsd_suffix_time + scdd_suffix_time + \
                            dscd_suffix_time + csdd_suffix_time + dcsd_suffix_time + \
                            sdcd_suffix_time + dsd_suffix_time + cdd_suffix_time + \
                            dcd_suffix_time + sdd_suffix_time + dd_suffix_time
        label .noop
        lines += '\\DefMacro{' + example.replace('-', '') + 'SuffixEndToEndTime}{' + \
                 '{0:.2f}'.format(total_suffix_time) + '}\n'
        all_examples_total_suffix_time.append(total_suffix_time)
        # no optimization
        cdsc_time = float(data_dict[example]['cslicer-definer-split-cslicer']['runtime'])
        cdsd_time = float(data_dict[example]['cslicer-definer-split-definer']['runtime'])
        scdd_time = float(data_dict[example]['split-cslicer-definer-definer']['runtime'])
        dscd_time = float(data_dict[example]['definer-split-cslicer-definer']['runtime'])
        csdd_time = float(data_dict[example]['cslicer-split-definer-definer']['runtime'])
        dcsd_time = float(data_dict[example]['definer-cslicer-split-definer']['runtime'])
        sdcd_time = float(data_dict[example]['split-definer-cslicer-definer']['runtime'])
        dsd_time = float(data_dict[example]['definer-split-definer']['runtime'])
        cdd_time = float(data_dict[example]['cslicer-definer-definer']['runtime'])
        dcd_time = float(data_dict[example]['definer-cslicer-definer']['runtime'])
        sdd_time = float(data_dict[example]['split-definer-definer']['runtime'])
        dd_time = float(data_dict[example]['definer-definer']['runtime'])
        total_time = cdsc_time + cdsd_time + scdd_time + dscd_time + csdd_time + dcsd_time + \
                     sdcd_time + dsd_time + cdd_time + dcd_time + sdd_time + dd_time
        lines += '\\DefMacro{' + example.replace('-', '') + 'EndToEndTime}{' + \
                 '{0:.2f}'.format(total_time) + '}\n'
        all_examples_total_time.append(total_time)
        #
        cache_saving_percentage = (total_time - total_cached_time) / total_time * 100
        lines += '\\DefMacro{' + example.replace('-', '') + 'CacheSavingPercentage}{' + \
                 '{0:.2f}'.format(cache_saving_percentage) + '\%}\n'
        all_examples_cached_saving_percentage.append(cache_saving_percentage)
        #
        suffix_saving_percentage = (total_time - total_suffix_time) / total_time * 100
        lines += '\\DefMacro{' + example.replace('-', '') + 'SuffixSavingPercentage}{' + \
                 '{0:.2f}'.format(suffix_saving_percentage) + '\%}\n'
        all_examples_suffix_saving_percentage.append(suffix_saving_percentage)
    avg_cached_total_time = sum(all_examples_total_cached_time) / \
                            len(all_examples_total_cached_time)
    avg_total_time = sum(all_examples_total_time) / len(all_examples_total_time)
    avg_cached_saving_percentage = sum(all_examples_cached_saving_percentage) / \
                            len(all_examples_cached_saving_percentage)
    avg_suffix_total_time = sum(all_examples_total_suffix_time) / \
                            len(all_examples_total_suffix_time)
    avg_suffix_saving_percentage = sum(all_examples_suffix_saving_percentage) / \
                            len(all_examples_suffix_saving_percentage)
    lines += '\\DefMacro{AvgCachedEndToEndTime}{' + \
             '{0:.2f}'.format(avg_cached_total_time) + '}\n'
    lines += '\\DefMacro{AvgEndToEndTime}{' + '{0:.2f}'.format(avg_total_time) + '}\n'
    lines += '\\DefMacro{AvgCacheSavingPercentage}{' + \
             '{0:.2f}'.format(avg_cached_saving_percentage) + '\%}\n'
    lines += '\\DefMacro{AvgSuffixEndToEndTime}{' + \
             '{0:.2f}'.format(avg_suffix_total_time) + '}\n'
    lines += '\\DefMacro{AvgSuffixSavingPercentage}{' + \
             '{0:.2f}'.format(avg_suffix_saving_percentage) + '\%}\n'
    fw = open(end_to_end_time_numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

# --- deprecated ---
def genDataDictFromDataNumbersTex(examples=runex.examples, configs=configs, \
                                  data_numbers_tex_file=TREE_NUMBERS_TEX_FILE):
    fr = open(data_numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    data_dict = collections.OrderedDict({})
    for example in examples:
        data_dict[example] = collections.OrderedDict({})
        example_id = example.replace('-', '')
        for config in configs:
            data_dict[example][config] = collections.OrderedDict({})
            config_id = config.replace('-', '')
            for i in range(len(lines)):
                if lines[i].startswith('\\DefMacro{' + example_id + config_id + \
                                       'NumOfCommitsMapBack'):
                    data_dict[example][config]['num_of_commits_map_back'] = \
                                                    lines[i].split('{')[2].split('}')[0]
                elif lines[i].startswith('\\DefMacro{' + example_id + config_id + \
                                         'NumOfChangedLines'):
                    data_dict[example][config]['num_of_changed_lines_map_back'] = \
                                                    lines[i].split('{')[2].split('}')[0]
                elif lines[i].startswith('\\DefMacro{' + example_id + config_id + \
                                         'RunTime'):
                    if lines[i].split('{')[2].split('}')[0] == 'TO':
                        data_dict[example][config]['runtime'] = '7200'
                    else:
                        data_dict[example][config]['runtime'] = \
                                                        lines[i].split('{')[2].split('}')[0]
                elif lines[i].startswith('\\DefMacro{' + example_id + config_id + \
                                         'CommitsReduction'):
                    data_dict[example][config]['commits_reduction'] = \
                                                            lines[i].split('{')[2].split('\%')[0]
                elif lines[i].startswith('\\DefMacro{' + example_id + config_id + \
                                         'ChangedLinesReduction'):
                    data_dict[example][config]['changed_lines_reduction'] = \
                                                        lines[i].split('{')[2].split('\%')[0]
    return data_dict
