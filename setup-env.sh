#! /bin/bash

# echo -e "Host gitlab.com\n" >> ~/.ssh/config
# echo -e "    HostName gitlab.com\n" >> ~/.ssh/config
# echo -e "    User git\n" >> ~/.ssh/config
# echo -e "    StrictHostKeyChecking no\n" >> ~/.ssh/config

# Setup variable
cfgBranch=master # The configuration repo branch/tag name will be checked out

# Check out build configuration repo from remote
rm -rf .config
mkdir .config
cd .config

git init
git remote add -f origin https://github.com/Inactionware/configuration.git
git config core.sparsecheckout true
echo "uapi" >> .git/info/sparse-checkout
git checkout ${cfgBranch}

# Run gradle build script
# cd ..
# ./gradlew clean build
