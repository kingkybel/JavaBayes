package QuasiBayesianInferences;

import BayesianInferences.Expectation;
import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.ProbabilityFunction;
import CredalSets.ConstantDensityBoundedSet;
import CredalSets.ConstantDensityRatioSet;
import CredalSets.EpsilonContaminatedSet;
import CredalSets.TotalVariationSet;
import QuasiBayesianNetworks.QuasiBayesNet;
import java.util.logging.Logger;

/*
 * QBExpectation provides methods for calculation of univariate
 * moments and expectations.
 *
 * Rule: LOCAL dominates NONE; GLOBAL dominates LOCAL.
 *       -> When there is a local credal set, compute with the
 *          local credal set.
 *       -> When there is a global credal set specification,
 *          compute with that (ignore local credal sets).
 */
/**
 *
 * @author kybelksd
 */
public class QBExpectation extends Expectation
{

    private static final Logger LOG =
                                Logger.getLogger(QBExpectation.class.getName());

    /**
     * Constructor for a QBExpectation.
     *
     * @param bN
     * @param dpc
     */
    public QBExpectation(BayesNet bN, boolean dpc)
    {
        super(bN, dpc);
    }

    /**
     * Initialize the Inference object.
     */
    private void initializeInference()
    {
        inference = new QBInference(bn, doProduceClusters);
    }

    /*
     * Do the Expectation.
     */
    /**
     *
     * @param df
     */
    @Override
    protected void doExpectationFromInference(DiscreteFunction df)
    {
        currentFunction = df;

        if (((QBInference) inference).isInferenceWithoutLocalNeighborhoods)
        {
            expectationWithoutLocalNeighborhoods(df);
        }
        else
        {
            expectationWithLocalNeighborhoods(df);
        }
    }

    /*
     * Perform calculation of expectations
     * when local neighborhoods are present.
     */
    private void expectationWithLocalNeighborhoods(DiscreteFunction df)
    {
        int i, j, jump = 1;
        double v, min, max;
        ProbabilityFunction normalizedResults;

        // Get result normalized with respect to transparent variables
        normalizedResults =
        ((QBInference) inference).listOfLocalNeighborhoodResults;

        // Get the bounds on expectations
        for (i = 1; i < normalizedResults.numberVariables(); i++)
        {
            jump *= normalizedResults.getVariable(i).numberValues();
        }
        min = df.getValue(0);
        max = df.getValue(0);
        for (i = 0; i < df.numberValues(); i++)
        {
            if (min < df.getValue(i))
            {
                min = df.getValue(i);
            }
            if (max > df.getValue(i))
            {
                max = df.getValue(i);
            }
        }
        for (j = 0; j < jump; j++)
        {
            v = 0.0;
            for (i = 0; i < normalizedResults.getVariable(0).numberValues();
                 i++)
            {
                v += df.getValue(i) * normalizedResults.
                     getValue(j + i * jump);
            }
            if (min > v)
            {
                min = v;
            }
            if (max < v)
            {
                max = v;
            }
        }

        // Construct results
        results = new double[2];
        results[0] = min;
        results[1] = max;
    }

    /*
     * Perform calculation of expectations
     * when local pneighborhoods are absent; handles global
     * neighborhoods if necessary.
     */
    private void expectationWithoutLocalNeighborhoods(DiscreteFunction df)
    {
        QBInference qbInference = (QBInference) inference;
        QuasiBayesNet qbn = ((QuasiBayesNet) (qbInference.getBayesNet()));

        switch (qbn.getGlobalNeighborhoodType())
        {
            case QuasiBayesNet.NO_CREDAL_SET:
                ProbabilityFunction res = qbInference.getResult();
                results = new double[1];
                results[0] = res.expectedValue(df);
                break;
            case QuasiBayesNet.CONSTANT_DENSITY_RATIO:
                ProbabilityFunction cdrRes =
                                    new ProbabilityFunction(qbInference.
                                            getBucketTree().
                                            getUnnormalizedResult(), qbn);
                ConstantDensityRatioSet cdr =
                                        new ConstantDensityRatioSet(cdrRes,
                                                                    qbn.
                                                                    getGlobalNeighborhoodParameter());
                results = cdr.posteriorExpectedValues(df);
                break;
            case QuasiBayesNet.EPSILON_CONTAMINATED:
                ProbabilityFunction epsRes =
                                    new ProbabilityFunction(qbInference.
                                            getBucketTree().
                                            getUnnormalizedResult(), qbn);
                EpsilonContaminatedSet eps =
                                       new EpsilonContaminatedSet(epsRes, qbn.
                                                                  getGlobalNeighborhoodParameter());
                results = eps.posteriorExpectedValues(df);
                break;
            case QuasiBayesNet.CONSTANT_DENSITY_BOUNDED:
                ProbabilityFunction cdbRes =
                                    new ProbabilityFunction(qbInference.
                                            getBucketTree().
                                            getUnnormalizedResult(), qbn);
                ConstantDensityBoundedSet cdb =
                                          new ConstantDensityBoundedSet(cdbRes,
                                                                        qbn.
                                                                        getGlobalNeighborhoodParameter());
                results = cdb.posteriorExpectedValues(df);
                break;
            case QuasiBayesNet.TOTAL_VARIATION:
                ProbabilityFunction tvRes =
                                    new ProbabilityFunction(qbInference.
                                            getBucketTree().
                                            getUnnormalizedResult(), qbn);
                TotalVariationSet tv =
                                  new TotalVariationSet(tvRes, qbn.
                                                        getGlobalNeighborhoodParameter());
                results = tv.posteriorExpectedValues(df);
                break;
        }
    }
}
