#!/bin/sh

# embedded options to qsub - start with #PBS
# -- name, cores, time
# -- these values are used unless otherwise specified in the qsub
#PBS -N iDynomics
#PBS -l procs=1
#PBS -l walltime=72:00:00
# -- write output, errors to dir..
#PBS -o logs
#PBS -e logs
# -- run in current directory --
cd $PBS_O_WORKDIR
cd idy

# -- begin script after this --

# check protocol file and run
if [ "" == "$protocol" ] ; then
    echo "Protocol file not set"
else
idy.jar -p $protocol

fi
# generating protocol files