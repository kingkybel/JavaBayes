/*
 * DiscreteFunction.java
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

import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class DiscreteFunction
{

    private static final Class CLAZZ = DiscreteFunction.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     *
     */
    protected DiscreteVariable variables[];

    /**
     *
     */
    protected double values[];

    /**
     * Default constructor for a DiscreteFunction.
     */
    public DiscreteFunction()
    {
    }

    /**
     * Simple constructor for DiscreteFunction.
     *
     * @param numberOfVars   Number of variables in the function.
     * @param numberOfValues Number of values in the function.
     */
    public DiscreteFunction(int numberOfVars, int numberOfValues)
    {
        variables = new DiscreteVariable[numberOfVars];
        values = new double[numberOfValues];
    }

    /**
     * Simple constructor for DiscreteFunction.
     *
     * @param variables An array of ProbabilityVariable objects.
     * @param values    An array of values for the function.
     */
    public DiscreteFunction(DiscreteVariable variables[], double values[])
    {
        this.variables = variables;
        this.values = values;
    }

    /**
     * Check whether an index is present in the function.
     *
     * @param index
     * @return
     */
    public boolean memberOf(int index)
    {
        for (DiscreteVariable variable : variables)
        {
            if (index == variable.index)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that determines whether a DiscreteFunction contain some
     * DiscreteVariable in common with the current DiscreteFunction.
     *
     * @param discrFunc DiscreteFunction to be compared with the current
     *                  DiscreteFunction.
     */
    boolean sameVariables(DiscreteFunction discrFunc)
    {
        if (variables.length != discrFunc.variables.length)
        {
            return false;
        }
        for (int i = 0; i < variables.length; i++)
        {
            if (variables[i] != discrFunc.variables[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluate a function given a (possibly partial) instantiation of variables
     * through the indexes. Indexes indicate which variables are present in the
     * function to be evaluated, assuming an array of DiscreteVariable objects
     * is present.
     *
     * @param variables    The array of DiscreteVariable objects that is used to
     *                     compute the position indicated by the markers.
     * @param valueIndexes The markers.
     * @return
     */
    public double evaluate(DiscreteVariable variables[], int valueIndexes[])
    {
        int position = getPositionFromIndexes(variables, valueIndexes);
        return values[position];
    }

    /**
     * Get position in a function from a (possibly partial) instantiation of
     * variables through the indexes.
     *
     * @param probVar
     * @param variableIndexes
     * @return
     */
    public int getPositionFromIndexes(DiscreteVariable probVar[],
                                      int variableIndexes[])
    {
        int k, pos = 0, jump = 1;
        for (int i = (variables.length - 1); i >= 0; i--)
        {
            k = variables[i].index;
            pos += variableIndexes[k] * jump;
            jump *= probVar[k].values.length;
        }
        return pos;
    }

    /**
     * Sum out some variables in the function.
     *
     * @param newVariables
     * @param markers      A boolean vector indicating which variables are to be
     *                     summed out. If markers[i] is true, then the ith
     *                     variable is to be summed out; if markers[i] is false,
     *                     the ith variable is not to be summed out.
     * @return
     */
    public DiscreteFunction sumOut(DiscreteVariable newVariables[],
                                   boolean markers[])
    {
        int i, j, k, current;
        double t, v;

        // Initialize the indexes and the maximum length for all ProbabilityVariable objects.
        // This is used to circle through all the values in the newDf.
        int indexes[] = new int[newVariables.length];
        int valueLengths[] = new int[newVariables.length];
        for (i = 0; i < newVariables.length; i++)
        {
            indexes[i] = 0;
            valueLengths[i] = newVariables[i].numberValues();
        }

        // Collect some information used to construct the newDf.
        int numberOfVariablesToSumOut = 0;
        int numberOfVariablesToStay = 0;
        int numberOfValuesNewDf = 1;
        int numberOfValuesToSumOut = 1;
        for (i = 0; i < variables.length; i++)
        {
            if (markers[variables[i].getIndex()] == true)
            {
                numberOfVariablesToSumOut++;
                numberOfValuesToSumOut *= variables[i].numberValues();
            }
            else
            {
                numberOfVariablesToStay++;
                numberOfValuesNewDf *= variables[i].numberValues();
            }
        }

        // If there is no variable that must stay, then return null.
        if (numberOfVariablesToStay == 0)
        {
            return null;
        }

        // If there is no variable to sum out, then return copy.
        if (numberOfVariablesToSumOut == 0)
        {
            return new DiscreteFunction(variables, values);
        }

        // Initialize a vector with the indexes of variables to sum out.
        int indexForVariablesToSumOut[] =
              new int[numberOfVariablesToSumOut];

        // Build the newDf and the indexes of variables to sum out.
        DiscreteFunction newDf = new DiscreteFunction(numberOfVariablesToStay,
                                                      numberOfValuesNewDf);

        for (i = 0, j = 0, k = 0; i < variables.length; i++)
        {
            if (markers[variables[i].getIndex()] == true)
            { // Fill in the index of variables to sum out.
                indexForVariablesToSumOut[k] = variables[i].getIndex();
                k++;
            }
            else
            { // Fill in the variables in the newDf.
                newDf.setVariable(j, variables[i]);
                j++;
            }
        }

        // Store the last valid indexes (efficiency purposes only).
        int lastNewDf = newDf.numberVariables() - 1;
        int lastIndexForVariablesToSumOut =
            indexForVariablesToSumOut.length - 1;

        // Now circle through all the values, doing the summation.
        for (i = 0; i < newDf.numberValues(); i++)
        { // Go through all values of the newDf.
            v = 0.0;
            // Reset the indexes for the values to sum out.
            for (j = 0; j < indexForVariablesToSumOut.length; j++)
            {
                indexes[indexForVariablesToSumOut[j]] = 0;
            }
            // Do the summation for a value.
            for (j = 0; j < numberOfValuesToSumOut; j++)
            { // Go through all values to be summed out.

                // Do the summation for each value of the newDf.
                v += evaluate(newVariables, indexes);

                // Increment the last index to be summed out.
                indexes[indexForVariablesToSumOut[lastIndexForVariablesToSumOut]]++;
                for (k = lastIndexForVariablesToSumOut; k > 0; k--)
                { // Now do the updating of all indexes.
                    current = indexForVariablesToSumOut[k];
                    if (indexes[current] >= valueLengths[current])
                    { // If overflow in an index,
                        indexes[current] = 0;
                        indexes[indexForVariablesToSumOut[k - 1]]++; // then update the next index.
                    }
                    else
                    {
                        break;
                    }
                }
            }
            // Insert the summation for the value of newDf into newDf.
            newDf.setValue(i, v);

            // Update the indexes.
            indexes[newDf.getIndex(lastNewDf)]++; // Increment the last index.
            for (j = lastNewDf; j > 0; j--)
            { // Now do the updating of all indexes.
                current = newDf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                { // If overflow in an index,
                    indexes[current] = 0;
                    indexes[newDf.getIndex(j - 1)]++; // then update the next index.
                }
                else
                {
                    break;
                }
            }
        }
        return newDf;
    }

    /**
     * Multiply two DiscreteFunction objects.
     *
     * @param variables
     * @param multDiscrFunc
     * @return
     */
    public DiscreteFunction multiply(DiscreteVariable variables[],
                                     DiscreteFunction multDiscrFunc)
    {
        int i, j, k, n, v, current, joinedIndexes[];
        double t;
        boolean variableMarkers[] = new boolean[variables.length];

        // This is used to circle through all the values in the newDf.
        int indexes[] = new int[variables.length];
        int valueLengths[] = new int[variables.length];
        for (i = 0; i < variables.length; i++)
        {
            indexes[i] = 0;
            valueLengths[i] = variables[i].numberValues();
            variableMarkers[i] = false;
        }

        // Join the indexes of this and mult.
        n = 0;
        for (j = 0; j < this.numberVariables(); j++)
        {
            k = this.getIndex(j);
            if (variableMarkers[k] == false)
            {
                variableMarkers[k] = true;
                n++;
            }
        }
        for (j = 0; j < multDiscrFunc.numberVariables(); j++)
        {
            k = multDiscrFunc.getIndex(j);
            if (variableMarkers[k] == false)
            {
                variableMarkers[k] = true;
                n++;
            }
        }
        // Calculate necessary quantities
        joinedIndexes = new int[n];
        j = 0;
        v = 1;
        for (i = 0; i < variableMarkers.length; i++)
        {
            if (variableMarkers[i] == true)
            {
                joinedIndexes[j] = i;
                j++;
                v *= variables[i].numberValues();
            }
        }

        // Create new function to be filled with joined variables
        DiscreteFunction newDf = new DiscreteFunction(n, v);
        for (i = 0; i < n; i++)
        {
            newDf.setVariable(i, variables[joinedIndexes[i]]);
        }

        // Store the last valid indexes (efficiency purposes only).
        int lastNewDf = newDf.numberVariables() - 1;

        // Now circle through all the values, doing the multiplication.
        for (i = 0; i < newDf.numberValues(); i++)
        { // Go through all values of the newDf.
            t = this.evaluate(variables, indexes) *
                multDiscrFunc.evaluate(variables, indexes);
            // Insert the summation for the value of newDf into newDf.
            newDf.setValue(i, t);

            // Update the indexes.
            indexes[newDf.getIndex(lastNewDf)]++; // Increment the last index.
            for (j = lastNewDf; j > 0; j--)
            { // Now do the updating of all indexes.
                current = newDf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                { // If overflow in an index,
                    indexes[current] = 0;
                    indexes[newDf.getIndex(j - 1)]++; // then update the next index.
                }
                else
                {
                    break;
                }
            }
        }

        return newDf;
    }

    /**
     * Normalize a function (in-place).
     */
    public void normalize()
    {
        int i;
        double total = 0.0;

        for (i = 0; i < values.length; i++)
        {
            total += values[i];
        }
        if (total > 0.0)
        {
            for (i = 0; i < values.length; i++)
            {
                values[i] /= total;
            }
        }
    }

    /**
     * Normalize a function (in-place) assuming that it is a conditional
     * distribution for the first variable.
     */
    public void normalizeFirst()
    {
        int i, j;
        int jump = 1;
        double n;

        for (i = 1; i < variables.length; i++)
        {
            jump *= variables[i].values.length;
        }

        for (i = 0; i < jump; i++)
        {
            n = 0.0;
            for (j = 0; j < variables[0].values.length; j++)
            {
                n += values[i + j * jump];
            }
            if (n > 0.0)
            {
                for (j = 0; j < variables[0].values.length; j++)
                {
                    values[i + j * jump] /= n;
                }
            }
        }
    }

    /**
     * Print method for DiscreteFunction.
     */
    public void print()
    {
        print(System.out);
    }

    /**
     * Print method for DiscreteFunction into a PrintStream.
     *
     * @param out output print stream
     */
    public void print(PrintStream out)
    {
        int j;

        if (variables != null)
        {
            out.print("discrete function ( ");
            for (j = 0; j < variables.length; j++)
            {
                out.print(" \"" + variables[j].name + "\" ");
            }
            out.print(") {");
            out.println(" //" + variables.length +
                        " variable(s) and " + values.length + " values");
            out.print("\ttable ");
            for (j = 0; j < values.length; j++)
            {
                out.print(values[j] + " ");
            }
            out.print(";");
        }
        out.println();
        out.println("}");
    }

    /**
     * Return the number of DiscreteVariable objects in the current
     * DiscreteFunction.
     *
     * @return
     */
    public int numberVariables()
    {
        return variables.length;
    }

    /**
     * Return the number of values in the current DiscreteFunction.
     *
     * @return
     */
    public int numberValues()
    {
        return values.length;
    }

    /**
     * Get the variables in the current DiscreteFunction.
     *
     * @return
     */
    public DiscreteVariable[] getVariables()
    {
        return variables;
    }

    /**
     * Get a variable in the current DiscreteFunction.
     *
     * @param index Position of the variable to be returned in the array of
     *              DiscreteVariable objects.
     * @return
     */
    public DiscreteVariable getVariable(int index)
    {
        return variables[index];
    }

    /**
     * Get an array with all the indexes of the DiscreteVariable objects in the
     * current DiscreteFunction.
     *
     * @return
     */
    public int[] getIndexes()
    {
        int ind[] = new int[variables.length];
        for (int i = 0; i < ind.length; i++)
        {
            ind[i] = variables[i].index;
        }
        return ind;
    }

    /**
     * Get a DiscreteVariable object with a particular index.
     *
     * @param ind Index of the desired DiscreteVariable.
     * @return
     */
    public int getIndex(int ind)
    {
        return variables[ind].index;
    }

    /**
     * Get all values of the current DiscreteFunction.
     *
     * @return
     */
    public double[] getValues()
    {
        return values;
    }

    /**
     * Get a value of the current DiscreteFunction given the position of the
     * value in the array of values.
     *
     * @param index
     * @return
     */
    public double getValue(int index)
    {
        return values[index];
    }

    /**
     * Set a value in the current DiscreteFunction given its position in the
     * array of values.
     *
     * @param index The position of the value.
     * @param v     The new value.
     */
    public void setValue(int index, double v)
    {
        values[index] = v;
    }

    /**
     * Set the values in the DiscreteFunction.
     *
     * @param vs
     */
    public void setValues(double vs[])
    {
        values = vs;
    }

    /**
     * Set a DiscreteVariable in the current DiscreteFunction given its position
     * in the array of values.
     *
     * @param index The position of the value.
     * @param dv
     */
    public void setVariable(int index, DiscreteVariable dv)
    {
        variables[index] = dv;
    }
}
