#!/usr/bin/python3

import os
import os.path
import sys
import csv
import argparse
import subprocess as sub

def parseArgs(argv):
    '''
    Parse the args of the script.
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument('--repo', help='Repo path', required=True)
    parser.add_argument('--start', help='Select start commit', required=True)
    parser.add_argument('--end', help='Select end commit', required=True)
    parser.add_argument('--branch', help='New branch name', required=True)

    if (len(argv) == 0):
        parser.print_help()
        exit(1)
    opts = parser.parse_args(argv)
    return opts

if __name__ == '__main__':
    opts = parseArgs(sys.argv[1:])
    if opts.repo:
        repopath = opts.repo
    if opts.start:
        start = opts.start
    if opts.end:
        end = opts.end
    if opts.branch:
        branch = opts.branch

    # Get the commit list        
    os.chdir(repopath)
    p = sub.Popen('git --no-pager log ' + start + '..' + end + ' --oneline', shell=True, \
                  stdout=sub.PIPE, stderr=sub.PIPE)
    p.wait()
    commit_list = p.stdout.readlines()
    for i in range(len(commit_list)):
        commit_list[i] = commit_list[i].decode("utf-8")[:-1]
        #print (commit_list[i])
    commit_list.reverse()

    # Check out to a new branch (for splitted commits)
    os.chdir(repopath)
    sub.run('git checkout ' + start + ' -b ' + branch, shell=True)

    # For each commit, split by file
    for commit in commit_list:
        print ('Processing commit ' + commit)
        orig_cmt_id = commit.strip().split(' ')[0]
        orig_cmt_msg = commit.strip().split(' ')[1]
        sub.run('git checkout ' + orig_cmt_id + ' -b BASE' + orig_cmt_id, shell=True)
        sub.run('git reset HEAD~', shell=True)

        counter = 0

        # unstaged files
        while True:
            p = sub.Popen('git diff --name-only', shell=True, stdout=sub.PIPE, \
                          stderr=sub.PIPE)
            p.wait()
            unstaged_file_list = []
            lines = p.stdout.readlines()
            for i in range(len(lines)):
                lines[i] = lines[i].decode("utf-8")[:-1]
                #if not lines[i].startswith('src/test') and \
                #   not lines[i].startswith('src/changes/changes.xml'):
                unstaged_file_list.append(lines[i])
            #print (unstaged_file_list)

            if len(unstaged_file_list) == 0:
                sub.run('git stash', shell=True) # stash the changes on test files.
                break
            uf = unstaged_file_list[0].strip()
            if ' ' in uf:
                uf = "\'" + uf + "\'"

            while True:
                q = sub.Popen('git add -p ' + uf, shell=True, stdout=sub.PIPE, stdin=sub.PIPE)
                q.communicate("y\nq\n".encode('utf-8'))[0]

                # Get the next unstage file
                p = sub.Popen('git diff --name-only', shell=True, stdout=sub.PIPE, \
                          stderr=sub.PIPE)
                p.wait()
                next_unstaged_files = p.stdout.readlines()
                if len(next_unstaged_files) <= 0:
                    break
                next_uf = next_unstaged_files[0].decode("utf-8")[:-1]
                if ' ' in next_uf:
                    next_uf = "\'" + next_uf + "\'"
                if next_uf != uf:
                    break

            commit = commit.replace('\"', '')
            #print (commit)
            sub.run('git commit -m \"[' + commit + '] ' + uf + '\"', shell=True)
            counter += 1

        # untracked files
        p = sub.Popen('git ls-files --others --exclude-standard', shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
        p.wait()
        untracked_file_list = p.stdout.readlines()
        for uf in untracked_file_list:
            uf = uf.decode("utf-8")[:-1]
            if uf.startswith('target/'):
                continue
            if ' ' in uf:
                uf = "\'" + uf + "\'"
            sub.run('git add ' + uf.strip(), shell=True)
            commit = commit.replace('\"', '')
            sub.run('git commit -m \"[' + commit + '] ' + uf + '\"', shell=True)
            counter += 1

        print (counter)
        splitted_commits = []
        p2 = sub.Popen('git --no-pager log --oneline -' + str(counter), shell=True, \
                      stdout=sub.PIPE, stderr=sub.PIPE)
        p2.wait()
        log_commits = p2.stdout.readlines()
        for i in range(counter):
            splitted_commits.append(log_commits[i].decode("utf-8")[:-1].split()[0])
        splitted_commits.reverse()
            
        sub.run('git checkout ' + branch, shell=True)
        for commit in splitted_commits:
            print (commit)
            sub.run('git cherry-pick ' +  commit, shell=True)
