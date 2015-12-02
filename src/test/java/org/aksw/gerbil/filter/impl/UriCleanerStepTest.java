package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mock and test the cleaning of filter request based on a whitelist for URI's.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class UriCleanerStepTest {

    private final List<String> entities = Arrays.asList("http://dbpedia.org/resource/Paul_Allen",
            "http://dbpedia.org/resource/Paul", "www.org/resource/Paul_Allen", "something one",
                    "something two");

    @Test
    public void testWhitelist() {
        final FilterDefinition defWhitelist = new FilterDefinition("person", "something", Arrays.asList(
                "www.org/", "http://dbpedia.org/"
        ), "");
        EntityResolutionMock mock = new EntityResolutionMock(defWhitelist);
        UriCleaner step = new UriCleaner(mock);
        step.resolveEntities(entities, "data1");
        Assert.assertEquals(Arrays.asList("http://dbpedia.org/resource/Paul_Allen",
                "http://dbpedia.org/resource/Paul", "www.org/resource/Paul_Allen"), mock.getEntities());
        mock.resetMock();
    }

    @Test
    public void testWithoutWhitelist() {
        final FilterDefinition defWhitelist = new FilterDefinition("person", "something", new ArrayList<String>(), "");
        EntityResolutionMock mock = new EntityResolutionMock(defWhitelist);
        UriCleaner step = new UriCleaner(mock);
        step.resolveEntities(entities, "data1");
        Assert.assertEquals(entities, mock.getEntities());
        mock.resetMock();
    }

    public class EntityResolutionMock implements Filter {

        private List<String> entities = new ArrayList<>();

        private FilterDefinition def;

        public EntityResolutionMock(FilterDefinition def) {
            this.def = def;
        }

        public void resetMock() {
            entities = new ArrayList<>();
        }

        public List<String> getEntities() {
            return entities;
        }

        @Override
        public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
            this.entities.addAll(entities);
            return new ArrayList<>();
        }

        @Override
        public List<String> resolveEntities(List<String> entities, String datasetName) {
            this.entities.addAll(entities);
            return new ArrayList<>();
        }

        @Override
        public FilterDefinition getConfiguration() {
            return def;
        }
    }

}