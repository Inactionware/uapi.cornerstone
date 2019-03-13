#! /bin/bash

# The configuration repo branch/tag name which will be checked out
cfgBranch=master

rm -rf .config
mkdir .config
cd .config

git init
git remote add -f origin https://gitlab.com/Inactionware/configuration.git
git config core.sparsecheckout true
echo "uapi" >> .git/info/sparse-checkout
git checkout ${cfgBranch}

cd ..

# Initialize development evnironment
source .config/uapi/setup-dev.sh
