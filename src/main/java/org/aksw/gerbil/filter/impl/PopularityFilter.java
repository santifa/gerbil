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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The popularity filter interacts with a perl script via common-exec.
 * It provides a as first argument a file containing all entities and one or more files
 * for searching.
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
    public PopularityFilter(FilterDefinition def, String[] prefixes) throws InstantiationException {
        super(def, prefixes);
        createMapping();
    }

    // create a simple filter part to filename mapping; the filename is the base part
    private void createMapping() throws InstantiationException {
        String[] parts = def.getFilter().split(",");
        this.fileMapping = new ArrayList<>(parts.length);
        for (String part : parts) {
            File f = new File(def.getServiceLocation() + "_" + part);
            // check is the files are present
            if (!f.exists() || !f.isFile()) {
                throw new InstantiationException("File not found or usable. " + f);
            }
            fileMapping.add(f);
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

    // write all entities to a temporary file
    private File writeEntities(List<String> entities) throws IOException {
        File entityFile = new File(System.getProperty("java.io.tmpdir"),
                getConfiguration().getName().replace(" ", "_") + Thread.currentThread().getName());

        FileWriter writer = new FileWriter(entityFile);
        for (String entity : entities) {
            writer.write(entity);
            writer.write("\n");
        }
        writer.flush();
        writer.close();
        return entityFile;
    }

    // a bit ugly but use perl for text processing; cause java is to slow
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

        // read input string, every line represents a entity IRI
        List<String> output = Splitter.on("\n").omitEmptyStrings().splitToList(stdout.toString("UTF-8"));
        result.addAll(output);
    }

    @Override
    Object cloneChild() throws InstantiationException {
        return new PopularityFilter(getConfiguration(), new String[0]);
    }
}
