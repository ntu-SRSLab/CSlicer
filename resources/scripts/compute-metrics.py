#!/usr/bin/python3

# 1. File distance: For each pair of atomic changes in a commit, if they
# are both in the same file, their file distance = the number of lines
# between them / the total number of lines of the file to which they are
# applied.

# 2. Package distance: For each pair of atomic changes in a commit, if
# they are not in the same file, their package distance = the number of
# different package name segments comparing the package names of them.

# 3. Change coupling: For each pair of commits in a given history, their
# change coupling score = the number of the files modified by both
# commits / the total number of files modified by the two commits.

# 4. Time difference: For each pair of commits in a given history, their
# time difference score = the time (in seconds) between the two commits
# / the total time span of the selected history.

# 5. Author difference: For each pair of commits in a given history,
# their author difference score = the number of unique authors making
# the commits / the total number of authors that have contributed to the
# selected history.

# 6. the number of statements / methods / files modified per commit
# out of all statements / methods / files in the project

# 7. absolute distance of (1)

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
import scipy.stats as stats
import run_examples as runex

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
_DOWNLOADS_DIR = SCRIPT_DIR + '/../../_downloads'
METRICS_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/icse20-metrics-numbers.tex'
METRICS_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/icse20-metrics-table.tex'
THEORY_CONFIG_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/ase19-optimal-configs-numbers.tex'
CSLICER_ORIG_CONFIGS_DIR = SCRIPT_DIR + '/../../resources/file-level/orig-configs'
OUTPUT_DIR = SCRIPT_DIR + '/../../resources/file-level/output'
CSLICER_JAR = SCRIPT_DIR + '/../../target/cslicer-1.0.0-jar-with-dependencies.jar'
METRICS_LOGS_DIR = SCRIPT_DIR + '/../../resources/file-level/metrics'
CORRELATION_FILE = SCRIPT_DIR + '/correlation.txt'
CORRELATION_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/icse20-correlation-numbers.tex'
CORRELATION_TABLE_TEX_FILE = SCRIPT_DIR + '/../../../ISSTA2019_Pipeline/tables/icse20-correlation-table.tex'

CONFIGS = ['SD', 'SCD', 'CSD', 'DSD', 'CDSD', 'DSCD', 'CDSCD']

examples = ['COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
            'CONFIGURATION-624', 'CONFIGURATION-626',
            'CSV-159', 'CSV-175', 'CSV-179', 'CSV-180',
            'FLUME-2628',
            'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
            'LANG-993', 'LANG-1006',
            'MNG-4904', 'MNG-4909', 'MNG-4910',
            'NET-436', 'NET-525', 'NET-527',
            'PDFBOX-3069', 'PDFBOX-3418', 'PDFBOX-3307']

example_best_configs_map = {'COMPRESS-327': 'SCD',
                            'COMPRESS-369': 'CDSCD',
                            'COMPRESS-373': 'SCD',
                            'COMPRESS-374': 'CDSCD',
                            'COMPRESS-375': 'DSD',
                            'CONFIGURATION-624': 'SCD',
                            'CONFIGURATION-626': 'SCD',
                            'CSV-159': 'SD',
                            'CSV-175': 'SCD',
                            'CSV-179': 'SCD',
                            'CSV-180': 'SCD',
                            'FLUME-2628': 'CDSCD',
                            'IO-173': 'SD',
                            'IO-275': 'DSD',
                            'IO-288': 'SCD',
                            'IO-290': 'DSD',
                            'IO-305': 'SCD',
                            'LANG-993': 'SCD',
                            'LANG-1006': 'SCD',
                            'MNG-4904': 'CDSCD',
                            'MNG-4909': 'CDSCD',
                            'MNG-4910': 'CDSCD',
                            'NET-436': 'SCD',
                            'NET-525': 'SCD',
                            'NET-527': 'SCD',
                            'PDFBOX-3069': 'DSD',
                            'PDFBOX-3418': 'CSD',
                            'PDFBOX-3307': 'SD'}

config_names_map = {'SD': 'split-definer',
                    'SCD': 'split-cslicer-definer',
                    'CSD': 'cslicer-split-definer',
                    'DSD': 'definer-split-definer',
                    'CDSD': 'cslicer-definer-split-definer',
                    'DSCD': 'definer-split-cslicer-definer',
                    'CDSCD': 'cslicer-definer-split-cslicer-definer'}

