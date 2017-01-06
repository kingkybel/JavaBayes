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
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Class that converts the contents of an InterchangeFormat object to the
 * BayesNet object in the BayesianNetworks package.
 */
public class ConvertInterchangeFormat
{

    private static final Class CLAZZ = ConvertInterchangeFormat.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static final double INVALID_VALUE = -1.0;
    InterchangeFormat interchangeFmt;

    /**
     * Default constructor that loads an InterchangeFormat.
     *
     * @param interchangeFmt
     */
    public ConvertInterchangeFormat(InterchangeFormat interchangeFmt)
    {
        this.interchangeFmt = new InterchangeFormat(interchangeFmt);
    }

    /**
     * Get the name of the IFBayesNet in the InterchangeFormat.
     *
     * @return
     */
    public String getName()
    {
        IFBayesNet ifbn = interchangeFmt.getBayesNetFromInterchangeFmt();
        if (ifbn == null)
        {
            return null;
        }
        return ifbn.getName();
    }

    /**
     * Get the properties of the IFBayesNet in the InterchangeFormat.
     *
     * @return
     */
    public ArrayList<String> getProperties()
    {
        IFBayesNet ifbn = interchangeFmt.getBayesNetFromInterchangeFmt();
        if (ifbn == null)
        {
            return null;
        }
        return ifbn.getProperties();
    }

    /**
     * Generate an array of ProbabilityVariable objects from the data structures
     * in the IFBayesNet.
     *
     * @param bayesNet the underlying Bayesian network The BayesNet that will
     *                 receive the ProbabilityVariable objects.
     * @return
     */
    public ProbabilityVariable[] getProbabilityVariables(BayesNet bayesNet)
    {
        int i;
        IFBayesNet ifbn = interchangeFmt.getBayesNetFromInterchangeFmt();
        if (ifbn == null)
        {
            return null;
        }
        ArrayList<IFProbabilityVariable> pvs = ifbn.getProbabilityVariables();

        ProbabilityVariable probabilityVariables[] =
                              new ProbabilityVariable[pvs.size()];

        i = 0;
        for (IFProbabilityVariable ipv : pvs)
        {
            probabilityVariables[i] =
            new ProbabilityVariable(bayesNet,
                                    ipv.getName(),
                                    i,
                                    ipv.getValues(),
                                    ipv.getProperties());
            i++;
        }

        return probabilityVariables;
    }

    /**
     * Generate an array of ProbabilityFunction objects from the data structures
     * in the IFBayesNet.
     *
     * @param bayesNet the underlying Bayesian network The BayesNet that will
     *                 receive the ProbabilityVariable objects.
     * @return the generated array of ProbabilityFunctions
     */
    public ProbabilityFunction[] getProbabilityFunctions(BayesNet bayesNet)
    {
        int i;
        IFBayesNet ifbn = interchangeFmt.getBayesNetFromInterchangeFmt();
        if (ifbn == null)
        {
            return null;
        }
        ArrayList<IFProbabilityFunction> ifProbFuncs = ifbn.
                                         getProbabilityFunctions();

        ProbabilityFunction probabilityFunctions[] =
                              new ProbabilityFunction[ifProbFuncs.size()];

        i = 0;
        for (IFProbabilityFunction ifProbFunc : ifProbFuncs)
        {
            probabilityFunctions[i] = getProbabilityFunction(bayesNet,
                                                             ifProbFunc);
            i++;
        }

        return probabilityFunctions;
    }

    private ArrayList<Integer> incrementValueIndex(
            ArrayList<Integer> currentValueIndex,
            ArrayList<String[]> valueList)
    {
        int index = currentValueIndex.size() - 1;
        boolean hasOverflow = true; // first one treated as having overflow
        while (index > -1 && hasOverflow)
        {
            int module = valueList.get(index).length;
            currentValueIndex.set(index,
                                  (currentValueIndex.get(index) + 1) % module);
            hasOverflow = currentValueIndex.get(index) == 0;
            index--;

        }
        return currentValueIndex;
    }

