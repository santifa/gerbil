package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PopularityFilter extends ConcreteFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PopularityFilter.class);

    private HashMap<String, String> fileMapping = new HashMap<>(10);

    /**
     * Instantiates a new Concrete filter.
     *
     * @param def      the def
     * @param prefixes the prefixes
     */
    public PopularityFilter(FilterDefinition def, String[] prefixes) {
        super(def, prefixes);
        createMapping();
    }

    // create a simple filter part to filename mapping; the filename is the base part
    private void createMapping() {
        String[] parts = def.getFilter().split(",");
        for (String part : parts) {
            fileMapping.put(part, def.getServiceLocation() + "_" + part);
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        return resolve(entities);
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        return resolve(entities);
    }

    // search all mapped files for entities
    private List<String> resolve(List<String> entities) {
        List<String> result = new ArrayList<>();

        for (String part : fileMapping.keySet()) {
            String filename = fileMapping.get(part);
            try {
                findEntities(new File(filename), entities, result);
            } catch (IOException e) {
                LOGGER.error("File " + filename + " not readable. Skipping... " + e.getMessage(), e);
            }
        }

        return result;
    }

    // TODO think about a better way for searching the files instead of creating a new stream for every round
    private void findEntities(File f, List<String> entities, List<String> result) throws IOException {
        for (String searchItem : entities) {

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (StringUtils.contains(line, searchItem)) {
                        result.add(searchItem);
                        break;
                    }
                }
            }
        }
    }


    @Override
    Object cloneChild() {
        return new PopularityFilter(getConfiguration(), new String[0]);
    }
}