def runMetricsCollector(examples, _downloads_dir=_DOWNLOADS_DIR, \
                        metrics_logs_dir=METRICS_LOGS_DIR, \
                        cslicer_jar=CSLICER_JAR):
    for example in examples:
        start, end, repo_name, test_suite, repo_path, lines, config_file = \
                                                runex.extractInfoFromCSlicerConfigs(example)
        os.chdir(repo_path)
        sub.run('git stash', shell=True)
        sub.run('git checkout ' + end, shell=True)
        sub.run('java -jar ' + cslicer_jar + ' -c ' + config_file + ' -e metrics', shell=True, \
                stdout=open(metrics_logs_dir + '/' + example + '.metrics', 'w'), \
                stderr=sub.STDOUT)

def extractMetricsFromLogFile(log_file):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('[DEBUG] [METRIC] Avg file distance:'):
            avg_file_distance = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg package distance:'):
            avg_package_distance = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg change coupling score:'):
            avg_change_coupling_score = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg time diff score:'):
            avg_time_diff_score = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg author diff score:'):
            avg_author_diff_score = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg num of changed files:'):
            avg_num_of_changed_files = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg num of changed lines:'):
            avg_num_of_changed_lines = str(round(float(lines[i].strip().split()[-1]), 2))
        if lines[i].startswith('[DEBUG] [METRIC] Avg absolute file distance:'):
            avg_abs_file_distance = str(round(float(lines[i].strip().split()[-1]), 2))
    return avg_file_distance, avg_package_distance, avg_change_coupling_score, \
            avg_time_diff_score, avg_author_diff_score, avg_num_of_changed_files, \
            avg_num_of_changed_lines, avg_abs_file_distance

def genMetricsNumbers(examples, metrics_logs_dir=METRICS_LOGS_DIR, \
                      metrics_numbers_tex_file=METRICS_NUMBERS_TEX_FILE):
    lines = ''
    for example in examples:
        metrics_log = metrics_logs_dir + '/' + example + '.metrics'
        avg_file_distance, avg_package_distance, avg_change_coupling_score, \
            avg_time_diff_score, avg_author_diff_score, avg_num_of_changed_files, \
            avg_num_of_changed_lines, avg_abs_file_distance = \
                                                extractMetricsFromLogFile(metrics_log)
        lines += '\\DefMacro{' + example + 'AVGFileDistance}{' + avg_file_distance + '}\n'
        lines += '\\DefMacro{' + example + 'AVGPackageDistance}{' + avg_package_distance + '}\n'
        lines += '\\DefMacro{' + example + 'AVGChangeCouplingScore}{' + \
                 avg_change_coupling_score + '}\n'
        lines += '\\DefMacro{' + example + 'AVGTimeDiffScore}{' + avg_time_diff_score + '}\n'
        lines += '\\DefMacro{' + example + 'AVGAuthorDiffScore}{' + avg_author_diff_score + '}\n'
        lines += '\\DefMacro{' + example + 'AVGNumOfChangedFiles}{' + \
                avg_num_of_changed_files + '}\n'
        lines += '\\DefMacro{' + example + 'AVGNumOfChangedLines}{' + \
                 avg_num_of_changed_lines + '}\n'
        lines += '\\DefMacro{' + example + 'AVGAbsoluteFileDistance}{' + \
                 avg_abs_file_distance + '}\n'
        lines += '\\DefMacro{' + example + 'BestConfig}{' + \
                 example_best_configs_map[example] + '}\n'
        fw = open(metrics_numbers_tex_file, 'w')
        fw.write(lines)
        fw.close()

