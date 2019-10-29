#!/usr/bin/env python3

import os
import os.path
import sys
import csv
import time
import json
import argparse
import shutil
import collections
import subprocess as sub
from goto import with_goto

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
DOWNLOADS_DIR = SCRIPT_DIR + '/../../_downloads'

CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/orig-configs'
SPLIT_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/split-configs'
DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/definer-configs'
SPLIT_CSLICER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/split-cslicer'
SPLIT_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/split-definer'
CSLICER_SPLIT_CSLICER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-split-cslicer'
CSLICER_SPLIT_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-split-definer'
DEFINER_SPLIT_CSLICER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer-split-cslicer'
DEFINER_SPLIT_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer-split-definer'
CSLICER_DEFINER_SPLIT_CSLICER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-definer-split-cslicer'
CSLICER_DEFINER_SPLIT_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-definer-split-definer'
CSLICER_STANDALONE_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer'
DEFINER_STANDALONE_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer'
CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-definer'
SPLIT_CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/split-cslicer-definer'
SPLIT_CSLICER_DEFINER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/split-cslicer-definer-definer'
CSLICER_SPLIT_DEFINER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-split-definer-definer'
DEFINER_SPLIT_CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer-split-cslicer-definer'
DEFINER_CSLICER_SPLIT_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer-cslicer-split-definer'
SPLIT_DEFINER_CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/split-definer-cslicer-definer'
CSLICER_DEFINER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/cslicer-definer-definer'
DEFINER_CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer-cslicer-definer'
SPLIT_DEFINER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/split-definer-definer'
DEFINER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/configs/definer-definer'
CSLICER_DEFINER_SPLIT_CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + \
                    '/../../resources/file-level/configs/cslicer-definer-split-cslicer-definer'
CSLICER_SPLIT_DEFINER_CSLICER_DEFINER_CONFIGS_DIR = SCRIPT_DIR + \
                    '/../../resources/file-level/configs/cslicer-split-definer-cslicer-definer'

POMS_DIR = SCRIPT_DIR + '/../../resources/file-level/example-poms'
CSLICER_SPLIT_CSLICER_SECOND_PHASE_POM_DIR = SCRIPT_DIR + '/../../resources/file-level/second-phase-poms'

JACOCOS_DIR = SCRIPT_DIR + '/../../resources/file-level/jacoco-files'

CSLICER_ORIG_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/cslicer-orig-output'
CSLICER_SPLIT_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/cslicer-split-output'
OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output'
SPLIT_CSLICER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/split-cslicer'
SPLIT_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/split-definer'
CSLICER_SPLIT_CSLICER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/cslicer-split-cslicer'
CSLICER_SPLIT_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/cslicer-split-definer'
DEFINER_SPLIT_CSLICER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-split-cslicer'
DEFINER_SPLIT_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-split-definer'
CSLICER_DEFINER_SPLIT_CSLICER_OUTPUT_DIR = SCRIPT_DIR + \
                            '/../../resources/file-level/output/cslicer-definer-split-cslicer'
CSLICER_DEFINER_SPLIT_DEFINER_OUTPUT_DIR = SCRIPT_DIR + \
                            '/../../resources/file-level/output/cslicer-definer-split-definer'
CSLICER_STANDALONE_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/cslicer'
DEFINER_STANDALONE_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer'
CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/cslicer-definer'
SPLIT_CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/split-cslicer-definer'
SPLIT_CSLICER_DEFINER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/split-cslicer-definer-definer'
CSLICER_SPLIT_DEFINER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/cslicer-split-definer-definer'
DEFINER_SPLIT_CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-split-cslicer-definer'
DEFINER_CSLICER_SPLIT_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-cslicer-split-definer'
SPLIT_DEFINER_CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/split-definer-cslicer-definer'
CSLICER_DEFINER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/cslicer-definer-definer'
DEFINER_CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-cslicer-definer'
SPLIT_DEFINER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/split-definer-definer'
DEFINER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-definer'
CSLICER_DEFINER_SPLIT_CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + \
                    '/../../resources/file-level/output/cslicer-definer-split-cslicer-definer'
CSLICER_SPLIT_DEFINER_CSLICER_DEFINER_OUTPUT_DIR = SCRIPT_DIR + \
                    '/../../resources/file-level/output/cslicer-split-definer-cslicer-definer'
# For true minimal exp
DEFINER_WITH_MEMORY_STANDALONE_OUTPUT_DIR = SCRIPT_DIR + \
                                        '/../../resources/file-level/output/definer-with-memory'
# For asej exp 2
LEARNING_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-learning'
BASIC_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-basic'
# For asej exp 3
NEG_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-neg'
NOPOS_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-nopos'
LOW3_OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output/definer-low3'

TEMP_LOGS_DIR = SCRIPT_DIR + '/../../resources/file-level/temp-logs'
TEMP_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/temp-configs'
TEMP_FILES_DIR = SCRIPT_DIR + '/../../resources/file-level/temp-files'

REPOS_BACKUP_DIR = SCRIPT_DIR + '/../../resources/file-level/_repos'
SPLIT_LOGS_DIR = SCRIPT_DIR + '/../../resources/file-level/_split_logs'
VALIDATE_LOGS_DIR = SCRIPT_DIR + '/../../resources/file-level/_validate_logs'
CSLICER_JAR_PATH = SCRIPT_DIR + '/../../target/cslicer-1.0.0-jar-with-dependencies.jar'
NUMBERS_TEX_PATH = SCRIPT_DIR + '/../../resources/file-level/results/tables/examples-numbers.tex'
TABLE_TEX_PATH = SCRIPT_DIR + '/../../resources/file-level/results/tables/examples-table.tex'
TIME_TABLE_TEX_PATH = SCRIPT_DIR + '/../../resources/file-level/results/tables/time-table.tex'
AST_LINES_TABLE_TEX_PATH = SCRIPT_DIR + '/../../resources/file-level/results/tables/ast-lines-table.tex'
DOSC_META_DIR = SCRIPT_DIR + '/../../../DoSC/meta-data'
DEMO_RESULTS_DIR = SCRIPT_DIR + '/../../../demo/_results/definer'
DEFINER_PROGRESS_DIR = SCRIPT_DIR + '/../../resources/file-level/results/plots'

TEST_CLASSES_BACKUP_DIR = SCRIPT_DIR + '/../../resources/file-level/_test_classes_backup'

TOUCH_SET_DIR = SCRIPT_DIR + '/../../resources/file-level/touchset'

CACHED_REPOS_DIR = SCRIPT_DIR + '/../../resources/file-level/cached-repos' # ISSTA 19
GLOBAL_PHASE_TIME_TABLE_FILE = SCRIPT_DIR + '/../../resources/file-level/global-phase-time.txt' # ISSTA 19
SUFFIX_SHARING_CACHE_DIR = SCRIPT_DIR + '/../../resources/file-level/suffix-cache' # ISSTA 19
# ISSTA 19
ORIG_HISTORY_DIR = SCRIPT_DIR + '/../../resources/file-level/orig-history'
# ISSTA 19
SPLIT_TEMP_FILE = '/tmp/split.tmp'

# examples = ['LANG-883', 'LANG-993', 'LANG-1006', 'LANG-1080', 'LANG-1093',
#             'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
#             'COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
#             'CSV-159', 'CSV-175', 'CSV-180', 'NET-525', 'NET-527']

# examples = ['IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
#             'CSV-159', 'CSV-175', 'CSV-180',
#             'NET-525', 'NET-527']

# examples = ['LANG-883', 'LANG-993', 'LANG-1006', 'LANG-1080', 'LANG-1093',
#             'COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375']

# FSE 19
# examples = ['LANG-993', 'LANG-1006',
#             'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
#             'COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
#             'CSV-159', 'CSV-175', 'CSV-180', 'NET-527']

# ASE 19
definer_optimal_examples = ['COMPRESS-375', 'CONFIGURATION-626', 'CSV-159', 'FLUME-2628',
                            'IO-275', 'IO-288', 'PDFBOX-3069', 'PDFBOX-3307', 'PDFBOX-3418']

# DOSC
# examples = ['MNG-4904', 'MNG-4909', 'MNG-4910', 'FLUME-2056',
#             'FLUME-2130', 'FLUME-2206', 'PDFBOX-3069', 'PDFBOX-3418',
#             'PDFBOX-3461', 'CONFIGURATION-624', 'CONFIGURATION-626']

# examples = ['CONFIGURATION-466', 'PDFBOX-3262', 'PDFBOX-3307', 'CSV-179', 'NET-436',
#             'CALCITE-718', 'CALCITE-758', 'CALCITE-811', 'CALCITE-803', 'CALCITE-767',
#             'FLUME-2498', 'FLUME-2628']

# ASE 19 final list
# bug: compress-374 not same
# bug: pdfbox-3461 not same
examples = ['COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
            'CONFIGURATION-624', 'CONFIGURATION-626',
            'CSV-159', 'CSV-175', 'CSV-179', 'CSV-180',
            'FLUME-2628',
            'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
            'LANG-993', 'LANG-1006',
            'MNG-4904', 'MNG-4909', 'MNG-4910',
            'NET-436', 'NET-525', 'NET-527',
            'PDFBOX-3069', 'PDFBOX-3418', 'PDFBOX-3307']

#examples = ['COMPRESS-374']

