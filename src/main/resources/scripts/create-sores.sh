#!/bin/sh
#
# This script collects all entities from all datasets und gerbil_data
# and ranks them with scores provided under gerbil_data/resources/filter/sources .
# The result is used for popularity filters. 
#


# variables
BASE=../../../../gerbil_data
SCORE_FOLDER=${BASE}/resources/filter/sources/*
RESULT_FOLDER=${BASE}/resources/filter/
ENTITY_FILE=${RESULT_FOLDER}entities

# collect entities
fgrep -hR http ${BASE}/datasets |                       # collect all links
sed 's/http/\nhttp/g' |                                 # insert newline before link
awk '{print $1}' |                                      # take only the links
grep -E '(dbpedia|wikipedia)' |                         # take only wikipedia and dbpedia
sed 's/wikipedia\.org\/wiki/\dbpedia\.org\/resource/' | # substitute wikipedia with dbpedia
sed 's/>$//' | sort -u > ${ENTITY_FILE}                 # make result unique

# create diff over created file and score files
for file in ${SCORE_FOLDER}
do
	SCORE_FILE=$(basename "${file}")
	SCORE_NAME=${RESULT_FOLDER}ranked_"${SCORE_FILE%.*}"
	echo "creating ${SCORE_NAME}"
	
	grep -F -f ${ENTITY_FILE} ${file} |           # diff with entity file as search pattern 
	awk '{print $1, $3 }' | sed 's/\^\^.*$//' |   # take only the url and score
	sed 's/^<//' | sed 's/>//' | sed 's/\"//g' |  # remove < > and ""
	sed '/E-/ s/\(.*\)E/\1e/' | awk 'x$2' |        # scientific doubles and delete empty lines
	sort --stable -g -k 2 -o ${SCORE_NAME}        # store sorted result

	# create five parts 10% / 10 to 32,5% / 32,5 to 55% / 55 to 75,5% / 75,5 to 100%
	LINES=`cat ${SCORE_NAME} | wc -l`
	FIRST=$((${LINES} * 10 / 100))	
	SECOND=$((${LINES} * 32 /100))
	THIRD=$((${LINES} * 55 / 100))
	FOURTH=$((${LINES} * 75 / 100))
	
	# split file into multiple parts
	sed -n '1,'${FIRST}'p' ${SCORE_NAME} | sort --stable -f -k 1 -o ${SCORE_NAME}_0
	sed -n ''$((${FIRST} +1))','${SECOND}'p' ${SCORE_NAME} | sort --stable -f -k 1 -o ${SCORE_NAME}_1
	sed -n ''$((${SECOND} +1))','${THIRD}'p' ${SCORE_NAME} | sort --stable -f -k 1 -o ${SCORE_NAME}_2
	sed -n ''$((${THIRD} +1))','${FOURTH}'p' ${SCORE_NAME} | sort --stable -f -k 1 -o ${SCORE_NAME}_3
	sed -n ''$((${FOURTH} +1))','${LINES}'p' ${SCORE_NAME} | sort --stable -f -k 1 -o ${SCORE_NAME}_4
done
