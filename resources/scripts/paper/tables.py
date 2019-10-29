#!/usr/bin/python3

import os
import sys
import re
import csv
import time
import argparse
import shutil
import subprocess as sub

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
_DOWNLOADS_DIR = SCRIPT_DIR + '/../../../_downloads'
OUTPUT_DIR = SCRIPT_DIR + '/../../file-level/output'
CONFIGS_DIR = SCRIPT_DIR + '/../../file-level/definer-configs'
ALL_CONFIGS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                               '/../../file-level/results/tables/allconfigs-numbers.tex'
EFFECTIVENESS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/effectiveness-exp-numbers.tex'

PROJECTS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/projects-numbers.tex'
PROJECTS_TABLE_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/projects-table.tex'
SUBJECTS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/subjects-numbers.tex'
SUBJECTS_TABLE_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/subjects-table.tex'
SCHEME_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/scheme-numbers.tex'
SCHEME_TABLE_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/scheme-table.tex'

EXAMPLES = ['COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
            'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
            'LANG-993', 'LANG-1006',
            'NET-525', 'NET-527',
            'CSV-159', 'CSV-175', 'CSV-180']

PROJECTS = ['compress', 'io', 'lang', 'net', 'csv']

PARTITION_SCHEMES = ['definer-neg', 'definer-nopos', 'definer-low3', 'definer']

DESCRIPTIONS = {'COMPRESS-327': 'Support in-memory processing for ZipFile',
                'COMPRESS-369': 'Allow archiver extensions through a standard JRE ServiceLoader',
                'COMPRESS-373': 'Support writing the \"old\" lzma format',
                'COMPRESS-374': 'Add support for writing lzma streams in 7z',
                'COMPRESS-375': 'Allow the clients of ParallelScatterZipCreator to provide ZipArchiveEntryRequestSupplier',
                'IO-173': 'FileUtils.listFiles() doesn\'t return directories',
                'IO-275': 'Add option to ignore line endings',
                'IO-288': 'Supply a ReversedLinesFileReader',
                'IO-290': 'Add read/readFully methods to IOUtils',
                'IO-305': 'New copy() method in IOUtils that takes additional offset, length and buffersize arguments',
                'LANG-883': 'Add StringUtils.containsAny(CharSequence, CharSequence...) method',
                'LANG-993': 'Add zero copy write method to StrBuilder',
                'LANG-1006': 'Add wrap (with String or char) to StringUtils',
                'LANG-1080': 'Add NoClassNameToStringStyle implementation of ToStringStyle',
                'LANG-1093': 'Add ClassUtils.getAbbreviatedName',
                'NET-525': 'Added missing set methods on NTP class and interface',
                'NET-527': 'Add SimpleNTPServer as example and for testing',
                'CSV-159': 'Add IgnoreCase option for accessing header names',
                'CSV-175': 'Support for ignoring trailing delimiter',
                'CSV-180': 'Add withHeader(Class<? extends Enum>) to CSVFormat'}

def countNumOfJavaFiles(project_dir):
    counter = 0
    for path, subpaths, files in os.walk(project_dir, False):
        for f in files:
            if f.endswith('.java'):
                counter += 1
    return counter

