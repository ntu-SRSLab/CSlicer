#!/usr/bin/python3

import os

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
DATA_DIR = SCRIPT_DIR + '/../../../../ASEJ2018_SemanticSlicing/plots/data'
BARCHART_TEX_FILE = SCRIPT_DIR + '/../../../../ASEJ2018_SemanticSlicing/plots/slicesize.tex'
ALL_CONFIGS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                               '/../../file-level/results/tables/allconfigs-numbers.tex'
EFFECTIVENESS_NUMBERS_TEX_FILE = SCRIPT_DIR + \
                    '/../../../../ASEJ2018_SemanticSlicing/tables/effectiveness-exp-numbers.tex'

EXAMPLES = ['COMPRESS-327', 'COMPRESS-369', 'COMPRESS-373', 'COMPRESS-374', 'COMPRESS-375',
            'IO-173', 'IO-275', 'IO-288', 'IO-290', 'IO-305',
            'LANG-993', 'LANG-1006',
            'NET-525', 'NET-527',
            'CSV-159', 'CSV-175', 'CSV-180']

CONFIGS = ['cslicer', 'definer', 'splitdefiner']

def getHistoryLen(example, numbers_tex_file=ALL_CONFIGS_NUMBERS_TEX_FILE):
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if example.replace('-', '') + 'OrigHisLen' in lines[i]:
            his_len = lines[i].split('{')[2].split('}')[0]
    return int(his_len)

def extractTupleList(config, examples=EXAMPLES, numbers_tex_file=ALL_CONFIGS_NUMBERS_TEX_FILE, \
                     effectiveness_numbers_tex_file=EFFECTIVENESS_NUMBERS_TEX_FILE):
    fr = open(numbers_tex_file, 'r')
    lines = fr.readlines()
    fr.close()
    tuple_list = []
    index = 0
    for example in examples:
        for i in range(len(lines)):
            if example.replace('-', '') + config + 'NumOfCommitsMapBack' in lines[i]:
                size = int(lines[i].split('{')[2].split('}')[0])
                his_len = getHistoryLen(example)
                relative_his_len = (size / his_len) * 100
                tuple_list.append((index+1, relative_his_len))
                index += 1
    return tuple_list

def genDatFile(config, data_dir=DATA_DIR):
    # (index, size)
    tuple_list = extractTupleList(config)
    total_size = 0
    lines = ''
    lines += 'index,size\n'
    for t in tuple_list:
        total_size += t[1]
        lines += str(t[0]) + ',' + str(t[1]) + '\n'
    fw = open(data_dir + '/' + config + '.dat', 'w')
    fw.write(lines)
    fw.close()
    return total_size

def genAllDatFiles(configs=CONFIGS, ):
    for config in configs:
        if config == 'definer':
            definer_total_size = genDatFile(config)
        elif config == 'cslicer':
            cslicer_total_size = genDatFile(config)
        elif config == 'splitdefiner':
            split_definer_total_size = genDatFile(config)
    definer_shorter_than_cslicer_percent = (cslicer_total_size - definer_total_size) \
                                           / cslicer_total_size
    split_definer_shorter_than_definer_percent = \
                    (definer_total_size - split_definer_total_size) / definer_total_size
    print ('Definer on average ' + '{0:.2f}'.format(definer_shorter_than_cslicer_percent) + \
           ' shorter than CSlicer')
    print ('Split-Definer on average ' + \
           '{0:.2f}'.format(split_definer_shorter_than_definer_percent) + \
           ' shorter than Definer')

if __name__ == '__main__':
    genAllDatFiles()



# --- deprecated ---
def genBarPlotTexFile(cslicer_results_list, definer_results_list, split_definer_results_list, \
                      barchart_tex_file=BARCHART_TEX_FILE):
    lines = ''
    # lines += '\\documentclass{article}\n'
    # lines += '% Compile using: pdflatex --shell-escape slicesize.tex\n'
    # lines += '\\usepackage{tikz}\n'
    # lines += '\\usepackage{pgfplots}\n'
    # lines += '\\usetikzlibrary{patterns}\n'
    # lines += '\\usetikzlibrary{pgfplots.groupplots}\n'
    # lines += '\\usetikzlibrary{matrix}\n'
    # lines += '\\usepgfplotslibrary{external}\n'
    # lines += '\\tikzexternalize\n'
    # lines += '\\pgfplotsset{\n'
    # lines += '  width=.8\\columnwidth,\n'
    # lines += '  height=.5\\columnwidth,\n'
    # lines += '  compat=1.13\n'
    # lines += '}\n'
    # lines += '\\begin{document}\n'
    lines += '\\begin{tikzpicture}\n'
    lines += '\\begin{axis}[\n'
    lines += '  ybar,\n'
    lines += '  bar width=4,\n'
    lines += '  ymin = 0,\n'
    lines += '  ymax = 35,\n'
    lines += '  ytick = {0,5,...,35},\n'
    lines += '  xmin = 0,\n'
    lines += '  xmax = 9,\n'
    lines += '  xtick = {1,2,...,8},\n'
    lines += '  xtick pos=left,\n'
    lines += '  xticklabels = {\n'
    lines += '   C1, C2, C3, C4 ,C5, S1, S2, S3, I1, I2, I3, I4, I5, L1, L2, L3, L4, L5, N1, N2\n'
    lines += '  },\n'
    lines += '  x tick label style = {\n'
    lines += '  font=\\sffamily\\footnotesize\n'
    lines += '  },\n'
    lines += '  ylabel = {Relative slice sizes (\\%)},\n'
    lines += '  y tick label style = {\n'
    lines += '    font=\\footnotesize\n'
    lines += '  },\n'
    lines += '  legend entries = {\n'
    lines += ' \\textsc{CSlicer}, \\textsc{Definer}-\\textsc{Default}, \\textsc{Split}-\\textsc{Definer}\n'
    lines += '  },\n'
    lines += '  legend style = {\n'
    lines += '    legend pos=north west,\n'
    lines += '    font=\\footnotesize\n'
    lines += '  }\n'
    lines += ']\n'
    lines += '\\addplot [draw=blue,pattern=horizontal lines light blue] coordinates \n'
    lines += '{'
    for i in range(len(cslicer_results_list)):
        lines += '(' + str(i+1) + ', ' + cslicer_results_list[i] + ')'
    lines += '};\n'
    lines += '\\addplot [draw=red, pattern color = red, pattern = north west lines] coordinates \n'
    lines += '{'
    for i in range(len(definer_results_list)):
        lines += '(' + str(i+1) + ', ' + definer_results_list[i] + ')'
    lines += '};\n'
    lines += '\\addplot [draw=black, pattern color = black, pattern = north west lines] coordinates \n'
    lines += '{'
    for i in range(len(split_definer_results_list)):
        lines += '(' + str(i+1) + ', ' + split_definer_results_list[i] + ')'
    lines += '};\n'
    lines += '\\end{axis}\n'
    lines += '\\end{tikzpicture}\n'
    # lines += '\\end{document}\n'

    fw = open(barchart_tex_file, 'w')
    fw.write(lines)
    fw.close()