def parseArgs(argv):
    '''
    Parse the args of the script.
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument('--clean-prefix-cache', help='Clean cached repos', \
                        action='store_true', required=False) # ISSTA 19
    parser.add_argument('--clean-suffix-cache', help='Clean cached suffix', \
                        action='store_true', required=False) # ISSTA 19
    parser.add_argument('--share-prefix', help='Enable prefix sharing', \
                        action='store_true', required=False) # ISSTA 19
    parser.add_argument('--share-suffix', help='Enable suffix sharing', \
                        action='store_true', required=False) # ISSTA 19
    parser.add_argument('--clean-touchset', help='Clean touch set', \
                        action='store_true', required=False)
    parser.add_argument('--split-cslicer', help='Run split then cslicer', \
                        action='store_true', required=False)
    parser.add_argument('--split-definer', help='Run split then definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-split-cslicer', help='Run cslicer then split then cslicer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-split-definer', help='Run cslicer then split then definer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-split-cslicer', help='Run definer then split then cslicer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-split-definer', help='Run definer then split then definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-definer-split-cslicer', \
                        help='Run cslicer then definer then split then cslicer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-definer-split-definer', \
                        help='Run cslicer then definer then split then definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer', help='Run cslicer standalone', \
                        action='store_true', required=False)
    parser.add_argument('--definer', help='Run definer standalone', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-definer', help='Run cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--split-cslicer-definer', help='Run split cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--split-cslicer-definer-definer', \
                        help='Run split cslicer definer definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-split-definer-definer', \
                        help='Run cslicer split definer definer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-split-cslicer-definer', \
                        help='Run definer split cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-cslicer-split-definer', \
                        help='Run definer cslicer split definer', \
                        action='store_true', required=False)
    parser.add_argument('--split-definer-cslicer-definer', \
                        help='Run split definer cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-definer-definer', help='Run cslicer definer definer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-cslicer-definer', help='Run definer cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--split-definer-definer', help='Run split definer definer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-definer', help='Run definer definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-definer-split-cslicer-definer', \
                        help='Run cslicer definer split cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--cslicer-split-definer-cslicer-definer', \
                        help='Run cslicer split definer cslicer definer', \
                        action='store_true', required=False)
    parser.add_argument('--definer-with-memory', help='Run memorized definer', \
                        action='store_true', required=False) # for true minimal exp
    parser.add_argument('--definer-asej-exp', help='Run definer asej exp', \
                        required=False) # for asej exp
    parser.add_argument('--split-cslicer-one', help='Run split then cslicer', \
                        required=False)
    parser.add_argument('--split-definer-one', help='Run split then definer', \
                        required=False)
    parser.add_argument('--cslicer-split-cslicer-one', \
                        help='Run cslicer then split then cslicer', \
                        required=False)
    parser.add_argument('--cslicer-split-definer-one', \
                        help='Run cslicer then split then definer', \
                        required=False)
    parser.add_argument('--definer-split-cslicer-one', \
                        help='Run definer then split then cslicer', \
                        required=False)
    parser.add_argument('--definer-split-definer-one', \
                        help='Run definer then split then definer', \
                        required=False)
    parser.add_argument('--cslicer-definer-split-cslicer-one', \
                        help='Run cslicer then definer then split then cslicer', \
                        required=False)
    parser.add_argument('--cslicer-definer-split-definer-one', \
                        help='Run cslicer then definer then split then definer', \
                        required=False)
    parser.add_argument('--cslicer-one', help='Run cslicer standalone', \
                        required=False)
    parser.add_argument('--definer-one', help='Run definer standalone', \
                        required=False)
    parser.add_argument('--cslicer-definer-one', help='Run cslicer definer', \
                        required=False)
    parser.add_argument('--split-cslicer-definer-one', help='Run split cslicer definer', \
                        required=False)
    parser.add_argument('--split-cslicer-definer-definer-one', \
                        help='Run split cslicer definer definer', \
                        required=False)
    parser.add_argument('--cslicer-split-definer-definer-one', \
                        help='Run cslicer split definer definer', \
                        required=False)
    parser.add_argument('--definer-split-cslicer-definer-one', \
                        help='Run definer split cslicer definer', \
                        required=False)
    parser.add_argument('--definer-cslicer-split-definer-one', \
                        help='Run definer cslicer split definer', \
                        required=False)
    parser.add_argument('--split-definer-cslicer-definer-one', \
                        help='Run split definer cslicer definer', \
                        required=False)
    parser.add_argument('--cslicer-definer-definer-one', help='Run cslicer definer definer', \
                        required=False)
    parser.add_argument('--definer-cslicer-definer-one', help='Run definer cslicer definer', \
                        required=False)
    parser.add_argument('--split-definer-definer-one', help='Run split definer definer', \
                        required=False)
    parser.add_argument('--definer-definer-one', help='Run definer definer', \
                        required=False)
    parser.add_argument('--cslicer-definer-split-cslicer-definer-one', \
                        help='Run cslicer definer split cslicer definer', \
                        required=False)
    parser.add_argument('--cslicer-split-definer-cslicer-definer-one', \
                        help='Run cslicer split definer cslicer definer', \
                        required=False)
    parser.add_argument('--definer-with-memory-one', help='Run memorized definer', \
                        action='store_true', required=False) # for true minimal exp
    parser.add_argument('--definer-asej-exp-one', help='Run definer asej exp one', \
                        required=False) # for asej exp
    parser.add_argument('--example', help='Specify the example to run', \
                        required=False) # for asej exp

    if (len(argv) == 0):
        parser.print_help()
        exit(1)
    opts = parser.parse_args(argv)
    return opts

def searchFile(dir_root, file_name):
    for dir_path, subpaths, files in os.walk(dir_root):
        for f in files:
            if f == file_name:
                return dir_path + '/' + f
    return None

def replacePomSurefireVersions(example, repo_path, new_pom_file):
    '''
    update pom file to use a newer surefire version to support the "mvn test # +" format
    '''
    if example.startswith('PDFBOX'):
        pom_path = repo_path + '/pdfbox/pom.xml'
    else:
        pom_path = repo_path + '/pom.xml' # single module projects
    shutil.copyfile(new_pom_file, pom_path)
    # insert argLine for all the submodules
    if example.startswith('MNG') or example.startswith('CALCITE') or example.startswith('FLUME'):
        poms = findAllPomsInDir(repo_path)
        for pom in poms:
            if '/src/test' not in pom:
                insertArgsInOnePom(pom)

def findAllPomsInDir(target_dir):
    poms = []
    for dir_path, subpaths, files in os.walk(target_dir):
        for f in files:
            if f == 'pom.xml':
                poms.append(dir_path + '/' + f)
    return poms

def insertArgsInOnePom(pom):
    fr = open(pom, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if '<artifactId>maven-surefire-plugin</artifactId>' in lines[i]:
            for j in range(i, len(lines)):
                if '</plugin>' in lines[j]:
                    break
            for k in range(i, j):
                if '<argLine>' in lines[k]:
                    lines[k] = lines[k].replace('</argLine>', ' ${argLine}</argLine>')
    fw = open(pom, 'w')
    fw.write(''.join(lines))
    fw.close()

def extractInfoFromCSlicerConfigs(example):
    '''
    read start commit, end commit, repo, and test suite
    '''
    # find the config file
    config_file = searchFile(CONFIGS_DIR, example + '.properties')
    if config_file == None:
        print ('Cannot find config file!')
        exit(0)
    fr = open(config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('startCommit'):
            start = lines[i].strip().split()[-1]
        elif lines[i].startswith('endCommit'):
            end = lines[i].strip().split()[-1]
        elif lines[i].startswith('repoPath'):
            repo_name = lines[i].split('/')[-2]
        elif lines[i].startswith('testScope'):
            test_suite = lines[i].strip().split()[-1]
    repo_path = DOWNLOADS_DIR + '/' + repo_name
    #print (start, end, repo_name, test_suite, repo_path)
    return start, end, repo_name, test_suite, repo_path, lines, config_file

def extractInfoFromDefinerConfigs(example):
    # find the config file
    config_file = searchFile(DEFINER_CONFIGS_DIR, example + '.properties')
    if config_file == None:
        print ('Cannot find config file!')
        exit(0)
    fr = open(config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('startCommit'):
            start = lines[i].strip().split()[-1]
        elif lines[i].startswith('endCommit'):
            end = lines[i].strip().split()[-1]
        elif lines[i].startswith('repoPath'):
            repo_name = lines[i].split('/')[-2]
        elif lines[i].startswith('buildScriptPath'):
            build_script_path = lines[i].strip().split()[-1]
        elif lines[i].startswith('testScope'):
            test_suite = lines[i].strip().split()[-1]
    repo_path = DOWNLOADS_DIR + '/' + repo_name
    #print (start, end, repo_name, test_suite, repo_path)
    return start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file

def updateCSlicerConfig(example, end, dst_dir):
    # find the config file
    config_file = searchFile(CONFIGS_DIR, example + '.properties')
    if config_file == None:
        print ('Cannot find config file!')
        exit(0)
    fr = open(config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('endCommit'):
            lines[i] = ' '.join(lines[i].split()[:-1]) + ' ' + end + '\n'
    updated_config_file = dst_dir + '/' + example + '.properties'
    fw = open(updated_config_file, 'w')
    fw.write(''.join(lines))
    fw.close()
    return updated_config_file

def updateDefinerConfig(example, end, dst_dir):
    # find the config file
    config_file = searchFile(DEFINER_CONFIGS_DIR, example + '.properties')
    if config_file == None:
        print ('Cannot find config file!')
        exit(0)
    fr = open(config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('endCommit'):
            lines[i] = ' '.join(lines[i].split()[:-1]) + ' ' + end + '\n'
    updated_config_file = dst_dir + '/' + example + '.properties'
    fw = open(updated_config_file, 'w')
    fw.write(''.join(lines))
    fw.close()
    return updated_config_file

def runTestsGenJacoco(example, end, repo_path, test_suite, poms_dir=POMS_DIR):
    # run mvn test at the end commit, generate jacoco
    os.chdir(repo_path)
    sub.run('git checkout ' + end + ' -b orig', shell=True)
    new_pom_file = searchFile(poms_dir, example + '.pom.xml')
    replacePomSurefireVersions(example, repo_path, new_pom_file)
    sub.run('mvn install -DskipTests', shell=True, \
            stdout=open(os.devnull, 'w'), stderr=open(os.devnull, 'w'))
    # multimodule
    submodule_path = getSubModulePathForAGivenProject(example)
    os.chdir(repo_path + submodule_path)
    sub.run('mvn test -Dtest=' + test_suite, shell=True)
    # save jacoco file for analysis
    target_path = getTargetPathForAGivenProject(example)
    jacoco_path = repo_path + target_path + '/jacoco.exec'
    shutil.move(jacoco_path, JACOCOS_DIR + '/' + example + '-jacoco.exec')
    os.chdir(repo_path)

def runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite, poms_dir=POMS_DIR):
    os.chdir(repo_path)
    # delete definerorig branch if already exist
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    print ('delete definerorig branch')
    sub.run('git br -D definerorig', shell=True)
    # create definerorig branch
    sub.run('git checkout ' + end + ' -b definerorig', shell=True)
    new_pom_file = searchFile(poms_dir, example + '.pom.xml')
    replacePomSurefireVersions(example, repo_path, new_pom_file)
    sub.run('mvn install -DskipTests', shell=True, \
            stdout=open(os.devnull, 'w'), stderr=open(os.devnull, 'w'))
    # multi-module projects
    submodule_path = getSubModulePathForAGivenProject(example)
    os.chdir(repo_path + submodule_path)
    if example == 'PDFBOX-3262':
        preprocessPDFBOX3262(repo_path) # Only for PDFBOX-3262
    sub.run('mvn test -Dtest=' + test_suite, shell=True)
    os.chdir(repo_path)
    # copy target/test-classes to temp dir
    if os.path.isdir(TEST_CLASSES_BACKUP_DIR + '/test-classes'):
        shutil.rmtree(TEST_CLASSES_BACKUP_DIR + '/test-classes')
    # multi-module projects
    target_path = getTargetPathForAGivenProject(example)
    test_classes_path = repo_path + target_path + '/test-classes'
    shutil.copytree(test_classes_path, TEST_CLASSES_BACKUP_DIR + '/test-classes')
    # stash changes on pom
    sub.run('git stash', shell=True)

# ASE 2019
def preprocessPDFBOX3262(repo_path):
    test_file = searchFile(repo_path, 'PDAcroFormFlattenTest.java')
    fr = open(test_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if 'public void testFlattenPDFBOX3262() throws IOException' in lines[i]:
            lines[i-1] = lines[i-1].replace('// @Test', '@Test')
    fw = open(test_file, 'w')
    fw.write(''.join(lines))
    fw.close()

def splitCommitsByFile(example, repo_path, start, end, branch='filelevel'):
    # split commits by file, create separate branches
    # measure the overhead of splitting
    split_commits_start_time = time.time()
    os.chdir(SCRIPT_DIR)
    print ('===> Splitting ...')
    sub.run('python3 split_commits.py --repo ' + repo_path + \
            ' --start ' + start + \
            ' --end ' + end + \
            ' --branch ' + branch, shell=True, \
            stdout=open(SPLIT_LOGS_DIR + '/' + example + '.logs', 'w'),
            stderr=sub.STDOUT)
    split_commits_end_time = time.time()
    split_commits_overhead = split_commits_end_time - split_commits_start_time
    # write the time into split logs
    fr = open(SPLIT_LOGS_DIR + '/' + example + '.logs', 'r', encoding = 'ISO-8859-1')
    split_lines = fr.readlines()
    fr.close()
    split_lines.append(str(split_commits_overhead))
    fw = open(SPLIT_LOGS_DIR + '/' + example + '.logs', 'w')
    fw.write(''.join(split_lines))
    fw.close()

def genSplittedConfigFile(example, repo_path, lines, configs_dir, branch='filelevel'):
    # get the sha of splitted end commit, create config files
    os.chdir(repo_path)
    sub.run('git checkout ' + branch, shell=True)
    p = sub.Popen('git --no-pager log --oneline -1', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    file_level_end_commit = p.stdout.readlines()[0].decode("utf-8").split()[0]
    for i in range(len(lines)):
        if lines[i].startswith('endCommit'):
            lines[i] = ' '.join(lines[i].split()[:-1]) + ' ' + file_level_end_commit + '\n'
    split_config_file = configs_dir + '/' + example + '.split.properties'
    fw = open(split_config_file, 'w')
    fw.write(''.join(lines))
    fw.close()
    return split_config_file

def runCSlicerTool(cslicer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    sub.run('java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
            ' -e slicer', shell=True, \
            stdout=open(cslicer_log, 'w'), stderr=sub.STDOUT)

def runDefinerTool(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    if 'CONFIGURATION-466' in config_file:
        p = sub.run('timeout 14400 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                    ' -e refiner -l noinv -q', shell=True, stdout=fw, stderr=fw)
    else:
        p = sub.run('timeout 14400 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                    ' -e refiner -l noinv', shell=True, stdout=fw, stderr=fw)
    # try:
    #     p.wait(timeout=10) # 30 min
    # except sub.TimeoutExpired:
    #     print('Definer time out!')
    #     p.terminate()
    #     return

# For true minimal exp
def runDefinerToolWithMemory(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                  ' -e srr -l noinv', shell=True, stdout=fw, stderr=fw)

# for asej exp2
def runDefinerToolLearning(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                  ' -e refiner -l noinv,nocomp', shell=True, stdout=fw, stderr=fw)

# for asej exp2
def runDefinerToolBasic(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                ' -e refiner -l noinv,nocomp,nolearn', shell=True, stdout=fw, stderr=fw)
    # p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
    #              ' -e delta', shell=True, stdout=fw, stderr=fw)

# for asej exp3
def runDefinerToolNeg(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                  ' -e refiner -l noinv,nocomp,neg', shell=True, stdout=fw, stderr=fw)

# for asej exp3
def runDefinerToolNoPos(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                  ' -e refiner -l noinv,nocomp,nopos', shell=True, stdout=fw, stderr=fw)

# for asej exp3
def runDefinerToolLow3(definer_log, config_file, branch):
    sub.run('git checkout ' + branch, shell=True)
    fw = open(definer_log, 'w')
    p = sub.run('timeout 7200 java -jar ' + CSLICER_JAR_PATH + ' -c ' + config_file + \
                  ' -e refiner -l noinv,nocomp,low3', shell=True, stdout=fw, stderr=fw)

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

def applyHistorySlice(repo_path, start, history_slice, commit_msg_list, branch_name):
    cwd = os.getcwd()
    os.chdir(repo_path)
    sub.run('git checkout ' + start + ' -b ' + branch_name, shell=True)
    # print ('===> Applying History Slice ...')
    for i in range(len(history_slice)):
        commit = history_slice[i]
        commit_msg = commit_msg_list[i].replace('\"', '')
        # print ('Applying commit: ' + commit + ' ' + commit_msg)
        # drop changes on src/test
        sub.run('git cherry-pick -n ' + commit, shell=True, stdout=open(os.devnull, 'w'), \
                stderr=sub.STDOUT)
        p = sub.Popen('git status', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
        p.wait()
        lines = p.stdout.readlines()
        for i in range(len(lines)):
            lines[i] = lines[i].decode("utf-8")[:-1]
            if ('modified: ' in lines[i] and 'src/test/' in lines[i]) or \
               ('both modified: ' in lines[i] and  'src/test/' in lines[i]) or \
               ('deleted by us: ' in lines[i] and  'src/test/' in lines[i]) or \
               ('added by us: ' in lines[i] and  'src/test/' in lines[i]) or \
               ('both deleted: ' in lines[i] and  'src/test/' in lines[i]) or \
               ('added by them: ' in lines[i] and  'src/test/' in lines[i]):
                file_path = lines[i].strip().split()[-1]
                sub.run('git reset ' + file_path, shell=True, stdout=open(os.devnull, 'w'), \
                stderr=sub.STDOUT)
                sub.run('git checkout -- ' + file_path, shell=True, \
                        stdout=open(os.devnull, 'w'), stderr=sub.STDOUT)
                sub.run('git rm ' + file_path, shell=True, \
                        stdout=open(os.devnull, 'w'), stderr=sub.STDOUT)
            if 'both modified: ' in lines[i] and  'src/main/' in lines[i]:
                file_path = lines[i].strip().split()[-1]
                resolveConflict(file_path)
                sub.run('git add ' + file_path, shell=True, stdout=open(os.devnull, 'w'), \
                        stderr=sub.STDOUT)
        # configuration: target dir not ignored
        if repo_path.endswith('commons-configuration'):
            os.system('rm -rf target')
        os.system('find -name test -type d | xargs rm -rf')
        os.system('git checkout .')

        # untracked files
        # p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
        #               stdout=sub.PIPE, stderr=sub.PIPE)
        # p.wait()
        # lines = p.stdout.readlines()
        # for i in range(len(lines)):
        #     lines[i] = lines[i].decode("utf-8")[:-1]
        #     if 'src/test' in lines[i]:
        #         os.remove(lines[i].strip())

        sub.run('git commit -m \"' + commit_msg + '\"', shell=True, \
                stdout=open(os.devnull, 'w'), stderr=sub.STDOUT)

    # get the new end commit
    p = sub.Popen('git --no-pager log --oneline -1', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    end_commit = p.stdout.readlines()[0].decode("utf-8").split()[0]
    os.chdir(cwd)
    return end_commit

# ASE 19
def resolveConflict(file_path):
    fr = open(file_path, 'r')
    lines = fr.readlines()
    fr.close()
    new_lines = ''
    i = 0
    while i < len(lines):
        if '<<<<<<< HEAD' in lines[i].strip():
            for j in range(i, len(lines)):
                if '=======' in lines[j].strip():
                    i = j+1
                    break
            continue
        if '>>>>>>>' in lines[i].strip():
            i += 1
            continue
        new_lines += lines[i]
        i +=1
    fw = open(file_path, 'w')
    fw.write(new_lines)
    fw.close()

def countChangedLines(log_file, repo, tool):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    if tool == 'cslicer':
        commits, _ = extractHistorySliceFromCSlicerLog(log_file)
    elif tool == 'definer':
        commits, _ = extractHistorySliceFromDefinerLog(log_file)
    elif tool == 'split':
        commits, _ = extractHistorySliceFromSplitLog(log_file)
    total_num_of_insertions = 0
    total_num_of_deletions = 0
    os.chdir(repo)
    for sha in commits:
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
    lines.append('Total Changed Lines: ' + str(total_num_of_edits) + '\n')
    fw = open(log_file, 'w')
    fw.write(''.join(lines))
    fw.close()

# CZ: old, upgrade to insertTimeDictinLog() in the future
def putTimeinLog(log_file, run_time):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    lines.append('Total Run Time: ' + str(run_time) + '\n')
    fw = open(log_file, 'w')
    fw.write(''.join(lines))
    fw.close()

def insertTimeDictinLog(log_file, time_dict):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for key in time_dict:
        lines += key + ': ' + str(time_dict[key]) + '\n'
    fw = open(log_file, 'w')
    fw.write(''.join(lines))
    fw.close()

# ISSTA 19
def insertTimeDictInGlobalTable(example, config, time_dict, table=GLOBAL_PHASE_TIME_TABLE_FILE):
    fr = open(table, 'r')
    global_dict = dict(json.load(fr))
    fr.close()
    #print (global_dict)
    if example not in global_dict.keys():
        example_dict = collections.OrderedDict({})
        global_dict[example] = example_dict
    else:
        example_dict = global_dict[example]
    if config in example_dict.keys():
        example_dict[config] = time_dict
    else:
        example_dict[config] = time_dict
    #print (global_dict)
    fw = open(table, 'w')
    json.dump(global_dict, fw)
    fw.close()

def backupRepoForDebugging(example, repo_path):
    if os.path.isdir(REPOS_BACKUP_DIR + '/' + example + '-repo'):
        shutil.rmtree(REPOS_BACKUP_DIR + '/' + example + '-repo')
    sub.run('cp -r ' + repo_path + ' ' + REPOS_BACKUP_DIR + '/' + example + '-repo', shell=True)

def cleanTempLogs():
    if os.path.isdir(TEMP_LOGS_DIR):
        shutil.rmtree(TEMP_LOGS_DIR)
    os.makedirs(TEMP_LOGS_DIR)
    if os.path.isdir(TEMP_CONFIGS_DIR):
        shutil.rmtree(TEMP_CONFIGS_DIR)
    os.makedirs(TEMP_CONFIGS_DIR)
    if os.path.isdir(TEMP_FILES_DIR):
        shutil.rmtree(TEMP_FILES_DIR)
    os.makedirs(TEMP_FILES_DIR)

def cleanRepoAfterDefinerTimeout(repo_path):
    cwd = os.getcwd()
    os.chdir(repo_path)
    print ('After definer timeout, clean')
    # remove git lock file
    if os.path.isfile(repo_path + '/.git/index.lock'):
        os.remove(repo_path + '/.git/index.lock')
    sub.run('git stash', shell=True)
    os.chdir(cwd)

def cleanTouchSet():
    # Remove old touchset
    if os.path.isdir(TOUCH_SET_DIR):
        print ('Clean touch set')
        shutil.rmtree(TOUCH_SET_DIR)
    os.makedirs(TOUCH_SET_DIR)

# ISSTA 19
def isPrefixRepoCached(example, config, cached_repos_dir=CACHED_REPOS_DIR):
    if not os.path.isdir(cached_repos_dir  + '/' + example):
        return False
    for repo in os.listdir(cached_repos_dir + '/' + example):
        if repo == config:
            return True
    return False

# ISSTA 19
def cachePrefixRepoIfNotAlreadyCached(example, config, repo_path, \
                                            cached_repos_dir=CACHED_REPOS_DIR):
    # if already cached, do nothing
    if isPrefixRepoCached(example, config):
        return
    # cache the repo
    example_dir = cached_repos_dir + '/' + example
    #shutil.copytree(repo_path, example_dir + '/' + config)
    if not os.path.isdir(example_dir):
        os.makedirs(example_dir)
    sub.run('cp -r ' + repo_path + ' ' + example_dir + '/' + config, shell=True)

# ISSTA 19
def getEndSHAFrombranch(example, config, branch, cached_repos_dir=CACHED_REPOS_DIR):
    os.chdir(cached_repos_dir + '/' + example + '/' + config)
    p = sub.Popen('git log ' + branch + ' -1', shell=True, stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    end_sha = lines[0].decode("utf-8")[:-1].split()[1][:7]
    return end_sha

# ISSTA 19
def isSuffixExist(suffix, example, suffix_sharing_cache_dir=SUFFIX_SHARING_CACHE_DIR):
    suffix_dir = suffix_sharing_cache_dir + '/' + example + '/' + suffix
    if os.path.isdir(suffix_dir):
        return True
    else:
        return False

# ISSTA 19
def isCSlicerLog(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('[STATS] test.count : '):
            return True
    return False

# ISSTA 19
def isDefinerLog(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('[STATS] hstar.length : '):
            return True
    return False

# ISSTA 19
def isSplitLog(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('[AFTER SPLIT] '):
            return True
    return False

# ISSTA 19
def isCommitLevel(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('TEST: '):
            if len(lines[i].split(' : ')[1].split()) == 1:
                continue
            if lines[i].split(' : ')[1].startswith('[') and \
               lines[i].split(' : ')[1].split()[-2].endswith(']'):
                return False
        if lines[i].startswith('[OUTPUT] H*: '):
            if len(lines[i].split(' : ')[1].split()) == 1:
                continue
            if lines[i].split(' : ')[1].startswith('[') and \
               lines[i].split(' : ')[1].split()[-2].endswith(']'):
                return False
        if lines[i].startswith('[AFTER SPLIT] '):
            return False
    return True

# ISSTA 19
def isFileLevel(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('TEST: '):
            if len(lines[i].split(' : ')[1].split()) == 1:
                continue
            if lines[i].split(' : ')[1].startswith('[') and \
               lines[i].split(' : ')[1].split()[-2].endswith(']'):
                return True
        if lines[i].startswith('[OUTPUT] H*: '):
            if len(lines[i].split(' : ')[1].split()) == 1:
                continue
            if lines[i].split(' : ')[1].startswith('[') and \
               lines[i].split(' : ')[1].split()[-2].endswith(']'):
                return True
        if lines[i].startswith('[AFTER SPLIT] '):
            return True
    return False

# ISSTA 19
# May change to hash implementation later
def extractSliceFromCommitLevelLog(log_file):
    if isCSlicerLog(log_file):
        cmt_msgs = extractHistorySliceFromCSlicerLog(log_file)[1]
    elif isDefinerLog(log_file):
        cmt_msgs = extractHistorySliceFromDefinerLog(log_file)[1]
    else: # orig hist
        cmt_msgs = extractHistorySliceFromOrigHistory(log_file)[1]
    for i in range(len(cmt_msgs)):
        cmt_msgs[i] = cmt_msgs[i].replace('\"', '')
    return cmt_msgs

# ASE 19
# May change to hash implementation later
def extractSliceSHAsAndMsgsFromCommitLevelLog(example, log_file):
    if isCSlicerLog(log_file):
        shas, cmt_msgs = extractHistorySliceFromCSlicerLog(log_file)
    elif isDefinerLog(log_file):
        shas, cmt_msgs = extractHistorySliceFromDefinerLog(log_file)
    else: # orig hist
        shas, cmt_msgs = extractHistorySliceFromOrigHistory(log_file)
    for i in range(len(cmt_msgs)):
        cmt_msgs[i] = cmt_msgs[i].replace('\"', '')
    return shas, cmt_msgs

# ISSTA 19
# May change to hash implementation later
def extractSliceFromFileLevelLog(log_file):
    if isCSlicerLog(log_file):
        cmt_msgs = extractHistorySliceFromCSlicerLog(log_file)[1]
    elif isDefinerLog(log_file):
        cmt_msgs = extractHistorySliceFromDefinerLog(log_file)[1]
    elif isSplitLog(log_file):
        cmt_msgs = extractHistorySliceFromSplitLog(log_file)[1]
    for i in range(len(cmt_msgs)):
        msg = cmt_msgs[i]
        last_bracket_idx = ' '.join(msg.split()[1:]).rfind(']')
        msg_without_sha = (' '.join(msg.split()[1:]))[:last_bracket_idx] + \
                          (' '.join(msg.split()[1:]))[last_bracket_idx+1:]
        msg_without_sha = msg_without_sha.replace('\"', '')
        cmt_msgs[i] = msg_without_sha
    return cmt_msgs

# ASE 19
# May change to hash implementation later
def extractSliceSHAsAndMsgsFromFileLevelLog(example, log_file):
    if isCSlicerLog(log_file):
        shas, cmt_msgs = extractHistorySliceFromCSlicerLog(log_file)
    elif isDefinerLog(log_file):
        shas, cmt_msgs = extractHistorySliceFromDefinerLog(log_file)
    elif isSplitLog(log_file):
        shas, cmt_msgs = extractHistorySliceFromSplitLog(log_file)
    for i in range(len(cmt_msgs)):
        msg = cmt_msgs[i]
        f = msg.strip().split()[-1]
        files.append(f)
        last_bracket_idx = ' '.join(msg.split()[1:]).rfind(']')
        msg_without_sha = (' '.join(msg.split()[1:]))[:last_bracket_idx] + \
                          (' '.join(msg.split()[1:]))[last_bracket_idx+1:]
        msg_without_sha = msg_without_sha.replace('\"', '')
        cmt_msgs[i] = msg_without_sha
    return shas, cmt_msgs

# ASE 19
def searchSHAFromOrigHistUsingCmtMsgs(example, cmt_msg, orig_history_dir=ORIG_HISTORY_DIR):
    print (example, cmt_msg)
    orig_hist_file = orig_history_dir + '/' + example + '.hist'
    fr = open(orig_hist_file, 'r')
    lines = fr.readlines()
    fr.close()
    cadidate_shas = []
    for i in range(len(lines)):
        if cmt_msg.replace(' ', '').replace('`ZipArchiveEntry`', '') in \
           lines[i].replace('\"', '').replace(' ', '').replace('`ZipArchiveEntry`', ''):
            sha = lines[i].split()[0]
            cadidate_shas.append(sha)
    return cadidate_shas

# ISSTA 19
def getOriginalHistory(start, end, repo_path): # include end, exclude start
    os.chdir(repo_path)
    p = sub.Popen('git --no-pager log ' + start + '..' + end + ' --oneline', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    commits = p.stdout.readlines()
    orig_history = []
    for commit in commits:
        sha = commit.decode("utf-8")[:-1].strip().split(' ')[0]
        #print (sha)
        p = sub.Popen('git --no-pager log --oneline ' + sha + ' -1', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
        p.wait()
        commit_messages = p.stdout.readlines()
        msg = ''
        for msg_line in commit_messages:
            msg_line = msg_line.decode("utf-8")[:-1]
        msg += msg_line
        orig_history.append(msg)
    return orig_history

# ISSTA 19
def isStateMatch(current_log, suffix, example, \
                 start=None, end=None, repo_path=None, \
                 suffix_sharing_cache_dir=SUFFIX_SHARING_CACHE_DIR):
    cached_configs = os.listdir(suffix_sharing_cache_dir + '/' + example + '/' + suffix)
    for config in cached_configs:
        cached_log = suffix_sharing_cache_dir + '/' + example + '/' + suffix + '/' + \
                     config + '/states.log'
        # see if entire config can be saved
        if current_log == None:
            current_slice = [' '.join(cmt.split()[1:]).replace('\"', '') for cmt in \
                             getOriginalHistory(start, end, repo_path)]
            if isCommitLevel(cached_log):
                cached_slice = extractSliceFromCommitLevelLog(cached_log)
            elif isFileLevel(cached_log):
                cached_slice = extractSliceFromFileLevelLog(cached_log)
            if current_slice == cached_slice:
                return True, config.replace('savedby-', '')
            else:
                return False, None
        if isCommitLevel(current_log) and isFileLevel(cached_log):
            continue
        if isFileLevel(current_log) and isCommitLevel(cached_log):
            continue
        if isCommitLevel(current_log) and isCommitLevel(cached_log):
            current_slice = extractSliceFromCommitLevelLog(current_log)
            cached_slice = extractSliceFromCommitLevelLog(cached_log)
            if current_slice == cached_slice:
                return True, config.replace('savedby-', '')
            else:
                continue
        if isFileLevel(current_log) and isFileLevel(cached_log):
            current_slice = extractSliceFromFileLevelLog(current_log)
            cached_slice = extractSliceFromFileLevelLog(cached_log)
            if current_slice == cached_slice:
                return True, config.replace('savedby-', '')
            else:
                continue
    return False, None

# ISSTA 19
def copyTheSliceFromOneConfigLogToFinalLog(config, example, dest_log, output_dir=OUTPUT_DIR):
    # find out saved by which config, then copy the slice and time of that config.
    config_which_saving_cache = config
    source_log = output_dir + '/' + config_which_saving_cache + '/' + example + '.log'
    slice_lines = ''
    slice_lines += 'COPIED FROM: ' + config_which_saving_cache.replace('savedby-', '') + '\n'
    fr = open(source_log)
    lines = fr.readlines()
    fr.close()
    if isCSlicerLog(source_log):
        for i in range(len(lines)):
            if lines[i].startswith('[OUTPUT] Results:'):
                for j in range(i, len(lines)):
                    if ' Exec Time]: ' in lines[j]: # do not copy exec time
                        continue
                    slice_lines += lines[j]
                break
    elif isDefinerLog(source_log):
        for i in range(len(lines)):
            if lines[i].startswith('[OUTPUT] H*: '):
                for j in range(i-1, len(lines)):
                    if ' Exec Time]: ' in lines[j]: # do not copy exec time
                        continue
                    slice_lines += lines[j]
                break
    fw = open(dest_log, 'w')
    fw.write(slice_lines)
    fw.close()

# ISSTA 19
def genSplitLogFile(example, config, start, repo_path, branch, \
                    split_temp_file=SPLIT_TEMP_FILE, output_dir=OUTPUT_DIR):
    cwd = os.getcwd()
    os.chdir(repo_path)
    p = sub.Popen('git --no-pager log ' + branch + ' --oneline -1', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    file_level_end_commit = p.stdout.readlines()[0].decode("utf-8").split()[0]
    end = file_level_end_commit
    sub.run('git --no-pager log ' + start + '..' + end + ' --oneline', shell=True, \
            stdout=open(split_temp_file, 'w'), stderr=sub.STDOUT)
    fr = open(split_temp_file, 'r')
    commits = fr.readlines()
    fr.close()
    for i in range(len(commits)):
        cmt = commits[i]
        commits[i] = '[AFTER SPLIT] : ' + cmt
    # write the splitted history to file
    split_log = output_dir + '/' + config + '/' + example + '.log.split'
    fw = open(split_log, 'w')
    fw.write(''.join(commits))
    fw.close()
    os.chdir(cwd)
    return split_log

# ISSTA 19
def extractHistorySliceFromSplitLog(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    shas = []
    msgs = []
    for i in range(len(lines)):
        if lines[i].startswith('[AFTER SPLIT] : '):
            sha = lines[i].split(' : ')[1].split()[0]
            msg = lines[i].split(' : ')[1].split()[1]
            shas.append(sha)
            msgs.append(msg)
    return shas, msgs

# ISSTA 19
def extractHistorySliceFromOrigHistory(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    shas = []
    msgs = []
    for i in range(len(lines)):
        if not lines[i].startswith('CACHED BY:'):
            sha = lines[i].split()[0]
            msg = ' '.join(lines[i].split()[1:])
            shas.append(sha)
            msgs.append(msg)
    return shas, msgs

# ISSTA 19
def cacheSuffixIfNotAlreadyCached(example, config, suffix, log_file, \
                                  suffix_sharing_cache_dir=SUFFIX_SHARING_CACHE_DIR):
    suffix_dir = suffix_sharing_cache_dir + '/' + example + '/' + suffix + '/' + 'savedby-' + \
                 config
    if os.path.isdir(suffix_dir):
        return
    os.makedirs(suffix_dir)
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    lines.insert(0, 'CACHED BY: ' + config + '\n')
    fw = open(suffix_dir + '/states.log', 'w')
    fw.write(''.join(lines))
    fw.close()

# ASE 19
def getSubModulePathForAGivenProject(example):
    if example in ['CALCITE-627', 'CALCITE-758', 'CALCITE-811', 'CALCITE-803', 'CALCITE-991',
                   'CALCITE-1288', 'CALCITE-1309']:
        submodule_path = '/core'
    elif example in ['CALCITE-655', 'CALCITE-718']:
        submodule_path = '/avatica-server'
    elif example in ['CALCITE-767']:
        submodule_path = '/avatica'
    elif example in ['MNG-4904', 'MNG-4910', 'MNG-5530', 'MNG-5549']:
        submodule_path = '/maven-core'
    elif example in ['MNG-4909']:
        submodule_path = '/maven-model-builder'
    elif example in ['FLUME-2052', 'FLUME-2056', 'FLUME-2130', 'FLUME-2628', 'FLUME-2982']:
        submodule_path = '/flume-ng-core'
    elif example in ['FLUME-2206']:
        submodule_path = '/flume-ng-sinks/flume-ng-elasticsearch-sink'
    elif example in ['FLUME-2498', 'FLUME-2955']:
        submodule_path = '/flume-ng-sources/flume-taildir-source'
    elif example in ['FLUME-1710']:
        submodule_path = '/flume-ng-sdk'
    elif example.startswith('PDFBOX'):
        submodule_path = '/pdfbox'
    else:
        submodule_path = '' # single-module project
    return submodule_path

# ASE 19
def getTargetPathForAGivenProject(example):
    submodule_path = getSubModulePathForAGivenProject(example)
    return submodule_path + '/target'

# ASE 19
def cacheTargetDirForCSlicer2(example, repo_path, cached_repos_dir=CACHED_REPOS_DIR):
    target_path = getTargetPathForAGivenProject(example)
    if isPrefixRepoCached(example, 'target'):
        shutil.rmtree(cached_repos_dir + '/' + example + '/target')
    shutil.copytree(repo_path + target_path, cached_repos_dir + '/' + example + '/target')

# ASE 19
def copyTargetDirBackForCSlicer2(example, repo_path, cached_repos_dir=CACHED_REPOS_DIR):
    target_path = getTargetPathForAGivenProject(example)
    if os.path.isdir(repo_path + target_path):
        shutil.rmtree(repo_path + target_path)
    shutil.copytree(cached_repos_dir + '/' + example + '/target', repo_path + target_path)

def runCSlicerStandalone(example):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from cslicer orig config file
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    cslicer_orig_log = CSLICER_STANDALONE_OUTPUT_DIR + '/' + example + '.log'
    runCSlicerTool(cslicer_orig_log, config_file, 'orig')
    # -------------------------------- cslicer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(cslicer_orig_log, run_time)
    countChangedLines(cslicer_orig_log, repo_path, 'cslicer')
    #backupRepoForDebugging(example, repo_path)

def runDefinerStandalone(example):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                    extractInfoFromDefinerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = DEFINER_STANDALONE_OUTPUT_DIR + '/' + example + '.log'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # -------------------------------- definer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(definer_log, run_time)
    countChangedLines(definer_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)

def runSplitCSlicer(example):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from cslicer orig config file
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end)
    # generate new config files for splitted history
    split_config_file = genSplittedConfigFile(example, repo_path, lines, \
                                                  SPLIT_CSLICER_CONFIGS_DIR)
    # run cslicer on splitted history, save logs
    cslicer_split_log = SPLIT_CSLICER_OUTPUT_DIR + '/' + example + '.log'
    runCSlicerTool(cslicer_split_log, split_config_file, 'filelevel')
    # -------------------------------- cslicer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(cslicer_split_log, run_time)
    countChangedLines(cslicer_split_log, repo_path, 'cslicer')
    #backupRepoForDebugging(example, repo_path)

@with_goto
def runSplitDefiner(example, share_prefix, share_suffix, orig_history_dir=ORIG_HISTORY_DIR, \
                    cached_repos_dir=CACHED_REPOS_DIR, output_dir=SPLIT_DEFINER_OUTPUT_DIR, \
                    configs_dir=SPLIT_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'split'):
        is_run_from_cache  =True
        split_end_time = start_time
        split_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .split
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer', example):
            is_match, matched_config = isStateMatch(None, 'split-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='split-definer', suffix='split-definer', \
                                  log_file=orig_history_file)
    label .split
    # -------------------------------- split start -------------------------------------
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-split')
    # cache intermediate repo after split (S)
    cachePrefixRepoIfNotAlreadyCached(example, 'split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='split-definer', start=start, \
                                repo_path=repo_path, branch='after-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-definer', suffix='definer', \
                                  log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-split')
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log'
    runDefinerTool(definer_log, split_config_file, 'after-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                            'after-split-definer')
    # cache intermediate repo after split-definer (SD)
    cachePrefixRepoIfNotAlreadyCached(example, 'split-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - split_end_time
    final_log = definer_log
    # -------------------------------- definer end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'split-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)

def runCSlicerSplitCSlicer(example, regenerate=False):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from cslicer orig config file
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on original history, save temp logs
    cslicer_temp_log = CSLICER_SPLIT_CSLICER_OUTPUT_DIR + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # -------------------------------- cslicer end -------------------------------------
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'aftercslicer')
    ## --- re-generate jacoco at the new end commit of phase 1
    if regenerate:
        # shutil.copyfile(repo_path + '/pom.xml', \
        #         CSLICER_SPLIT_CSLICER_SECOND_PHASE_POM_DIR + '/' + example + '.pom.xml')
        runTestsGenJacoco(example, end, repo_path, test_suite, \
                          poms_dir=CSLICER_SPLIT_CSLICER_SECOND_PHASE_POM_DIR)
    ## ---
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end)
    # generate new config files for splitted history
    split_config_file = \
            genSplittedConfigFile(example, repo_path, lines, CSLICER_SPLIT_CSLICER_CONFIGS_DIR)
    # run cslicer on splitted history, save logs
    cslicer_split_log = CSLICER_SPLIT_CSLICER_OUTPUT_DIR + '/' + example + '.log'
    runCSlicerTool(cslicer_split_log, split_config_file, 'filelevel')
    # -------------------------------- cslicer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(cslicer_split_log, run_time)
    countChangedLines(cslicer_split_log, repo_path, 'cslicer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runCSlicerSplitDefiner(example, share_prefix, share_suffix, \
                           orig_history_dir=ORIG_HISTORY_DIR, \
                           cached_repos_dir=CACHED_REPOS_DIR, \
                           output_dir=CSLICER_SPLIT_DEFINER_OUTPUT_DIR, \
                           configs_dir=CSLICER_SPLIT_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-split', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .split
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-split-definer', example):
            is_match, matched_config = isStateMatch(None, 'cslicer-split-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer', \
                                  suffix='cslicer-split-definer', log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on original history, save temp logs
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, 'split-definer', example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer', \
                                  suffix='split-definer', log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-cslicer-split')
    # cache intermediate repo after cslicer-split (CS)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='cslicer-split-definer', start=start, \
                                repo_path=repo_path, branch='after-cslicer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer', suffix='definer', \
                                  log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-cslicer-split')
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log'
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, split_config_file, 'after-cslicer-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                            'after-cslicer-split-definer')
    # cache intermediate repo after cslicer-definer-split-definer (CSD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - split_end_time
    final_log = definer_log
    # -------------------------------- definer end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-split-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

def runDefinerSplitCSlicer(example):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = DEFINER_SPLIT_CSLICER_OUTPUT_DIR + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # -------------------------------- definer end -------------------------------------
    # extract history slice from definer log
    definer_history_slice, commit_msg_list = \
                                         extractHistorySliceFromDefinerLog(definer_log)
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                                'afterdefiner')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end)
    # generate new config files for splitted history
    _, end, _, test_suite, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    split_config_file = \
            genSplittedConfigFile(example, repo_path, lines, DEFINER_SPLIT_CSLICER_CONFIGS_DIR)
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on split history, save logs
    cslicer_log = DEFINER_SPLIT_CSLICER_OUTPUT_DIR + '/' + example + '.log'
    runCSlicerTool(cslicer_log, split_config_file, 'filelevel')
    # -------------------------------- cslicer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(cslicer_log, run_time)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runDefinerSplitDefiner(example, share_prefix, share_suffix, \
                           cached_repos_dir=CACHED_REPOS_DIR, \
                           orig_history_dir=ORIG_HISTORY_DIR, \
                           output_dir=DEFINER_SPLIT_DEFINER_OUTPUT_DIR, \
                           configs_dir=DEFINER_SPLIT_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if prefix cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'definer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer-split', repo_path)
    elif isPrefixRepoCached(example, 'definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer', repo_path)
    else:
        # no cached repo, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'definer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        goto .split
    else: # prefix cache not exist
        is_run_from_cache = False
        goto .definer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-split-definer', example):
            is_match, matched_config = isStateMatch(None, 'definer-split-definer', example, \
                                            start=start, end=end, repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-definer', \
                                  suffix='definer-split-definer', log_file=orig_history_file)
    label .definer
    # -------------------------------- definer start -------------------------------------
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        split_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                            'after-definer')
    # cache prefix: intermediate repo after definer (D)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'split-definer', example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-definer', \
                                  suffix='split-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer', branch='after-definer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-definer-split')
    # cache intermediate repo after definer-split (DS)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='definer-split-definer', start=start, \
                                repo_path=repo_path, branch='after-definer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-definer', suffix='definer', \
                                  log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-definer-split')
    definer_log = output_dir + '/' + example + '.log'
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # Run definer on splitted history
    runDefinerTool(definer_log, split_config_file, 'after-definer-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                            'after-definer-split-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-split-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - split_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'definer-split-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

def runCSlicerDefiner(example):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from cslicer orig config file
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on original history, save temp logs
    if os.path.isdir(TEMP_FILES_DIR + '/target'):
        shutil.rmtree(TEMP_FILES_DIR + '/target')
    shutil.copytree(repo_path + '/target', TEMP_FILES_DIR + '/target') # copy target dir
    cslicer_temp_log = CSLICER_DEFINER_OUTPUT_DIR + '/' \
                           + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # -------------------------------- cslicer end -------------------------------------
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'aftercslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, TEMP_CONFIGS_DIR)
    definer_log = CSLICER_DEFINER_OUTPUT_DIR + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # CZ: the following part might be not needed. Need test.
    # move all untracked test files to temp dir (for running jacoco needed) -----------
    p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        if lines[i].startswith('src/test/'):
            dir_structure = '/'.join(lines[i].strip().split('/')[:-1])
            dest_dir = TEMP_FILES_DIR + '/' + dir_structure
            if os.path.isdir(dest_dir):
                shutil.rmtree(dest_dir)
            os.makedirs(dest_dir)
            shutil.move(lines[i].strip(), dest_dir)
            #os.remove(lines[i].strip())
    # -------------------------------------------------------------------------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'aftercslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # -------------------------------- definer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(definer_log, run_time)
    countChangedLines(definer_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runSplitCSlicerDefiner(example, share_prefix, share_suffix, \
                           orig_history_dir=ORIG_HISTORY_DIR, \
                           cached_repos_dir=CACHED_REPOS_DIR, \
                           output_dir=SPLIT_CSLICER_DEFINER_OUTPUT_DIR, \
                           configs_dir=SPLIT_CSLICER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'split-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split-cslicer', repo_path)
    elif isPrefixRepoCached(example, 'split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'split-cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        split_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        goto .definer
    elif isPrefixRepoCached(example, 'split'):
        is_run_from_cache  =True
        split_end_time = start_time
        split_exec_time = 'NOT RUN'
        goto .cslicer
    else: # cache not exist
        is_run_from_cache = False
        goto .split
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-cslicer-definer', example):
            is_match, matched_config = isStateMatch(None, 'split-cslicer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer', \
                                  suffix='split-cslicer-definer', \
                                  log_file=orig_history_file)
    label .split
    # -------------------------------- split start -------------------------------------
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-split')
    # cache intermediate repo after cslicer-definer-split (CDS)
    cachePrefixRepoIfNotAlreadyCached(example, 'split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='split-cslicer-definer', start=start, \
                                repo_path=repo_path, branch='after-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer', \
                                  suffix='cslicer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split', branch='after-split')
    # generate new config files for splitted history
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-split')
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on splitted history, save logs
    cslicer_split_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_split_log, split_config_file, 'after-split')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_split_log)
    # for NET-525, NET-527 (how to do in split level?)
    # if example == 'NET-525' or example == 'NET-527':
    #     cslicer_history_slice.append('4379a681')
    #     commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-split-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(cslicer_split_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer', suffix='definer', \
                                  log_file=cslicer_split_log)
    countChangedLines(cslicer_split_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split-cslicer', branch='after-split-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-split-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-split-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-cslicer-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - cslicer_end_time
    final_log = definer_log
    # -------------------------------- definer end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'split-cslicer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runCSlicerDefinerSplitCSlicer(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=CSLICER_DEFINER_SPLIT_CSLICER_OUTPUT_DIR, \
                                  configs_dir=CSLICER_DEFINER_SPLIT_CSLICER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # remove the old repo in _downloads dir
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-definer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer-split', \
                        repo_path)
    elif isPrefixRepoCached(example, 'cslicer-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-definer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .cslicer2
    elif isPrefixRepoCached(example, 'cslicer-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .split
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer-split-cslicer', example):
            is_match, matched_config = isStateMatch(None, 'cslicer-definer-split-cslicer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer', \
                                  suffix='cslicer-definer-split-cslicer', \
                                  log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # copy target dir because we will run CSlicer again later
    # cache target dir, otherwise we cannot run CSlicer2 in the middle
    cacheTargetDirForCSlicer2(example, repo_path)
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-split-cslicer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, 'definer-split-cslicer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer', \
                                  suffix='definer-split-cslicer', log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # temp definer config file (CZ: we may change in the future to keep all the temp files)
    definer_config_file = updateDefinerConfig(example, end, TEMP_CONFIGS_DIR)
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # move all untracked test files to temp dir (for running jacoco needed)-----------
    p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        if lines[i].startswith('src/test/'):
            dir_structure = '/'.join(lines[i].strip().split('/')[:-1])
            dest_dir = TEMP_FILES_DIR + '/' + dir_structure
            if os.path.isdir(dest_dir):
                shutil.rmtree(dest_dir)
            os.makedirs(dest_dir)
            shutil.move(lines[i].strip(), dest_dir)
            #os.remove(lines[i].strip())
    # -------------------------------------------------------------------------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        split_exec_time = 'NOT RUN'
        cslicer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                                'after-cslicer-definer')
    # cache intermediate repo after cslicer-definer (CD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-cslicer', example):
            is_match, matched_config = isStateMatch(definer_log, 'split-cslicer', example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer', \
                                  suffix='split-cslicer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-definer', \
                                  branch='after-cslicer-definer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-cslicer-definer-split')
    # cache intermediate repo after cslicer-definer-split (CDS)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='cslicer-definer-split-cslicer', start=start, \
                                repo_path=repo_path, branch='after-cslicer-definer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer', example):
            is_match, matched_config = isStateMatch(split_log, 'cslicer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer', \
                                  suffix='cslicer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .cslicer2
    # -------------------------------- cslicer2 start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-cslicer-definer-split')
    # move untracked files back
    os.chdir(repo_path)
    for dir_path, subpaths, files in os.walk(TEMP_FILES_DIR):
        for f in files:
            if '/src/test' in dir_path:
                shutil.copy(dir_path + '/' + f, \
                                repo_path + dir_path[dir_path.index('/src/test'):])
    # copy target dir back (required by CSlicer)
    copyTargetDirBackForCSlicer2(example, repo_path)
    # run cslicer on split history, save logs
    cslicer_log = output_dir + '/' + example + '.log'
    runCSlicerTool(cslicer_log, split_config_file, 'after-cslicer-definer-split')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                            'after-cslicer-definer-split-cslicer')
    # cache intermediate repo after cslicer (CDSC)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split-cslicer', repo_path)
    cslicer2_end_time = time.time()
    cslicer2_exec_time = cslicer2_end_time - split_end_time
    final_log = cslicer_log
    # -------------------------------- cslicer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[CSlicer2 Exec Time]'] = cslicer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-definer-split-cslicer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'cslicer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runCSlicerDefinerSplitDefiner(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=CSLICER_DEFINER_SPLIT_DEFINER_OUTPUT_DIR, \
                                  configs_dir=CSLICER_DEFINER_SPLIT_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # remove the old repo in _downloads dir
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-definer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer-split', \
                        repo_path)
    elif isPrefixRepoCached(example, 'cslicer-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-definer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'cslicer-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .split
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer-split-definer', example):
            is_match, matched_config = isStateMatch(None, 'cslicer-definer-split-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-definer', \
                                  suffix='cslicer-definer-split-definer', \
                                  log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-split-definer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, 'definer-split-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-definer', \
                                  suffix='definer-split-definer', log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # temp definer config file (CZ: we may change in the future to keep all the temp files)
    definer_config_file = updateDefinerConfig(example, end, TEMP_CONFIGS_DIR)
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # move all untracked test files to temp dir (for running jacoco needed)-----------
    p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        if lines[i].startswith('src/test/'):
            dir_structure = '/'.join(lines[i].strip().split('/')[:-1])
            dest_dir = TEMP_FILES_DIR + '/' + dir_structure
            if os.path.isdir(dest_dir):
                shutil.rmtree(dest_dir)
            os.makedirs(dest_dir)
            shutil.move(lines[i].strip(), dest_dir)
            #os.remove(lines[i].strip())
    # -------------------------------------------------------------------------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    definer_history_slice, commit_msg_list = \
                                         extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        split_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                                'after-cslicer-definer')
    # cache intermediate repo after cslicer-definer (CD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'split-definer', example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-definer', \
                                  suffix='split-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-definer', \
                                  branch='after-cslicer-definer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-cslicer-definer-split')
    # cache intermediate repo after cslicer-definer-split (CDS)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='cslicer-definer-split-definer', start=start, \
                                repo_path=repo_path, branch='after-cslicer-definer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-definer', \
                                  suffix='definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-cslicer-definer-split')
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log'
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, split_config_file, 'after-cslicer-definer-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch, reset start and end
    definer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                            'after-cslicer-definer-split-definer')
    # cache intermediate repo after cslicer-definer-split-definer (CDSD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - split_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-definer-split-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runSplitCSlicerDefinerDefiner(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=SPLIT_CSLICER_DEFINER_DEFINER_OUTPUT_DIR, \
                                  configs_dir=SPLIT_CSLICER_DEFINER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'split-cslicer-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split-cslicer-definer', \
                        repo_path)
    elif isPrefixRepoCached(example, 'split-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split-cslicer', repo_path)
    elif isPrefixRepoCached(example, 'split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'split-cslicer-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        split_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'split-cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        split_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        goto .definer
    elif isPrefixRepoCached(example, 'split'):
        is_run_from_cache  =True
        split_end_time = start_time
        split_exec_time = 'NOT RUN'
        goto .cslicer
    else: # cache not exist
        is_run_from_cache = False
        goto .split
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-cslicer-definer-definer', example):
            is_match, matched_config = isStateMatch(None, 'split-cslicer-definer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer-definer', \
                                  suffix='split-cslicer-definer-definer', \
                                  log_file=orig_history_file)
    label .split
    # -------------------------------- split start -------------------------------------
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-split')
    # cache intermediate repo after cslicer-definer-split (CDS)
    cachePrefixRepoIfNotAlreadyCached(example, 'split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='split-cslicer-definer-definer', start=start, \
                                repo_path=repo_path, branch='after-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'cslicer-definer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer-definer', \
                                  suffix='cslicer-definer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split', branch='after-split')
    # generate new config files for splitted history
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-split')
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on splitted history, save logs
    cslicer_split_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_split_log, split_config_file, 'after-split')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_split_log)
    # for NET-525, NET-527 (how to do in split level?)
    # if example == 'NET-525' or example == 'NET-527':
    #     cslicer_history_slice.append('4379a681')
    #     commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-split-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-definer', example):
            is_match, matched_config = isStateMatch(cslicer_split_log, 'definer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer-definer', \
                                  suffix='definer-definer', log_file=cslicer_split_log)
    countChangedLines(cslicer_split_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split-cslicer', branch='after-split-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-split-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-split-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-cslicer-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-cslicer-definer-definer', \
                                  suffix='definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split-cslicer-definer', \
                                  branch='after-split-cslicer-definer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-split-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-split-cslicer-definer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-cslicer-definer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - definer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'split-cslicer-definer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runCSlicerSplitDefinerDefiner(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=CSLICER_SPLIT_DEFINER_DEFINER_OUTPUT_DIR, \
                                  configs_dir=CSLICER_SPLIT_DEFINER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-split-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-split-definer', \
                        repo_path)
    elif isPrefixRepoCached(example, 'cslicer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-split', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-split-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'cslicer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .split
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-split-definer-definer', example):
            is_match, matched_config = isStateMatch(None, 'cslicer-split-definer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-definer', \
                                  suffix='cslicer-split-definer-definer', \
                                  log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on original history, save temp logs
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer-definer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, 'split-definer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-definer', \
                                  suffix='split-definer-definer', log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-cslicer-split')
    # cache intermediate repo after cslicer-split (CS)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='cslicer-split-definer-definer', start=start, \
                                repo_path=repo_path, branch='after-cslicer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer-definer', example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-definer', \
                                  suffix='definer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-cslicer-split')
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, split_config_file, 'after-cslicer-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                            'after-cslicer-split-definer')
    # cache intermediate repo after cslicer-definer-split-definer (CSD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-definer', \
                                  suffix='definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-split-definer', \
                                  branch='after-cslicer-split-definer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer-split-definer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-cslicer-split-definer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split-definer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - definer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-split-definer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runDefinerSplitCSlicerDefiner(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=DEFINER_SPLIT_CSLICER_DEFINER_OUTPUT_DIR, \
                                  configs_dir=DEFINER_SPLIT_CSLICER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'definer-split-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer-split-cslicer', \
                        repo_path)
    elif isPrefixRepoCached(example, 'definer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer-split', repo_path)
    elif isPrefixRepoCached(example, 'definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'definer-split-cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'definer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .cslicer
    elif isPrefixRepoCached(example, 'definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        goto .split
    else: # cache not exist
        is_run_from_cache = False
        goto .definer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-split-cslicer-definer', example):
            is_match, matched_config = isStateMatch(None, 'definer-split-cslicer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-cslicer-definer', \
                                  suffix='definer-split-cslicer-definer', \
                                  log_file=orig_history_file)
    label .definer
    # -------------------------------- definer start -------------------------------------
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        split_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, 'after-definer')
    # cache intermediate repo after definer (D)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-cslicer-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'split-cslicer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-cslicer-definer', \
                                  suffix='split-cslicer-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer', branch='after-definer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-definer-split')
    # cache intermediate repo after definer-split (DS)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='definer-split-cslicer-definer', start=start, \
                                repo_path=repo_path, branch='after-definer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-cslicer-definer', \
                                  suffix='cslicer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer-split', branch='after-definer-split')
    # generate new config files for splitted history
    _, end, _, test_suite, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    split_config_file = \
            genSplittedConfigFile(example, repo_path, lines, configs_dir, 'after-definer-split')
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on split history, save logs
    cslicer_log = output_dir + '/' + example + '.log.phase2'
    runCSlicerTool(cslicer_log, split_config_file, 'after-definer-split')
    history_slice, commit_msg_list = extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-split-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-split-cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(cslicer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-split-cslicer-definer', \
                                  suffix='definer', log_file=cslicer_log)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer-split-cslicer', \
                                  branch='after-definer-split-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-definer-split-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-split-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-split-cslicer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - cslicer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'definer-split-cslicer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runDefinerCSlicerSplitDefiner(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=DEFINER_CSLICER_SPLIT_DEFINER_OUTPUT_DIR, \
                                  configs_dir=DEFINER_CSLICER_SPLIT_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'definer-cslicer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer-cslicer-split', \
                        repo_path)
    elif isPrefixRepoCached(example, 'definer-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer-cslicer', repo_path)
    elif isPrefixRepoCached(example, 'definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'definer-cslicer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        definer_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'definer-cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        goto .split
    elif isPrefixRepoCached(example, 'definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        goto .cslicer
    else: # cache not exist
        is_run_from_cache = False
        goto .definer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-cslicer-split-definer', example):
            is_match, matched_config = isStateMatch(None, 'definer-cslicer-split-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-split-definer', \
                                  suffix='definer-cslicer-split-definer', \
                                  log_file=orig_history_file)
    label .definer
    # -------------------------------- definer start -------------------------------------
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, 'after-definer')
    # cache intermediate repo after definer (D)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-split-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'cslicer-split-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-split-definer', \
                                  suffix='cslicer-split-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer', branch='after-definer')
    # update cslicer config
    cslicer_config_file = updateCSlicerConfig(example, end, configs_dir)
    # run tests at the original end commit, generate jacoco files
    _, end, _, test_suite, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer and save logs
    cslicer_log = output_dir + '/' + example + '.log.phase2'
    runCSlicerTool(cslicer_log, cslicer_config_file, 'after-definer')
    history_slice, commit_msg_list = extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer', example):
            is_match, matched_config = isStateMatch(cslicer_log, 'split-definer', example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-split-definer', \
                                  suffix='split-definer', log_file=cslicer_log)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    # split commits by file
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer-cslicer', \
                                  branch='after-definer-cslicer')
    splitCommitsByFile(example, repo_path, start, end, 'after-definer-cslicer-split')
    # cache intermediate repo after definer-split (DCS)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-cslicer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='definer-cslicer-split-definer', start=start, \
                                repo_path=repo_path, branch='after-definer-cslicer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-split-definer', \
                                  suffix='definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-definer-cslicer-split')
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, split_config_file, 'after-definer-cslicer-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-cslicer-split-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-cslicer-split-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - split_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'definer-cslicer-split-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runSplitDefinerCSlicerDefiner(example, share_prefix, share_suffix, \
                                  orig_history_dir=ORIG_HISTORY_DIR, \
                                  cached_repos_dir=CACHED_REPOS_DIR, \
                                  output_dir=SPLIT_DEFINER_CSLICER_DEFINER_OUTPUT_DIR, \
                                  configs_dir=SPLIT_DEFINER_CSLICER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'split-definer-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split-definer-cslicer', \
                        repo_path)
    elif isPrefixRepoCached(example, 'split-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split-definer', repo_path)
    elif isPrefixRepoCached(example, 'split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'split-definer-cslicer'):
        is_run_from_cache = True
        cslicer_end_time = start_time
        split_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'split-definer'):
        is_run_from_cache = True
        definer_end_time = start_time
        split_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .cslicer
    elif isPrefixRepoCached(example, 'split'):
        is_run_from_cache = True
        split_end_time = start_time
        split_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .split
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer-cslicer-definer', example):
            is_match, matched_config = isStateMatch(None, 'split-definer-cslicer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-cslicer-definer', \
                                  suffix='split-definer-cslicer-definer', \
                                  log_file=orig_history_file)
    label .split
    # -------------------------------- split start -------------------------------------
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-split')
    # cache intermediate repo after split (S)
    cachePrefixRepoIfNotAlreadyCached(example, 'split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='split-definer-cslicer-definer', start=start, \
                                repo_path=repo_path, branch='after-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-cslicer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer-cslicer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-cslicer-definer', \
                                  suffix='definer-cslicer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer
    # -------------------------------- definer start ---------------------------------
    # generate new config files for splitted history
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-split')
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, split_config_file, 'after-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        cslicer_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-split-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-cslicer-definer', \
                                  suffix='cslicer-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .cslicer
    # -------------------------------- cslicer start ---------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split-definer', branch='after-split-definer')
    # update cslicer config
    cslicer_config_file = updateCSlicerConfig(example, end, configs_dir)
    # run tests at end commit, generate jacoco files
    _, end, _, test_suite, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer on splitted history, save logs
    cslicer_log = output_dir + '/' + example + '.log.phase2'
    runCSlicerTool(cslicer_log, cslicer_config_file, 'after-split-definer')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-split-definer-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-definer-cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(cslicer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-cslicer-definer', \
                                  suffix='definer', log_file=cslicer_log)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer2
    # --------------------------------  definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split-definer-cslicer', \
                                  branch='after-split-definer-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    # checkout to original end commit and run the tests
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-split-definer-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-split-definer-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-definer-cslicer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - cslicer_end_time
    final_log = definer_log
    # --------------------------------  definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'split-definer-cslicer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)

@with_goto
def runCSlicerDefinerDefiner(example, share_prefix, share_suffix, \
                             orig_history_dir=ORIG_HISTORY_DIR, \
                             cached_repos_dir=CACHED_REPOS_DIR, \
                             output_dir=CSLICER_DEFINER_DEFINER_OUTPUT_DIR, \
                             configs_dir=CSLICER_DEFINER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # remove the old repo in _downloads dir
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer-definer', example):
            is_match, matched_config = isStateMatch(None, 'cslicer-definer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-definer', \
                                  suffix='cslicer-definer-definer', log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-definer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, 'definer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-definer', \
                                  suffix='definer-definer', log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # temp definer config file (CZ: we may change in the future to keep all the temp files)
    definer_config_file = updateDefinerConfig(example, end, TEMP_CONFIGS_DIR)
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # move all untracked test files to temp dir (for running jacoco needed)-----------
    p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        if lines[i].startswith('src/test/'):
            dir_structure = '/'.join(lines[i].strip().split('/')[:-1])
            dest_dir = TEMP_FILES_DIR + '/' + dir_structure
            if os.path.isdir(dest_dir):
                shutil.rmtree(dest_dir)
            os.makedirs(dest_dir)
            shutil.move(lines[i].strip(), dest_dir)
            #os.remove(lines[i].strip())
    # -------------------------------------------------------------------------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                                'after-cslicer-definer')
    # cache intermediate repo after cslicer-definer (CD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-definer', \
                                  suffix='definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-definer', \
                                  branch='after-cslicer-definer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer-definer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-cslicer-definer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - definer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-definer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runDefinerCSlicerDefiner(example, share_prefix, share_suffix, \
                             orig_history_dir=ORIG_HISTORY_DIR, \
                             cached_repos_dir=CACHED_REPOS_DIR, \
                             output_dir=DEFINER_CSLICER_DEFINER_OUTPUT_DIR, \
                             configs_dir=DEFINER_CSLICER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'definer-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer-cslicer', repo_path)
    elif isPrefixRepoCached(example, 'definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'definer-cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        cslicer_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        goto .cslicer
    else: # cache not exist
        is_run_from_cache = False
        goto .definer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-cslicer-definer', example):
            is_match, matched_config = isStateMatch(None, 'definer-cslicer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-definer', \
                                  suffix='definer-cslicer-definer', log_file=orig_history_file)
    label .definer
    # -------------------------------- definer start -------------------------------------
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        cslicer_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, 'after-definer')
    # cache intermediate repo after definer (D)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-definer', \
                                  suffix='cslicer-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer', branch='after-definer')
    # update cslicer config
    cslicer_config_file = updateCSlicerConfig(example, end, configs_dir)
    # run tests at the original end commit, generate jacoco files
    _, end, _, test_suite, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # run cslicer and save logs
    cslicer_log = output_dir + '/' + example + '.log.phase2'
    runCSlicerTool(cslicer_log, cslicer_config_file, 'after-definer')
    history_slice, commit_msg_list = extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(cslicer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-cslicer-definer', \
                                  suffix='definer', log_file=cslicer_log)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer-cslicer', \
                                  branch='after-definer-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    # checkout to original end commit and run the tests
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-definer-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-cslicer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - cslicer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'definer-cslicer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runSplitDefinerDefiner(example, share_prefix, share_suffix, \
                           orig_history_dir=ORIG_HISTORY_DIR, \
                           cached_repos_dir=CACHED_REPOS_DIR, \
                           output_dir=SPLIT_DEFINER_DEFINER_OUTPUT_DIR, \
                           configs_dir=SPLIT_DEFINER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'split-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split-definer', repo_path)
    elif isPrefixRepoCached(example, 'split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'split', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'split-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'split'):
        is_run_from_cache  =True
        split_end_time = start_time
        split_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .split
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer-definer', example):
            is_match, matched_config = isStateMatch(None, 'split-definer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-definer', \
                                  suffix='split-definer-definer', log_file=orig_history_file)
    label .split
    # -------------------------------- split start -------------------------------------
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-split')
    # cache intermediate repo after split (S)
    cachePrefixRepoIfNotAlreadyCached(example, 'split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='split-definer-definer', start=start, \
                                repo_path=repo_path, branch='after-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer-definer', example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-definer', \
                                  suffix='definer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-split')
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, split_config_file, 'after-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                            'after-split-definer')
    # cache intermediate repo after split-definer (SD)
    cachePrefixRepoIfNotAlreadyCached(example, 'split-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='split-definer-definer', \
                                  suffix='definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='split-definer', branch='after-split-definer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-split-definer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-split-definer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'split-definer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - definer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'split-definer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)

@with_goto
def runDefinerDefiner(example, share_prefix, share_suffix, \
                      orig_history_dir=ORIG_HISTORY_DIR, \
                      cached_repos_dir=CACHED_REPOS_DIR, \
                      output_dir=DEFINER_DEFINER_OUTPUT_DIR, \
                      configs_dir=DEFINER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                     extractInfoFromDefinerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'definer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        definer_exec_time = 'NOT RUN'
        goto .definer2
    else: # cache not exist
        is_run_from_cache = False
        goto .definer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-definer', example):
            is_match, matched_config = isStateMatch(None, 'definer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='definer-definer', \
                                  suffix='definer-definer', log_file=orig_history_file)
    label .definer
    # -------------------------------- definer start -------------------------------------
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = output_dir + '/' + example + '.log.phase1'
    runDefinerTool(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, 'after-definer')
    # cache intermediate repo after definer (D)
    cachePrefixRepoIfNotAlreadyCached(example, 'definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='definer-definer', suffix='definer', \
                                  log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='definer', branch='after-definer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-definer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-definer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'definer-definer', repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - definer_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'definer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

@with_goto
def runCSlicerDefinerSplitCSlicerDefiner(example, share_prefix, share_suffix, \
                                         orig_history_dir=ORIG_HISTORY_DIR, \
                                         cached_repos_dir=CACHED_REPOS_DIR, \
                                output_dir=CSLICER_DEFINER_SPLIT_CSLICER_DEFINER_OUTPUT_DIR, \
                                configs_dir=CSLICER_DEFINER_SPLIT_CSLICER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    # remove the old repo in _downloads dir
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-definer-split-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + \
                        'cslicer-definer-split-cslicer', repo_path)
    elif isPrefixRepoCached(example, 'cslicer-definer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer-split', \
                        repo_path)
    elif isPrefixRepoCached(example, 'cslicer-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-definer', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-definer-split-cslicer'):
        is_run_from_cache  =True
        cslicer2_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        cslicer2_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'cslicer-definer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .cslicer2
    elif isPrefixRepoCached(example, 'cslicer-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .split
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .definer
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer-split-cslicer-definer', example):
            is_match, matched_config = isStateMatch(None, \
                                                    'cslicer-definer-split-cslicer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer-definer', \
                                  suffix='cslicer-definer-split-cslicer-definer', \
                                  log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # copy target dir because we will run CSlicer again later
    # cache target dir, otherwise we cannot run CSlicer2 in the middle
    cacheTargetDirForCSlicer2(example, repo_path)
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-split-cslicer-definer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, \
                                                    'definer-split-cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer-definer', \
                            suffix='definer-split-cslicer-definer', log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # temp definer config file (CZ: we may change in the future to keep all the temp files)
    definer_config_file = updateDefinerConfig(example, end, TEMP_CONFIGS_DIR)
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # move all untracked test files to temp dir (for running jacoco needed)-----------
    p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        if lines[i].startswith('src/test/'):
            dir_structure = '/'.join(lines[i].strip().split('/')[:-1])
            dest_dir = TEMP_FILES_DIR + '/' + dir_structure
            if os.path.isdir(dest_dir):
                shutil.rmtree(dest_dir)
            os.makedirs(dest_dir)
            shutil.move(lines[i].strip(), dest_dir)
            #os.remove(lines[i].strip())
    # -------------------------------------------------------------------------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # extract history slice from definer log
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        split_exec_time = 'NOT RUN'
        cslicer2_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                                'after-cslicer-definer')
    # cache intermediate repo after cslicer-definer (CD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-cslicer-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'split-cslicer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer-definer', \
                                  suffix='split-cslicer-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-definer', \
                                  branch='after-cslicer-definer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-cslicer-definer-split')
    # cache intermediate repo after cslicer-definer-split (CDS)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='cslicer-definer-split-cslicer-definer', \
                                start=start, repo_path=repo_path, \
                                branch='after-cslicer-definer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer-definer', \
                                  suffix='cslicer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .cslicer2
    # -------------------------------- cslicer2 start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, _, lines, _ = extractInfoFromCSlicerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-cslicer-definer-split')
    # move untracked files back
    os.chdir(repo_path)
    for dir_path, subpaths, files in os.walk(TEMP_FILES_DIR):
        for f in files:
            if '/src/test' in dir_path:
                shutil.copy(dir_path + '/' + f, \
                                repo_path + dir_path[dir_path.index('/src/test'):])
    # copy target dir back (required by CSlicer)
    copyTargetDirBackForCSlicer2(example, repo_path)
    # run cslicer on split history, save logs
    cslicer_log = output_dir + '/' + example + '.log.phase3'
    runCSlicerTool(cslicer_log, split_config_file, 'after-cslicer-definer-split')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                            'after-cslicer-definer-split-cslicer')
    # cache intermediate repo after cslicer (CDSC)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split-cslicer', repo_path)
    cslicer2_end_time = time.time()
    cslicer2_exec_time = cslicer2_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(cslicer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-definer-split-cslicer-definer', \
                                  suffix='definer', log_file=cslicer_log)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    # -------------------------------- cslicer2 end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-definer-split-cslicer', \
                                  branch='after-cslicer-definer-split-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer-definer-split-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch, reset start and end
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-cslicer-definer-split-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-definer-split-cslicer-definer', \
                                      repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - cslicer2_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[CSlicer2 Exec Time]'] = cslicer2_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-definer-split-cslicer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    cleanTempLogs()

@with_goto
def runCSlicerSplitDefinerCSlicerDefiner(example, share_prefix, share_suffix, \
                                         orig_history_dir=ORIG_HISTORY_DIR, \
                                         cached_repos_dir=CACHED_REPOS_DIR, \
                                output_dir=CSLICER_SPLIT_DEFINER_CSLICER_DEFINER_OUTPUT_DIR, \
                                configs_dir=CSLICER_SPLIT_DEFINER_CSLICER_DEFINER_CONFIGS_DIR):
    print ('Starting Example :' + example)
    # start counting the exec time
    start_time = time.time()
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                            extractInfoFromCSlicerConfigs(example)
    # remove the old repo in _downloads dir
    if os.path.isdir(repo_path):
        print ('remove old repo')
        time.sleep(30)
        shutil.rmtree(repo_path, ignore_errors=True)
    # check if cache is disabled
    if not share_prefix:
        is_run_from_cache = False
        shutil.copytree(repo_path + '-fake', repo_path)
        goto .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # copy the cached repo if exist
    if isPrefixRepoCached(example, 'cslicer-split-definer-cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + \
                        'cslicer-split-definer-cslicer', repo_path)
    elif isPrefixRepoCached(example, 'cslicer-split-definer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-split-definer', \
                        repo_path)
    elif isPrefixRepoCached(example, 'cslicer-split'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer-split', repo_path)
    elif isPrefixRepoCached(example, 'cslicer'):
        shutil.copytree(cached_repos_dir + '/' + example + '/' + 'cslicer', repo_path)
    else:
        # no cache, copy a new repo
        shutil.copytree(repo_path + '-fake', repo_path)
    if isPrefixRepoCached(example, 'cslicer-split-definer-cslicer'):
        is_run_from_cache  =True
        cslicer2_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        cslicer2_exec_time = 'NOT RUN'
        goto .definer2
    elif isPrefixRepoCached(example, 'cslicer-split-definer'):
        is_run_from_cache  =True
        definer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        definer_exec_time = 'NOT RUN'
        goto .cslicer2
    elif isPrefixRepoCached(example, 'cslicer-split'):
        is_run_from_cache  =True
        split_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        split_exec_time = 'NOT RUN'
        goto .definer
    elif isPrefixRepoCached(example, 'cslicer'):
        is_run_from_cache  =True
        cslicer_end_time = start_time
        cslicer_exec_time = 'NOT RUN'
        goto .split
    else: # cache not exist
        is_run_from_cache = False
        goto .cslicer
    label .prefix_disabled
    # a label indicating whether any suffix is saved in this run
    is_suffix_skipped = False
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-split-definer-cslicer-definer', example):
            is_match, matched_config = isStateMatch(None, \
                                                    'cslicer-split-definer-cslicer-definer', \
                                                    example, start=start, end=end, \
                                                    repo_path=repo_path)
            if is_match:
                is_suffix_skipped = True
                cslicer_exec_time = 'NOT RUN'
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # suffix cache: cache initial state, using full suffix
    orig_history = getOriginalHistory(start, end, repo_path)
    orig_history_file = orig_history_dir + '/' + example + '.hist'
    fw = open(orig_history_file, 'w')
    fw.write('\n'.join(orig_history))
    fw.close()
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-cslicer-definer', \
                                  suffix='cslicer-split-definer-cslicer-definer', \
                                  log_file=orig_history_file)
    label .cslicer
    # -------------------------------- cslicer start -------------------------------------
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)
    # stash changes on pom
    sub.run('git stash', shell=True)
    # copy target dir because we will run CSlicer again later
    # cache target dir, otherwise we cannot run CSlicer2 in the middle
    cacheTargetDirForCSlicer2(example, repo_path)
    # run cslicer on original history, save temp logs
    cslicer_temp_log = output_dir + '/' + example + '.log.phase1'
    runCSlicerTool(cslicer_temp_log, config_file, 'orig')
    # delete orig branch
    sub.run('git co trunk', shell=True)
    sub.run('git co master', shell=True)
    sub.run('git br -D orig', shell=True)
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_temp_log)
    # for NET-525, NET-527
    if example == 'NET-525' or example == 'NET-527':
        cslicer_history_slice.append('4379a681')
        commit_msg_list.append('Cut-n-paste bug')
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer')
    # cache intermediate repo after cslicer (C)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer', repo_path)
    cslicer_end_time = time.time()
    cslicer_exec_time = cslicer_end_time - start_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('split-definer-cslicer-definer', example):
            is_match, matched_config = isStateMatch(cslicer_temp_log, \
                                                    'split-definer-cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                split_exec_time = 'NOT RUN'
                definer_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-cslicer-definer', \
                                  suffix='split-definer-cslicer-definer', \
                                  log_file=cslicer_temp_log)
    countChangedLines(cslicer_temp_log, repo_path, 'cslicer')
    # -------------------------------- cslicer end -------------------------------------
    label .split
    # -------------------------------- split start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer', branch='after-cslicer')
    # split commits by file
    splitCommitsByFile(example, repo_path, start, end, 'after-cslicer-split')
    # cache intermediate repo after cslicer-split (CS)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split', repo_path)
    # generate split log file
    split_log = genSplitLogFile(example, config='cslicer-split-definer-cslicer-definer', \
                                start=start, repo_path=repo_path, branch='after-cslicer-split')
    split_end_time = time.time()
    split_exec_time = split_end_time - cslicer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer-cslicer-definer', example):
            is_match, matched_config = isStateMatch(split_log, 'definer-cslicer-definer', \
                                                    example)
            if is_match:
                is_suffix_skipped = True
                definer_exec_time = 'NOT RUN'
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-cslicer-definer', \
                                  suffix='definer-cslicer-definer', log_file=split_log)
    countChangedLines(split_log, repo_path, 'split')
    # -------------------------------- split end -------------------------------------
    label .definer
    # -------------------------------- definer start -------------------------------------
    # generate new config files for splitted history
    _, end, _, _, test_suite, _, lines, _ = extractInfoFromDefinerConfigs(example)
    split_config_file = genSplittedConfigFile(example, repo_path, lines, configs_dir, \
                                              'after-cslicer-split')
    # run definer on splitted history, save logs
    definer_log = output_dir + '/' + example + '.log.phase2'
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, split_config_file, 'after-cslicer-split')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # cherry-pick history slice to a new branch
    definer_history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(definer_history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer_exec_time = 'TIME OUT'
        cslicer2_exec_time = 'NOT RUN'
        definer2_exec_time = 'NOT RUN'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, definer_history_slice, commit_msg_list, \
                            'after-cslicer-split-definer')
    # cache intermediate repo after cslicer-definer-split-definer (CSD)
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split-definer', repo_path)
    definer_end_time = time.time()
    definer_exec_time = definer_end_time - split_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('cslicer-definer', example):
            is_match, matched_config = isStateMatch(definer_log, 'cslicer-definer', example)
            if is_match:
                is_suffix_skipped = True
                cslicer2_exec_time = 'NOT RUN'
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-cslicer-definer', \
                                  suffix='cslicer-definer', log_file=definer_log)
    countChangedLines(definer_log, repo_path, 'definer')
    # -------------------------------- definer end -------------------------------------
    label .cslicer2
    # -------------------------------- cslicer2 start ---------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-split-definer', \
                                  branch='after-cslicer-split-definer')
    # update cslicer config
    cslicer_config_file = updateCSlicerConfig(example, end, configs_dir)
    # move untracked files back
    os.chdir(repo_path)
    for dir_path, subpaths, files in os.walk(TEMP_FILES_DIR):
        for f in files:
            if '/src/test' in dir_path:
                shutil.copy(dir_path + '/' + f, \
                                repo_path + dir_path[dir_path.index('/src/test'):])
    # copy target dir back (required by CSlicer)
    copyTargetDirBackForCSlicer2(example, repo_path)
    # run cslicer on splitted history, save logs
    cslicer_log = output_dir + '/' + example + '.log.phase3'
    runCSlicerTool(cslicer_log, cslicer_config_file, 'after-cslicer-split-definer')
    # cherry-pick history slice to a new branch, reset start and end
    cslicer_history_slice, commit_msg_list = \
                                    extractHistorySliceFromCSlicerLog(cslicer_log)
    end = applyHistorySlice(repo_path, start, cslicer_history_slice, commit_msg_list, \
                                'after-cslicer-split-definer-cslicer')
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split-definer-cslicer', repo_path)
    cslicer2_end_time = time.time()
    cslicer2_exec_time = cslicer2_end_time - definer_end_time
    # check if any suffix reusable
    if share_suffix:
        if isSuffixExist('definer', example):
            is_match, matched_config = isStateMatch(cslicer_log, 'definer', example)
            if is_match:
                is_suffix_skipped = True
                definer2_exec_time = 'NOT RUN'
                # find out saved by which config, then copy the slice and time of that config.
                final_log = output_dir + '/' + example + '.log'
                copyTheSliceFromOneConfigLogToFinalLog(matched_config, example, final_log)
                goto .skip_suffix
    # cache suffix:
    cacheSuffixIfNotAlreadyCached(example, config='cslicer-split-definer-cslicer-definer', \
                                  suffix='definer', log_file=cslicer_log)
    countChangedLines(cslicer_log, repo_path, 'cslicer')
    # -------------------------------- cslicer2 end -------------------------------------
    label .definer2
    # -------------------------------- definer2 start -------------------------------------
    if is_run_from_cache:
        end = getEndSHAFrombranch(example, config='cslicer-split-definer-cslicer', \
                                  branch='after-cslicer-split-definer-cslicer')
    # temp definer config file
    definer_config_file = updateDefinerConfig(example, end, configs_dir)
    definer_log = output_dir + '/' + example + '.log'
    # checkout to original end commit and run the tests
    _, end, _, _, test_suite, _, _, _ = extractInfoFromDefinerConfigs(example)
    os.chdir(repo_path)
    # --------------
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    runDefinerTool(definer_log, definer_config_file, 'after-cslicer-split-definer-cslicer')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # --------------
    # cherry-pick history slice to a new branch
    history_slice, commit_msg_list = extractHistorySliceFromDefinerLog(definer_log)
    if len(history_slice) == 0 and len(commit_msg_list) == 0:
        print ('Definer times out!')
        definer2_exec_time = 'TIME OUT'
        final_log = definer_log
        goto .timeout
    end = applyHistorySlice(repo_path, start, history_slice, commit_msg_list, \
                                'after-cslicer-split-definer-cslicer-definer')
    cachePrefixRepoIfNotAlreadyCached(example, 'cslicer-split-definer-cslicer-definer', \
                                      repo_path)
    definer2_end_time = time.time()
    definer2_exec_time = definer2_end_time - cslicer2_end_time
    final_log = definer_log
    # -------------------------------- definer2 end -------------------------------------
    label .timeout
    label .skip_suffix
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    time_dict = collections.OrderedDict({})
    time_dict['[CSlicer Exec Time]'] = cslicer_exec_time
    time_dict['[Split Exec Time]'] = split_exec_time
    time_dict['[Definer Exec Time]'] = definer_exec_time
    time_dict['[CSlicer2 Exec Time]'] = cslicer2_exec_time
    time_dict['[Definer2 Exec Time]'] = definer2_exec_time
    time_dict['[Total Exec Time]'] = run_time
    insertTimeDictinLog(final_log, time_dict)
    if not share_prefix and not share_suffix:
        insertTimeDictInGlobalTable(example, 'cslicer-split-definer-cslicer-definer', time_dict)
    if not is_suffix_skipped:
        countChangedLines(final_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)
    # clean temp logs
    cleanTempLogs()

# For true minimal exp
def runDefinerWithMemoryStandalone(example):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                    extractInfoFromDefinerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    definer_log = DEFINER_WITH_MEMORY_STANDALONE_OUTPUT_DIR + '/' + example + '.log'
    runDefinerToolWithMemory(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # -------------------------------- definer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(definer_log, run_time)
    countChangedLines(definer_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)

# For asej exp
def runDefinerASEJ(example, option):
    print ('Starting Example :' + example)
    start_time = time.time()
    # extract info from config file
    start, end, repo_name, build_script_path, test_suite, repo_path, lines, config_file = \
                                                    extractInfoFromDefinerConfigs(example)
    if os.path.isdir(repo_path):
        print ('remove old repo')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + '-fake', repo_path)
    # checkout to end commit and run the tests
    runTestsAtEndCommitForDefiner(example, end, repo_path, test_suite)
    # run definer, save temp logs
    if option == 'learning':
        definer_log = LEARNING_OUTPUT_DIR + '/' + example + '.log'
        runDefinerToolLearning(definer_log, config_file, 'definerorig')
    elif option == 'basic':
        definer_log = BASIC_OUTPUT_DIR + '/' + example + '.log'
        runDefinerToolBasic(definer_log, config_file, 'definerorig')
    elif option == 'neg':
        definer_log = NEG_OUTPUT_DIR + '/' + example + '.log'
        runDefinerToolNeg(definer_log, config_file, 'definerorig')
    elif option == 'nopos':
        definer_log = NOPOS_OUTPUT_DIR + '/' + example + '.log'
        runDefinerToolNoPos(definer_log, config_file, 'definerorig')
    elif option == 'low3':
        definer_log = LOW3_OUTPUT_DIR + '/' + example + '.log'
        runDefinerToolLow3(definer_log, config_file, 'definerorig')
    cleanRepoAfterDefinerTimeout(repo_path) # when definer timeout, remove lock files
    # -------------------------------- definer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(definer_log, run_time)
    countChangedLines(definer_log, repo_path, 'definer')
    #backupRepoForDebugging(example, repo_path)

if __name__ == '__main__':
    opts = parseArgs(sys.argv[1:])
    # check dirs: orig-history, temp-files, temp-logs, _split_logs, jacoco-files, _repo, temp-configs
    # check repos: create _downloads dir and clone csv, lang, net, io, compress, then create fakes
    if opts.clean_prefix_cache: # ISSTA 19
        shutil.rmtree(CACHED_REPOS_DIR)
        os.makedirs(CACHED_REPOS_DIR)
    if opts.clean_suffix_cache: # ISSTA 19
        shutil.rmtree(SUFFIX_SHARING_CACHE_DIR)
        os.makedirs(SUFFIX_SHARING_CACHE_DIR)
    if opts.share_prefix: # ISSTA 19
        share_prefix = True
    else:
        share_prefix = False
    if opts.share_suffix: # ISSTA 19
        share_suffix = True
    else:
        share_suffix = False

    if opts.clean_touchset:
        cleanTouchSet()
        exit(0)
    if opts.split_cslicer:
        for example in examples:
            runSplitCSlicer(example)
        exit(0)
    if opts.split_definer: # ISSTA 19
        for example in examples:
            runSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_split_cslicer:
        for example in examples:
            runCSlicerSplitCSlicer(example)
        exit(0)
    if opts.cslicer_split_definer: # ISSTA 19
        for example in examples:
            runCSlicerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_split_cslicer:
        for example in examples:
            runDefinerSplitCSlicer(example)
        exit(0)
    if opts.definer_split_definer: # ISSTA 19
        for example in examples:
            runDefinerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_split_cslicer: # ISSTA 19
        for example in examples:
            runCSlicerDefinerSplitCSlicer(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_split_definer: # ISSTA 19
        for example in examples:
            runCSlicerDefinerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer:
        for example in examples:
            runCSlicerStandalone(example)
        exit(0)
    if opts.definer:
        for example in examples:
            runDefinerStandalone(example)
        exit(0)
    if opts.cslicer_definer:
        for example in examples:
            runCSlicerDefiner(example)
        exit(0)
    if opts.split_cslicer_definer: # ISSTA 19
        for example in examples:
            runSplitCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.split_cslicer_definer_definer: # ISSTA 19
        for example in examples:
            runSplitCSlicerDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_split_definer_definer: # ISSTA 19
        for example in examples:
            runCSlicerSplitDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_split_cslicer_definer: # ISSTA 19
        for example in examples:
            runDefinerSplitCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_cslicer_split_definer: # ISSTA 19
        for example in examples:
            runDefinerCSlicerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.split_definer_cslicer_definer: # ISSTA 19
        for example in examples:
            runSplitDefinerCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_definer: # ISSTA 19
        for example in examples:
            runCSlicerDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_cslicer_definer: # ISSTA 19
        for example in examples:
            runDefinerCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.split_definer_definer: # ISSTA 19
        for example in examples:
            runSplitDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_definer: # ISSTA 19
        for example in examples:
            runDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_split_definer_cslicer_definer : # ISSTA 19
        for example in examples:
            time.sleep(30)
            runCSlicerSplitDefinerCSlicerDefiner(example, share_prefix, share_suffix)
            time.sleep(30)
        exit(0)
    if opts.cslicer_definer_split_cslicer_definer : # ISSTA 19
        for example in examples:
            time.sleep(30)
            runCSlicerDefinerSplitCSlicerDefiner(example, share_prefix, share_suffix)
            time.sleep(30)
        exit(0)
    if opts.definer_with_memory: # For true minimal exp
        for example in examples:
            runDefinerWithMemoryStandalone(example)
        exit(0)
    if opts.definer_asej_exp: # For asej exp
        option = opts.definer_asej_exp
        for example in examples:
            runDefinerASEJ(example, option)
        exit(0)
    if opts.split_cslicer_one:
        example = opts.split_cslicer_one
        runSplitCSlicer(example)
        exit(0)
    if opts.split_definer_one: # ISSTA 19
        example = opts.split_definer_one
        runSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_split_cslicer_one:
        example = opts.cslicer_split_cslicer_one
        runCSlicerSplitCSlicer(example)
        exit(0)
    if opts.cslicer_split_definer_one: # ISSTA 19
        example = opts.cslicer_split_definer_one
        runCSlicerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_split_cslicer_one:
        example = opts.definer_split_cslicer_one
        runDefinerSplitCSlicer(example)
        exit(0)
    if opts.definer_split_definer_one: # ISSTA 19
        example = opts.definer_split_definer_one
        runDefinerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_split_cslicer_one: # ISSTA 19
        example = opts.cslicer_definer_split_cslicer_one
        runCSlicerDefinerSplitCSlicer(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_split_definer_one: # ISSTA 19
        example = opts.cslicer_definer_split_definer_one
        runCSlicerDefinerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_one:
        example = opts.cslicer_one
        runCSlicerStandalone(example)
        exit(0)
    if opts.definer_one:
        example = opts.definer_one
        runDefinerStandalone(example)
        exit(0)
    if opts.cslicer_definer_one:
        example = opts.cslicer_definer_one
        runCSlicerDefiner(example)
        exit(0)
    if opts.split_cslicer_definer_one: # ISSTA 19
        example = opts.split_cslicer_definer_one
        runSplitCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.split_cslicer_definer_definer_one: # ISSTA 19
        example = opts.split_cslicer_definer_definer_one
        runSplitCSlicerDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_split_definer_definer_one: # ISSTA 19
        example = opts.cslicer_split_definer_definer_one
        runCSlicerSplitDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_split_cslicer_definer_one: # ISSTA 19
        example = opts.definer_split_cslicer_definer_one
        runDefinerSplitCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_cslicer_split_definer_one: # ISSTA 19
        example = opts.definer_cslicer_split_definer_one
        runDefinerCSlicerSplitDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.split_definer_cslicer_definer_one: # ISSTA 19
        example = opts.split_definer_cslicer_definer_one
        runSplitDefinerCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_definer_one: # ISSTA 19
        example = opts.cslicer_definer_definer_one
        runCSlicerDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_cslicer_definer_one: # ISSTA 19
        example = opts.definer_cslicer_definer_one
        runDefinerCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.split_definer_definer_one: # ISSTA 19
        example = opts.split_definer_definer_one
        runSplitDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_definer_one: # ISSTA 19
        example = opts.definer_definer_one
        runDefinerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_split_definer_cslicer_definer_one: # ISSTA 19
        example = opts.cslicer_split_definer_cslicer_definer_one
        runCSlicerSplitDefinerCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.cslicer_definer_split_cslicer_definer_one: # ISSTA 19
        example = opts.cslicer_definer_split_cslicer_definer_one
        runCSlicerDefinerSplitCSlicerDefiner(example, share_prefix, share_suffix)
        exit(0)
    if opts.definer_with_memory_one: # For true minimal exp
        example = opts.definer_with_memory_one
        runDefinerWithMemoryStandalone(example)
        exit(0)
    if opts.definer_asej_exp_one: # For asej exp
        option = opts.definer_asej_exp_one
        example = opts.example
        runDefinerASEJ(example, option)
        exit(0)

# --- deprecated --- 
# ISSTA 19
def findWhichConfigSavedTheCache(suffix, example, \
                                 suffix_sharing_cache_dir=SUFFIX_SHARING_CACHE_DIR):
    cached_log = suffix_sharing_cache_dir + '/' + example + '/suffix.log'
    fr = open(cached_log, 'r')
    lines = fr.readlines()
    fr.close()
    config_which_saving_cache = lines[0].strip().split(': ')[-1]
    return config_which_saving_cache

# --- deprecated ---
def runCSlicerToolOrigBr(cslicer_log, config_file):
    runCSlicerTool(cslicer_log, config_file, 'orig')

# --- deprecated ---
def runCSlicerToolSplitBr(cslicer_log, config_file):
    runCSlicerTool(cslicer_log, config_file, 'filelevel')
