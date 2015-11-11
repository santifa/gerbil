package org.aksw.gerbil.filter.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.aksw.gerbil.config.GerbilConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple cache for the filter results.
 * It's cheaper to store the results temporally rather
 * then asking a sparql endpoint all the time. <br>
 * For a simple reset just remove the cache folder or all files.
 *
 * Created by Henrik Jürges on 09.11.15.
 */
public class FilterCache {

    private static final Logger LOGGER = LogManager.getLogger(FilterCache.class);

    private static FilterCache instance = null;

    private static final String CACHE_LOCATION = "org.aksw.gerbil.util.filter.cachelocation";

    private final File cacheLocation;

    private List<File> cacheFiles;

    private FilterCache(String cacheLocationName) throws FileNotFoundException {
        this.cacheLocation = new File(cacheLocationName);

        if (cacheLocation.exists() && cacheLocation.isDirectory()) {
            this.cacheFiles = new ArrayList<>();
            initCache();
        } else {
            throw new FileNotFoundException("Could not find cache directory " + cacheLocation);
        }
    }

    // collects all cached files
    private void initCache() {
        cacheFiles.addAll(Arrays.asList(cacheLocation.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return file.isFile();
            }
        })));
    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @throws IOException the io exception if the path is not found.
     */
    public static synchronized FilterCache getInstance() throws IOException {
        if (instance != null) {
            return instance;

        } else {
            String location = GerbilConfiguration.getInstance().getString(CACHE_LOCATION);
            try {
                instance = new FilterCache(location);

            } catch (FileNotFoundException e) {
                LOGGER.error("File not found " + location + ". Creating....", e);

                if (new File(location).mkdir()) {
                    instance = new FilterCache(location);
                } else {
                    throw new IOException("Could not create new cache folder. " + e.getMessage(), e);
                }
            }
            return instance;
        }
    }

    /**
     * Is a goldstandard cached.
     *
     * @param filterName  the filter name
     * @param datasetName the dataset name
     * @return the boolean
     */
    public boolean isCached(String filterName, String datasetName) {
        return new CachedResult(filterName, datasetName).getCacheFile(cacheLocation).exists();

    }

    /**
     * Is a result cached.
     *
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @return the boolean
     */
    public boolean isCached(String filterName, String datasetName, String annotatorName) {
        return new CachedResult(filterName, datasetName, annotatorName).getCacheFile(cacheLocation).exists();

    }

    /**
     * Get a cached result.
     *
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @return the string [ ] or an empty array if nothing is cached.
     */
    public String[] getCachedResults(String filterName, String datasetName, String annotatorName) {
        return getCachedResults(new CachedResult(filterName, datasetName, annotatorName));
    }

    /**
     * Get a cached goldstandard.
     *
     * @param filterName  the filter name
     * @param datasetName the dataset name
     * @return the string [ ] or an empty array if nothing is cached.
     */
    public String[] getCachedResults(String filterName, String datasetName) {
        return getCachedResults(new CachedResult(filterName, datasetName));
    }

    private synchronized String[] getCachedResults(CachedResult result) {
        result = deserializeResult(result.getCacheFile(cacheLocation));
        return result != null ? result.getEntities() : new String[0];
    }

    /**
     * Cache annotator results entities.
     *
     * @param entities      the entities
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     */
    public void cache(String[] entities, String filterName, String datasetName, String annotatorName) {
        CachedResult obj;
        try {
            obj = new CachedResult(filterName, datasetName, annotatorName, entities);
            cache(obj);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Cache object generation failed. " + e.getMessage(), e);
        }
    }

    /**
     * Cache goldstandard entities.
     *
     * @param entities    the entities
     * @param filterName  the filter name
     * @param datasetName the dataset name
     */
    public void cache(String[] entities, String filterName, String datasetName) {
        CachedResult obj;
        try {
            obj = new CachedResult(filterName, datasetName, entities);
            cache(obj);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Cache object generation failed. " + e.getMessage(), e);
        }
    }

    private void cache(CachedResult result) {
        File cacheFile = result.getCacheFile(cacheLocation);

        if (!cacheFiles.contains(cacheFile)) {
            serializeResult(result, cacheFile);
            cacheFiles.add(cacheFile);
        } else {
            // check if we have a new result version
            CachedResult cachedResult = deserializeResult(cacheFile);
            if (cachedResult == null || !cachedResult.getChecksum().equals(result.getChecksum())) {
                serializeResult(result, cacheFile);
                cacheFiles.add(cacheFile);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("No newer version of " + cacheFile + " stored.");
                }
            }
        }
    }

    // write a result to filesystem
    private synchronized void serializeResult(CachedResult result, File cacheFile) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        String jsonString = gson.toJson(result, CachedResult.class);

        // write to filesystem
        try (BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(cacheFile))) {
            buf.write(jsonString.getBytes());
            buf.close();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cache object " + cacheFile + " written.");
            }

        } catch (IOException e) {
            LOGGER.error("Could not cache " + cacheFile + " ." + e.getMessage(), e);
        }
    }

    // fetch a result from filesystem
    private synchronized CachedResult deserializeResult(File cacheFile) {
        CachedResult result = null;
        try (FileReader reader = new FileReader(cacheFile)) {
            Gson gson = new Gson();
            result = gson.fromJson(reader, CachedResult.class);
        } catch (IOException e) {
            LOGGER.error("Could not fetch serialized data from " + cacheFile + " ." + e.getMessage(), e);
        }
        return result;
    }
}