    public TreeMap<String, ArrayList<ArrayList<Object>>> getFunctionsAsTables(
            BayesNet bayesNet)
    {
        TreeMap<String, ArrayList<ArrayList<Object>>> reval = new TreeMap<>();
        IFBayesNet ifbn = interchangeFmt.getBayesNetFromInterchangeFmt();
        if (ifbn == null)
        {
            return null;
        }

        // now all combinations for all functions need to be added
        ArrayList<IFProbabilityFunction> ifProbFuncs =
                                         ifbn.getProbabilityFunctions();

        // this is the header done
        for (IFProbabilityFunction ifProbFunc : ifProbFuncs)
        {
            ArrayList<ArrayList<Object>> funcTable = new ArrayList<>();
            ProbabilityFunction pf =
                                getProbabilityFunction(bayesNet, ifProbFunc);
            ArrayList<String[]> valueList = new ArrayList<>();
            ArrayList<Object> record = new ArrayList<>();
            ArrayList<Integer> currentValueIndex = new ArrayList<>();

            int numRows = 1;
            for (DiscreteVariable var : pf.variables)
            {
                String varName = var.getName();
                record.add(varName);
                valueList.add(var.getValues());
                currentValueIndex.add(0);
                numRows *= var.getValues().length;
            }
            record.add("#PROBABILITY#");
            funcTable.add(record);

            // now make the records
            ArrayList<ArrayList<Object>> valuesTable = new ArrayList<>();
            int row;
            for (row = 0; row < numRows; row++)
            {
                valuesTable.add(new ArrayList<>());
                Object o[] = new Object[valueList.size() + 1];
                valuesTable.get(row).addAll(Arrays.asList(o));
            }

            double probs[] = pf.getValues();
            for (row = 0; row < numRows; row++)
            {
                for (int col = 0; col < valueList.size(); col++)
                {
                    Object value =
                           valueList.get(col)[currentValueIndex.get(col)];
                    valuesTable.get(row).set(col, value);
                }
                valuesTable.get(row).set(valuesTable.get(row).size() - 1,
                                         probs[row]);
                currentValueIndex = incrementValueIndex(currentValueIndex,
                                                        valueList);
            }
            funcTable.addAll(valuesTable);
//
//            for (int y = 0; y < funcTable.size(); y++)
//            {
//                for (int x = 0; x < funcTable.get(0).size(); x++)
//                {
//                    System.out.print(funcTable.get(y).get(x) + " , ");
//                }
//                System.out.println("");
//            }
            reval.put(ifProbFunc.getVariables()[0], funcTable);
        }

        return reval;
    }

    /**
     * Method that does all the work involved in creating a ProbabilityFunction
     * object out of the definition found by the parser and the information
     * contained in the BayesNet object; the ProbabilityFunction object may in
     * fact be a Quasi-Bayesian model.
     *
     * @param bayesNet   the underlying Bayesian network
     * @param ifProbFunc interchange format probability function
     *
     * @return
     */
    protected ProbabilityFunction getProbabilityFunction(BayesNet bayesNet,
                                                         IFProbabilityFunction ifProbFunc)
    {
        int i, jump, numberOfValues;
        double values[];
        ProbabilityVariable probVar, variables[];

        // Check and insert the probability variable indexes
        variables = createVariables(bayesNet, ifProbFunc);

        // Calculate the jump, i.e., the number of numeric values
        // in the conditional distribution table for each value
        // of the first variable (this is used for default/entries)
        jump = 1;
        for (i = 1; i < variables.length; i++)
        {
            probVar = variables[i];
            jump *= probVar.values.length;
        }

        // Calculate the number of values in the distribution
        numberOfValues = jump * variables[0].values.length;

        // Allocate values and initialize
        values = new double[numberOfValues];
        for (i = 0; i < values.length; i++)
        {
            values[i] = ConvertInterchangeFormat.INVALID_VALUE;
        }

        // Process tables
        processTables(ifProbFunc, values);

        // Process defaults
        processDefaults(ifProbFunc, values, jump);

        // Process entries
        processEntries(bayesNet, ifProbFunc, variables, values, jump);

        // Finish calculating the values
        finishValues(values);

        // Return the ProbabilityFunction
        return new ProbabilityFunction(bayesNet,
                                       variables,
                                       values,
                                       ifProbFunc.getProperties());
    }

