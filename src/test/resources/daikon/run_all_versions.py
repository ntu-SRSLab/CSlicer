import sys
import os
import subprocess as sub

TEST_CLASS = "TestJavaSlicer"
CONFIG_FILE = "/home/polaris/Desktop/gitslice/src/test/resources/daikon/example-settings.txt"

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

def search_tracefile(path):
    for f in os.listdir(path):
        if os.path.isfile(os.path.join(path, f)) and ".dtrace.gz" in f:
            return f
    return None

if __name__ == '__main__':
    dir = sys.argv[1]
    sub_dirs = os.listdir(dir)
    java = which('java')
    mvn = which('mvn')
    touch = which('touch')
    rm = which('rm')
    for sub_dir in sub_dirs:
        full_sub_dir = os.path.join(dir, sub_dir)
        if os.path.isdir(full_sub_dir):
            print full_sub_dir
#            javafile_list = []
#            for f in os.listdir(full_sub_dir):
#                filename = os.path.join(full_sub_dir, f)
#                if os.path.isfile(filename):
#                    javafile_list.append(filename)
#            print javafile_list
             
#            sub.call("find . -maxdepth 1 -name \"*.class\" -print -exec rm {} \\;", shell=True)
#            sub.call("find . -maxdepth 1 -name \"*.dtrace.gz\" -print -exec rm {} \\;", shell=True)
#            sub.call("find . -maxdepth 1 -name \"*.inv.gz\" -print -exec rm {} \\;", shell=True)
#            sub.call("find . -maxdepth 1 -name \"*.result\" -print -exec rm {} \\;", shell=True)
#            sub.call([javac, TEST_CLASS + ".java"])
#            sub.call([java, "daikon.Chicory", "org.junit.runner.JUnitCore", TEST_CLASS])
            os.chdir(full_sub_dir) 
            sub.call([mvn, 'clean', 'compile'])
            sub.call([mvn, 'test', '-Dtest=TestA'])
            tracefile = search_tracefile(full_sub_dir + "/target")
            if tracefile != None:
                print tracefile
                sub.call([touch, sub_dir + ".result"])
                outfile = sub_dir + ".result"
                p = sub.Popen ([java, "daikon.Daikon", "--config", CONFIG_FILE, "target/" + tracefile],
                        stdout=open(outfile, 'w'), stderr=open(outfile, 'w'))
                p.wait ()
                fr = open(outfile, 'r')
                lines = fr.readlines()
                fw = open(outfile, 'w')
                for l in lines:
                    if not l.startswith('warning:'):
                        fw.write(l)
                fw.close()
