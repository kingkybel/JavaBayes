/*
 * ConstantDensityRatioSet.java
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
public class ConstantDensityRatioSet
        extends FinitelyGeneratedSet
        implements MappingDouble
{

    private static final Class CLAZZ = ConstantDensityRatioSet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final static double ACCURACY = 10E-8;
    private double k;
    // Auxiliary variable that holds a discrete function for bracketing.
    private DiscreteFunction temporaryDiscreteFunction;

    /**
     * Constructor for an ConstantDensityRatioSet ProbabilityFunction object and
     * given constant.
     *
     * @param probFunc
     * @param kk
     */
    public ConstantDensityRatioSet(ProbabilityFunction probFunc, double kk)
    {
        super(probFunc, probFunc.getValues());
        k = kk;
        if (k <= 0.0)
        {
            k = 1.0;
        }
        else
        {
            if (k < 1.0)
            {
                k = 1.0 / k;
            }
        }
    }

    /**
     * Perform calculation of marginal posterior distributions for a density
     * ratio global neighbourhood.
     *
     * @return
     */
    public ProbabilityFunction posteriorMarginal()
    {
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
            double total = 0.0;
            for (int i = 0; i < values.length; i++)
            {
                total += values[i];
            }
            for (int i = 0; i < values.length; i++)
            {
                lowerValues[i] =
                (values[i] / k) /
                ((values[i] / k) + k * (total - values[i]));
            }
            for (int i = 0; i < values.length; i++)
            {
                upperValues[i] =
                (k * values[i]) /
                (k * values[i] + (total - values[i]) / k);
            }
        }

        return (new QBProbabilityFunction(bayesNet, variables, values,
                                          lowerValues, upperValues, properties));
    }

    /**
     * Perform calculation of expected value for density ratio.
     *
     * @param discrFunc
     * @return
     */
    public double[] expectedValues(DiscreteFunction discrFunc)
    {
        Bracketing bracket = new Bracketing();
        double results[] = new double[2];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((variables[0] instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) variables[0]).isObserved() == true))
        {
            results[0] =
            discrFunc.getValue(((ProbabilityVariable) variables[0]).
                    getObservedIndex());
            results[1] = results[0];
            return (results);
        }
     // Else, apply the marginalization property.

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

        // Prepare the temporaryDiscreteFunction variable for bracketing
        temporaryDiscreteFunction = discrFunc;

        // Bracket the lower expectation
        double lowerExpectation =
               bracket.perform(this,
                               MappingDouble.Type.LOWER_EXPECTATION_BRACKET,
                               minDfValue,
                               maxDfValue,
                               ACCURACY);

        // Bracket the upper expectation
        double upperExpectation =
               bracket.perform(this,
                               MappingDouble.Type.UPPER_EXPECTATION_BRACKET,
                               minDfValue,
                               maxDfValue,
                               ACCURACY);

        // Calculate the values
        results[0] = lowerExpectation;
        results[1] = upperExpectation;

        return (results);
    }

    /**
     * Perform calculation of posterior expected value. Assumes that the
     * probability values are not normalised; probability values are p(x, e)
     * where e is the fixed evidence
     *
     * @param discrFunc
     * @return
     */
    public double[] posteriorExpectedValues(DiscreteFunction discrFunc)
    {
        return (expectedValues(discrFunc));
    }

    @Override
    public double map(MappingDouble.Type mapType, double mapInput)
    {
        int i;
        double aux;
        double mapOutputUpper = 0.0;
        double mapOutputLower = 0.0;
        double mapOutput = 0.0;
        DiscreteFunction tdf = temporaryDiscreteFunction;

        switch (mapType)
        {
            case LOWER_EXPECTATION_BRACKET:
                for (i = 0; i < values.length; i++)
                {
                    aux = tdf.getValue(i) - mapInput;
                    mapOutputUpper += (k * values[i]) * (-Math.max(-aux, 0.0));
                    mapOutputLower += (values[i] / k) * (Math.max(aux, 0.0));
                }
                break;
            case UPPER_EXPECTATION_BRACKET:
                for (i = 0; i < values.length; i++)
                {
                    aux = tdf.getValue(i) - mapInput;
                    mapOutputUpper += (k * values[i]) * (Math.max(aux, 0.0));
                    mapOutputLower += (values[i] / k) * (-Math.max(-aux, 0.0));
                }
                break;
        }
        mapOutput = mapOutputUpper + mapOutputLower;
        return (mapOutput);
    }
}
