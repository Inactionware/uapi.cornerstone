#! /bin/bash

# The configuration repo branch/tag name which will be checked out
cfgBranch=master

# The required java version for build environment
jversion=1.8

# Define java home for different development environment
jhome_mac="/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home"
jhome_vm="/home/min/Dev/zuluJdk-8.0.192"

os=`uname -s`
host=`hostname`
cmd_javac=`command -v javac`
ver_java=0
msg_err=""

# Check java version
if [ -n "$cmd_javac" ]
then
    echo -e "Java environment is detected...\c"
    ver_java=`$cmd_javac -version 2>&1`
    echo -e "version is $ver_java\n"
    ver_java=`expr substr "$ver_java" 7 3`
    if [ "$?" != "0" ]
    then
        msg_err="Run expr command error!"
    fi
fi

# Setup java development envirnoment
if [ -z "$msg_err" ] && [ "$ver_java" != "$jversion" ]
then
    echo "No Java environment found or the version of Java is not supported."
    echo -e "Initialize JAVA environment......\c"
    if [ $USER == "min" ] && [ $host == "min-vm-elementary" ]       # At Linux VM host development
    then
        export JAVA_HOME=$jhome_vm
    elif [ $USER == "xiaoming" ] && [ $host == "min-home.local" ]   # At MacOS host development
    then
        export JAVA_HOME=$jhome_mac
    else
        msg_err="\nUnsupported user - $USER@$host, please reconfig the user"
    fi
    export PATH=$JAVA_HOME/bin:$PATH
    echo "Done"
fi

# Check out build configuration repo from remote
if [ -z "$msg_err" ]
then
    rm -rf .config
    mkdir .config
    cd .config

    git init
    git remote add -f origin https://gitlab.com/Inactionware/configuration.git
    git config core.sparsecheckout true
    echo "uapi" >> .git/info/sparse-checkout
    git checkout ${cfgBranch}

    # Run gradle build script
    cd ..
    # ./gradlew clean build
fi

if [ -n "$msg_err" ]
then
    echo -e "$msg_err"
fi