def countLOC(project_dir):
    p = sub.Popen('sloccount ' + project_dir, shell=True, stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        if lines[i].strip().startswith('java:') and lines[i].strip().endswith(')'):
            loc = lines[i].split()[1]
    return int(loc)

def countNumOfCommitsInOneYearAndFourMonths(project_dir):
    cwd = os.getcwd()
    os.chdir(project_dir)
    p = sub.Popen('git --no-pager log --since \"JAN 1 2015\" --until \"JAN 1 2016\" --pretty=format:\"%h %an %ad\"', shell=True, stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    commits = p.stdout.readlines()
    num_of_commits_in_one_year = len(commits)
    num_of_commits_in_four_months = float(num_of_commits_in_one_year) / 4
    os.chdir(cwd)
    return num_of_commits_in_one_year, round(num_of_commits_in_four_months, 2)

def genProjectNumbers(projects=PROJECTS, projects_numbers_tex_file=PROJECTS_NUMBERS_TEX_FILE, \
                      downloads_dir=_DOWNLOADS_DIR):
    lines = ''
    for project in projects:
        project_dir = downloads_dir + '/commons-' + project + '-fake'
        num_of_java_files = countNumOfJavaFiles(project_dir)
        loc = countLOC(project_dir)
        num_of_commits_in_one_year, num_of_commits_in_four_months = \
                            countNumOfCommitsInOneYearAndFourMonths(project_dir)
        lines += '\\DefMacro{' + project + 'NumOfJavaFiles}{' + \
                 '{:,}'.format(num_of_java_files) + '}\n'
        lines += '\\DefMacro{' + project + 'LOC}{' + '{:,}'.format(loc) + '}\n'
        lines += '\\DefMacro{' + project + 'NumOfCommitsOneYear}{' + \
                 '{:,}'.format(num_of_commits_in_one_year) + '}\n'
        lines += '\\DefMacro{' + project + 'NumOfCommitsFourMonths}{' + \
                 '{0:,.2f}'.format(num_of_commits_in_four_months) + '}\n'
    fw = open(projects_numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

def genProjectTable(projects=PROJECTS, projects_table_tex_file=PROJECTS_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += '\\begin{table}[t]\n'
    table_lines += '\\caption{\\TCaptionProjects}\n'
    table_lines += '\\centering\n'
    #table_lines += '\\resizebox{.4\columnwidth}{!}{\n'
    table_lines += '\\begin{small}\n'
    table_lines += '\\begin{tabular}{lrr}\n'
    table_lines += '\\toprule\n'
    #table_lines += '\\TableHeadProjects & \\TableHeadNumOfFiles & \\TableHeadLOC & \\TableHeadNumOfCommitsOneYear & \\TableHeadNumOfCommitsFourMonths \\\\\n'
    table_lines += '\\TableHeadProjects & \\TableHeadNumOfFiles & \\TableHeadLOC \\\\\n'
    table_lines += '\\midrule\n'
    for project in projects:
        table_lines += '\\' + project
        table_lines += ' & \\UseMacro{' + project + 'NumOfJavaFiles}'
        table_lines += ' & \\UseMacro{' + project + 'LOC}'
        #table_lines += ' & \\UseMacro{' + project + 'NumOfCommitsOneYear}'
        #table_lines += ' & \\UseMacro{' + project + 'NumOfCommitsFourMonths}'
        table_lines += ' \\\\\n'
    table_lines += '\\bottomrule\n'
    table_lines += '\\end{tabular}\n'
    table_lines += '\\end{small}\n'
    table_lines += '\\end{table}\n'
    fw = open(projects_table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def extractInfoFromAllConfigNumbersTexFile(example, \
                                    all_configs_numbers_tex_file=ALL_CONFIGS_NUMBERS_TEX_FILE):
    fr = open(all_configs_numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('\\DefMacro{' + example.replace('-', '') + \
                               'definerNumOfCommitsMapBack'):
            hstar = lines[i].split('{')[2].split('}')[0]
        elif lines[i].startswith('\\DefMacro{' + example.replace('-', '') + 'OrigHisLen'):
            h = int(lines[i].split('{')[2].split('}')[0])
    return hstar, h

def extractInfoFromConfigFile(example, configs_dir=CONFIGS_DIR):
    config_file = configs_dir + '/' + example + '.properties'
    fr = open(config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('endCommit = '):
            end_point = lines[i].strip().split()[-1]
        elif lines[i].startswith('testScope = '):
            num_of_test_methods = lines[i].count('#') + lines[i].count('+')
    return end_point, num_of_test_methods

def genSubjectsNumbers(examples=EXAMPLES, descriptions=DESCRIPTIONS, \
                       subjects_numbers_tex_file=SUBJECTS_NUMBERS_TEX_FILE):
    lines = ''
    for example in examples:
        lines += '\\DefMacro{' + example.replace('-', '') + 'IssueKey}{' + \
                 example + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'Descriptions}{' + \
                 descriptions[example] + '}\n'
        hstar, h = extractInfoFromAllConfigNumbersTexFile(example)
        end_point, num_of_test_methods = extractInfoFromConfigFile(example)
        lines += '\\DefMacro{' + example.replace('-', '') + 'EndPoint}{' + \
                 end_point[:7] + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'AbsHisSize}{' + \
                 '{:,}'.format(h) + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'AbsTestSize}{' + \
                 '{:,}'.format(num_of_test_methods) + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'HstarSize}{' + \
                 hstar + '}\n'
    fw = open(subjects_numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

def genSubjectsTable(examples=EXAMPLES, subjects_table_tex_file=SUBJECTS_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += '\\begin{table*}[t]\n'
    table_lines += '\\caption{\\TCaptionSubjects}\n'
    # \label{tab.subjects}
    table_lines += '\\centering\n'
    table_lines += '\\resizebox{\\textwidth}{!}{\n'
    table_lines += '\\begin{tabular}{l|llL{8cm}l|rrr}\n'
    table_lines += '\\toprule\n'
    table_lines += '\\TableHeadProjects & \\TableHeadSubjectID & \\TableHeadIssueKey & \\TableHeadDesc & \\TableHeadEndPoint & \\TableHeadAbsHisSize & \\TableHeadAbsTestSize & \\TableHeadHstarSize \\\\\n'
    table_lines += '\midrule\n'
    for example in examples:
        if example == 'LANG-883':
            table_lines += '\midrule\n'
            table_lines += '\\multirow{5}{*}{\\lang}'
        elif example == 'IO-173':
            table_lines += '\midrule\n'
            table_lines += '\\multirow{5}{*}{\\io}'
        elif example == 'COMPRESS-327':
            table_lines += '\\multirow{5}{*}{\\compress}'
        elif example == 'CSV-159':
            table_lines += '\midrule\n'
            table_lines += '\\multirow{3}{*}{\\csv}'
        elif example == 'NET-525':
            table_lines += '\midrule\n'
            table_lines += '\\multirow{2}{*}{\\net}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'ID}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'IssueKey}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'Descriptions}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'EndPoint}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'AbsHisSize}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'AbsTestSize}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'HstarSize}'
        table_lines += ' \\\\\n'
    table_lines += '\\bottomrule\n'
    table_lines += '\\end{tabular}}\n'
    table_lines += '\\end{table*}\n'
    fw = open(subjects_table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def extractInfoFromPartitionNumbers(example, partition_schemes=PARTITION_SCHEMES, \
                                    output_dir=OUTPUT_DIR):
    scheme_tests_dict = {}
    for scheme in partition_schemes:
        fr = open(output_dir + '/' + scheme + '/' + example + '.log', 'r')
        lines = fr.readlines()
        fr.close()
        pattern = re.compile('.* at Iteration .* after .* test.*.$')
        for i in range(len(lines)):
            if re.match(pattern, lines[i].strip()):
                num_of_tests = lines[i].strip().split()[-2]
        scheme_tests_dict[scheme] = num_of_tests
    return scheme_tests_dict

def genPartitionSchemeNumbers(examples=EXAMPLES, \
                              scheme_numbers_tex_file=SCHEME_NUMBERS_TEX_FILE):
    lines = ''
    for example in examples:
        scheme_tests_dict = extractInfoFromPartitionNumbers(example)
        lines += '\\DefMacro{' + example.replace('-', '') + 'NegNumOfRuns}{' + \
                 scheme_tests_dict['definer-neg'] + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'NonPosNumOfRuns}{' + \
                 scheme_tests_dict['definer-nopos'] + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'Low3NumOfRuns}{' + \
                 scheme_tests_dict['definer-low3'] + '}\n'
        lines += '\\DefMacro{' + example.replace('-', '') + 'CombinedNumOfRuns}{' + \
                 scheme_tests_dict['definer'] + '}\n'
    fw = open(scheme_numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()

def findMinSchemes(example, scheme_numbers_tex_file=SCHEME_NUMBERS_TEX_FILE):
    fr = open(scheme_numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('\\DefMacro{' + example.replace('-', '') + 'NegNumOfRuns'):
            neg = int(lines[i].split('{')[2].split('}')[0])
        elif lines[i].startswith('\\DefMacro{' + example.replace('-', '') + 'NonPosNumOfRuns'):
            nonpos = int(lines[i].split('{')[2].split('}')[0])
        elif lines[i].startswith('\\DefMacro{' + example.replace('-', '') + 'Low3NumOfRuns'):
            low3 = int(lines[i].split('{')[2].split('}')[0])
        elif lines[i].startswith('\\DefMacro{' + example.replace('-', '') + 'CombinedNumOfRuns'):
            combined = int(lines[i].split('{')[2].split('}')[0])
    min_val = min([neg, nonpos, low3, combined])
    min_schemes = []
    if neg == min_val:
        min_schemes.append('neg')
    if nonpos == min_val:
        min_schemes.append('nonpos')
    if low3 == min_val:
        min_schemes.append('low3')
    if combined == min_val:
        min_schemes.append('combined')
    return min_schemes

def genPartitionSchemeTable(examples=EXAMPLES, scheme_table_tex_file=SCHEME_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += '\\begin{table}[t]\n'
    table_lines += '\\caption{\\TCaptionPartitionSchemes}\n'
    table_lines += '\\centering\n'
    table_lines += '\\resizebox{.5\\columnwidth}{!}{\n'
    table_lines += '\\begin{tabular}{lrrrr}\n'
    table_lines += '\\toprule\n'
    table_lines += '\\TableHeadSubjectID & \\TableHeadNeg & \\TableHeadNonPos & \\TableHeadLowThree & \\TableHeadCombined \\\\\n'
    table_lines += '\\midrule\n'
    for example in examples:
        if example == 'LANG-993':
            table_lines += '\midrule\n'
        elif example == 'IO-173':
            table_lines += '\midrule\n'
        elif example == 'CSV-159':
            table_lines += '\midrule\n'
        elif example == 'NET-525':
            table_lines += '\midrule\n'
        table_lines += '\\UseMacro{' + example.replace('-', '') + 'ID}'
        min_schemes = findMinSchemes(example)
        if 'neg' in min_schemes:
            table_lines += ' & \\textbf{\\UseMacro{' + example.replace('-', '') + \
                           'NegNumOfRuns}}'
        else:
            table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'NegNumOfRuns}'
        if 'nonpos' in min_schemes:
            table_lines += ' & \\textbf{\\UseMacro{' + example.replace('-', '') + \
                           'NonPosNumOfRuns}}'
        else:
            table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'NonPosNumOfRuns}'
        if 'low3' in min_schemes:
            table_lines += ' & \\textbf{\\UseMacro{' + example.replace('-', '') + \
                           'Low3NumOfRuns}}'
        else:
            table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'Low3NumOfRuns}'
        if 'combined' in min_schemes:
            table_lines += ' & \\textbf{\\UseMacro{' + example.replace('-', '') + \
                       'CombinedNumOfRuns}}'
        else:
            table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'CombinedNumOfRuns}'
        table_lines += ' \\\\\n'
    table_lines += '\\bottomrule\n'
    table_lines += '\\end{tabular}}\n'
    table_lines += '\\end{table}\n'
    fw = open(scheme_table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

if __name__ == '__main__':
    genProjectNumbers()
    genProjectTable()
    examples = ['COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
                'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
                'LANG-883', 'LANG-993', 'LANG-1006', 'LANG-1080', 'LANG-1093',
                'NET-525', 'NET-527',
                'CSV-159', 'CSV-175', 'CSV-180']
    genSubjectsNumbers(examples)
    genSubjectsTable(examples)
    genPartitionSchemeNumbers()
    genPartitionSchemeTable()
