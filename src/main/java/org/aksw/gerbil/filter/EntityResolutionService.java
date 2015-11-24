package org.aksw.gerbil.filter;

/**
 * An entity resolution service looks up a number of entities
 * for a filter definition.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 */
public interface EntityResolutionService {

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
