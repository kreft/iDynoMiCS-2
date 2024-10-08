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

# check master protocol file and generate an initial population of 100
if [ "" == "$protocol" ] ; then
    echo "Master protocol file not set"
else
idy.jar -s LHC $protocol $pop_size

# check target path and submit single Job scripts protocols/gen_0
if [ "" == "$path" ] ; then
    echo "path not set"
else
cd "$path"; for j in *; do qsub -v "protocol=$j" -N "$j" single.sh ; done
fi

# todo wait until all jobs have finished?

while 
wait 

idy.jar -ga 1 $path $data $protocol $fit $max

fi
# generating protocol files
# runnint idy.jar -ga $gen $path $data $protocol $fit $max