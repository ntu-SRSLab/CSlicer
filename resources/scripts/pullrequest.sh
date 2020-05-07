#!/bin/bash
../pullrequestdata.sh
cd \
cd $repo
cd ..
git remote add upstream $upstream
git remote add origin $origin
git push --set-upstream origin feature_branch
git request-pull $incommit origin $finalcommit
