#!/usr/bin/python3

import os
import os.path
import sys
import csv
import collections
import argparse
import subprocess as sub
import run_examples as runex
import gen_all_configs_table as gentables

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
DATA_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/allconfigs-numbers.tex'
END_TO_END_TIME_NUMBERS_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/end-to-end-time-numbers.tex'
END_TO_END_TIME_TABLE_TEX_FILE = SCRIPT_DIR + '/../../resources/file-level/results/tables/end-to-end-time-table.tex'

def genDataDictFromDataNumbersTex(examples=runex.examples, configs=gentables.configs, \
                                  data_numbers_tex_file=DATA_NUMBERS_TEX_FILE):
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

def genEndToEndTimeNumbers(data_dict, examples=runex.examples, \
                           end_to_end_time_numbers_tex_file=END_TO_END_TIME_NUMBERS_TEX_FILE):
    lines = ''
    end_to_end_time_dict = collections.OrderedDict({})
    for example in examples:
        print (example)
        example_id = example.replace('-', '')
        end_to_end_time_dict[example] = collections.OrderedDict({})
        # Total time taken by running the 7 “reasonable” configurations
        # (C, D, CD, CSD, SCD, SC, SD) one by one
        total_time_C_D_CD_CSD_SCD_SC_SD = float(data_dict[example]['cslicer']['runtime']) + \
                                          float(data_dict[example]['definer']['runtime']) + \
                                  float(data_dict[example]['cslicer-definer']['runtime']) + \
                            float(data_dict[example]['cslicer-split-definer']['runtime']) + \
                            float(data_dict[example]['split-cslicer-definer']['runtime']) + \
                                    float(data_dict[example]['split-cslicer']['runtime']) + \
                                    float(data_dict[example]['split-definer']['runtime'])
        total_time_C_D_CD_CSD_SCD_SC_SD = round(total_time_C_D_CD_CSD_SCD_SC_SD, 2)
        end_to_end_time_dict[example]['total_time_C_D_CD_CSD_SCD_SC_SD'] = \
                                                                total_time_C_D_CD_CSD_SCD_SC_SD
        # Total time taken by running the 5 configurations (D, CD, CSD, SCD, SD) one by one
        total_time_D_CD_CSD_SCD_SD = float(data_dict[example]['definer']['runtime']) + \
                                  float(data_dict[example]['cslicer-definer']['runtime']) + \
                            float(data_dict[example]['cslicer-split-definer']['runtime']) + \
                            float(data_dict[example]['split-cslicer-definer']['runtime']) + \
                                    float(data_dict[example]['split-definer']['runtime'])
        total_time_D_CD_CSD_SCD_SD = round(total_time_D_CD_CSD_SCD_SD, 2)
        end_to_end_time_dict[example]['total_time_D_CD_CSD_SCD_SD'] = total_time_D_CD_CSD_SCD_SD
        # Total time taken by running the 5 configurations and sharing intermediate results
        # (not running duplicates): D + C + (CD - C) + (CSD - C) + S + (SCD - S) + (SD - S)
        total_time_shared = float(data_dict[example]['definer']['runtime']) + \
                                     float(data_dict[example]['cslicer']['runtime']) + \
                             float(data_dict[example]['cslicer-definer']['runtime']) - \
                                     float(data_dict[example]['cslicer']['runtime']) + \
                       float(data_dict[example]['cslicer-split-definer']['runtime']) - \
                                     float(data_dict[example]['cslicer']['runtime']) + \
                       float(data_dict[example]['split-cslicer-definer']['runtime']) + \
                               float(data_dict[example]['split-definer']['runtime'])
                               # - S !
        total_time_shared = round(total_time_shared, 2)
        end_to_end_time_dict[example]['total_time_shared'] = total_time_shared
        #print (end_to_end_time_dict)
        lines += '\\DefMacro{' + example_id + 'SevenConfigsTime}{' + \
                 str(total_time_C_D_CD_CSD_SCD_SC_SD) + '}\n'
        lines += '\\DefMacro{' + example_id + 'FiveConfigsTime}{' + \
                 str(total_time_D_CD_CSD_SCD_SD) + '}\n'
        lines += '\\DefMacro{' + example_id + 'SharedConfigsTime}{' + \
                 str(total_time_shared) + '}\n'
    fw = open(end_to_end_time_numbers_tex_file, 'w')
    fw.write(lines)
    fw.close()
    return end_to_end_time_dict

def genEndToEndTimeTable(data_dict, examples=runex.examples, \
                         table_tex_file=END_TO_END_TIME_TABLE_TEX_FILE):
    table_lines = ''
    table_lines += "%% Automatically generated by compute_end_to_end_time.py\n"
    table_lines += "\\begin{table*}[t]\n"
    table_lines += "\\begin{small}\n"
    table_lines += "\\begin{center}\n"
    table_lines += "\\caption{\\TableCaptionEndToEndTime}\n"
    table_lines += "\\begin{tabular}{l|rrr}\n"
    table_lines += "\\toprule\n"
    table_lines += "\\TableHeadExampleId & \\TableHeadSevenConfigsTime & \\TableHeadFiveConfigsTime & \\TableHeadSharedConfigsTime \\\\\n"
    table_lines += "\\midrule\n"
    for example in examples:
        table_lines +=  example
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'SevenConfigsTime}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'FiveConfigsTime}'
        table_lines += ' & \\UseMacro{' + example.replace('-', '') + 'SharedConfigsTime}'
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
    data_dict = genDataDictFromDataNumbersTex()
    genEndToEndTimeNumbers(data_dict)
    genEndToEndTimeTable(data_dict)
