#!/bin/bash

NEW_FILES=$(git ls-files --other --exclude-standard | tr " " "\n")
UPDATED_FILES=$({ git diff --name-only ; git diff --name-only --staged ; } | tr " " "\n" | sort | uniq)

ALL_FILES="$NEW_FILES
$UPDATED_FILES"

NUMBERED_FILES=$(echo "${ALL_FILES}" |  sed -r '/^\s*$/d' | nl -bt -w1 -s". ")
BODY="${NUMBERED_FILES//'%'/'%25'}"
BODY="${BODY//$'\n'/'%0A'}"
BODY="${BODY//$'\r'/'%0D'}"
echo ::set-output name=changed::"$BODY"