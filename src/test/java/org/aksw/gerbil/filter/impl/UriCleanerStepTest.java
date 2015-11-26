package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.FilterStep;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by ratzeputz on 26.11.15.
 */
public class UriCleanerStepTest {

    private final EntityResolutionMock mock = new EntityResolutionMock();

    private final List<String> entities = Arrays.asList("http://dbpedia.org/resource/Paul_Allen",
            "http://dbpedia.org/resource/Paul", "www.org/resource/Paul_Allen", "something one",
                    "something two");

    @Test
    public void testWhitelist() {
        final FilterDefinition defWhitelist = new FilterDefinition("person", "something", Arrays.asList(
                "www.org/", "http://dbpedia.org/"
        ));
        UriCleanerStep step = new UriCleanerStep(mock);
        step.resolveEntities(entities.toArray(new String[entities.size()]), defWhitelist, "data1");
        Assert.assertEquals(Arrays.asList("http://dbpedia.org/resource/Paul_Allen",
                "http://dbpedia.org/resource/Paul", "www.org/resource/Paul_Allen"), mock.getEntities());
        mock.resetMock();
    }

    @Test
    public void testWithoutWhitelist() {
        final FilterDefinition defWhitelist = new FilterDefinition("person", "something", new ArrayList<String>());
        UriCleanerStep step = new UriCleanerStep(mock);
        step.resolveEntities(entities.toArray(new String[entities.size()]), defWhitelist, "data1");
        Assert.assertEquals(entities, mock.getEntities());
        mock.resetMock();
    }

    public class EntityResolutionMock implements FilterStep {

        private List<String> entities = new ArrayList<>();

        public void resetMock() {
            entities = new ArrayList<>();
        }

        public List<String> getEntities() {
            return entities;
        }

        @Override
        public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName, String annotatorName) {
            Collections.addAll(this.entities, entities);
            return new String[0];
        }

        @Override
        public String[] resolveEntities(String[] entities, FilterDefinition conf, String datasetName) {
            Collections.addAll(this.entities, entities);
            return new String[0];
        }
    }

}