#!/bin/bash
#
# This scirpt generates metadata from cached filter results for datasets.
# This should be done if most of the datasets are cached.
#

WORK_DIR=../../../../gerbil_data/cache/filter
RESULT_FILE=../../../../gerbil_data/resources/filter/metadata

 > ${RESULT_FILE};

for FILE in ${WORK_DIR}/*_gt_*
do
    NAME=$(basename "${FILE}");
    FILTER=${NAME%_gt_*};
    DATASET=${NAME#*_gt_};
    ENTITIES=`fgrep "http" ${FILE} | wc -l | awk "{print $1}"`;
    echo "${FILTER} ${DATASET} ${ENTITIES}" >> ${RESULT_FILE};
done

