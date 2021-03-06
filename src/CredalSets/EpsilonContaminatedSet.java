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

    private static final Class CLAZZ = EpsilonContaminatedSet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    double epsilon;

    /**
     * Construct an EpsilonContaminatedSet with ProbabilityFunction object and
     * given epsilon.
     *
     * @param probFunc probability function
     * @param epsilon
     */
    public EpsilonContaminatedSet(ProbabilityFunction probFunc, double epsilon)
    {
        super(probFunc, probFunc.getValues());
        this.epsilon = (epsilon < 0.0) || (epsilon > 1.0) ? 0.0 : epsilon;
    }

    /**
     * Perform calculation of marginal posterior distributions for an
     * epsilon-contaminated global neighbourhood The method assumes that the
     * values in the EpsilonContaminated are actually unnormalized --- if not,
     * incorrect results are produced.
     *
     * @return he calculated probability function
     */
    public ProbabilityFunction posteriorMarginal()
    {
        double oneMinusEpsilon = 1.0 - epsilon;

        double lowerValues[] = new double[numberValues()];
        double upperValues[] = new double[numberValues()];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((getVariable(0) instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) getVariable(0)).isObserved() == true))
        {
            for (int i = 0; i < numberValues(); i++)
            {
                lowerValues[i] = getValue(i);
                upperValues[i] = getValue(i);
            }
        } // Else, apply the marginalization property.
        else
        {
            double summation = 0.0;
            for (int i = 0; i < numberValues(); i++)
            {
                summation += getValue(i);
            }

            for (int i = 0; i < numberValues(); i++)
            {
                lowerValues[i] = (oneMinusEpsilon * getValue(i)) /
                                 ((oneMinusEpsilon * summation) + epsilon);
            }

            for (int i = 0; i < numberValues(); i++)
            {
                upperValues[i] = ((oneMinusEpsilon * getValue(i)) + epsilon) /
                                 ((oneMinusEpsilon * summation) + epsilon);
            }
        }

        return new QBProbabilityFunction(bayesNet,
                                         getVariables(),
                                         getValues(),
                                         lowerValues,
                                         upperValues,
                                         properties);
    }

    /**
     * Perform calculation of expected value.
     *
     * @param discrFunc discrete function
     * @return double array of the expected values
     */
    public double[] expectedValues(DiscreteFunction discrFunc)
    {
        double oneMinusEpsilon = 1.0 - epsilon;
        double results[] = new double[2];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((getVariable(0) instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) getVariable(0)).isObserved() == true))
        {
            results[0] =
            discrFunc.getValue(
                    ((ProbabilityVariable) getVariable(0)).getObservedIndex());
            results[1] = results[0];
        } // Else, apply the marginalization property.
        else
        {
            // Obtain the summations
            double uTotal = 0.0;
            for (int i = 0; i < numberValues(); i++)
            {
                uTotal += discrFunc.getValue(i) * getValue(i);
            }
            // Obtain the maximum and minimum of functions
            double maxDfValue = discrFunc.getValue(0);
            double minDfValue = discrFunc.getValue(0);
            for (int i = 1; i < discrFunc.numberValues(); i++)
            {
                if (maxDfValue < discrFunc.getValue(i))
                {
                    maxDfValue = discrFunc.getValue(i);
                }
                if (minDfValue > discrFunc.getValue(i))
                {
                    minDfValue = discrFunc.getValue(i);
                }
            }
            // Calculate the values
            results[0] = oneMinusEpsilon * uTotal + epsilon * maxDfValue;
            results[1] = oneMinusEpsilon * uTotal + epsilon * minDfValue;
        }

        return results;
    }

    /**
     * Perform calculation of posterior expected value. Assumes that the
     * probability values are not normalised; probability values are p(x, e)
     * where e is the fixed evidence.
     *
     * @param discrFunc discrete function
     * @return double array of the posterior expected values
     */
    public double[] posteriorExpectedValues(DiscreteFunction discrFunc)
    {
        double oneMinusEpsilon = 1.0 - epsilon;
        double results[] = new double[2];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((getVariable(0) instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) getVariable(0)).isObserved() == true))
        {
            results[0] =
            discrFunc.getValue(
                    ((ProbabilityVariable) getVariable(0)).getObservedIndex());
            results[1] = results[0];
        } // Else, apply the marginalization property.
        else
        {
            // Obtain the summations
            double pTotal = 0.0;
            double uTotal = 0.0;
            for (int i = 0; i < numberValues(); i++)
            {
                pTotal += getValue(i);
                uTotal += discrFunc.getValue(i) * getValue(i);
            }
            // Obtain the maximum and minimum of functions
            double maxDfValue = discrFunc.getValue(0);
            double minDfValue = discrFunc.getValue(0);
            for (int i = 1; i < discrFunc.numberValues(); i++)
            {
                if (maxDfValue < discrFunc.getValue(i))
                {
                    maxDfValue = discrFunc.getValue(i);
                }
                if (minDfValue > discrFunc.getValue(i))
                {
                    minDfValue = discrFunc.getValue(i);
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

        return results;
    }
}
