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
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Arbitrary Discrete Function.
 *
 * @author Fabio G. Cozman
 */
public class DiscreteFunction
{

    private static final Class CLAZZ = DiscreteFunction.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private DiscreteVariable variables[];
    private double values[];

    /**
     * Default constructor for a DiscreteFunction.
     */
    public DiscreteFunction()
    {
    }

    /**
     * Simple constructor for DiscreteFunction.
     *
     * @param numberOfVars   number of variables in the function
     * @param numberOfValues number of values in the function
     */
    public DiscreteFunction(int numberOfVars, int numberOfValues)
    {
        variables = new DiscreteVariable[numberOfVars];
        values = new double[numberOfValues];
    }

    /**
     * Simple constructor for DiscreteFunction.
     *
     * @param variables  an array of ProbabilityVariable objects
     * @param funcValues the values of the function as array of doubles
     */
    public DiscreteFunction(DiscreteVariable variables[], double funcValues[])
    {
        this.variables = variables;
        this.values = funcValues;
    }

    /**
     * Simple constructor for DiscreteFunction.
     *
     * @param variables  list of ProbabilityVariable objects.
     * @param funcValues the values of the function as array of doubles
     */
    public DiscreteFunction(ArrayList<DiscreteVariable> variables,
                            double funcValues[])
    {
        this.variables = new DiscreteVariable[variables.size()];
        for (int i = 0; i < variables.size(); i++)
        {
            this.variables[i] = variables.get(i);
        }
        this.values = funcValues;
    }

