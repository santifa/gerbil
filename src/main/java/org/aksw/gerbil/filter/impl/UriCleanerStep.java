package org.aksw.gerbil.filter.impl;

import com.google.common.collect.Lists;
import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.FilterStep;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The uri cleaner takes the whitelist from a {@link FilterDefinition}
 * and sorts out all non white listed URI's. If the whitelist is empty
 * all URI's are allowed.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UriCleanerStep extends FilterStepDecorator {

    /**
     * Instantiates a new Uri cleaner step.
     *
     * @param service the service
     */
    public UriCleanerStep(FilterStep service) {
        super(service);
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName, String annotatorName) {
        if (conf.getEntityUriWhitelist().isEmpty()) {
            return super.resolveEntities(entities, conf, datasetName, annotatorName);
        } else {
            List<String> cleanedUris = cleanUris(Lists.newArrayList(entities), conf.getEntityUriWhitelist());
            return super.resolveEntities(cleanedUris.toArray(new String[cleanedUris.size()]), conf, datasetName, annotatorName);
        }
    }

    @Override
    public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName) {
        if (conf.getEntityUriWhitelist().isEmpty()) {
            return super.resolveEntities(entities, conf, datasetName);
        } else {
            List<String> cleanedUris = cleanUris(Lists.newArrayList(entities), conf.getEntityUriWhitelist());
            return super.resolveEntities(cleanedUris.toArray(new String[cleanedUris.size()]), conf, datasetName);
        }

    }


    private List<String> cleanUris(List<String> entities, List<String> whitelist) {
        List<String> result = new ArrayList<>(entities.size());

        for (String uri : entities) {
            if (isWhitelisted(uri, whitelist)) {
                result.add(uri);
            }
        }
        return result;
    }

    private boolean isWhitelisted(String uri, List<String> whitlist) {
        for (String whiteUri : whitlist) {
            if (StringUtils.contains(uri, whiteUri)) {
                return true;
            }
        }
        return false;
    }
}
