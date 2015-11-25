package org.aksw.gerbil.filter.cache;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * A cached results is a serializable representation of either
 * a annotator result or a goldstandard result.
 * <p/>
 * Created by Henrik Jürges on 11.11.15.
 */
public class CachedResult {

    private String filterName;

    private String datasetName;

    private String annotatorName;

    private String[] entities;

    private String checksum;

    /**
     * Instantiates a new Cached result for annotators.
     *
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @param entities      the entities
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public CachedResult(String filterName, String datasetName, String annotatorName, String[] entities) {
        this.filterName = filterName;
        this.datasetName = datasetName;
        this.annotatorName = annotatorName;
        this.entities = entities.clone();
    }

    /**
     * Instantiates a new Cached result for goldstandards.
     *
     * @param filterName  the filter name
     * @param datasetName the dataset name
     * @param entities    the entities
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public CachedResult(String filterName, String datasetName, String[] entities) {
        this.datasetName = datasetName;
        this.filterName = filterName;
        this.entities = entities.clone();
        this.annotatorName = "gt";
    }

    /**
     * Instantiates a new Cached result.
     *
     * @param filterName    the filter name
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     */
    public CachedResult(String filterName, String datasetName, String annotatorName) {
        this.datasetName = datasetName;
        this.filterName = filterName;
        this.annotatorName = annotatorName;
    }

    /**
     * Instantiates a new Cached result.
     *
     * @param filterName  the filter name
     * @param datasetName the dataset name
     */
    public CachedResult(String filterName, String datasetName) {
        this.datasetName = datasetName;
        this.filterName = filterName;
        this.annotatorName = "gt";
    }

    /**
     * Gets filter name.
     *
     * @return the filter name
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * Gets dataset name.
     *
     * @return the dataset name
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * Gets annotator name.
     *
     * @return the annotator name
     */
    public String getAnnotatorName() {
        return annotatorName;
    }

    /**
     * Get entities string [ ].
     *
     * @return the string [ ]
     */
    public String[] getEntities() {
        return entities.clone();
    }

    /**
     * Gets checksum.
     *
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets checksum for this cached object.
     * Note: To create a checksum use the provided implementation.
     * The checksum is used for validate if a newer version has to be stored,
     * so the checksum should be taken over all entities and not the filtered ones.
     *
     * @param checksum the checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Generate md 5 checksum over the entities.
     *
     * @param entities the entities
     * @return the string
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static String generateMd5Checksum(String[] entities) throws NoSuchAlgorithmException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException("Expected a MD5 implmentation. " + e.getMessage(), e);
        }

        String entityRep = Arrays.toString(entities);
        md.update(entityRep.getBytes(Charset.forName("utf8")));
        byte[] digest = md.digest();
        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            builder.append(String.format("%02x", b & 0xff));
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedResult that = (CachedResult) o;

        if (!filterName.equals(that.filterName)) return false;
        if (!datasetName.equals(that.datasetName)) return false;
        if (annotatorName != null ? !annotatorName.equals(that.annotatorName) : that.annotatorName != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(entities, that.entities)) return false;
        return !(checksum != null ? !checksum.equals(that.checksum) : that.checksum != null);

    }

    @Override
    public int hashCode() {
        int result = filterName.hashCode();
        result = 31 * result + datasetName.hashCode();
        result = 31 * result + (annotatorName != null ? annotatorName.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(entities);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CachedResult{" +
                ", filterName='" + filterName + '\'' +
                ", datasetName='" + datasetName + '\'' +
                ", annotatorName='" + annotatorName + '\'' +
                ", entities=" + Arrays.toString(entities) +
                ", checksum='" + checksum + '\'' +
                '}';
    }

    /**
     * Gets the cache file.
     *
     * @param cacheLocation the cache location
     * @return the cache file
     */
    public File getCacheFile(File cacheLocation) {
        StringBuilder builder = new StringBuilder();
        builder.append(normalize(filterName)).append("_");
        builder.append(normalize(annotatorName)).append("_");
        builder.append(normalize(datasetName));

        return new File(cacheLocation, builder.toString());
    }

    private String normalize(String s) {
        String normalized = StringUtils.replaceChars(s, "/", "_");
        return StringUtils.replaceChars(normalized, " ", "_");
    }
}