package org.aksw.gerbil.filter.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PopularityFilter extends ConcreteFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PopularityFilter.class);

    private HashMap<String, File> fileMapping = new HashMap<>(10);

    private List<String> foundItems = new ArrayList<>();
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
            fileMapping.put(part, new File(def.getServiceLocation() + "_" + part));
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
            //String filename = fileMapping.get(part);
            try {
                findEntities(fileMapping.get(part), entities, result);
            } catch (IOException e) {
                LOGGER.error("Search in file failed for " + fileMapping.get(part) + " with "
                        + entities + " ; Skipping... " + e.getMessage(), e);
            }
        }

        return result;
    }

    // TODO think about a better way for searching the files instead of fgrep,
    // but it is the fastest  way at the moment, with more than 10 times faster then regular java io.
    private void findEntities(File f, List<String> entities, List<String> result) throws IOException {
        String searchString = Joiner.on('\n').skipNulls().join(entities);
        searchString = StringUtils.replace(searchString, "'", "\\'");

        String cmd = "fgrep -F \"" + searchString + "\" \"" + f.getAbsolutePath() + "\"";
        CommandLine cmdLine = CommandLine.parse(cmd);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(stdout));
        // kill process after 10 secs.
        executor.setWatchdog(new ExecuteWatchdog(10000));
        executor.execute(cmdLine);

        List<String> output = Splitter.on("\n").omitEmptyStrings().splitToList(stdout.toString("UTF-8"));
        for (String s : output) {
            //System.out.println("substring " + s);
            String uri = Splitter.on(" ").split(s).iterator().next();
           // System.out.println("uri: " + uri);
            if (entities.contains(uri)) {
                result.add(uri);
            }
        }
    }


    @Override
    Object cloneChild() {
        return new PopularityFilter(getConfiguration(), new String[0]);
    }
}
