/*
 * ConvertInterchangeFormat.java
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
package BayesianNetworks;

import InterchangeFormat.IFBayesNet;
import InterchangeFormat.IFProbabilityEntry;
import InterchangeFormat.IFProbabilityFunction;
import InterchangeFormat.IFProbabilityVariable;
import InterchangeFormat.InterchangeFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

/* ********************************************************** *
 * Class that converts the contents of an InterchangeFormat   *
 * object to the BayesNet object in the BayesianNetworks      *
 * package                                                    *
 * ********************************************************** */
/**
 *
 * @author kybelksd
 */
public class ConvertInterchangeFormat
{
    private static final Logger LOG =
    Logger.getLogger(ConvertInterchangeFormat.class.
            getName());

    InterchangeFormat ifo;

    /**
     * Default constructor that loads an InterchangeFormat.
     *
     * @param interFormat
     */
    public ConvertInterchangeFormat(InterchangeFormat interFormat)
    {
        ifo = interFormat;
    }

    /**
     * Get the name of the IFBayesNet in the InterchangeFormat.
     *
     * @return
     */
    public String getName()
    {
        IFBayesNet ifbn = ifo.getIfbn();
        if (ifbn == null)
        {
            return (null);
        }
        return (ifbn.getName());
    }

    /**
     * Get the properties of the IFBayesNet in the InterchangeFormat.
     *
     * @return
     */
    public ArrayList getProperties()
    {
        IFBayesNet ifbn = ifo.getIfbn();
        if (ifbn == null)
        {
            return (null);
        }
        return (ifbn.getProperties());
    }

    /**
     * Generate an array of ProbabilityVariable objects from the data structures
     * in the IFBayesNet.
     *
     * @param bn The BayesNet that will receive the ProbabilityVariable objects.
     * @return
     */
    public ProbabilityVariable[] getProbabilityVariables(BayesNet bn)
    {
        int i;
        IFProbabilityVariable ipv;
        IFBayesNet ifbn = ifo.getIfbn();
        if (ifbn == null)
        {
            return (null);
        }
        ArrayList pvs = ifbn.getPvs();

        ProbabilityVariable probabilityVariables[] =
                              new ProbabilityVariable[pvs.size()];

        i = 0;
        for (Object e : pvs)
        {
            ipv = (IFProbabilityVariable) (e);
            probabilityVariables[i] =
            new ProbabilityVariable(bn, ipv.getName(), i,
                                    ipv.getValues(), ipv.getProperties());
            i++;
        }

        return (probabilityVariables);
    }

    /**
     * Generate an array of ProbabilityFunction objects from the data structures
     * in the IFBayesNet.
     *
     * @param bn The BayesNet that will receive the ProbabilityVariable objects.
     * @return
     */
    public ProbabilityFunction[] getProbabilityFunctions(BayesNet bn)
    {
        int i;
        IFProbabilityFunction upf;
        IFBayesNet ifbn = ifo.getIfbn();
        if (ifbn == null)
        {
            return (null);
        }
        ArrayList upfs = ifbn.getUpfs();

        ProbabilityFunction probabilityFunctions[] =
                              new ProbabilityFunction[upfs.size()];

        i = 0;
        for (Object e : upfs)
        {
            upf = (IFProbabilityFunction) (e);
            probabilityFunctions[i] = getProbabilityFunction(bn, upf);
            i++;
        }

        return (probabilityFunctions);
    }

    /*
     * Create a
     * ProbabilityFunction out of the definition
     * found by the parser and the information contained in
     * the BayesNet.
     */
    /**
     *
     * @param bn
     * @param upf
     * @return
     */
    protected ProbabilityFunction getProbabilityFunction(BayesNet bn,
                                                           IFProbabilityFunction upf)
    {
        int i, jump, numberOfValues;
        double values[];
        ProbabilityVariable pv, variables[];

        // Check and insert the probability variable indexes
        variables = createVariables(bn, upf);

        // Calculate the jump, i.e., the number of numeric values
        // in the conditional distribution table for each value
        // of the first variable (this is used for default/entries)
        jump = 1;
        for (i = 1; i < variables.length; i++)
        {
            pv = variables[i];
            jump *= pv.values.length;
        }

        // Calculate the number of values in the distribution
        numberOfValues = jump * variables[0].values.length;

        // Allocate values and initialize
        values = new double[numberOfValues];
        for (i = 0; i < values.length; i++)
        {
            values[i] = -1.0;
        }

        // Process tables
        processTables(upf, values);

        // Process defaults
        processDefaults(upf, values, jump);

        // Process entries
        processEntries(bn, upf, variables, values, jump);

        // Finish calculating the values
        finishValues(values);

        // Return the ProbabilityFunction
        return (new ProbabilityFunction(bn, variables,
                                        values, upf.getProperties()));
    }

    /*
     * Create the variables in the ProbabilityFunction object
     * from the variables indicated in the IFProbabilityFunction.
     */
    /**
     *
     * @param bn
     * @param upf
     * @return
     */
    protected ProbabilityVariable[] createVariables(BayesNet bn,
                                                     IFProbabilityFunction upf)
    {
        int index;
        String ssVariables[] = upf.getVariables();

        ProbabilityVariable variables[] =
                              new ProbabilityVariable[ssVariables.length];
        for (int i = 0; i < ssVariables.length; i++)
        {
            index = bn.indexOfVariable(ssVariables[i]);
            if (index != BayesNet.INVALID_INDEX)
            {
                variables[i] = bn.probabilityVariables[index];
            }
        }
        return (variables);
    }

    /*
     * Fill the values with the contents of the first table in
     * the tables contained in the upf object.
     */
    /**
     *
     * @param upf
     * @param values
     */
    protected void processTables(IFProbabilityFunction upf,
                                  double values[])
    {
        // Put the table values
        ArrayList ttables = upf.getTables();
        if (ttables.size() > 0)
        {
            double ttable[] = (double[]) (ttables.get(0));
            copyTableToValues(ttable, values);
        }
    }

    /*
     * Copy content from a table to another.
     */
    /**
     *
     * @param table
     * @param values
     */
    protected void copyTableToValues(double table[], double values[])
    {
        for (int i = 0; (i < table.length) && (i < values.length); i++)
        {
            values[i] = table[i];
        }
    }

    /*
     * Insert default values from the contents of the first
     * specification of defaults in the IFProbabilityFunction.
     */
    void processDefaults(IFProbabilityFunction upf,
                          double values[], int jump)
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

    /*
     * Insert entries specified in the IFProbabilityFunction.
     */
    void processEntries(BayesNet bn,
                         IFProbabilityFunction upf,
                         ProbabilityVariable variables[], double values[],
                         int jump)
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
                    variables[k].values.length;
                }
                pv = variables[0];
                for (i = 0; i < pv.values.length; i++)
                {
                    k = i * jump + pos;
                    values[k] = eentryEntries[i];
                }
            }
        }
    }

    /*
     * Perform final calculations in the values
     */
    void finishValues(double values[])
    {
        // Put zeroes in the values that were not defined
        for (int i = 0; i < values.length; i++)
        {
            if (values[i] == -1.0)
            {
                values[i] = 0.0;
            }
        }
    }

}
