package org.aksw.gerbil.filter.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple cache for the filter results.
 * It's cheaper to store the results temporally rather
 * then asking a sparql endpoint all the time. <br>
 * For a simple reset just remove the cache folder or all files.
 * <p/>
 * Created by Henrik Jürges on 09.11.15.
 */
public class FilterCache {

    private static final Logger LOGGER = LogManager.getLogger(FilterCache.class);

    private static FilterCache instance = null;

    private final File cacheLocation;

    private List<File> cacheFiles =  new ArrayList<>();

    private final Gson gson;

    private FilterCache(String cacheLocationName) throws FileNotFoundException {
        this.cacheLocation = new File(cacheLocationName);

        if (cacheLocation.exists() && cacheLocation.isDirectory()) {
            initCache();
        } else {
            throw new FileNotFoundException("Could not find cache directory " + cacheLocation);
        }

        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // collects all cached files
    private void initCache() {
        cacheFiles.addAll(Arrays.asList(cacheLocation.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return new File(file, s).isFile();
            }
        })));
    }

    /**
     * Gets instance of an filter cache. If the location does not exists, we try to create it.
     *
     * @param location the location of the cache
     * @return the filter cache instance
     * @throws IOException the io exception if the path is not found.
     */
    public static synchronized FilterCache getInstance(String location) throws IOException {
        if (instance != null) {
            return instance;

        } else {
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
        return cacheFiles.contains(new CachedResult(filterName, datasetName).getCacheFile(cacheLocation));

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
        return cacheFiles.contains(new CachedResult(filterName, datasetName, annotatorName).getCacheFile(cacheLocation));
    }

    /**
     * Is version cached boolean.
     *
     * @param filterName  the filter name
     * @param datasetName the dataset name
     * @param md5sum      the md 5 sum
     * @return the boolean
     */
    public boolean isVersionCached(String filterName, String datasetName, String md5sum) {
        if (isCached(filterName, datasetName)) {
            CachedResult res = getCachedResults(new CachedResult(filterName, datasetName));
            return StringUtils.equals(md5sum, res.getChecksum());
        } else {
            return false;
        }
    }

    /**
     * Is version cached boolean.
     *
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @param md5sum        the md 5 sum
     * @return the boolean
     */
    public boolean isVersionCached(String filterName, String datasetName, String annotatorName, String md5sum) {
        if (isCached(filterName, datasetName, annotatorName)) {
            CachedResult res = getCachedResults(new CachedResult(filterName, datasetName, annotatorName));
            return StringUtils.equals(md5sum, res.getChecksum());
        } else {
            return false;
        }
    }

    /**
     * Get a cached result.
     *
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @return the string [ ] or an empty array if nothing is cached.
     */
    public List<String> getCachedResults(String filterName, String datasetName, String annotatorName) {
        CachedResult res = getCachedResults(new CachedResult(filterName, datasetName, annotatorName));
        List<String> result = new ArrayList<>();
        Collections.addAll(result, res.getEntities());
        return result;
    }

    /**
     * Get a cached goldstandard.
     *
     * @param filterName  the filter name
     * @param datasetName the dataset name
     * @return the string [ ] or an empty array if nothing is cached.
     */
    public List<String> getCachedResults(String filterName, String datasetName) {
        CachedResult res = getCachedResults(new CachedResult(filterName, datasetName));
        List<String> result = new ArrayList<>();
        Collections.addAll(result, res.getEntities());
        return result;
    }

    private CachedResult getCachedResults(CachedResult result) {
        return deserializeResult(result.getCacheFile(cacheLocation));
    }

    /**
     * Cache.
     *
     * @param result the result
     */
    public void cache(CachedResult result) {
        File cacheFile = result.getCacheFile(cacheLocation);

        if (!cacheFiles.contains(cacheFile)) {
            serializeResult(result, cacheFile);
            cacheFiles.add(cacheFile);
        } else {
            // check if we have a new result version
            CachedResult cachedResult = deserializeResult(cacheFile);
            if (cachedResult == null || !StringUtils.equals(cachedResult.getChecksum(), result.getChecksum())) {
                serializeResult(result, cacheFile);
                cacheFiles.add(cacheFile);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("No new version of " + cacheFile + " stored.");
                }
            }
        }
    }

    // write a result to filesystem
    private synchronized void serializeResult(CachedResult result, File cacheFile) {
        String jsonString = gson.toJson(result, CachedResult.class);

        try (BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(cacheFile))) {
            buf.write(jsonString.getBytes(Charset.forName("UTF-8")));
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
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(cacheFile), Charset.forName("UTF-8"))) {
            Gson gson = new Gson();
            result = gson.fromJson(reader, CachedResult.class);
        } catch (IOException e) {
            LOGGER.error("Could not fetch serialized data from " + cacheFile + " ." + e.getMessage(), e);
        }
        return result;
    }
}
