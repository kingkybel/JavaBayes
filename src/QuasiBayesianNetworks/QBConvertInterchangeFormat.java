/*
 * QBConvertInterchangeFormat.java
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
package QuasiBayesianNetworks;

import BayesianNetworks.BayesNet;
import BayesianNetworks.ConvertInterchangeFormat;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import CredalSets.VertexSet;
import InterchangeFormat.IFProbabilityEntry;
import InterchangeFormat.IFProbabilityFunction;
import InterchangeFormat.InterchangeFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Class that converts the contents of an InterchangeFormat object to the
 * BayesNet object in the BayesianNetworks - package.
 */
public class QBConvertInterchangeFormat extends ConvertInterchangeFormat
{

    private static final Class CLAZZ = QBConvertInterchangeFormat.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor.
     *
     * @param interchangeFormat
     */
    public QBConvertInterchangeFormat(InterchangeFormat interchangeFormat)
    {
        super(interchangeFormat);
    }

    @Override
    protected ProbabilityFunction getProbabilityFunction(
            BayesNet bayesNet,
            IFProbabilityFunction ifProbFunc)
    {
        int i, jump, numberOfValues;
        double values[];
        ProbabilityVariable probVar, variables[];

        // Check and insert the probability variable indexes
        variables = createVariables(bayesNet, ifProbFunc);

        // Calculate the jump, i.e., the number of values
        // in the conditional distribution table for each value
        // of the first variable (this is used for default/entries)
        jump = 1;
        for (i = 1; i < variables.length; i++)
        {
            probVar = variables[i];
            jump *= probVar.numberValues();
        }

        // Calculate the number of values in the distribution
        numberOfValues = jump * variables[0].numberValues();

        // Allocate values and initialize
        values = new double[numberOfValues];
        for (i = 0; i < values.length; i++)
        {
            values[i] = ConvertInterchangeFormat.INVALID_VALUE;
        }

        // Process tables
        double extremePoints[][] = processExtremeTables(ifProbFunc, values);

        // Process defaults
        processDefaults(ifProbFunc, values, extremePoints, jump);

        // Process entries
        processEntries(bayesNet, ifProbFunc, variables, values, extremePoints,
                       jump);

        // Finish calculating the values
        finishValues(values, extremePoints);

        // Insert the data
        if (extremePoints == null)
        {
            return new ProbabilityFunction(bayesNet,
                                           variables,
                                           values,
                                           ifProbFunc.getProperties());
        }
        else
        {
            return new VertexSet(bayesNet,
                                 variables,
                                 extremePoints,
                                 ifProbFunc.getProperties());
        }
    }

    /**
     * Fill the values with the contents of the tables in the
     * IFProbabilityFunction object.
     *
     * @param ifProbFunc    interchange format probability function
     * @param values an array of double values
     * @return extreme points
     */
    double[][] processExtremeTables(IFProbabilityFunction ifProbFunc, double values[])
    {
        int i, j;
        double table[], extremePoints[][];

        // Put the table values
        ArrayList tables = ifProbFunc.getTables();
        int n = tables.size();

        // If there are no available tables
        if (n == 0)
        {
            return null;
        }

        // If there is a single table, no extremePoints are created
        // and it just acts as a standard Bayesian model
        if (n == 1)
        {
            table = (double[]) (tables.get(0));
            copyTableToValues(table, values);
            return null;
        }

        // Else, if there are several extremePoints in the credal set
        extremePoints = new double[n][];
        for (i = 0; i < extremePoints.length; i++)
        {
            extremePoints[i] = new double[values.length];
            for (j = 0; j < extremePoints[i].length; j++)
            {
                extremePoints[i][j] = ConvertInterchangeFormat.INVALID_VALUE;
            }
        }
        i = 0;
        for (Object e : tables)
        {
            table = (double[]) (e);
            copyTableToValues(table, extremePoints[i]);
            i++;
        }
        return extremePoints;
    }

    /**
     * Insert default values from the contents of the first specification of
     * defaults in the IFProbabilityFunction object.
     *
     * @param ifProbFunc           interchange format probability function
     * @param values        an array of double values
     * @param extremePoints matrix of extreme points
     * @param jump
     */
    void processDefaults(IFProbabilityFunction ifProbFunc,
                         double values[],
                         double extremePoints[][],
                         int jump)
    {
        int i, j, k;

        // Process the default values
        ArrayList ddefaultss = ifProbFunc.getDefaults();
        if (ddefaultss.size() > 0)
        {
            double ddefaults[] = (double[]) (ddefaultss.get(0));
            for (i = 0; i < values.length; i++)
            {
                for (j = 0; j < jump; j++)
                {
                    k = i * jump + j;
                    if (values[k] == ConvertInterchangeFormat.INVALID_VALUE)
                    {
                        values[k] = ddefaults[i];
                    }
                }
            }
        }
    }

    /**
     * Insert entries specified in the IFProbabilityFunction object.
     *
     * @param bayesNet      the underlying Bayes net
     * @param ifProbFunc           interchange format probability function
     * @param variables     array of probability variables
     * @param values        an array of double values
     * @param extremePoints matrix of extreme points
     * @param jump
     */
    void processEntries(BayesNet bayesNet,
                        IFProbabilityFunction ifProbFunc,
                        ProbabilityVariable variables[],
                        double values[],
                        double extremePoints[][],
                        int jump)
    {
        int i, j, k, pos, step;
        int entryValueIndexes[];
        double eentryEntries[];
        String eentryValues[];
        ProbabilityVariable probVar;
        IFProbabilityEntry entry;

        // Process the entries
        ArrayList eentries = ifProbFunc.getEntries();
        if ((eentries != null) && (eentries.size() > 0))
        {
            for (Object e : eentries)
            {
                entry =
                (IFProbabilityEntry) (e);
                eentryValues = entry.getValues();
                eentryEntries = entry.getEntries();
                entryValueIndexes = new int[eentryValues.length];
                for (i = 0; i < entryValueIndexes.length; i++)
                {
                    probVar = variables[i + 1];
                    entryValueIndexes[i] =
                    probVar.indexOfValue(eentryValues[i]);
                }
                pos = 0;
                step = 1;
                for (k = (entryValueIndexes.length); k > 0; k--)
                {
                    pos += entryValueIndexes[k - 1] * step;
                    step *=
                    variables[k].numberValues();
                }
                probVar = variables[0];
                for (i = 0; i < probVar.numberValues(); i++)
                {
                    k = i * jump + pos;
                    values[k] = eentryEntries[i];
                }
            }
        }
    }

    /**
     * Perform final calculations in the values.
     *
     * @param values        an array of double values
     * @param extremePoints matrix of extreme points
     */
    void finishValues(double values[], double extremePoints[][])
    {
        int i, j;

        // First case: more than one distribution specifies a credal set
        if (extremePoints != null)
        {
            // Fill with zeros where needed for all distributions
            for (j = 0; j < extremePoints.length; j++)
            {
                for (i = 0; i < extremePoints[j].length; i++)
                {
                    if (extremePoints[j][i] ==
                        ConvertInterchangeFormat.INVALID_VALUE)
                    {
                        extremePoints[j][i] = 0.0;
                    }
                }
            }
        }
        else
        { // Second case: single distribution; just fill zeros where needed
            for (i = 0; i < values.length; i++)
            {
                if (values[i] == ConvertInterchangeFormat.INVALID_VALUE)
                {
                    values[i] = 0.0;
                }
            }
        }
    }
}
