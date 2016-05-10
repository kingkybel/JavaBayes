/*
 * GeneralizedChoquetIntegral.java
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
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Use Walley generalization of the Choquet lower and upper integrals to obtain
 * the lower and upper expectations of 2-monotone capacities.
 */
public class GeneralizedChoquetIntegral
{

    private static final String CLASS_NAME =
                                GeneralizedChoquetIntegral.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    double results[];

    /**
     * Calculate the lower and upper Choquet integrals using Walley's
     * generalization, for a total variation neighborhood.
     *
     * @param tmc
     * @param df
     */
    public GeneralizedChoquetIntegral(TwoMonotoneCapacity tmc,
                                      DiscreteFunction df)
    {
        int i;
        double positiveSide, negativeSide;

        // Allocate the results
        results = new double[2];

        // Order the non-negative values of df in increasing order
        // Zero is the first element of the vector
        ArrayList positive = sortPositive(df);

        // Order the non-positive values of df in decreasing order
        // Zero is the first element of the vector
        ArrayList negative = sortNegative(df);

        // Create an array of value differences from the positive side
        double dfPositive[] = new double[positive.size() - 1];
        for (i = 1; i <= dfPositive.length; i++)
        {
            dfPositive[i - 1] = ((Double) (positive.get(i))) -
                                ((Double) (positive.get(i - 1)));
        }

        // Create an array of value differences from the negative side
        double dfNegative[] = new double[negative.size() - 1];
        for (i = 1; i <= dfNegative.length; i++)
        {
            dfNegative[i - 1] = ((Double) (negative.get(i))) -
                                ((Double) (negative.get(i - 1)));
        }

        // Create arrays of lower and upper probability
        // values for the positive side
        double lpPositive[] = new double[dfPositive.length];
        double upPositive[] = new double[dfPositive.length];
        boundPositive(tmc, df, positive, lpPositive, upPositive);

        // Create arrays of lower and upper probability
        // values for the positive side
        double lpNegative[] = new double[dfNegative.length];
        double upNegative[] = new double[dfNegative.length];
        boundNegative(tmc, df, negative, lpNegative, upNegative);

        // First obtain the lower Walley integral
        positiveSide = 0.0;
        negativeSide = 0.0;
        for (i = 0; i < dfPositive.length; i++)
        {
            positiveSide += dfPositive[i] * lpPositive[i];
        }
        for (i = 0; i < dfNegative.length; i++)
        {
            negativeSide += dfNegative[i] * upNegative[i];
        }
        results[0] = positiveSide + negativeSide;

        // Now obtain the upper Walley integral
        positiveSide = 0.0;
        negativeSide = 0.0;
        for (i = 0; i < dfPositive.length; i++)
        {
            positiveSide += dfPositive[i] * upPositive[i];
        }
        for (i = 0; i < dfNegative.length; i++)
        {
            negativeSide += dfNegative[i] * lpNegative[i];
        }
        results[1] = positiveSide + negativeSide;
    }

    /**
     * Collect the positive values in df and sort them in increasing order
     * (first value is assumed zero).
     */
    private ArrayList sortPositive(DiscreteFunction df)
    {
        ArrayList sorted = new ArrayList();

        // First element is zero, regardless of df values
        sorted.add(0.0);

        // Go through df values
        for (int i = 0; i < df.numberValues(); i++)
        {
            // Process only positive values
            if (df.getValue(i) <= 0.0)
            {
                continue;
            }

            // Insert value in vector
            for (int j = 0; j < sorted.size(); j++)
            {
                if (df.getValue(i) < ((Double) sorted.get(j)))
                {
                    sorted.add(df.getValue(i));
                }
            }

            // Insert value last in vector if it is not inserted at this point
            sorted.add(df.getValue(i));
        }
        return (sorted);
    }

    /**
     * Collect the negative values in df and sort them in decreasing order
     * (first value is assumed zero).
     */
    private ArrayList sortNegative(DiscreteFunction df)
    {
        ArrayList sorted = new ArrayList();

        // First element is zero, regardless of df values
        sorted.add(0.0);

        // Go through df values
        for (int i = 0; i < df.numberValues(); i++)
        {
            // Process only negative values
            if (df.getValue(i) >= 0.0)
            {
                continue;
            }

            // Insert value in vector
            for (int j = 0; j < sorted.size(); j++)
            {
                if (df.getValue(i) > ((Double) sorted.get(j)))
                {
                    sorted.add(df.getValue(i));
                }
            }

            // Insert value last in vector if it is not inserted at this point
            sorted.add(df.getValue(i));
        }
        return (sorted);
    }

    /**
     * Obtain the lower and upper probability for the event { df(x) >
     * sortedValue[i] }
     */
    private void boundPositive(TwoMonotoneCapacity tmc,
                               DiscreteFunction df, ArrayList sortedValues,
                               double lps[], double ups[])
    {
        int i, j;
        double lp;
        double sortedValue;

        i = 0;
        for (Object e : sortedValues)
        {
            lp = 0.0; // Initialize
            sortedValue = ((Double) e);
            // Collect the base probability for
            // all atoms such that df(xJ > sortedValues[i])
            for (j = 0; j < df.numberValues(); j++)
            {
                if (df.getValue(j) > sortedValue)
                {
                    // Add base probability of this atom
                    lp += tmc.getAtomProbability(j);
                }
            }
            // Calculate the lower and upper probabilities
            lps[i] = tmc.getLowerProbabilityFromBase(lp);
            ups[i] = tmc.getUpperProbabilityFromBase(lp);
            i++;
        }
    }

    /**
     * Obtain the lower and upper probability for the event { df(x) <
     * sortedValue[i] }
     */
    private void boundNegative(TwoMonotoneCapacity tmc,
                               DiscreteFunction df, ArrayList sortedValues,
                               double lps[], double ups[])
    {
        int i, j;
        double lp;
        double sortedValue;

        i = 0;
        for (Object e : sortedValues)
        {
            lp = 0.0; // Initialize
            sortedValue = ((Double) e);
            // Collect the base probability for
            // all atoms such that df(xJ > sortedValues[i])
            for (j = 0; j < df.numberValues(); j++)
            {
                if (df.getValue(j) < sortedValue)
                {
                    // Add base probability of this atom
                    lp += tmc.getAtomProbability(j);
                }
            }
            // Calculate the lower and upper probabilities
            lps[i] = tmc.getLowerProbabilityFromBase(lp);
            ups[i] = tmc.getUpperProbabilityFromBase(lp);
            i++;
        }
    }
}