    /**
     * Check whether a variable identified by index is a parameter of the
     * function.
     *
     * @param varIndex index of a variable to test
     * @return true if the variable with the index is a parameter of the
     *         function, false otherwise
     */
    public boolean isParameter(int varIndex)
    {
        for (DiscreteVariable variable : variables)
        {
            if (varIndex == variable.index)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Method that determines whether a DiscreteFunction has exactly the same
     * DiscreteVariable's as with the current DiscreteFunction.
     *
     * @param discrFunc discrete function to be compared with the current
     *                  discrete function.
     * @return true if so, false otherwise
     */
    boolean sameVariables(DiscreteFunction discrFunc)
    {
        if (numberVariables() != discrFunc.numberVariables())
        {
            return false;
        }
        for (int i = 0; i < numberVariables(); i++)
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
     * @param variables    the array of DiscreteVariable objects that is used to
     *                     compute the position indicated by the markers
     * @param valueIndexes the markers
     * @return the evaluated double value
     */
    public double evaluate(DiscreteVariable variables[], int valueIndexes[])
    {
        int valuePos = findPositionOfProbabilityValue(variables, valueIndexes);
        return values[valuePos];
    }

    /**
     * Get position in a function from a (possibly partial) instantiation of
     * variables through the indexes.
     *
     * @param probVar         a probability variable
     * @param variableIndexes index array of the variables
     * @return the index of the probability value in the function
     */
    public int findPositionOfProbabilityValue(DiscreteVariable probVar[],
                                              int variableIndexes[])
    {
        int k, pos = 0, jump = 1;
        for (int i = (numberVariables() - 1); i >= 0; i--)
        {
            k = variables[i].index;
            pos += variableIndexes[k] * jump;
            jump *= probVar[k].numberValues();
        }
        return pos;
    }

    /**
     * Sum out some variables in the function.
     *
     * @param variableArray an array containing some of the variables
     * @param markers       A boolean vector indicating which variables are to
     *                      be summed out. If markers[i] is true, then the i'th
     *                      variable is to be summed out; if markers[i] is
     *                      false, the i'th variable is not to be summed out.
     * @return the resulting summed out discrete function
     */
    public DiscreteFunction sumOut(DiscreteVariable variableArray[],
                                   boolean markers[])
    {
        int i, j, k, current;

        // Initialize the indexes and the maximum length for all ProbabilityVariable
        // objects. This is used to circle through all the values in the newDf.
        int indexes[] = new int[variableArray.length];
        int valueLengths[] = new int[variableArray.length];
        for (i = 0; i < variableArray.length; i++)
        {
            indexes[i] = 0;
            valueLengths[i] = variableArray[i].numberValues();
        }

        // Collect some information used to construct the newDf.
        int numberOfVariablesToSumOut = 0;
        int numberOfVariablesToStay = 0;
        int numberOfValuesNewDf = 1;
        int numberOfValuesToSumOut = 1;
        for (i = 0; i < numberVariables(); i++)
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
        int indexForVariablesToSumOut[] = new int[numberOfVariablesToSumOut];

        // Build the newDf and the indexes of variables to sum out.
        DiscreteFunction newDf = new DiscreteFunction(numberOfVariablesToStay,
                                                      numberOfValuesNewDf);

        for (i = 0, j = 0, k = 0; i < numberVariables(); i++)
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
            double valueSum = 0.0;
            // Reset the indexes for the values to sum out.
            for (j = 0; j < indexForVariablesToSumOut.length; j++)
            {
                indexes[indexForVariablesToSumOut[j]] = 0;
            }
            // Do the summation for a value.
            for (j = 0; j < numberOfValuesToSumOut; j++)
            { // Go through all values to be summed out.

                // Do the summation for each value of the newDf.
                valueSum += evaluate(variableArray, indexes);

                // Increment the last index to be summed out.
                indexes[indexForVariablesToSumOut[lastIndexForVariablesToSumOut]]++;
                for (k = lastIndexForVariablesToSumOut; k > 0; k--)
                { // Now do the updating of all indexes.
                    current = indexForVariablesToSumOut[k];
                    if (indexes[current] >= valueLengths[current])
                    { // If overflow in an index,
                        indexes[current] = 0;
                        // then update the next index.
                        indexes[indexForVariablesToSumOut[k - 1]]++;
                    }
                    else
                    {
                        break;
                    }
                }
            }
            // Insert the summation for the value of newDf into newDf.
            newDf.setValue(i, valueSum);

            // Update the indexes.
            indexes[newDf.getIndex(lastNewDf)]++; // Increment the last index.
            for (j = lastNewDf; j > 0; j--)
            { // Now do the updating of all indexes.
                current = newDf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                { // If overflow in an index,
                    indexes[current] = 0;
                    // then update the next index.
                    indexes[newDf.getIndex(j - 1)]++;
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
     * @param variables     array of discrete variables
     * @param multDiscrFunc a discrete function
     * @return the resulting discrete function
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
        for (j = 0; j < numberVariables(); j++)
        {
            k = getIndex(j);
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
                    // then update the next index.
                    indexes[newDf.getIndex(j - 1)]++;
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
     * Normalise a function (in-place).
     */
    public void normalize()
    {
        int i;
        double total = 0.0;

        for (i = 0; i < numberValues(); i++)
        {
            total += values[i];
        }
        if (total > 0.0)
        {
            for (i = 0; i < numberValues(); i++)
            {
                values[i] /= total;
            }
        }
    }

    /**
     * Normalise a function (in-place) assuming that it is a conditional
     * distribution for the first variable.
     */
    public void normalizeFirst()
    {
        int i, j;
        int jump = 1;
        double n;

        for (i = 1; i < numberVariables(); i++)
        {
            jump *= variables[i].numberValues();
        }

        for (i = 0; i < jump; i++)
        {
            n = 0.0;
            for (j = 0; j < variables[0].numberValues(); j++)
            {
                n += values[i + j * jump];
            }
            if (n > 0.0)
            {
                for (j = 0; j < variables[0].numberValues(); j++)
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
        if (variables != null)
        {
            out.print("discrete function ( ");
            for (DiscreteVariable variable : variables)
            {
                out.print(" \"" + variable.name + "\" ");
            }
            out.print(") {");
            out.println(" //" + variables.length +
                        " variable(s) and " + numberValues() + " values");
            out.print("\ttable ");
            for (int valInd = 0; valInd < numberValues(); valInd++)
            {
                out.print(values[valInd] + " ");
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
     * @return number of variables
     */
    public final int numberVariables()
    {
        return variables.length;
    }

    /**
     * Return the number of values in the current DiscreteFunction.
     *
     * @return number of values
     */
    public final int numberValues()
    {
        return values.length;
    }

    /**
     * Get the variables in the current DiscreteFunction.
     *
     * @return array of all variables
     */
    public final DiscreteVariable[] getVariables()
    {
        return variables;
    }

    /**
     * Get the variables in the current DiscreteFunction.
     *
     * @param variables array of all variables
     */
    public final void setVariables(DiscreteVariable[] variables)
    {
        this.variables = variables;
    }

    /**
     * Get a variable in the current DiscreteFunction.
     *
     * @param index Position of the variable to be returned in the array of
     *              DiscreteVariable objects.
     * @return the variable at index
     */
    public final DiscreteVariable getVariable(int index)
    {
        return variables[index];
    }

    /**
     * Get an array with all the indexes of the DiscreteVariable objects in the
     * current DiscreteFunction.
     *
     * @return the indices as integer array
     */
    public final int[] getIndexes()
    {
        int ind[] = new int[numberVariables()];
        for (int i = 0; i < ind.length; i++)
        {
            ind[i] = variables[i].index;
        }
        return ind;
    }

    /**
     * Get the (member-)index of a variable given the index of that variable in
     * the collection of variables.
     *
     * @param varIndex index of the desired DiscreteVariable.
     * @return the member index
     */
    public final int getIndex(int varIndex)
    {
//        LOGGER.log(Level.INFO,
//                   "in DiscreteFunction.getIndex(int varIndex={0}): variables[{1}].index={2}",
//                   new Object[]
//                   {
//                       varIndex,
//                       varIndex,
//                       variables[varIndex].index
//                   });
        return variables[varIndex].index;
    }

    /**
     * Get all values of the current DiscreteFunction.
     *
     * @return the probability values as double array
     */
    public final double[] getValues()
    {
        return values;
    }

    /**
     * Get a value of the current DiscreteFunction given the position of the
     * value in the array of values.
     *
     * @param valIndex value index
     * @return the value at the index
     */
    public final double getValue(int valIndex)
    {
        return values[valIndex];
    }

    /**
     * Set a value in the current DiscreteFunction given its position in the
     * array of values.
     *
     * @param index position of the value.
     * @param value new value.
     */
    public final void setValue(int index, double value)
    {
        values[index] = value;
    }

    /**
     * Set the values in the DiscreteFunction.
     *
     * @param funcValues the values of the function as array of doubles
     */
    public final void setValues(double funcValues[])
    {
        this.values = funcValues;
    }

    /**
     * Set a DiscreteVariable in the current DiscreteFunction given its position
     * in the array of values.
     *
     * @param index    The position of the value.
     * @param discrVar the new discrete variable
     */
    public final void setVariable(int index, DiscreteVariable discrVar)
    {
        variables[index] = discrVar;
    }
}
