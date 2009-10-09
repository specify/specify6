#!/bin/sh

sum() {
if [ -z "$2" ]; then
echo "$1"
else
a=$1;
shift;
b=`sum $@`
echo `expr $a + $b`
fi
}

sum 5 8
