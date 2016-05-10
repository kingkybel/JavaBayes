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

    private static final String CLASS_NAME =
                                IFProbabilityFunction.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    String sVariables[];
    int conditionalIndex;
    ArrayList properties;
    ArrayList defaults;
    ArrayList tables;
    ArrayList entries;

    /**
     *
     * @param vs
     */
    public void setVariables(String vs[])
    {
        sVariables = vs;
    }

    /**
     *
     * @param p
     */
    public void setProperties(ArrayList p)
    {
        properties = p;
    }

    /**
     *
     * @param d
     */
    public void setDefaults(ArrayList d)
    {
        defaults = d;
    }

    /**
     *
     * @param t
     */
    public void setTables(ArrayList t)
    {
        tables = t;
    }

    /**
     *
     * @param e
     */
    public void setEntries(ArrayList e)
    {
        entries = e;
    }

    /**
     *
     * @param c
     */
    public void setConditionalIndex(int c)
    {
        conditionalIndex = c;
    }

    /**
     *
     * @return
     */
    public String[] getVariables()
    {
        return (sVariables);
    }

    /**
     *
     * @return
     */
    public ArrayList getProperties()
    {
        return (properties);
    } // ArrayList of String

    /**
     *
     * @return
     */
    public ArrayList getDefaults()
    {
        return (defaults);
    } // ArrayList of double[]

    /**
     *
     * @return
     */
    public ArrayList getTables()
    {
        return (tables);
    } // ArrayList of double[]

    /**
     *
     * @return
     */
    public ArrayList getEntries()
    {
        return (entries);
    } // ArrayList of IFProbabilityFunctionEntry

    /**
     *
     * @return
     */
    public int getConditionalIndex()
    {
        return (conditionalIndex);
    }

    /**
     * ***********************************************************
     * Method that inverts the tables in the ProbabilityFunction * object;
     * necessary for formats that put the GIVEN * variables as the lowest
     * running indexes in the tables. * At this point it assumes that there is
     * only one FOR * variable in the ProbabilityFunction object.
     *
     *
     * @param ifbn ***********************************************************
     */
    public void invertTables(IFBayesNet ifbn)
    {
        IFProbabilityVariable pv;
        ArrayList newTables;
        String runningName;
        double t[], newTable[];
        int i, j;
        int sizeOfFirst = 0, sizeOfOthers = 1;

        if (sVariables.length > 1)
        { // No need to do anything if only one variable.
            // Go through all the tables.
            newTables = new ArrayList(); // Initialize a ArrayList for the new tables.
            for (Object e : tables)
            {
                sizeOfFirst = 0;
                sizeOfOthers = 1;
                t = (double[]) (e); // Get the table.
                // Now get the first variable.
                for (Object ee : ifbn.pvs)
                {
                    pv = (IFProbabilityVariable) (ee);
                    runningName = pv.getName();
                    if (runningName.equals(sVariables[0]))
                    { // Found the first variable.
                        sizeOfFirst = pv.getValues().length; // Obtain its size.
                        break; // Get out of loop through variables.
                    }
                }
                // Get the size of all other variables;
                for (j = 1; j < sVariables.length; j++)
                {
                    for (Object ee : ifbn.pvs)
                    {
                        pv = (IFProbabilityVariable) (ee);
                        runningName = pv.getName();
                        if (runningName.equals(sVariables[j]))
                        { // Found the variable.
                            sizeOfOthers *= pv.getValues().length;
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
