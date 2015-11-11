package org.aksw.gerbil.filter.mock;

import org.aksw.gerbil.filter.EntityResolutionService;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Created by ratzeputz on 09.11.15.
 */
public class MockEntityResolution implements EntityResolutionService {

    private HashMap<String, String[]> entityPairs;

    public MockEntityResolution(HashMap<String, String[]> entityPairs) {
        this.entityPairs = entityPairs;
    }

    @Override
    public void setPrefixSet(String[] prefixes) { }

    @Override
    public void initialize(boolean precache, boolean cache, String cacheLocation) {

    }

    @Override
    public String getType(String entityName) throws NoSuchElementException {
        if (entityPairs.containsKey(entityName)) {
            return entityPairs.get(entityName)[0];
        } else {
            throw new NoSuchElementException("Element not found in Map");
        }
    }

    @Override
    public String[] getAllTypes(String entityName) throws NoSuchElementException {
        if (entityPairs.containsKey(entityName)) {
            return entityPairs.get(entityName);
        } else {
            throw new NoSuchElementException("Element not found in Map");
        }
    }
}
