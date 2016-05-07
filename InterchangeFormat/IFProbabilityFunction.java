/**
 * IFProbabilityFunction.java
 *
 * @author Fabio G. Cozman Copyright 1996 - 1999, Fabio G. Cozman, Carnergie
 * Mellon University, Universidade de Sao Paulo fgcozman@usp.br,
 * http://www.cs.cmu.edu/~fgcozman/home.html
 *
 * The JavaBayes distribution is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (either version 2 of the License or, at your
 * option, any later version), provided that this notice and the name of the
 * author appear in all copies. Upon request to the author, some of the packages
 * in the JavaBayes distribution can be licensed under the GNU Lesser General
 * Public License as published by the Free Software Foundation (either version 2
 * of the License, or (at your option) any later version). If you're using the
 * software, please notify fgcozman@usp.br so that you can receive updates and
 * patches. JavaBayes is distributed "as is", in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with the JavaBayes distribution. If not, write
 * to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 */
package InterchangeFormat;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * **********************************************************
 * Auxiliar class that stores the temporary information * about a
 * ProbabilityFunction, before it is possible to * actually create a
 * ProbabilityFunction object (because * not all variables may be read at this
 * point). The object * stores the variable names in the function, the vector *
 * of properties, and information that will be used to * create the conditional
 * distribution tables (the default * entry, the vector of entries, the table of
 * values; not * all of them may be filled for a particular probability) *
 * **********************************************************
 */
public class IFProbabilityFunction
{
    private static final Logger LOG =
    Logger.getLogger(IFProbabilityFunction.class.
            getName());

    String s_variables[];
    int conditional_index;
    ArrayList properties;
    ArrayList defaults;
    ArrayList tables;
    ArrayList entries;

    /**
     *
     * @param vs
     */
    public void set_variables(String vs[])
    {
        s_variables = vs;
    }

    /**
     *
     * @param p
     */
    public void set_properties(ArrayList p)
    {
        properties = p;
    }

    /**
     *
     * @param d
     */
    public void set_defaults(ArrayList d)
    {
        defaults = d;
    }

    /**
     *
     * @param t
     */
    public void set_tables(ArrayList t)
    {
        tables = t;
    }

    /**
     *
     * @param e
     */
    public void set_entries(ArrayList e)
    {
        entries = e;
    }

    /**
     *
     * @param c
     */
    public void set_conditional_index(int c)
    {
        conditional_index = c;
    }

    /**
     *
     * @return
     */
    public String[] get_variables()
    {
        return (s_variables);
    }

    /**
     *
     * @return
     */
    public ArrayList get_properties()
    {
        return (properties);
    } // ArrayList of String

    /**
     *
     * @return
     */
    public ArrayList get_defaults()
    {
        return (defaults);
    } // ArrayList of double[]

    /**
     *
     * @return
     */
    public ArrayList get_tables()
    {
        return (tables);
    } // ArrayList of double[]

    /**
     *
     * @return
     */
    public ArrayList get_entries()
    {
        return (entries);
    } // ArrayList of IFProbabilityFunctionEntry

    /**
     *
     * @return
     */
    public int get_conditional_index()
    {
        return (conditional_index);
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
    public void invert_tables(IFBayesNet ifbn)
    {
        IFProbabilityVariable pv;
        ArrayList new_tables;
        String running_name;
        double t[], new_table[];
        int i, j;
        int size_of_first = 0, size_of_others = 1;

        if (s_variables.length > 1)
        { // No need to do anything if only one variable.
            // Go through all the tables.
            new_tables = new ArrayList(); // Initialize a ArrayList for the new tables.
            for (Object e : tables)
            {
                size_of_first = 0;
                size_of_others = 1;
                t = (double[]) (e); // Get the table.
                // Now get the first variable.
                for (Object ee : ifbn.pvs)
                {
                    pv = (IFProbabilityVariable) (ee);
                    running_name = pv.get_name();
                    if (running_name.equals(s_variables[0]))
                    { // Found the first variable.
                        size_of_first = pv.get_values().length; // Obtain its size.
                        break; // Get out of loop through variables.
                    }
                }
                // Get the size of all other variables;
                for (j = 1; j < s_variables.length; j++)
                {
                    for (Object ee : ifbn.pvs)
                    {
                        pv = (IFProbabilityVariable) (ee);
                        running_name = pv.get_name();
                        if (running_name.equals(s_variables[j]))
                        { // Found the variable.
                            size_of_others *= pv.get_values().length;
                            break; // Get out of loop through variables.
                        }
                    }
                }
                // Build a new table.
                new_table = new double[t.length];
                for (i = 0; i < size_of_first; i++)
                {
                    for (j = 0; j < size_of_others;
                         j++)
                    {
                        new_table[i * size_of_others + j] =
                        t[j * size_of_first + i];
                    }
                }
                // Insert the new table in the ArrayList new_tables.
                new_tables.add(new_table);
            }
            // Now attach the new ArrayList.
            tables = new_tables;
        }
    }
}
