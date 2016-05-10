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

    private static final String CLASS_NAME =
                                QBConvertInterchangeFormat.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     *
     * @param interFormat
     */
    public QBConvertInterchangeFormat(InterchangeFormat interFormat)
    {
        super(interFormat);
    }

    /**
     * Method that does all the work involved in creating a *
     * ProbabilityFunction object out of the definition * found by the parser
     * and the information contained in * the BayesNet object; the
     * ProbabilityFunction object may * in fact be a Quasi-Bayesian model.
     *
     * @return
     */
    @Override
    protected ProbabilityFunction getProbabilityFunction(BayesNet bn,
                                                         IFProbabilityFunction upf)
    {
        int i, jump, numberOfValues;
        double values[];
        double extremePoints[][] = null;
        ProbabilityVariable pv, variables[];

        // Check and insert the probability variable indexes
        variables = createVariables(bn, upf);

        // Calculate the jump, i.e., the number of values
        // in the conditional distribution table for each value
        // of the first variable (this is used for default/entries)
        jump = 1;
        for (i = 1; i < variables.length; i++)
        {
            pv = variables[i];
            jump *= pv.numberValues();
        }

        // Calculate the number of values in the distribution
        numberOfValues = jump * variables[0].numberValues();

        // Allocate values and initialize
        values = new double[numberOfValues];
        for (i = 0; i < values.length; i++)
        {
            values[i] = -1.0;
        }

        // Process tables
        extremePoints = processExtremeTables(upf, values);

        // Process defaults
        processDefaults(upf, values, extremePoints, jump);

        // Process entries
        processEntries(bn, upf, variables, values, extremePoints, jump);

        // Finish calculating the values
        finishValues(values, extremePoints);

        // Insert the data
        if (extremePoints == null)
        {
            return (new ProbabilityFunction(bn, variables, values, upf.
                                            getProperties()));
        }
        else
        {
            return (new VertexSet(bn, variables, extremePoints,
                                  upf.getProperties()));
        }
    }

    /**
     * Fill the values with the contents of the tables * in the upf object.
     */
    double[][] processExtremeTables(IFProbabilityFunction upf,
                                    double values[])
    {
        int i, j;
        double table[], extremePoints[][];

        // Put the table values
        ArrayList tables = upf.getTables();
        int n = tables.size();

        // If there are no available tables
        if (n == 0)
        {
            return (null);
        }

        // If there is a single table, no extremePoints are created
        // and it just acts as a standard Bayesian model
        if (n == 1)
        {
            table = (double[]) (tables.get(0));
            copyTableToValues(table, values);
            return (null);
        }

        // Else, if there are several extremePoints in the credal set
        extremePoints = new double[n][];
        for (i = 0; i < extremePoints.length; i++)
        {
            extremePoints[i] = new double[values.length];
            for (j = 0; j < extremePoints[i].length; j++)
            {
                extremePoints[i][j] = -1.0;
            }
        }
        i = 0;
        for (Object e : tables)
        {
            table = (double[]) (e);
            copyTableToValues(table, extremePoints[i]);
            i++;
        }
        return (extremePoints);
    }

    /**
     * Insert default values from the contents of the first specification of
     * defaults in the upf object.
     */
    void processDefaults(IFProbabilityFunction upf,
                         double values[], double extremePoints[][], int jump)
    {
        int i, j, k;

        // Process the default values
        ArrayList ddefaultss = upf.getDefaults();
        if (ddefaultss.size() > 0)
        {
            double ddefaults[] = (double[]) (ddefaultss.get(0));
            for (i = 0; i < values.length; i++)
            {
                for (j = 0; j < jump; j++)
                {
                    k = i * jump + j;
                    if (values[k] == -1.0)
                    {
                        values[k] = ddefaults[i];
                    }
                }
            }
        }
    }

    /**
     * Insert entries specified in the upf object.
     */
    void processEntries(BayesNet bn,
                        IFProbabilityFunction upf,
                        ProbabilityVariable variables[],
                        double values[], double extremePoints[][], int jump)
    {
        int i, j, k, pos, step;
        int entryValueIndexes[];
        double eentryEntries[];
        String eentryValues[];
        ProbabilityVariable pv;
        IFProbabilityEntry entry;

        // Process the entries
        ArrayList eentries = upf.getEntries();
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
                    pv = variables[i + 1];
                    entryValueIndexes[i] =
                    pv.indexOfValue(eentryValues[i]);
                }
                pos = 0;
                step = 1;
                for (k = (entryValueIndexes.length); k > 0; k--)
                {
                    pos += entryValueIndexes[k - 1] * step;
                    step *=
                    variables[k].numberValues();
                }
                pv = variables[0];
                for (i = 0; i < pv.numberValues(); i++)
                {
                    k = i * jump + pos;
                    values[k] = eentryEntries[i];
                }
            }
        }
    }

    /**
     * Perform final calculations in the values
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
                    if (extremePoints[j][i] == -1.0)
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
                if (values[i] == -1.0)
                {
                    values[i] = 0.0;
                }
            }
        }
    }
}
