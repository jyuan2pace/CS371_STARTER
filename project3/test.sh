#!/bin/bash
sort -k 1 res/out.txt > $1_your_output.txt
diff $1_expect.txt $1_your_output.txt
exit $?
