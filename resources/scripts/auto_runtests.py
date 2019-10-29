import sys
import os
import os.path
import re
import subprocess as sub
import argparse

# Utility functions
def isexec(fpath):
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

# Command variables
mvn = which('mvn')
git = which('git')
rm = which('rm')

# Base class
class OpenSrcProject:
    @staticmethod
    def initial_build(repo_path):
        sub.call ([mvn, 'clean', 'compile'])
        ret = sub.call ([mvn, 'compiler:testCompile'])
        return ret
    @staticmethod
    def build(repo_path):
        # sub.call([mvn, 'clean', 'compile'])
        sub.call ([rm, '-rf', os.path.join(repo_path, 'target/classes')])
        ret = sub.call ([mvn, 'compiler:compile'])
        return ret
    @staticmethod
    def run_single_test(test_class):
        # sub.Popen([mvn, 'test', '-Dtest=' + test_class])
        ret = sub.call ([mvn, 'surefire:test', '-Dtest=' + test_class])
        return ret

# Each class represents a specific project
class CommonsCsv(OpenSrcProject):
    name = 'commons-csv'
    url = 'https://github.com/apache/commons-csv.git'
    end_sha = 'edb87f3a5e53c2160fc3da79f066beb4459c707c'

class CommonsMath(OpenSrcProject):
    name = 'commons-math'
    url = 'https://github.com/apache/commons-math.git'
    end_sha = '34886092d926da89e7040547d48cd3891e51595b'

class CommonsLang(OpenSrcProject):
    name = 'commons-lang'
    url = 'https://github.com/apache/commons-lang.git'
    end_sha = 'ff4967536b75fb18d048cc2a505f68835987d155'

class CommonsCollections(OpenSrcProject):
    name = 'commons-collections'
    url = 'https://github.com/apache/commons-collections.git'
    end_sha = '3633bbd32c07cb36039c9a1a1d5ca27a2ae3882e'

class CommonsIo(OpenSrcProject):
    name = 'commons-io'
    url = 'https://github.com/apache/commons-io.git'
    end_sha = '5899f1eb7239ea26291b9e38490f5922b86158d8'

class CommonsCompress(OpenSrcProject):
    name = 'commons-compress'
    url = 'https://github.com/apache/commons-compress.git'
    end_sha = '083dd8ca0d298e3f9f9407f3b81a6fbfb11bc5f6'

class CommonsConfiguration(OpenSrcProject):
    name = 'commons-configuration'
    url = 'https://github.com/apache/commons-configuration.git'
    end_sha = 'feb962488d8fbb628958cac1a754503591c4eac8'

class CommonsNet(OpenSrcProject):
    name = 'commons-net'
    url = 'https://github.com/apache/commons-net.git'
    end_sha = '2d935482d9b026ccd2cb2b55fcb05380a4466500'

class Calcite(OpenSrcProject): #TODO
    name = 'calcite'
    url = 'https://github.com/apache/calcite.git'
    end_sha = '67071b6b0ba52eeb953badfff39fa10d85b80bf5'
    @staticmethod
    def initial_build(repo_path): # Override
        ret = sub.call ([mvn, 'clean', 'install'])
        return ret
    @staticmethod
    def run_single_test(test_class): # Override TODO
        # sub.Popen([mvn, 'test', '-Dtest=' + test_class])
        ret = sub.call ([mvn, 'surefire:test', '-Dtest=' + test_class])
        return ret

class Maven(OpenSrcProject): #TODO
    name = 'maven'
    url = 'https://github.com/apache/maven.git'
    end_sha = '312eb53502b78355ab21e27610e7ef253990f574'

class Flume(OpenSrcProject): #TODO
    name = 'flume'
    url = 'https://github.com/apache/flume.git'
    end_sha = 'd3552627869b45956181149f4b7ecf0e344080b7'

class Pdfbox(OpenSrcProject): #TODO
    name = 'pdfbox'
    url = 'https://github.com/apache/pdfbox.git'
    end_sha = 'ddf224dec5c9feeb755b81ae6b2005c68ee072aa'

# Functions
def get_project_class(project_name):
    if project_name == 'commons-csv':
        return CommonsCsv
    elif project_name == 'commons-math':
        return CommonsMath
    elif project_name == 'commons-lang':
        return CommonsLang
    elif project_name == 'commons-collections':
        return CommonsCollections
    elif project_name == 'commons-io':
        return CommonsIo
    elif project_name == 'commons-compress':
        return CommonsCompress
    elif project_name == 'commons-configuration':
        return CommonsConfiguration
    elif project_name == 'commons-net':
        return CommonsNet
    elif project_name == 'calcite':
        return Calcite
    elif project_name == 'maven':
        return Maven
    elif project_name == 'flume':
        return Flume
    elif project_name == 'pdfbox':
        return Pdfbox
    else: # New project
        return None

def parse_args(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('-r', help='specify the directory of repository.', required=True)
    parser.add_argument('-i', help='initially build the project.', action='store_true', default=False, required=False)
    parser.add_argument('-b', help='build the project.', action='store_true', default=False, required=False)
    parser.add_argument('-t', help='run a single test class (or a test scope specification, for example, TestClassA,TestClassB#testMethod1,TestClassC#testMethod1+testMethod2).', required=False)
    opts = parser.parse_args(argv)
    return opts

if __name__ == '__main__':
    opts = parse_args(sys.argv[1:])
    repo_path = opts.r
    project_name = os.path.basename(repo_path)
    project_class = get_project_class(project_name)
    if project_class is None: # New project
        pass # TODO
    # Initial build
    if opts.i:
        os.chdir(repo_path)
        ret = project_class.initial_build(repo_path)
        if ret == 0:
            sys.exit(0)
        else:
            sys.exit(1)
    # Build
    elif opts.b:
        os.chdir(repo_path)
        ret = project_class.build(repo_path)
        if ret == 0:
            sys.exit(0)
        else:
            sys.exit(1)
    # Test
    elif opts.t:
        os.chdir(repo_path)
        test_class = opts.t
        ret = project_class.run_single_test(test_class)
        if ret == 0:
            sys.exit(0)
        else:
            sys.exit(1)
