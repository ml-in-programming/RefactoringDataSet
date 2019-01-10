#!/usr/bin/env bash

if [ $# -ne "2" ]; then
    echo "usage: generate-dataset <path to project> <path to output folder>"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )" # from https://stackoverflow.com/a/246128

$DIR/gradlew -p $DIR runDatasetGeneration -PprojectFolder="$PWD/$1" -PoutputDir="$PWD/$2"

