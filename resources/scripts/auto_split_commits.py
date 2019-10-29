import os
import os.path
import sys
import csv
import subprocess as sub
import re

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

if __name__ == '__main__':
    git = which('git')
    echo = which('echo')

    repopath = '/home/polaris/Desktop/msrexamples/calcite'
    start = '495f1859'
    temp = '/home/polaris/Desktop/featuregraph/CALCITE-v1.4/temp'
    temp_2 = '/home/polaris/Desktop/featuregraph/CALCITE-v1.4/temp_2'
    temp_3 = '/home/polaris/Desktop/featuregraph/CALCITE-v1.4/temp_3'
    featurebranch = 'V1_4_CALCITE-718'

    os.chdir(repopath)
    sub.call([git, 'checkout', start, '-b', featurebranch])
    
    commit_list = open('/home/polaris/Desktop/featuregraph/CALCITE-v1.4/CALCITE-718/functional_commits').readlines()
    for commit in commit_list:
        orig_cmt_id = commit.strip().split(' ')[0]
        orig_cmt_msg = commit.strip().split(' ')[1]
        sub.call([git, 'checkout', orig_cmt_id, '-b', 'BASE' + orig_cmt_id])
        sub.call([git, 'reset', 'HEAD~'])
        
        counter = 0

        # unstaged files
        while True:
            p = sub.Popen([git, 'diff', '--name-only'], stdout=open(temp, 'w'), stderr=open(temp, 'w'))
            p.wait()
            unstaged_file_list = []
            lines = open(temp).readlines()
            for l in lines:
                #if not l.startswith('core/src/test'):
                if not re.search('.*/src/test', l):
                    unstaged_file_list.append(l)
            # -----------------------------------
            #print 'UFLIST:'
            #for f in unstaged_file_list:
            #    print f
            # -----------------------------------
            if len(unstaged_file_list) == 0:
                sub.call([git, 'stash']) # stash the changes on test files.
                break
            uf = unstaged_file_list[0].strip()
            q = sub.Popen([git, 'add', '-p', uf], stdout=sub.PIPE, stdin=sub.PIPE)
            q.communicate("y\nq\n")[0]
            sub.call([git, 'commit', '-m', '['+commit+']'+str(counter)])
            counter += 1

        # untracked files
        p2 = sub.Popen([git, 'ls-files', '--others', '--exclude-standard'], stdout=open(temp_3, 'w'), stderr=open(temp_3, 'w'))
        p2.wait()
        untracked_file_list = open(temp_3, 'r').readlines()
        for uf in untracked_file_list:
            sub.call([git, 'add', uf.strip()])
            sub.call([git, 'commit', '-m', '['+commit+']'+str(counter)])
            counter += 1

        valid_hunks = []
        p2 = sub.Popen([git, 'log', '--oneline'], stdout=open(temp_2, 'w'), stderr=open(temp_2, 'w'))
        p2.wait()
        f = open(temp_2, 'r')
        i = 0
        while i < counter:
            l = f.readline()
            valid_hunks.append(l.strip().split(' ')[0])
            i += 1
        valid_hunks.reverse()
            
        sub.call([git, 'checkout', featurebranch])
        for hunk in valid_hunks:
            print hunk
            sub.call([git, 'cherry-pick', hunk])
