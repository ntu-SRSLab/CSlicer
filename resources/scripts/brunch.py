#!/usr/bin/env python

import os
import os.path
import sys
import csv

def isexec (fpath):
    if fpath == None: return False
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK) 

def which(program):
    fpath, fname = os.path.split(program)
    if fpath:
        if isexec (program):
            return program
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            exe_file = os.path.join(path, program)
            if isexec (exe_file):
                return exe_file
    return None

def parseArgs (argv):
    import argparse as a
    p = a.ArgumentParser (description='Benchmark Runner')
    
    p.add_argument ('--cpu', metavar='CPU',
                    type=int, help='CPU limit', default=3000)
    p.add_argument ('--mem', metavar='MEM',
                    type=int, help='Memory limit (MB)', default=2048)
    p.add_argument ('--bench', metavar='BENCH',
                    required=True,
                    help='File specifies paths to benchmark files')
    p.add_argument ('--prefix', default='BRUNCH_STAT', 
                    help='Prefix for stats')
    p.add_argument ('--format', required=True, help='Fields')
    p.add_argument ('--out', metavar='DIR', 
                    default='brunch.out', help='Output directory')

    if '-h' in argv or '--help' in argv:
        p.print_help ()
        p.exit (0)
    
    try:
        k = argv.index ('--')
    except ValueError:
        p.error ("No '--' argument")
    
    args = p.parse_args (argv[:k])
    args.tool_args = argv[k+1:]
    return args

def collectStats (stats, file):
    f = open (file, 'r')
    for line in f:
        if not line.startswith ('[STATS]'): continue

        fld = line.split (' ')
        
        if len(fld) < 3:
            continue
        
        stats [fld[1]] = fld[3].strip ()
    f.close ()
    return stats

def statsHeader (stats_file, flds):
    with open (stats_file, 'w') as sf:
        writer = csv.writer (sf)
        writer.writerow (flds)

def statsLine (stats_file, fmt, stats):
    line = list()
    for fld in fmt:
        if fld in stats: line.append (str (stats [fld]))
        else: line.append (None)

    with open (stats_file, 'a') as sf:
        writer = csv.writer (sf)
        writer.writerow (line)

cpuTotal = 0.0

def runTool (tool_args, f, out, cpu, mem, fmt):
    global cpuTotal
    import resource as r

    def set_limits ():
        if mem > 0:
            mem_bytes = mem * 1024 * 1024
            r.setrlimit (r.RLIMIT_AS, [mem_bytes, mem_bytes])
        if cpu > 0:
            r.setrlimit (r.RLIMIT_CPU, [cpu, cpu])

    fmt_tool_args = [v.format(f=f) for v in tool_args]
    fmt_tool_args[0] = which (fmt_tool_args[0])
    
    base = os.path.basename (f)
    print '[BASE] ' + base
    print '[OUT]' + out
    outfile = os.path.join (out, base + '.stdout')
    errfile = os.path.join (out, base + '.stderr')
    
    import subprocess as sub

    benchmarks = open(f)
    for line in benchmarks:
        if line.startswith('endCommit'):
            endcommit = line.replace('endCommit = ', '').strip()
        elif line.startswith('classRoot'):
            classroot = line.strip('classRoot = ').strip()
        elif line.startswith('repoPath'):
            repopath = line.strip('repoPath = ').replace('/.git', '').strip()
        elif line.startswith('testScope'):
            testscope = line.replace('testScope = ', '').strip()
    
    print repopath
    print endcommit
    print classroot
    print testscope

    git = which('git')
    #print git	

    os.chdir(repopath)

    sub.call ([git, 'stash'])
    sub.call ([git, 'checkout', endcommit])
    
    project = repopath.split('/')[len(repopath.split('/')) - 1]
    print '[PROJECT]: ' + project
    mvn = which('mvn')

    if project == 'hadoop':
        sub.call ([mvn, 'clean'])
        os.chdir(repopath + '/hadoop-maven-plugins')
        sub.call ([mvn, 'install'])
        os.chdir(repopath)
        sub.call ([mvn, 'compile'])
        sub.call ([mvn, 'test', '-Dtest=' + testscope])
    elif project == 'repo':
        if '#' in testscope:
            testclass = testscope.split('#')[0]
            testmethod = testscope.split('#')[1]
            print 'TESTCLASS:' + testclass
            print 'TESTMETHOD:' + testmethod
            sub.call ([mvn, 'clean', 'compile'])
            sub.call ([mvn, 'test', '-Dtests.class=' + testclass, '-Dtests.method=' + testmethod])
        else:
            testclass = testscope
            sub.call ([mvn, 'clean', 'compile'])
            sub.call ([mvn, 'test', '-Dtests.class=' + testclass])
    elif project == 'maven':
        submodule = classroot.split('/')[len(classroot.split('/')) - 3]
        print '[SUBMODULE]:' + submodule
        os.chdir(repopath + '/' + submodule)
        sub.call ([mvn, 'clean', 'compile'])
        sub.call ([mvn, 'test', '-Dtest=' + testscope])
    else:
        sub.call ([mvn, 'clean', 'compile'])
        sub.call ([mvn, 'test', '-Dtest=' + testscope])

    print fmt_tool_args

    os.chdir('/home/polaris/Desktop/run_benchmark')

    p = sub.Popen (fmt_tool_args, 
                   stdout=open(outfile, 'w'), stderr=open(errfile, 'w'))
    p.wait ()
    cpuUsage = r.getrusage (r.RUSAGE_CHILDREN).ru_utime

    stats = dict()
    stats['File'] = f
    stats['base'] = base
    stats['Status'] = p.returncode
    stats['Cpu'] = '{:.2f}'.format (cpuUsage - cpuTotal)
    cpuTotal = cpuUsage
    
    stats = collectStats (stats, outfile)
    stats = collectStats (stats, errfile)
    statsLine (os.path.join (out, 'stats'), fmt, stats)

def main (argv):
    args = parseArgs (argv[1:])

    if not os.path.exists (args.out):
        os.mkdir (args.out)

    fmt = args.format.split (':')
    statsHeader (os.path.join (args.out, 'stats'), fmt)

    global cpuTotal
    import resource as r
    cpuTotal = r.getrusage (r.RUSAGE_CHILDREN).ru_utime    

    for f in open(args.bench, 'r'):
        runTool (args.tool_args, f.strip(), args.out,
                 cpu=args.cpu, 
                 mem=args.mem, 
                 fmt=fmt)
    return 0
if __name__ == '__main__':
    sys.exit (main (sys.argv))
