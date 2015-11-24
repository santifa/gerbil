package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.EntityResolutionService;
import org.aksw.gerbil.filter.FilterConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by ratzeputz on 24.11.15.
 */
public class ChunkEntityResolutionTest {

    EntityResolutionMock mock = new EntityResolutionMock();

    private String[][] bigList = new String[][] {
            new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, // 2 times chunk size
            new String[] {"1", "2", "3", "4", "5"}, // equal chunk size
            new String[] {"1"}, // below chunk size
            new String[] {"1", "2", "3", "4", "5", "6"} // with rest
    };

    @Test
    public void testChunk() {
        ChunkEntityResolution service = new ChunkEntityResolution(mock, 5);
        service.resolveEntities(bigList[0], NullFilter.CONF, "data1", "anno1");
        assertEquals(2, mock.getPartsResolved());
        mock.resetCounter();
        service.resolveEntities(bigList[1], NullFilter.CONF, "data1", "anno1");
        assertEquals(1, mock.getPartsResolved());
        mock.resetCounter();
        service.resolveEntities(bigList[2], NullFilter.CONF, "data1", "anno1");
        assertEquals(1, mock.getPartsResolved());
        mock.resetCounter();
        service.resolveEntities(bigList[3], NullFilter.CONF, "data1", "anno1");
        assertEquals(2, mock.getPartsResolved());
    }


    public class EntityResolutionMock implements EntityResolutionService {

        private int parts = 0;

        public int getPartsResolved() {
            return parts;
        }

        public void resetCounter() {
            this.parts = 0;
        }

        @Override
        public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName, String annotatorName) {
            parts++;
            return new String[0];
        }

        @Override
        public String[] resolveEntities(String[] entities, FilterConfiguration conf, String datasetName) {
            parts++;
            return new String[0];
        }
    }
}