    /**
     * Create the variables in the ProbabilityFunction object from the variables
     * indicated in the IFProbabilityFunction.
     *
     * @param bayesNet   the underlying Bayesian network
     * @param ifProbFunc probability function in interchange format
     * @return
     */
    protected ProbabilityVariable[] createVariables(
            BayesNet bayesNet,
            IFProbabilityFunction ifProbFunc)
    {
        int index;
        String strVariables[] = ifProbFunc.getVariables();

        ProbabilityVariable variables[] =
                              new ProbabilityVariable[strVariables.length];
        for (int i = 0; i < strVariables.length; i++)
        {
            index = bayesNet.indexOfVariable(strVariables[i]);
            if (index != BayesNet.INVALID_INDEX)
            {
                variables[i] = bayesNet.getProbabilityVariable(index);
            }
        }
        return variables;
    }

    /**
     * Fill the values with the contents of the first table in the tables
     * contained in the ifProbFunc object.
     *
     * @param ifProbFunc probability function in interchange format
     * @param values
     */
    protected void processTables(IFProbabilityFunction ifProbFunc,
                                 double values[])
    {
        // Put the table values
        ArrayList<double[]> ttables = ifProbFunc.getTables();
        if (ttables.size() > 0)
        {
            double ttable[] = (double[]) (ttables.get(0));
            copyTableToValues(ttable, values);
        }
    }

    /**
     * Copy content from a table to another.
     *
     * @param table
     * @param probValues probability values
     */
    protected void copyTableToValues(double table[], double probValues[])
    {
        if (table == null)
        {
            throw new NullPointerException(
                    "Source array cnnot be null when copying arrays.");
        }
        if (probValues == null || table.length != probValues.length)
        {
            probValues = new double[table.length];
        }
        System.arraycopy(table, 0, probValues, 0, table.length);
    }

    /**
     * Insert default values from the contents of the first specification of
     * defaults in the IFProbabilityFunction.
     *
     * @param ifProbFunc
     * @param values
     * @param jump
     */
    void processDefaults(IFProbabilityFunction ifProbFunc,
                         double values[],
                         int jump)
    {
        int i, j, k;

        // Process the default values
        ArrayList<double[]> ddefaultss = ifProbFunc.getDefaults();
        if (ddefaultss.size() > 0)
        {
            double ddefaults[] = ddefaultss.get(0);
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
     * Insert entries specified in the IFProbabilityFunction.
     *
     * @param bayesNet
     * @param ifProbFunc
     * @param variables
     * @param values
     * @param jump
     */
    void processEntries(BayesNet bayesNet,
                        IFProbabilityFunction ifProbFunc,
                        ProbabilityVariable variables[],
                        double values[],
                        int jump)
    {
        int i, j, k, pos, step;
        int entryValueIndexes[];
        double eentryEntries[];
        String eentryValues[];
        ProbabilityVariable probVar;

        // Process the entries
        ArrayList<IFProbabilityEntry> eentries = ifProbFunc.getEntries();
        if ((eentries != null) && (eentries.size() > 0))
        {
            for (IFProbabilityEntry entry : eentries)
            {
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
                    variables[k].values.length;
                }
                probVar = variables[0];
                for (i = 0; i < probVar.values.length; i++)
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
     * @param values
     */
    void finishValues(double values[])
    {
        // Put zeroes in the values that were not defined
        for (int i = 0; i < values.length; i++)
        {
            if (values[i] == ConvertInterchangeFormat.INVALID_VALUE)
            {
                values[i] = 0.0;
            }
        }
    }

}
