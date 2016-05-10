/*
 * EpsilonContaminatedSet.java
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
package CredalSets;

import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class EpsilonContaminatedSet
        extends FinitelyGeneratedSet
{

    private static final Logger LOGGER =
                                Logger.getLogger(EpsilonContaminatedSet.class.
                                        getName());

    double epsilon;

    /**
     * Constructor for an EpsilonContaminatedSet ProbabilityFunction object and
     * given epsilon.
     *
     * @param pf
     * @param eps
     */
    public EpsilonContaminatedSet(ProbabilityFunction pf, double eps)
    {
        super(pf, pf.getValues());
        epsilon = eps;
        if ((epsilon < 0.0) || (epsilon > 1.0))
        {
            epsilon = 0.0;
        }
    }

    /**
     * Perform calculation of marginal posterior distributions for an
     * epsilon-contaminated global neighborhood The method assumes that the
     * values in the EpsilonContaminated are actually unnormalized --- if not,
     * incorrect results are produced.
     *
     * @return
     */
    public ProbabilityFunction posteriorMarginal()
    {
        double oneMinusEpsilon = 1.0 - epsilon;

        double lowerValues[] = new double[values.length];
        double upperValues[] = new double[values.length];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((variables[0] instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) variables[0]).isObserved() == true))
        {
            for (int i = 0; i < values.length; i++)
            {
                lowerValues[i] = values[i];
                upperValues[i] = values[i];
            }
        } // Else, apply the marginalization property.
        else
        {
            double summation = 0.0;
            for (int i = 0; i < values.length; i++)
            {
                summation += values[i];
            }

            for (int i = 0; i < values.length; i++)
            {
                lowerValues[i] = (oneMinusEpsilon * values[i]) /
                                 ((oneMinusEpsilon * summation) + epsilon);
            }

            for (int i = 0; i < values.length; i++)
            {
                upperValues[i] = ((oneMinusEpsilon * values[i]) + epsilon) /
                                 ((oneMinusEpsilon * summation) + epsilon);
            }
        }

        return (new QBProbabilityFunction(bn, variables, values,
                                          lowerValues, upperValues, properties));
    }

    /**
     * Perform calculation of expected value.
     *
     * @param df
     * @return
     */
    public double[] expectedValues(DiscreteFunction df)
    {
        double oneMinusEpsilon = 1.0 - epsilon;
        double results[] = new double[2];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((variables[0] instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) variables[0]).isObserved() == true))
        {
            results[0] =
            df.getValue(((ProbabilityVariable) variables[0]).
                    getObservedIndex());
            results[1] = results[0];
        } // Else, apply the marginalization property.
        else
        {
            // Obtain the summations
            double uTotal = 0.0;
            for (int i = 0; i < numberValues(); i++)
            {
                uTotal += df.getValue(i) * values[i];
            }
            // Obtain the maximum and minimum of functions
            double maxDfValue = df.getValue(0);
            double minDfValue = df.getValue(0);
            for (int i = 1; i < df.numberValues(); i++)
            {
                if (maxDfValue < df.getValue(i))
                {
                    maxDfValue = df.getValue(i);
                }
                if (minDfValue > df.getValue(i))
                {
                    minDfValue = df.getValue(i);
                }
            }
            // Calculate the values
            results[0] = oneMinusEpsilon * uTotal + epsilon * maxDfValue;
            results[1] = oneMinusEpsilon * uTotal + epsilon * minDfValue;
        }

        return (results);
    }

    /**
     * Perform calculation of posterior expected value. Assumes that the
     * probability values are not normalized; probability values are p(x, e)
     * where e is the fixed evidence.
     *
     * @param df
     * @return
     */
    public double[] posteriorExpectedValues(DiscreteFunction df)
    {
        double oneMinusEpsilon = 1.0 - epsilon;
        double results[] = new double[2];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((variables[0] instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) variables[0]).isObserved() == true))
        {
            results[0] =
            df.getValue(((ProbabilityVariable) variables[0]).
                    getObservedIndex());
            results[1] = results[0];
        } // Else, apply the marginalization property.
        else
        {
            // Obtain the summations
            double pTotal = 0.0;
            double uTotal = 0.0;
            for (int i = 0; i < values.length; i++)
            {
                pTotal += values[i];
                uTotal += df.getValue(i) * values[i];
            }
            // Obtain the maximum and minimum of functions
            double maxDfValue = df.getValue(0);
            double minDfValue = df.getValue(0);
            for (int i = 1; i < df.numberValues(); i++)
            {
                if (maxDfValue < df.getValue(i))
                {
                    maxDfValue = df.getValue(i);
                }
                if (minDfValue > df.getValue(i))
                {
                    minDfValue = df.getValue(i);
                }
            }
            // Calculate the values
            results[0] =
            (oneMinusEpsilon * uTotal + epsilon * minDfValue) /
            (oneMinusEpsilon * pTotal + epsilon);
            results[1] =
            (oneMinusEpsilon * uTotal + epsilon * maxDfValue) /
            (oneMinusEpsilon * pTotal + epsilon);
        }

        return (results);
    }
}
