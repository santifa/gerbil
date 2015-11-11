package org.aksw.gerbil.filter;

import java.util.NoSuchElementException;

/**
 * An entity resolution service looks up the entity name or id and
 * returns the found type or an empty string. As well as searching
 * for same as types.
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

    /**
     * Initialize a entity resolution service.
     *
     * @param precache      true if the goldstandard has to be prechached
     * @param cache         true if for caching goldstandard and annotator results
     * @param cacheLocation the cache location
     */
    void initialize(boolean precache, boolean cache, String cacheLocation);

    /**
     * Gets the first found type.
     *
     * @param entityName the entity name
     * @return the found type
     * @throws NoSuchElementException the no such element exception
     */
    String getType(String entityName) throws NoSuchElementException;

    /**
     * Get all types connected to the entity.
     *
     * @param entityName the entity name
     * @return the array of all types
     * @throws NoSuchElementException the no such element exception
     */
    String[] getAllTypes(String entityName) throws NoSuchElementException ;
}
