#!/bin/bash

# This script is used to manually create the dashboard package.

# Before you use this script, first run `npm install; npm run build`.

# This script is used by Jenkins to create the final package file and Jenkins itself will
# do `npm run build` before using this script.

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <name> <version>"
  exit 1
fi

name=$1
version=$2

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

tarcmd='tar'

if [[ `uname -s` ==  "Darwin" ]]; then
  echo "Warn! It seems that you are on Mac OS, the default 'tar' command on Mac OS is not compatible with GNU tar."
  echo "Try to found 'gtar' command..."
  command -v gtar >/dev/null
  if [[ $? -eq 0 ]]; then
    echo "Found 'gtar', will use 'gtar'."
    tarcmd='gtar'
  else
    echo "Run 'brew install gnu-tar' on Mac OS to install GNU tar." && exit 1
  fi
fi


if [[ ! -d ${SCRIPT_DIR}/dist ]]; then
  echo "[Error]: Not found dist directory, you might need to run 'npm run build' first."
  exit 1
fi

# make sure you are running under the root dir of the project
cd $SCRIPT_DIR

${tarcmd} \
  --owner root --group root \
  -czvf ${SCRIPT_DIR}/${name}-${version}.tar.gz \
  --transform "s,^./,${name}/," \
  ./dist

[[ $? -eq 0 ]] && echo "Generate package file: ${SCRIPT_DIR}/${name}-${version}.tar.gz"

echo "Generated file: ${SCRIPT_DIR}/${name}-${version}.tar.gz"
