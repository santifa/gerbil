package org.aksw.gerbil.filter;

import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.filter.impl.NullFilter;
import org.aksw.gerbil.transfer.nif.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A filter factory creates and holds all filters defined
 * in gerbil.properties
 *
 * Created by Henrik Jürges on 07.11.15.
 */
public class FilterFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(FilterFactory.class);

    private static final String FILTER_PREFIX = "org.aksw.gerbil.util.filter.prefix.";
    private static final String FILTER_BASIC = "org.aksw.gerbil.util.filter.";
    private static final String PRECACHE = "org.aksw.gerbil.util.filter.precache";

    private EntityResolutionService service;

    private List<EntityFilter> filters = new ArrayList<>(42);

    public FilterFactory(EntityResolutionService service) {
        // initialize entity resolver
        List<String> prefixSet = getPrefixSet();
        service.setPrefixSet(prefixSet.toArray(new String[prefixSet.size()]));
        this.service = service;

        // initialize null object
        addNull();
    }


    public FilterFactory() {
        addNull();
    }

    private void addNull() {
        filters.add(new NullFilter());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Filter " + NullFilter.class.getName() + " loaded with " + new NullFilter().getConfig() + " configuration.");
        }
    }

    public <T, E extends EntityFilter> void registerFilter(Class<E> filter, ConfigResolver<T> resolver) {
        List<T> configurations = resolver.resolve();
        for (T c : configurations) {

            try {
                for (Constructor co : filter.getConstructors()) {
                    if (co.getParameterTypes().length == 1 && c.getClass().isAssignableFrom(co.getParameterTypes()[0])) {
                        EntityFilter eFilter = (EntityFilter) co.newInstance(c);
                        eFilter.setEntityResolution(service);
                        filters.add(eFilter);
                        LOGGER.info("Filter " + filter + " with " + c + " loaded.");
                    }
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LOGGER.error("Filter configuration " + c + " for " + filter.getClass() + " could not be loaded", e.getMessage(), e);
            }
        }
    }

    public boolean hasToBePrechached() {
        return GerbilConfiguration.getInstance().getBoolean(PRECACHE);
    }

    public void precache(List<Document> datasets) {
        for (Document doc : datasets) {
            System.out.println("document: " + doc);
        }
    }

    public List<EntityFilter> getFilters() {
        return filters;
    }

    // returns a set of prefixes defined in the filter.properties
    private static final List<String> getPrefixSet() {
        return new ConfigResolver<String>() {

            @Override
            int resolve(int counter, List<String> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_PREFIX + counter)) {
                    result.add(GerbilConfiguration.getInstance().getString(FILTER_PREFIX + counter));
                    return ++counter;
                }
                return -1;
            }
        }.resolve();
    }

    public static final ConfigResolver<FilterConfiguration> getBasicResolver() {
        return new ConfigResolver<FilterConfiguration>() {

            @Override
            int resolve(int counter, List<FilterConfiguration> result) {
                if (GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".name") &&
                        GerbilConfiguration.getInstance().containsKey(FILTER_BASIC + counter + ".filter")) {
                    result.add(new FilterConfiguration(GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".name"),
                            GerbilConfiguration.getInstance().getString(FILTER_BASIC + counter + ".filter")));
                    return ++counter;
                }
                return -1;
            }
        };
    }

    public EntityFilter getFilterByConfig(FilterConfiguration filterConfig) {
        for (EntityFilter f : filters) {
            if (f.getConfig().equals(filterConfig)) {
                return f;
            }
        }
        return new NullFilter();
    }

    public abstract static class ConfigResolver<T> {

        public List<T> resolve() {
            List<T> result = new ArrayList<>();
            int counter = 1;
            // search endless for the next prefix definition
            while (counter != -1) {
                counter = resolve(counter, result);
            }
            return result;
        }

        abstract int resolve(int counter, List<T> result);
    }
}
