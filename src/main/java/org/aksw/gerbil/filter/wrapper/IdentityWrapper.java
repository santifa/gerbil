package org.aksw.gerbil.filter.wrapper;

import org.aksw.gerbil.filter.FilterDefinition;
import org.aksw.gerbil.filter.FilterWrapper;
import org.aksw.gerbil.transfer.nif.Marking;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic id filter, it returns it's input and is used for an unfiltered task result.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class IdentityWrapper implements FilterWrapper {

    public final static FilterDefinition CONF = new FilterDefinition("nofilter", "", new ArrayList<String>(), "");


    @Override
    public FilterDefinition getConfig() {
        return CONF;
    }

    @Override
    public <E extends Marking> List<List<E>> filterGoldstandard(List<List<E>> entities, String datasetName) {
        return entities;
    }

    @Override
    public <E extends Marking> List<List<E>> filterAnnotatorResults(List<List<E>> entities, String datasetName, String annotatorName) {
        return entities;
    }
}
