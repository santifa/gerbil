package org.aksw.gerbil.filter.impl.decorators;

import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * The uri cleaner takes the whitelist from a {@link FilterDefinition}
 * and sorts out all non white listed URI's. If the whitelist is empty
 * all URI's are allowed.
 *
 * TODO Think about using the dbpedia lookup service for resolving unknown or not linked entities
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UriCleaner extends FilterDecorator {

    /**
     * Instantiates a new Uri cleaner step.
     *
     * @param service the service
     */
    public UriCleaner(Filter service) {
        super(service);
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
        if (getConfiguration().getEntityUriWhitelist().isEmpty()) {
            return super.resolveEntities(entities, datasetName, annotatorName);
        } else {
            List<String> cleanedUris = cleanUris(entities, getConfiguration().getEntityUriWhitelist());
            return super.resolveEntities(cleanedUris, datasetName, annotatorName);
        }
    }

    @Override
    public List<String> resolveEntities(List<String> entities, String datasetName) {
        if (getConfiguration().getEntityUriWhitelist().isEmpty()) {
            return super.resolveEntities(entities, datasetName);
        } else {
            List<String> cleanedUris = cleanUris(entities, getConfiguration().getEntityUriWhitelist());
            return super.resolveEntities(cleanedUris, datasetName);
        }
    }

    private List<String> cleanUris(List<String> entities, List<String> whitelist) {
        Iterator<String> itr = entities.iterator();
        while (itr.hasNext()) {
            if (!isWhitelisted(itr.next(), whitelist)) {
                itr.remove();
            }
        }
        return entities;
    }

    private boolean isWhitelisted(String uri, List<String> whitelist) {
        for (String whiteUri : whitelist) {
            if (StringUtils.contains(uri, whiteUri)) {
                return true;
            }
        }
        return false;
    }
}
