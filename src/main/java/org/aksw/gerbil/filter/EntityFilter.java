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
public interface EntityFilter {

    FilterConfiguration getConfig();

    void setEntityResolution(EntityResolutionService service);

    <E extends Marking> List<List<E>> filterGoldstandard(List<List<E>> entities, String datasetName);

    <E extends Marking> List<List<E>> filterAnnotatorResults(List<List<E>> entities, String datasetName, String annotatorName);

    <E extends Marking> void cache(List<List<E>> entities, String datasetName);
}
