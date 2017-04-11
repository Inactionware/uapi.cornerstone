#! /bin/bash

usedBranch=""

branches=`git branch`
oldIFS="$IFS"
IFS='
'
lines=( $branches )
IFS="$oldIFS"

branch=""
for line in "${lines[@]}"
do
    isCurrentBranch=false
    t=`expr substr "$line" 1 1`
    if [ "$t" = "*" ]
    then
        isCurrentBranch=true
    fi
    branch=`expr substr "$line" 3 "${#line}"`
    if test -z "$1"
    then
        if [ "$isCurrentBranch" = true ]
        then
            usedBranch="$branch"
            break
         fi
    else
        if [ "$1" = "$branch" ]
        then
            usedBranch="$branch"
            break
        fi
    fi
done

if test -z "$usedBranch"
then
    if test -z "$1"
    then
        echo "Push failed: working branch was not found, are you under gir repository?"
        echo "Usage: push [branch name]"
        exit 1
    else
        echo "Push failed: specified branch is not valid - $1"
        echo "Usage: push [branch name]"
        exit 1
    fi
fi

repositories=`git remote`
for repository in ${repositories[@]}
do
    echo ">>> push change to $repository for branch $usedBranch <<<"
    git push $repository $1
done

exit 0
