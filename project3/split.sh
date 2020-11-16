#!/bin/bash
set -x
rm res/cybersla.0*
rm res/cybersla_your_output.txt
rm res/out.txt
line=`wc -l $1 | cut -d" " -f1`
echo $line
filesize=$[ $line/$2+1 ]
split -l $filesize -d $1 $1.
