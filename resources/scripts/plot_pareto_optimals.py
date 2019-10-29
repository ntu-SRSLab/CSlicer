#!/usr/bin/python3

import os
import os.path
import sys
import csv
import argparse
import subprocess as sub
import run_examples as runex
import gen_all_configs_table as allconfigs
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import seaborn as sns
import pandas as pd
from adjustText import adjust_text

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
DATA_FILES_DIR = SCRIPT_DIR + '/../../resources/file-level/pareto-data'
PLOTS_DIR = SCRIPT_DIR + '/../../resources/file-level/results/plots/pareto'
PLOT_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/plot.tex'
ALLCONFIGS_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-numbers.tex'

EXAMPLES = runex.examples
CONFIGS = allconfigs.configs

def get_pareto_data_dict(examples=EXAMPLES, configs=CONFIGS, \
                         allconfigs_numbers_tex_file=ALLCONFIGS_NUMBERS_TEX_FILE):
    fr = open(allconfigs_numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    data_dict = {}
    for example in examples:
        data_dict[example] = {}
        for config in configs:
            data_dict[example][config] = {}
            #print (example, config)
            for i in range(len(lines)):
                if lines[i].startswith('\\DefMacro{' + example.replace('-', '') + \
                                       config.replace('-', '') + 'ChangedLinesReduction}{'):
                    loc_red = lines[i].split('}{')[1].split('}')[0]
                    if loc_red == 'TO':
                        data_dict[example][config]['loc_red'] = '0'
                    else:
                        data_dict[example][config]['loc_red'] = loc_red.replace('\\%', '')
                elif lines[i].startswith('\\DefMacro{' + example.replace('-', '') + \
                                         config.replace('-', '') + 'RunTime}{'):
                    time = lines[i].split('}{')[1].split('}')[0]
                    if time == 'TO':
                        data_dict[example][config]['time'] = '9999'
                    else:
                        data_dict[example][config]['time'] = time
    return data_dict

def gen_pareto_data_files(examples=EXAMPLES, configs=CONFIGS, data_files_dir=DATA_FILES_DIR, \
                          allconfigs_numbers_tex_file=ALLCONFIGS_NUMBERS_TEX_FILE):
    data_dict = get_pareto_data_dict()
    #print (data_dict['IO-173'])
    for example in examples:
        data_lines = '#config,loc.red,time\n'
        for config in configs:
            data_lines += config + ',' + data_dict[example][config]['loc_red'] + ',' + \
                          data_dict[example][config]['time'] + '\n'
        fw = open(data_files_dir + '/' + example + '.txt', 'w')
        fw.write(data_lines)
        fw.close()

def run_pareto_cmd(input_file):
    p = sub.Popen('./pareto.py ' + input_file + \
                  ' -o 1-2 -m 1 --delimiter=\',\' --comment=\"#\"', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    lines = p.stdout.readlines()
    pareto_loc_red = []
    pareto_time = []
    for i in range(len(lines)):
        lines[i] = lines[i].decode("utf-8")[:-1]
        pareto_loc_red.append(float(lines[i].split(',')[1]))
        pareto_time.append(float(lines[i].strip().split(',')[2]))
    return pareto_loc_red, pareto_time

def plot_pareto_optimals(examples=EXAMPLES, configs=CONFIGS, plots_dir=PLOTS_DIR, \
                         data_files_dir=DATA_FILES_DIR):
    sns.set()
    matplotlib.rcParams.update({'font.size': 8})
    data_dict = get_pareto_data_dict()
    for example in examples:
        loc_red = []
        time = []
        annotations = []
        for config in configs:
            if data_dict[example][config]['loc_red'] == '0' and \
               data_dict[example][config]['time'] == '9999':
                continue
            loc_red.append(float(data_dict[example][config]['loc_red']))
            time.append(float(data_dict[example][config]['time']))
            annotations.append(config)
        ax = plt.subplot(111)
        ax.scatter(loc_red, time, color='dodgerblue', marker='*', label='Others')
        texts = [plt.text(loc_red[i], time[i], annotations[i]) for i in range(len(loc_red))]
        adjust_text(texts, loc_red, time, arrowprops=dict(arrowstyle="->", color='k', lw=0.5))
        pareto_loc_red, pareto_time = run_pareto_cmd(data_files_dir + '/' + example + '.txt')
        ax.scatter(pareto_loc_red, pareto_time, color='orangered', marker='*', label='Pareto optimal')
        ax.legend(loc='upper center', bbox_to_anchor=(0.5, 1.15), ncol=4, fancybox=True)
        #plt.xticks(range(len(loc_red)), loc_red, rotation=45)
        plt.xlim(0, 100)
        plt.ylabel('Time (s)')
        plt.xlabel('LOC.Red (%)')
        #fig = plt.gcf()
        plt.savefig(plots_dir + '/' +  example + '-pareto.eps')
        #plt.show()
        plt.clf() # MUST CLEAN

def generate_plot_tex(examples=EXAMPLES, plot_tex_file=PLOT_TEX_FILE):
    lines = ''
    for example in examples:
        lines += '\\begin{figure}\n'
        lines += '\\includegraphics[scale=0.7]{plots/pareto/' + example + '-pareto}\n'
        lines += '\\caption{' + example + '}\n'
        lines += '\\end{figure}\n'
    fw = open(plot_tex_file, 'w')
    fw.write(lines)
    fw.close()

if __name__ == '__main__':
    #gen_pareto_data_files()
    plot_pareto_optimals()
    generate_plot_tex()