def genMetricsTable(examples, table_tex_file=METRICS_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by compute-metrics.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionMetrics}\n"
    table_lines += "\\begin{tabular}{lrrrrrrrr|l}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadExampleId & \\TableHeadAVGFileDistance & \\TableHeadAVGPackageDistance & \\TableHeadAVGChangeCouplingScore & \\TableHeadAVGTimeDiffScore & \\TableHeadAVGAuthorDiffScore  & \\TableHeadAVGNumOfChangedFiles  & \\TableHeadAVGNumOfChangedLines & \\TableHeadAVGAbsoluteFileDistance & \\TableHeadBestConfig \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        table_lines +=  example.lower()
        table_lines += ' & \\UseMacro{' + example + 'AVGFileDistance}'
        table_lines += ' & \\UseMacro{' + example + 'AVGPackageDistance}'
        table_lines += ' & \\UseMacro{' + example + 'AVGChangeCouplingScore}'
        table_lines += ' & \\UseMacro{' + example + 'AVGTimeDiffScore}'
        table_lines += ' & \\UseMacro{' + example + 'AVGAuthorDiffScore}'
        table_lines += ' & \\UseMacro{' + example + 'AVGNumOfChangedFiles}'
        table_lines += ' & \\UseMacro{' + example + 'AVGNumOfChangedLines}'
        table_lines += ' & \\UseMacro{' + example + 'AVGAbsoluteFileDistance}'
        table_lines += ' & \\UseMacro{' + example + 'BestConfig}'
        table_lines += ' \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table*}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

