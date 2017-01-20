/*
 * QBExpectation.java
 * @author Fabio G. Cozman
 * Copyright 1996 - 1999, Fabio G. Cozman,
 *          Carnergie Mellon University, Universidade de Sao Paulo
 * fgcozman@usp.br, http://www.cs.cmu.edu/~fgcozman/home.html
 *
 * The JavaBayes distribution is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation (either
 * version 2 of the License or, at your option, any later version),
 * provided that this notice and the name of the author appear in all
 * copies. Upon request to the author, some of the packages in the
 * JavaBayes distribution can be licensed under the GNU Lesser General
 * Public License as published by the Free Software Foundation (either
 * version 2 of the License, or (at your option) any later version).
 * If you're using the software, please notify fgcozman@usp.br so
 * that you can receive updates and patches. JavaBayes is distributed
 * "as is", in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with the JavaBayes distribution. If not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
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

/**
 * QBExpectation provides methods for calculation of univariate moments and
 * expectations.
 *
 * Rule: LOCAL dominates NONE; GLOBAL dominates LOCAL.
 * <ol>
 * <li> When there is a local credal set, compute with the local credal
 * set.</li>
 * <li> When there is a global credal set specification, compute with that
 * (ignore local credal sets).</li>
 * </ol>
 *
 */
public class QBExpectation extends Expectation
{

    private static final Class CLAZZ = QBExpectation.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor for a QBExpectation.
     *
     * @param bayesNet            the underlying Bayesian network
     * @param isProducingClusters true, if clusters should be produced, false
     *                            otherwise
     */
    public QBExpectation(BayesNet bayesNet, boolean isProducingClusters)
    {
        super(bayesNet, isProducingClusters);
        initializeInference();
    }

    /**
     * Initialize the Inference object.
     */
    private void initializeInference()
    {
        setInference(new QBInference(getBayesNet(), isProducingClusters()));
    }

    /**
     * Do the Expectation.
     *
     * @param discrFunc discrete function
     */
    @Override
    protected void doExpectationFromInference(DiscreteFunction discrFunc)
    {
        setCurrentFunction(discrFunc);

        if (((QBInference) getInference()).isInferenceWithoutLocalNeighborhoods)
        {
            expectationWithoutLocalNeighborhoods(discrFunc);
        }
        else
        {
            expectationWithLocalNeighborhoods(discrFunc);
        }
    }

    /**
     * Perform calculation of expectations when local neighborhoods are present.
     */
    private void expectationWithLocalNeighborhoods(DiscreteFunction discrFunc)
    {
        int i, j, jump = 1;
        double v, min, max;
        ProbabilityFunction normalizedResults;

        // Get result normalized with respect to transparent variables
        normalizedResults =
        ((QBInference) getInference()).listOfLocalNeighborhoodResults;

        // Get the bounds on expectations
        for (i = 1; i < normalizedResults.numberVariables(); i++)
        {
            jump *= normalizedResults.getVariable(i).numberValues();
        }
        min = discrFunc.getValue(0);
        max = discrFunc.getValue(0);
        for (i = 0; i < discrFunc.numberValues(); i++)
        {
            if (min < discrFunc.getValue(i))
            {
                min = discrFunc.getValue(i);
            }
            if (max > discrFunc.getValue(i))
            {
                max = discrFunc.getValue(i);
            }
        }
        for (j = 0; j < jump; j++)
        {
            v = 0.0;
            for (i = 0; i < normalizedResults.getVariable(0).numberValues();
                 i++)
            {
                v += discrFunc.getValue(i) * normalizedResults.
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

        setResults(min, max);
    }

    /**
     * Perform calculation of expectations when local pneighborhoods are absent;
     * handles global neighborhoods if necessary.
     */
    private void expectationWithoutLocalNeighborhoods(DiscreteFunction discrFunc)
    {
        QBInference qbInference = (QBInference) getInference();
        QuasiBayesNet qbn = ((QuasiBayesNet) (qbInference.getBayesNet()));

        switch (qbn.getGlobalNeighborhoodType())
        {
            case NO_CREDAL_SET:
                ProbabilityFunction res = qbInference.getResult();
                setResults(res.expectedValue(discrFunc));
                break;
            case CONSTANT_DENSITY_RATIO:
                ProbabilityFunction cdrRes =
                                    new ProbabilityFunction(qbInference.
                                            getBucketTree().
                                            getUnnormalizedResult(), qbn);
                ConstantDensityRatioSet cdr =
                                        new ConstantDensityRatioSet(cdrRes,
                                                                    qbn.
                                                                    getGlobalNeighborhoodParameter());
                setResults(cdr.posteriorExpectedValues(discrFunc));
                break;
            case EPSILON_CONTAMINATED:
                ProbabilityFunction epsRes =
                                    new ProbabilityFunction(
                                            qbInference.getBucketTree().
                                            getUnnormalizedResult(),
                                            qbn);
                EpsilonContaminatedSet eps =
                                       new EpsilonContaminatedSet(
                                               epsRes,
                                               qbn.
                                               getGlobalNeighborhoodParameter());
                setResults(eps.posteriorExpectedValues(discrFunc));
                break;
            case CONSTANT_DENSITY_BOUNDED:
                ProbabilityFunction cdbRes = new ProbabilityFunction(
                                    qbInference.getBucketTree().
                                    getUnnormalizedResult(),
                                    qbn);
                ConstantDensityBoundedSet cdb = new ConstantDensityBoundedSet(
                                          cdbRes,
                                          qbn.getGlobalNeighborhoodParameter());
                setResults(cdb.posteriorExpectedValues(discrFunc));
                break;
            case TOTAL_VARIATION:
                ProbabilityFunction tvRes =
                                    new ProbabilityFunction(qbInference.
                                            getBucketTree().
                                            getUnnormalizedResult(), qbn);
                TotalVariationSet tv = new TotalVariationSet(
                                  tvRes,
                                  qbn.getGlobalNeighborhoodParameter());
                setResults(tv.posteriorExpectedValues(discrFunc));
                break;
        }
    }
}
