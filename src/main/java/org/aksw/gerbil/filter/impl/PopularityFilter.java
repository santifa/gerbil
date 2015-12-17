package org.aksw.gerbil.filter.impl;

import com.google.common.base.Splitter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unix4j.Unix4j;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PopularityFilter extends ConcreteFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PopularityFilter.class);

    private ArrayList<File> fileMapping;

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
        this.fileMapping = new ArrayList<>(parts.length);
        for (String part : parts) {
            fileMapping.add(new File(def.getServiceLocation() + "_" + part));
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
        List<String> result = new ArrayList<>(entities.size());
        // create a newline seperated string with a whitespace in front for only exact matches
        Collections.sort(entities);
        try {
                findEntities(writeEntities(entities), result);
        //        System.gc();
        } catch (Exception e) {
                LOGGER.warn("Skipping... " + e.getMessage(), e);
        }

        return result;
    }

    public File writeEntities(List<String> entities) {
        File entityFile = new File(System.getProperty("java.io.tmpdir"),
                getConfiguration().getName().replace(" ", "_") + Thread.currentThread().getName());
        Unix4j.from(entities).toFile(entityFile);
        return entityFile;
    }

    // TODO think about a better way for searching the files instead of fgrep,
    // but it is the fastest  way at the moment, with more than 10 times faster then regular java io.
    private void findEntities(File entityFile, List<String> result) throws IOException {
        CommandLine cmdLine = new CommandLine("perl");
        cmdLine.addArgument("src/main/resources/scripts/entity-filter.pl");
        cmdLine.addArguments(entityFile.getAbsolutePath(), true);

        String[] files = new String[fileMapping.size()];
        for (int i = 0; i < fileMapping.size(); i++) {
            files[i] = fileMapping.get(i).getAbsolutePath();
        }

        cmdLine.addArguments(files, true);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(stdout));
        // kill process after 5min.
        executor.setWatchdog(new ExecuteWatchdog(300000));
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new IOException("Search with " + entityFile + " failed in " + fileMapping + ".\n"
                + " Cmd was " + cmdLine + " ;" + e.getMessage(), e);
        }

        // read input string and return everything found
        System.out.println(entityFile + " gives " + stdout.toString());
        List<String> output = Splitter.on("\n").omitEmptyStrings().splitToList(stdout.toString("UTF-8"));
        for (String s : output) {
            result.add(Splitter.on(" ").omitEmptyStrings().split(s).iterator().next());
        }
    }


    @Override
    Object cloneChild() {
        return new PopularityFilter(getConfiguration(), new String[0]);
    }
}