def queryMetricFromTexFile(example, metric, metrics_numbers_tex_file=METRICS_NUMBERS_TEX_FILE):
    fr = open(metrics_numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if example + metric in lines[i]:
            metric_value = float(lines[i].split('{')[2].split('}')[0])
    return metric_value

def computePerformanceScore(example, config, numbers_tex_file=THEORY_CONFIG_NUMBERS_TEX_FILE):
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if example in lines[i] and 'textbf' in lines[i]:
            best_time = float(lines[i].split('}')[-3].split('{')[-1])
        if example + config_names_map[config] + 'Runtime' in lines[i]:
            if 'textbf' in lines[i]:
                config_time = best_time
            else:
                config_time = float(lines[i].split('}')[-2].split('{')[-1])
    config_performance_score = (config_time - best_time) / config_time
    return round(config_performance_score, 2)

def printPearsonScore(x, y, r, p_value):
    print (x + ' -- ' + y + ', R = ' + '{0:.3f}'.format(r) + \
           ', P-Value: ' + '{0:.3f}'.format(p_value))

def printMannWhitneyScore(x, gorl, config, stat, p_value):
    print (x + ' is ' + gorl + ' if ' + config + ' is the best config' + ', stat = ' + \
           '{0:.3f}'.format(stat) + ', P-Value: ' + '{0:.3f}'.format(p_value))

def computeConfigPerformanceCorrelation(examples, config, file_distance_vector, \
                                        package_distance_vector, change_coupling_score_vector, \
                                        time_diff_score_vector, author_diff_score_vector, \
                                        num_of_changed_files_vector, \
                                        num_of_changed_lines_vector, \
                                        absolute_file_distance_vector, \
                                        combine_change_coupling_and_author_diff_vector):
    # 1. correlation between the performance of “SCD” with metric X:
    # the output variable would be, time(SCD) - time(best) / time(SCD)
    avg_config_performance_score_vector = []
    for example in examples:
        avg_config_performance_score_vector.append(computePerformanceScore(example, config))
    #print (avg_config_performance_score_vector)
    r, p_value = stats.pearsonr(file_distance_vector, avg_config_performance_score_vector)
    printPearsonScore('AVGFileDistance', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(package_distance_vector, avg_config_performance_score_vector)
    printPearsonScore('AVGPackageDistance', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(change_coupling_score_vector, \
                                avg_config_performance_score_vector)
    printPearsonScore('AVGChangeCouplingScore', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(time_diff_score_vector, avg_config_performance_score_vector)
    printPearsonScore('AVGTimeDiffScore', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(author_diff_score_vector, avg_config_performance_score_vector)
    printPearsonScore('AVGAuthorDiffScore', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(num_of_changed_files_vector, avg_config_performance_score_vector)
    printPearsonScore('AVGNumOfChangedFiles', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(num_of_changed_lines_vector, avg_config_performance_score_vector)
    printPearsonScore('AVGNumOfChangedLines', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(absolute_file_distance_vector, \
                                avg_config_performance_score_vector)
    printPearsonScore('AVGAbsoluteFileDistance', config + ' Performance Score', r, p_value)
    r, p_value = stats.pearsonr(combine_change_coupling_and_author_diff_vector, \
                                avg_config_performance_score_vector)
    # printPearsonScore('AVGChangeCouplingScore & AVGAuthorDiffScore', \
    #                   config + ' Performance Score', r, p_value)

def computeConfigMannWhitney(examples, config, file_distance_vector, package_distance_vector, \
                             change_coupling_score_vector, time_diff_score_vector, \
                             author_diff_score_vector, num_of_changed_files_vector, \
                             num_of_changed_lines_vector, absolute_file_distance_vector, \
                             combine_change_coupling_and_author_diff_vector):
    #print (config)
    file_distance_best_vector = []
    file_distance_not_best_vector = []
    package_distance_best_vector = []
    package_distance_not_best_vector = []
    change_coupling_score_best_vector = []
    change_coupling_score_not_best_vector = []
    time_diff_score_best_vector = []
    time_diff_score_not_best_vector = []
    author_diff_score_best_vector = []
    author_diff_score_not_best_vector = []
    num_of_changed_files_best_vector = []
    num_of_changed_files_not_best_vector = []
    num_of_changed_lines_best_vector = []
    num_of_changed_lines_not_best_vector = []
    absolute_file_distance_best_vector = []
    absolute_file_distance_not_best_vector = []
    # combine_change_coupling_and_author_diff_best_vector = []
    # combine_change_coupling_and_author_diff_not_best_vector = []
    indices = []
    for i in range(len(examples)):
        example = examples[i]
        best_config = example_best_configs_map[example]
        if best_config == config:
            indices.append(i+1)
            file_distance_best_vector.append(file_distance_vector[i])
            package_distance_best_vector.append(package_distance_vector[i])
            change_coupling_score_best_vector.append(change_coupling_score_vector[i])
            time_diff_score_best_vector.append(time_diff_score_vector[i])
            author_diff_score_best_vector.append(author_diff_score_vector[i])
            num_of_changed_files_best_vector.append(num_of_changed_files_vector[i])
            num_of_changed_lines_best_vector.append(num_of_changed_lines_vector[i])
            absolute_file_distance_best_vector.append(absolute_file_distance_vector[i])
        else:
            file_distance_not_best_vector.append(file_distance_vector[i])
            package_distance_not_best_vector.append(package_distance_vector[i])
            change_coupling_score_not_best_vector.append(change_coupling_score_vector[i])
            time_diff_score_not_best_vector.append(time_diff_score_vector[i])
            author_diff_score_not_best_vector.append(author_diff_score_vector[i])
            num_of_changed_files_not_best_vector.append(num_of_changed_files_vector[i])
            num_of_changed_lines_not_best_vector.append(num_of_changed_lines_vector[i])
            absolute_file_distance_not_best_vector.append(absolute_file_distance_vector[i])
    #print (indices)
    # 
    stat, p_value = stats.mannwhitneyu(file_distance_best_vector, \
                                       file_distance_not_best_vector, alternative='greater')
    printMannWhitneyScore('AVGFileDistance', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(file_distance_best_vector, \
                                       file_distance_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGFileDistance', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(package_distance_best_vector, \
                                       package_distance_not_best_vector, alternative='greater')
    printMannWhitneyScore('AVGPackageDistance', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(package_distance_best_vector, \
                                       package_distance_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGPackageDistance', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(change_coupling_score_best_vector, \
                                       change_coupling_score_not_best_vector, \
                                       alternative='greater')
    printMannWhitneyScore('AVGChangeCouplingScore', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(change_coupling_score_best_vector, \
                                       change_coupling_score_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGChangeCouplingScore', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(time_diff_score_best_vector, \
                                       time_diff_score_not_best_vector, alternative='greater')
    printMannWhitneyScore('AVGTimeDiffScore', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(time_diff_score_best_vector, \
                                       time_diff_score_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGTimeDiffScore', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(author_diff_score_best_vector, \
                                       author_diff_score_not_best_vector, alternative='greater')
    printMannWhitneyScore('AVGAuthorDiffScore', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(author_diff_score_best_vector, \
                                       author_diff_score_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGAuthorDiffScore', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(num_of_changed_files_best_vector, \
                                       num_of_changed_files_not_best_vector, \
                                       alternative='greater')
    printMannWhitneyScore('AVGNumOfChangedFiles', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(num_of_changed_files_best_vector, \
                                       num_of_changed_files_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGNumOfChangedFiles', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(num_of_changed_lines_best_vector, \
                                       num_of_changed_lines_not_best_vector, \
                                       alternative='greater')
    printMannWhitneyScore('AVGNumOfChangedLines', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(num_of_changed_lines_best_vector, \
                                       num_of_changed_lines_not_best_vector, alternative='less')
    printMannWhitneyScore('AVGNumOfChangedLines', 'less', config, stat, p_value)
    # 
    stat, p_value = stats.mannwhitneyu(absolute_file_distance_best_vector, \
                                       absolute_file_distance_not_best_vector, \
                                       alternative='greater')
    printMannWhitneyScore('AVGAbsoluteFileDistance', 'greater', config, stat, p_value)
    stat, p_value = stats.mannwhitneyu(absolute_file_distance_best_vector, \
                                       absolute_file_distance_not_best_vector, \
                                       alternative='less')
    printMannWhitneyScore('AVGAbsoluteFileDistance', 'less', config, stat, p_value)
    #print (file_distance_best_vector)
    #print (file_distance_not_best_vector)

def reportCorrelation(examples, configs=CONFIGS):
    # X
    file_distance_vector = []
    package_distance_vector = []
    change_coupling_score_vector = []
    time_diff_score_vector = []
    author_diff_score_vector = []
    num_of_changed_files_vector = []
    num_of_changed_lines_vector = []
    absolute_file_distance_vector = []
    #
    combine_change_coupling_and_author_diff_vector = []
    for example in examples:
        file_distance_vector.append(queryMetricFromTexFile(example, \
                                                           'AVGFileDistance'))
        package_distance_vector.append(queryMetricFromTexFile(example, \
                                                              'AVGPackageDistance'))
        change_coupling_score_vector.append(queryMetricFromTexFile(example, \
                                                                   'AVGChangeCouplingScore'))
        time_diff_score_vector.append(queryMetricFromTexFile(example, \
                                                             'AVGTimeDiffScore'))
        author_diff_score_vector.append(queryMetricFromTexFile(example, \
                                                               'AVGAuthorDiffScore'))
        num_of_changed_files_vector.append(queryMetricFromTexFile(example, \
                                                                  'AVGNumOfChangedFiles'))
        num_of_changed_lines_vector.append(queryMetricFromTexFile(example, \
                                                                  'AVGNumOfChangedLines'))
        absolute_file_distance_vector.append(queryMetricFromTexFile(example, \
                                                                    'AVGAbsoluteFileDistance'))
    #
    combine_change_coupling_and_author_diff_vector = \
            [(change_coupling_score_vector[i] + author_diff_score_vector[i]) / 2 \
             for i in range(len(change_coupling_score_vector))]
    #
    for config in configs:
        computeConfigPerformanceCorrelation(examples, config, file_distance_vector, \
                                            package_distance_vector, \
                                            change_coupling_score_vector, \
                                            time_diff_score_vector, author_diff_score_vector, \
                                            num_of_changed_files_vector, \
                                            num_of_changed_lines_vector, \
                                            absolute_file_distance_vector, \
                                            combine_change_coupling_and_author_diff_vector)
        print ('======================================================')
    #
    for config in configs:
        if config == 'CDSD' or config == 'DSCD':
            continue
        computeConfigMannWhitney(examples, config, file_distance_vector, \
                                 package_distance_vector, change_coupling_score_vector, \
                                 time_diff_score_vector, author_diff_score_vector, \
                                 num_of_changed_files_vector, num_of_changed_lines_vector, \
                                 absolute_file_distance_vector, \
                                 combine_change_coupling_and_author_diff_vector)
        print ('======================================================')


def genCorrelatonNumbers(examples, correlation_file=CORRELATION_FILE, \
                         numbers_tex_file=CORRELATION_NUMBERS_TEX_FILE):
    numbers_lines = ''
    fr = open(correlation_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        # pearson
        if ' -- ' in lines[i]:
            metric = lines[i].split()[0]
            config = lines[i].split()[2]
            p_value = lines[i].strip().split()[-1]
            if float(p_value) <= 0.05:
                p_value_str = '\\textbf{' + p_value + '}'
            else:
                p_value_str = p_value
            numbers_lines += '\\DefMacro{' + metric + '-' + config + '-' + \
                             'PerformancePearsonCorrelation}{' + p_value_str + '}\n'
        # mann whitney
        if 'is the best config' in lines[i]:
            metric = lines[i].split()[0]
            gol = lines[i].split()[2]
            config = lines[i].split()[4]
            p_value = lines[i].strip().split()[-1]
            if float(p_value) <= 0.05:
                p_value_str = '\\textbf{' + p_value + '}'
            else:
                p_value_str = p_value
            numbers_lines += '\\DefMacro{' + metric + '-' + gol + '-' + config + '-' + \
                             'BestMannWhitneyCorrelation}{' + p_value_str + '}\n'
    fw = open(numbers_tex_file, 'w')
    fw.write(numbers_lines)
    fw.close()

def genCorrelationTable(examples, configs=CONFIGS, table_tex_file=CORRELATION_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by compute-metrics.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionCorrelation}\n"
    table_lines += "\\begin{tabular}{lrrrrrrrr}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadConfig & \\TableHeadAVGFileDistance & \\TableHeadAVGPackageDistance & \\TableHeadAVGChangeCouplingScore & \\TableHeadAVGTimeDiffScore & \\TableHeadAVGAuthorDiffScore  & \\TableHeadAVGNumOfChangedFiles  & \\TableHeadAVGNumOfChangedLines & \\TableHeadAVGAbsoluteFileDistance \\\\\n"
    table_lines += "\\midrule\n"
    table_lines += "\\multicolumn{9}{c}{\\TableHeadPearsonPValue} \\\\\n"
    table_lines += "\\midrule\n"
    for config in configs:
        table_lines +=  config
        table_lines += ' & \\UseMacro{AVGFileDistance-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGPackageDistance-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGChangeCouplingScore-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGTimeDiffScore-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGAuthorDiffScore-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGNumOfChangedFiles-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGNumOfChangedLines-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' & \\UseMacro{AVGAbsoluteFileDistance-' + config + \
                       '-PerformancePearsonCorrelation}'
        table_lines += ' \\\\\n'
    table_lines += "\\midrule\n"
    table_lines += "\\multicolumn{9}{c}{\\TableHeadMannWhitneyGreaterPValue} \\\\\n"
    table_lines += "\\midrule\n"
    for config in configs:
        if config == 'CDSD' or config == 'DSCD':
            continue
        table_lines +=  config
        table_lines += ' & \\UseMacro{AVGFileDistance-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGPackageDistance-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGChangeCouplingScore-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGTimeDiffScore-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGAuthorDiffScore-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGNumOfChangedFiles-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGNumOfChangedLines-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGAbsoluteFileDistance-greater-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' \\\\\n'
    table_lines += "\\midrule\n"
    table_lines += "\\multicolumn{9}{c}{\TableHeadMannWhitneyLessPValue} \\\\\n"
    table_lines += "\\midrule\n"
    for config in configs:
        if config == 'CDSD' or config == 'DSCD':
            continue
        table_lines +=  config
        table_lines += ' & \\UseMacro{AVGFileDistance-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGPackageDistance-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGChangeCouplingScore-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGTimeDiffScore-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGAuthorDiffScore-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGNumOfChangedFiles-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGNumOfChangedLines-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' & \\UseMacro{AVGAbsoluteFileDistance-less-' + config + \
                       '-BestMannWhitneyCorrelation}'
        table_lines += ' \\\\\n'
    table_lines += "\\bottomrule\n"
    table_lines += "\\end{tabular}\n"
    table_lines += "\\end{center}\n"
    table_lines += "\\end{small}\n"
    table_lines += "\\end{table*}\n"
    fw = open(table_tex_file, 'w')
    fw.write(table_lines)
    fw.close()

if __name__ == '__main__':
    #runMetricsCollector(examples)
    #genMetricsNumbers(examples)
    #genMetricsTable(examples)
    reportCorrelation(examples)
    genCorrelatonNumbers(examples)
    genCorrelationTable(examples)

    


# --- deprecated ---
def computeCImprovementCorrelation(examples, file_distance_vector, package_distance_vector, \
                                   change_coupling_score_vector, time_diff_score_vector, \
                                   author_diff_score_vector, num_of_changed_files_vector, \
                                   num_of_changed_lines_vector, absolute_file_distance_vector, \
                                   combine_change_coupling_and_author_diff_vector):
    # 2. how “C” is effective on projects which are high in metric X: the
    # for each pair of XCX and XX, (time(XX) - time(XCX) / time(XX)), take the average
    avg_C_improvement_score_vector = []                                   
    for example in examples:
        avg_C_improvement_score_vector.append(computeCImprovementScore(example))
    print (avg_C_improvement_score_vector)
    r, p_value = stats.pearsonr(file_distance_vector, avg_C_improvement_score_vector)
    print ('AVGFileDistance -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(package_distance_vector, avg_C_improvement_score_vector)
    print ('AVGPackageDistance -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(change_coupling_score_vector, avg_C_improvement_score_vector)
    print ('AVGChangeCouplingScore -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(time_diff_score_vector, avg_C_improvement_score_vector)
    print ('AVGTimeDiffScore -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(author_diff_score_vector, avg_C_improvement_score_vector)
    print ('AVGAuthorDiffScore -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(num_of_changed_files_vector, avg_C_improvement_score_vector)
    print ('AVGNumOfChangedFiles -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(num_of_changed_lines_vector, avg_C_improvement_score_vector)
    print ('AVGNumOfChangedLines -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(absolute_file_distance_vector, avg_C_improvement_score_vector)
    print ('AVGAbsoluteFileDistance -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))
    r, p_value = stats.pearsonr(combine_change_coupling_and_author_diff_vector, \
                                avg_C_improvement_score_vector)
    print ('AVGChangeCouplingScore & AVGAuthorDiffScore -- C Improvement Score, R = ' + str(r) + ' P-Value: ' + \
           '{0:.3f}'.format(p_value))

# --- deprecated ---
def computeCImprovementScore(example, numbers_tex_file=THEORY_CONFIG_NUMBERS_TEX_FILE):
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if example + 'split-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                SD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                SD_time = float(lines[i].split('}')[-2].split('{')[-1])
        elif example + 'split-cslicer-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                SCD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                SCD_time = float(lines[i].split('}')[-2].split('{')[-1])
        elif example + 'cslicer-split-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                CSD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                CSD_time = float(lines[i].split('}')[-2].split('{')[-1])
        elif example + 'definer-split-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                DSD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                DSD_time = float(lines[i].split('}')[-2].split('{')[-1])
        elif example + 'cslicer-definer-split-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                CDSD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                CDSD_time = float(lines[i].split('}')[-2].split('{')[-1])
        elif example + 'definer-split-cslicer-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                DSCD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                DSCD_time = float(lines[i].split('}')[-2].split('{')[-1])
        elif example + 'cslicer-definer-split-cslicer-definerRuntime' in lines[i]:
            if 'textbf' in lines[i]:
                CDSCD_time = float(lines[i].split('}')[-3].split('{')[-1])
            else:
                CDSCD_time = float(lines[i].split('}')[-2].split('{')[-1])
    # SD vs SCD
    SCD_improve_SD = (SD_time - SCD_time) / SD_time
    # SD vs CSD
    CSD_improve_SD = (SD_time - CSD_time) / SD_time
    # DSD vs CDSD
    CDSD_improve_DSD = (DSD_time - CDSD_time) / DSD_time
    # DSD vs DSCD
    DSCD_improve_DSD = (DSD_time - DSCD_time) / DSD_time
    # CDSD vs CDSCD
    CDSCD_improve_CDSD = (CDSD_time - CDSCD_time) / CDSD_time
    # DSCD vs CDSCD
    CDSCD_improve_DSCD = (DSCD_time - CDSCD_time) / DSCD_time
    avg_improvement_score = (SCD_improve_SD + CSD_improve_SD + CDSD_improve_DSD + \
                            DSCD_improve_DSD + CDSCD_improve_CDSD + CDSCD_improve_DSCD) / 6
    return round(avg_improvement_score, 2)
