package org.aksw.gerbil.filter;

import org.aksw.gerbil.transfer.nif.Marking;

import java.util.List;

/**
 * An entity filter describes a mechanism to select
 * special entities out of the gold standard as well as the annotator result.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 *
 */
public interface FilterWrapper {

    FilterDefinition getConfig();

    <E extends Marking> List<List<E>> filterGoldstandard(List<List<E>> entities, String datasetName);

    <E extends Marking> List<List<E>> filterAnnotatorResults(List<List<E>> entities, String datasetName, String annotatorName);
}
