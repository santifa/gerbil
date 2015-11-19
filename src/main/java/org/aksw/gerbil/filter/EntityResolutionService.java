package org.aksw.gerbil.filter;

import org.aksw.gerbil.filter.cache.FilterCache;

/**
 * An entity resolution service looks up a number of entities
 * for a filter definition. The service caches entities for shorter response.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public interface EntityResolutionService {

    /**
     * Sets a prefix set, mostly a combination of a short form
     * and an uri as long form. E.g. foaf:&lt http://xmlns.com/foaf/0.1/&gt
     *
     * @param prefixes the prefixes
     */
    void setPrefixSet(String[] prefixes);

    void initCache(FilterCache cache);

    /**
     * Precache a dataset goldstandard.
     */
    void precache(String[] entitites, FilterConfiguration conf, String datasetName);

    /**
     * Resolve entities from an annotator result.
     *
     * @param entities      the entities
     * @param conf          the conf
     * @param datasetName   the dataset name
     * @param annotatorName the annotator name
     * @return the string [ ]
     */
    String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName);

    /**
     * Resolve entities from a gold standard.
     *
     * @param entities    the entities
     * @param conf        the conf
     * @param datasetName the dataset name
     * @return the string [ ]
     */
    String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName);
}
