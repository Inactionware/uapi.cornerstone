#! /bin/bash

if test -z "$1"
then
    echo "Push failed: not specified a branch to push"
    echo "Usage: push <branch name>"
    exit 1
fi

repositories=`git remote`
for repository in ${repositories[@]}
do
    echo "push change to $repository"
    git push $repository $1
done