#!/bin/sh
#
# Calculate the ambiguity of all entities/annotations and all surface forms
# For calculation we use the gerbil_data/resource/filter/taa file, which denotes to a table
# with entity as first and surface form as second column
#

# Variables
BASE=../../../../gerbil_data/resources/filter/

# calculate ambiguity of entities by counting the surface forms for every entity
# ignoring duplicated surface forms
sort -k1,1 -uk2,2 ${BASE}taa | awk '{ print $1 }' | uniq -c | sort -k1,1n > ${BASE}ambiguity_e

# calculate ambiguity of surface forms by counting the entities for every surface form
# ignoring duplicated entities
sort -k2,2 -uk1,1 ${BASE}taa |  awk '{print $2}' | uniq -c | sort -k1,1n > ${BASE}ambiguity_sf
