/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.gerbil.evaluate.impl;

import java.util.List;

import org.aksw.gerbil.evaluate.DoubleEvaluationResult;
import org.aksw.gerbil.evaluate.EvaluationResult;
import org.aksw.gerbil.evaluate.EvaluationResultContainer;
import org.aksw.gerbil.matching.ClassifiedEvaluationCounts;
import org.aksw.gerbil.matching.EvaluationCounts;
import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.gerbil.matching.impl.clas.ClassConsideringMatchingsCounter;
import org.aksw.gerbil.matching.impl.clas.MarkingClassifier;
import org.aksw.gerbil.semantic.kb.UriKBClassifier;
import org.aksw.gerbil.transfer.nif.Meaning;

public class InKBClassBasedFMeasureCalculator<T extends Meaning> extends FMeasureCalculator<T>
        implements MarkingClassifier<T> {

    public static final String MACRO_ACCURACY_NAME = "Macro Accuracy";
    public static final String MICRO_ACCURACY_NAME = "Micro Accuracy";

    public static final String IN_KB_MACRO_F1_SCORE_NAME = "InKB Macro F1 score";
    public static final String IN_KB_MACRO_PRECISION_NAME = "InKB Macro Precision";
    public static final String IN_KB_MACRO_RECALL_NAME = "InKB Macro Recall";
    public static final String IN_KB_MICRO_F1_SCORE_NAME = "InKB Micro F1 score";
    public static final String IN_KB_MICRO_PRECISION_NAME = "InKB Micro Precision";
    public static final String IN_KB_MICRO_RECALL_NAME = "InKB Micro Recall";

    public static final String EE_MACRO_F1_SCORE_NAME = "EE Macro F1 score";
    public static final String EE_MACRO_PRECISION_NAME = "EE Macro Precision";
    public static final String EE_MACRO_RECALL_NAME = "EE Macro Recall";
    public static final String EE_MICRO_F1_SCORE_NAME = "EE Micro F1 score";
    public static final String EE_MICRO_PRECISION_NAME = "EE Micro Precision";
    public static final String EE_MICRO_RECALL_NAME = "EE Micro Recall";

    private static final int IN_KB_CLASS_ID = 0;
    private static final int EE_CLASS_ID = 1;

    private UriKBClassifier classifier;

    public InKBClassBasedFMeasureCalculator(MatchingsSearcher<T> searcher, UriKBClassifier classifier) {
        super(null);
        this.classifier = classifier;
        this.matchingsCounter = new ClassConsideringMatchingsCounter<T>(searcher, this);
    }

    @Override
    public void evaluate(List<List<T>> annotatorResults, List<List<T>> goldStandard,
            EvaluationResultContainer results) {
        // the super class performs the matching counter calls
        EvaluationCounts counts[] = generateMatchingCounts(annotatorResults, goldStandard);
        results.addResults(calculateMicroFMeasure(counts));
        results.addResults(calculateMacroFMeasure(counts));
        results.addResults(calculateAccuracies(counts, goldStandard));

        EvaluationCounts classCounts[] = new EvaluationCounts[counts.length];
        int sums = 0;
        for (int i = 0; i < counts.length; ++i) {
            classCounts[i] = ((ClassifiedEvaluationCounts) counts[i]).classifiedCounts[IN_KB_CLASS_ID];
            sums += classCounts[i].truePositives + classCounts[i].falseNegatives + classCounts[i].falsePositives;
        }
        if (sums > 0) {
            results.addResults(calculateMicroFMeasure(classCounts, IN_KB_MICRO_PRECISION_NAME, IN_KB_MICRO_RECALL_NAME,
                    IN_KB_MICRO_F1_SCORE_NAME));
            results.addResults(calculateMacroFMeasure(classCounts, IN_KB_MACRO_PRECISION_NAME, IN_KB_MACRO_RECALL_NAME,
                    IN_KB_MACRO_F1_SCORE_NAME));
        }
        sums = 0;
        for (int i = 0; i < counts.length; ++i) {
            classCounts[i] = ((ClassifiedEvaluationCounts) counts[i]).classifiedCounts[EE_CLASS_ID];
            sums += classCounts[i].truePositives + classCounts[i].falseNegatives + classCounts[i].falsePositives;
        }
        if (sums > 0) {
            results.addResults(calculateMicroFMeasure(classCounts, EE_MICRO_PRECISION_NAME, EE_MICRO_RECALL_NAME,
                    EE_MICRO_F1_SCORE_NAME));
            results.addResults(calculateMacroFMeasure(classCounts, EE_MACRO_PRECISION_NAME, EE_MACRO_RECALL_NAME,
                    EE_MACRO_F1_SCORE_NAME));
        }
    }

    private EvaluationResult[] calculateAccuracies(EvaluationCounts counts[], List<List<T>> goldStandard) {
        int tp, elements, tpSum = 0, elementsSum = 0, docCount = 0;
        double microAcc = 0, macroAcc = 0;
        for (int i = 0; i < counts.length; ++i) {
            elements = goldStandard.get(i).size();
            if (elements > 0) {
                tp = counts[i].truePositives;
                tpSum += tp;
                elementsSum += elements;
                macroAcc += (double) tp / (double) elements;
                ++docCount;
            }
        }
        macroAcc /= docCount;
        microAcc = (double) tpSum / (double) elementsSum;
        return new EvaluationResult[] { new DoubleEvaluationResult(MACRO_ACCURACY_NAME, macroAcc),
                new DoubleEvaluationResult(MICRO_ACCURACY_NAME, microAcc) };
    }

    @Override
    public int getNumberOfClasses() {
        return 2; // inKB and EE
    }

    @Override
    public int getClass(T meaning) {
        if ((meaning == null) || (!(meaning instanceof Meaning))) {
            return EE_CLASS_ID;
        }
        for (String uri : ((Meaning) meaning).getUris()) {
            if (classifier.isKBUri(uri)) {
                return IN_KB_CLASS_ID;
            }
        }
        return EE_CLASS_ID;
    }
}
