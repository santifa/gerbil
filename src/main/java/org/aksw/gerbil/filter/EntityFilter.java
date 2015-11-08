package org.aksw.gerbil.filter;

import org.aksw.gerbil.transfer.nif.Marking;

import java.util.List;

/**
 * An entity filter describes a mechanism to select
 * special entities out of the gold standard as well as the annotator result.
 * <p/>
 * Created by Henrik Jürges on 07.11.15.
 *
 * @param <T> the type parameter
 */
public interface EntityFilter<T extends Marking> {

    /**
     * Filter a list of lists. The matching should be done one the gold standard,
     * discarding also every result found by the annotator. By contract the list order
     * should be the same as before.
     *
     * @param goldStandard    the gold standard
     * @param annotatorResult the annotator result
     * @return the list
     */
    List<List<T>> filterEntities(List<List<T>> goldStandard, List<List<T>> annotatorResult);
}
