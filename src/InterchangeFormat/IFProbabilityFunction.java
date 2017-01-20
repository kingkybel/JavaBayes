/*
 * IFProbabilityFunction.java
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
package InterchangeFormat;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Auxiliar class that stores the temporary information about a
 * ProbabilityFunction, before it is possible to actually create a
 * ProbabilityFunction object (because * not all variables may be read at this
 * point). The object stores the variable names in the function, the vector of
 * properties, and information that will be used to create the conditional
 * distribution tables (the default * entry, the vector of entries, the table of
 * values; not all of them may be filled for a particular probability)
 */
public class IFProbabilityFunction
{

    private static final Class CLAZZ = IFProbabilityFunction.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    String sVariables[];
    int conditionalIndex;
    ArrayList<String> properties;
    ArrayList<double[]> defaults;
    ArrayList<double[]> tables;
    ArrayList<IFProbabilityEntry> entries;

    /**
     * Set the variables string array.
     *
     * @param sVariables the variables as string array
     */
    public void setVariables(String sVariables[])
    {
        this.sVariables = sVariables;
    }

    /**
     * Set the properties list.
     *
     * @param properties list of properties
     */
    public void setProperties(ArrayList<String> properties)
    {
        this.properties = properties;
    }

    /**
     * Set the defaults.
     *
     * @param defaults list of double arrays
     */
    public void setDefaults(ArrayList<double[]> defaults)
    {
        this.defaults = defaults;
    }

    /**
     * Set the tables.
     *
     * @param tables list of double arrays
     */
    public void setTables(ArrayList<double[]> tables)
    {
        this.tables = tables;
    }

    /**
     * Set the entries.
     *
     * @param entries list of probability entries in interface format
     */
    public void setEntries(ArrayList<IFProbabilityEntry> entries)
    {
        this.entries = entries;
    }

    /**
     * Set the conditional index.
     *
     * @param conditionalIndex the new conditional index.
     */
    public void setConditionalIndex(int conditionalIndex)
    {
        this.conditionalIndex = conditionalIndex;
    }

    /**
     * Retrieve the variables.
     *
     * @return the variables as string array
     */
    public String[] getVariables()
    {
        return sVariables;
    }

    /**
     * Retrieve the properties.
     *
     * @return the properties as list of strings
     */
    public ArrayList<String> getProperties()
    {
        return properties;
    }

    /**
     * Retrieve the defaults.
     *
     * @return the defaults as list of strings
     */
    public ArrayList<double[]> getDefaults()
    {
        return defaults;
    }

    /**
     * Retrieve the tables.
     *
     * @return the tables as list of double arrays
     */
    public ArrayList<double[]> getTables()
    {
        return tables;
    }

    /**
     * Retrieve the entries.
     *
     * @return the entries las ist of probability entries in interface format
     */
    public ArrayList<IFProbabilityEntry> getEntries()
    {
        return entries;
    }

    /**
     * Retrieve the conditional index.
     *
     * @return the conditional index
     */
    public int getConditionalIndex()
    {
        return conditionalIndex;
    }

    /**
     * Method that inverts the tables in the ProbabilityFunction object;
     * necessary for formats that put the GIVEN variables as the lowest running
     * indexes in the tables. * At this point it assumes that there is only one
     * FOR variable in the ProbabilityFunction object.
     *
     * @param ifBayesNet Bayes net in interchange format
     */
    public void invertTables(IFBayesNet ifBayesNet)
    {

        if (sVariables.length > 1)
        { // No need to do anything if only one variable.
            // Go through all the tables.
            ArrayList<double[]> newTables = new ArrayList<>();
            String runningName;
            double newTable[];
            int i, j;
            int sizeOfFirst = 0, sizeOfOthers = 1;

            for (double[] t : tables)
            {
                sizeOfFirst = 0;
                sizeOfOthers = 1;
                // Now get the first variable.
                for (IFProbabilityVariable probVar : ifBayesNet.probVars)
                {
                    runningName = probVar.getName();
                    if (runningName.equals(sVariables[0]))
                    { // Found the first variable.
                        sizeOfFirst = probVar.getValues().length; // Obtain its size.
                        break; // Get out of loop through variables.
                    }
                }
                // Get the size of all other variables;
                for (j = 1; j < sVariables.length; j++)
                {
                    for (IFProbabilityVariable probVar : ifBayesNet.probVars)
                    {
                        runningName = probVar.getName();
                        if (runningName.equals(sVariables[j]))
                        { // Found the variable.
                            sizeOfOthers *= probVar.getValues().length;
                            break; // Get out of loop through variables.
                        }
                    }
                }
                // Build a new table.
                newTable = new double[t.length];
                for (i = 0; i < sizeOfFirst; i++)
                {
                    for (j = 0; j < sizeOfOthers;
                         j++)
                    {
                        newTable[i * sizeOfOthers + j] =
                        t[j * sizeOfFirst + i];
                    }
                }
                // Insert the new table in the ArrayList newTables.
                newTables.add(newTable);
            }
            // Now attach the new ArrayList.
            tables = newTables;
        }
    }
}
