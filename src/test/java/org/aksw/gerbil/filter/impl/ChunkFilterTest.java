package org.aksw.gerbil.filter.impl;

import org.aksw.gerbil.filter.Filter;
import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.wrapper.IdentityWrapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Mock and test the chunking of filter requests.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class ChunkFilterTest {

    EntityResolutionMock mock = new EntityResolutionMock();

    private List<List<String>> bigList = Arrays.asList(
            Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), // 2 times chunk size
            Arrays.asList("1", "2", "3", "4", "5"), // equal chunk size
            Arrays.asList("1"), // below chunk size
            Arrays.asList("1", "2", "3", "4", "5", "6") // with rest
    );

    @Test
    public void testChunk() {
        ChunkFilter service = new ChunkFilter(mock, 5);
        service.resolveEntities(bigList.get(0), "data1", "anno1");
        assertEquals(2, mock.getPartsResolved());
        mock.resetCounter();
        service.resolveEntities(bigList.get(1), "data1", "anno1");
        assertEquals(1, mock.getPartsResolved());
        mock.resetCounter();
        service.resolveEntities(bigList.get(2), "data1", "anno1");
        assertEquals(1, mock.getPartsResolved());
        mock.resetCounter();
        service.resolveEntities(bigList.get(3), "data1", "anno1");
        assertEquals(2, mock.getPartsResolved());
    }


    public class EntityResolutionMock implements Filter {

        private int parts = 0;

        public int getPartsResolved() {
            return parts;
        }

        public void resetCounter() {
            this.parts = 0;
        }

        @Override
        public List<String> resolveEntities(List<String> entities, String datasetName, String annotatorName) {
            parts++;
            return new ArrayList<>();
        }

        @Override
        public List<String> resolveEntities(List<String> entities, String datasetName) {
            parts++;
            return new ArrayList<>();
        }

        @Override
        public FilterDefinition getConfiguration() {
            return IdentityWrapper.CONF;
        }
    }
}