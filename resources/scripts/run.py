#!/usr/bin/python

import sys
import os
import subprocess as sp
import shutil
from path import Path

# constants
TEMPLATE_FILE = 'project-config.template'
CWD = os.getcwd()
SWD = os.path.dirname(os.path.abspath(__file__))
ELASTIC = 'elasticsearch'
ELASTIC_URL = 'https://github.com/elastic/elasticsearch.git'
MAVEN = 'maven'
HADOOP = 'hadoop'
REPO_ROOT = 'repo'
CONFIG_FILE = 'config.properties'
template = Path(os.path.join(SWD, TEMPLATE_FILE))
cslicer_path = os.path.join(SWD, '..', '..', 'target', 'cslicer-1.0.0-jar-with-dependencies.jar')

def check_out_repo(url):
    sp.check_call(['git', 'clone', url, REPO_ROOT])

def check_out_version(version, bname):
    # create new branch if not already exists
    if sp.call(['git', 'show-branch', bname]) != 0:
        sp.check_call(['git', 'checkout', '-b', bname, version])
    sp.call(['git', 'checkout', bname])

def clean_up_repo():
    sp.check_call(['git', 'reset', 'HEAD', '--hard'])
    sp.check_call(['git', 'checkout', 'master'])

def generate_config(temp, repo, jacoco, length, \
                     end, src_root, cls_root, build, \
                     cg='/tmp/cg.txt', touch='/tmp/touch.txt'):
    with open(CONFIG_FILE, 'w') as f:
        
        p = sp.Popen(['m4', \
                      '-D', 'REPO_PATH='+repo, \
                      '-D', 'EXEC_PATH='+jacoco, \
                      '-D', 'HISTORY_LENGTH='+length, \
                      '-D', 'END_COMMIT='+end, \
                      '-D', 'SOURCE_ROOT='+src_root, \
                      '-D', 'CLASS_ROOT='+cls_root, \
                      '-D', 'BUILD_PATH='+build, \
                      '-D', 'CALL_GRAPH_PATH='+cg, \
                      '-D', 'TOUCH_SET_PATH='+touch, \
                      temp], stdout=sp.PIPE)
        for line in p.stdout:
            f.write(line)
        
        p.wait()

        if p.returncode:
            raise sp.CalledProcessError(p.returncode)

def run_unit_tests(tname):
    sp.check_call(['mvn','test','-Dtest='+tname])

def run_functionality(tid, version, testname, r_root, e_root):
    e_root.mkdir_p()

    # check out specific version
    with r_root:
        check_out_version(version, tid)
        # replace pom file
        shutil.copy(os.path.join(SWD, \
                                 'elasticsearch/e1/pom.xml'), 'pom.xml')
        
        if not os.path.exists(os.path.join(e_root.abspath(), 'jacoco.exec')):
            # run unit tests
            run_unit_tests(testname)
            # copy jacoco dump
            shutil.copy('target/jacoco.exec', os.path.join(e_root.abspath(), 'jacoco.exec'))
            
    # generate config file for cslicer
    with e_root:
        generate_config(template.abspath(), os.path.join(r_root.abspath(), '.git'), \
                        os.path.join(e_root.abspath(), 'jacoco.exec'), \
                        str(50), \
                        version, \
                        os.path.join(r_root.abspath(), 'src/main/java'), \
                        os.path.join(r_root.abspath(), 'target/classes'), \
                        os.path.join(r_root.abspath(), 'pom.xml'), \
                        '/tmp/cg-'+tid+'.txt',
                        '/tmp/touch-'+tid+'.txt')

        # run cslicer
        sp.check_call(['java','-jar', cslicer_path, '-q', '-c', CONFIG_FILE])

        # collect results

    # clean up
    with r_root:
        clean_up_repo()


def run_elastic_search():
    p_root = Path(os.path.join(CWD, ELASTIC))
    p_root.mkdir_p()

    # check out repo
    with p_root:
        r_root = p_root / REPO_ROOT
        if not r_root.exists():
            check_out_repo(ELASTIC_URL)

        # setup first functionality
        tid = 'e1'
        version = '647327f4'
        testname = 'org.elasticsearch.script.GroovySandboxScriptTests#testDynamicBlacklist'
        e_root = p_root / tid
        run_functionality(tid, version, testname, r_root, e_root)

def main():
    run_elastic_search()

if __name__ == "__main__":
    sys.exit(main())
