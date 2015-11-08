package org.aksw.gerbil.filter;

import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.filter.impl.TypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
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
    private static final String FILTER_BASIC = "org.aksw.gerbil.util.filter.basic.";
    private static final String FILTER_ADVANCED = "org.aksw.gerbil.util.filter.advanced.";
    private static final String FILTER_COMPOSED = "org.aksw.gerbil.util.filter.composed.";

    private EntityResolutionService service;

    private List<EntityFilter> filters = new ArrayList<>(42);

    public FilterFactory(String serviceUrl) {
        List<String> prefixSet = getPrefixSet();
        this.service = new DbpediaEntityResolution(serviceUrl, prefixSet.toArray(new String[prefixSet.size()]));
        registerFilterType(TypeFilter.class, getBasicResolver());
    }

    public static <T, E> void registerFilterType(Class<E> filter, ConfigResolver<T> resolver) {
        List<T> configurations = resolver.resolve();
        for (T c : configurations) {
            try {
                for (Constructor co : filter.getConstructors()) {
                    if (co.getParameterTypes().length == 1 && c.getClass().isAssignableFrom(co.getParameterTypes()[0])) {
                        System.out.println(co.toGenericString());
                    }
                }

            } finally {

            }
            /* catch (NoSuchMethodException e) {
                LOGGER.error("Filter configuration " + c + " for " + filter.getClass() + " could not be loaded", e.getMessage(), e);
            }*/
            LOGGER.error("loaded " + filter + " with " + c);
        }
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

    private static final ConfigResolver<FilterConfiguration> getBasicResolver() {
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
