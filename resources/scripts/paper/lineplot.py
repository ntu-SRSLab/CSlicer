#!/usr/bin/python3

import os
import re

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
DATA_DIR = SCRIPT_DIR + '/../../../../ASEJ2018_SemanticSlicing/plots/data'
LINEPLOT_TEX_FILE = SCRIPT_DIR + '/../../../../ASEJ2018_SemanticSlicing/plots/reduction.tex'
OUTPUT_DIR = SCRIPT_DIR + '/../../file-level/output'
ALL_CONFIGS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                               '/../../file-level/results/tables/allconfigs-numbers.tex'
EFFECTIVENESS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/effectiveness-exp-numbers.tex'

EXAMPLES = ['LANG-883', 'LANG-993', 'LANG-1006', 'LANG-1080', 'LANG-1093',
            'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
            'COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
            'CSV-159', 'CSV-175', 'CSV-180', 'NET-525', 'NET-527']

CONFIGS = ['definer', 'learning', 'basic']

def getHistoryLen(example, numbers_tex_file=ALL_CONFIGS_NUMBERS_TEX_FILE):
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if example.replace('-', '') + 'OrigHisLen' in lines[i]:
            his_len = lines[i].split('{')[2].split('}')[0]
    return his_len

def extractTupleList(example, config, output_dir=OUTPUT_DIR):
    if config == 'definer':
        log_file = output_dir + '/definer/' + example + '.log'
    elif config == 'learning':
        log_file = output_dir + '/definer-learning/' + example + '.log'
    elif config == 'basic':
        log_file = output_dir + '/definer-basic/' + example + '.log'
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    tuple_list = []
    tuple_list.append(('0', getHistoryLen(example)))
    pattern = re.compile('.* at Iteration .* after .* test.*.$')
    for i in range(len(lines)):
        if re.match(pattern, lines[i].strip()):
            num_of_tests = lines[i].strip().split()[-2]
            hstar = lines[i].strip().split(' = ')[1].split()[0]
            tuple_list.append((num_of_tests, hstar))
    return tuple_list

def genCsdDatFile(config, example, data_dir=DATA_DIR):
    # (#tests, H*)
    tuple_list = extractTupleList(example, config)
    lines = ''
    lines += 'tests,hstar\n'
    for t in tuple_list:
        lines += t[0] + ',' + t[1] + '\n'
    fw = open(data_dir + '/' + example + '-' + config + '.dat', 'w')
    fw.write(lines)
    fw.close()

def genAllCsdDatFiles(examples=EXAMPLES, configs=CONFIGS):
    for example in examples:
        for config in configs:
            genCsdDatFile(config, example)

if __name__ == '__main__':
    genAllCsdDatFiles()
