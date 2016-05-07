/**
 * ProbabilityFunction.java
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
package BayesianNetworks;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author
 * kybelksd****************************************************************
 */
public class ProbabilityFunction extends DiscreteFunction
{
    private static final String CLASS_NAME = ProbabilityFunction.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     *
     */
    protected ArrayList properties;

    /**
     *
     */
    protected BayesNet bn;

    /**
     * Default constructor for a ProbabilityFunction.
     */
    public ProbabilityFunction()
    {
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param b_n
     * @param prop
     * @param n_vb
     * @param n_vl
     */
    public ProbabilityFunction(BayesNet b_n, int n_vb, int n_vl, ArrayList prop)
    {
        super(n_vb, n_vl);
        properties = prop;
        bn = b_n;
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param b_n
     * @param prop
     * @param pvs
     * @param v
     */
    public ProbabilityFunction(BayesNet b_n, DiscreteVariable pvs[],
                               double v[], ArrayList prop)
    {
        super(pvs, v);
        properties = prop;
        bn = b_n;
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param df
     * @param new_values
     */
    public ProbabilityFunction(DiscreteFunction df, double[] new_values)
    {
        super(df.variables, new_values);
        if (df instanceof ProbabilityFunction)
        {
            bn = ((ProbabilityFunction) df).bn;
            properties = ((ProbabilityFunction) df).properties;
        }
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param df
     * @param b_n
     */
    public ProbabilityFunction(DiscreteFunction df, BayesNet b_n)
    {
        super(df.variables, df.values);
        bn = b_n;
        if (df instanceof ProbabilityFunction)
        {
            properties = ((ProbabilityFunction) df).properties;
        }
    }

    /* ************************************************************* */
    /* Method that processes the properties                          */
    /* ************************************************************* */
    void process_properties()
    {
    }

    /**
     * Set a single value of the probability function.
     *
     * @param variable_value_pairs
     * @param val
     */
    public void set_value(String variable_value_pairs[][], double val)
    {
        int index;
        ProbabilityVariable pv;

        // Initialize with zeros an array of markers.
        int value_indexes[] = new int[bn.probability_variables.length];

        // Fill the array of markers.
        for (int i = 0; i < variable_value_pairs.length; i++)
        {
            index = bn.index_of_variable(variable_value_pairs[i][0]);
            pv = bn.probability_variables[index];
            value_indexes[index] = pv.index_of_value(variable_value_pairs[i][1]);
        }

        // Get the position of the value in the array of values
        int pos = get_position_from_indexes(bn.probability_variables,
                                            value_indexes);
        // Set the value.
        values[pos] = val;
    }

    /**
     * Evaluate a function given a list of pairs (Variable Value) which
     * specifies a value of the function.
     *
     * @param variable_value_pairs
     * @return
     */
    public double evaluate(String variable_value_pairs[][])
    {
        int index;
        ProbabilityVariable pv;

        // Initialize with zeros an array of markers.
        int value_indexes[] = new int[bn.probability_variables.length];

        // Fill the array of markers.
        for (int i = 0; i < variable_value_pairs.length; i++)
        {
            index = bn.index_of_variable(variable_value_pairs[i][0]);
            pv = bn.probability_variables[index];
            value_indexes[index] = pv.index_of_value(variable_value_pairs[i][1]);
        }

        // Now evaluate
        return (evaluate(value_indexes));
    }

    /**
     * Evaluate a function given a (possibly partial) instantiation of variables
     * through the markers. The markers indicate which variables are present in
     * the function to be evaluated.
     *
     * @param value_indexes
     * @return
     */
    public double evaluate(int value_indexes[])
    {
        return (super.evaluate(bn.probability_variables, value_indexes));
    }

    /**
     * Get position in a function from a (possibly partial) instantiation of
     * variables through the indexes.
     *
     * @param variable_indexes
     * @return
     */
    public int get_position_from_indexes(int variable_indexes[])
    {
        return (super.get_position_from_indexes(bn.probability_variables,
                                                variable_indexes));
    }

    /**
     * Obtain expected value of a DiscreteFunction The current implementation is
     * very limited; it assumes that both the ProbabilityFunction object and the
     * DiscreteFunctions object has a single variable, and the variable must be
     * the same for both functions.
     *
     * @param df
     * @return
     */
    public double expected_value(DiscreteFunction df)
    {
        double ev = 0.0;
        for (int i = 0; i < df.values.length; i++)
        {
            ev += values[i] * df.values[i];
        }
        return (ev);
    }

    /**
     * Obtain posterior expected value of a DiscreteFunction This assumes that
     * the probability values are unnormalized, equal to p(x, e) where e is the
     * evidence. The current implementation is very limited; it assumes that
     * both the ProbabilityFunction object and the DiscreteFunctions object has
     * a single variable, and the variable must be the same for both functions.
     *
     * @param df
     * @return
     */
    public double posterior_expected_value(DiscreteFunction df)
    {
        double ev = 0.0;
        double p = 0.0;
        for (int i = 0; i < df.values.length; i++)
        {
            p += values[i];
            ev += values[i] * df.values[i];
        }
        return (ev / p);
    }

    /**
     * Calculate the variance of a DiscreteFunction. The current implementation
     * is very limited; it assumes that both the ProbabilityFunction object and
     * the DiscreteFunctions object has a single variable, and the variable must
     * be the same for both functions.
     *
     * @param df
     * @return
     */
    public double variance(DiscreteFunction df)
    {
        double aux, ev = 0.0, evv = 0.0;
        for (int i = 0; i < df.values.length; i++)
        {
            aux = values[i] * df.values[i];
            ev += aux;
            evv = df.values[i] * aux;
        }
        return (evv - ev * ev);
    }

    /**
     * Save the contents of a ProbabilityFunction object into a PrintStream in
     * the XMLBIF v0.3 format.
     *
     * @param out
     */
    public void save_xml_0_3(PrintStream out)
    {
        int i, j, size_of_first = 0, size_of_others = 1;
        String property;

        out.println("<DEFINITION>");

        if (variables != null)
        {
            out.println("\t<FOR>" + variables[0].name + "</FOR>");
            for (j = 1; j < variables.length; j++)
            {
                out.println("\t<GIVEN>" + variables[j].name + "</GIVEN>");
            }

            out.print("\t<TABLE>");

            if (variables.length > 1)
            { // Necessary to invert variables.
                size_of_first = variables[0].number_values();
                for (j = 1; j < variables.length; j++)
                {
                    size_of_others *= variables[j].number_values();
                }
                for (i = 0; i < size_of_others; i++)
                {
                    for (j = 0; j < size_of_first;
                         j++)
                    {
                        out.print(values[j * size_of_others + i] + " ");
                    }
                }
            }
            else
            { // Not necessary to invert variables.
                for (j = 0; j < values.length; j++)
                {
                    out.print(values[j] + " ");
                }
            }

            out.println("</TABLE>");
        }

        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</DEFINITION>\n");
    }

    /**
     * Save the contents of a ProbabilityFunction object into a PrintStream.
     *
     * @param out
     */
    public void save_xml(PrintStream out)
    {
        int j;
        String property;

        out.println("<PROBABILITY>");

        if (variables != null)
        {
            out.println("\t<FOR>" + variables[0].name + "</FOR>");
            for (j = 1; j < variables.length; j++)
            {
                out.println("\t<GIVEN>" + variables[j].name + "</GIVEN>");
            }

            out.print("\t<TABLE>");
            for (j = 0; j < values.length; j++)
            {
                out.print(values[j] + " ");
            }
            out.println("</TABLE>");
        }

        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</PROBABILITY>\n");
    }

    /**
     * Print method.
     *
     * @param out
     */
    @Override
    public void print(PrintStream out)
    {
        int j;
        String property;

        if (variables != null)
        {
            out.print("probability ( ");
            for (j = 0; j < variables.length; j++)
            {
                out.print(" \"" + variables[j].name + "\" ");
            }
            out.print(") {");
            out.println(" //" + variables.length +
                        " variable(s) and " + values.length + " values");

            out.println("\ttable ");
            if (variables.length == 1)
            {
                for (j = 0; j < values.length; j++)
                {
                    out.print("\t\t" + values[j]);
                    if (j == (values.length - 1))
                    {
                        out.print("; ");
                    }
                    out.print("\t// p(" + variables[0].values[j] +
                              " | evidence )");
                    if (j != (values.length - 1))
                    {
                        out.println();
                    }
                }
            }
            else
            {
                out.print("\t\t");
                for (j = 0; j < values.length; j++)
                {
                    out.print(" " + values[j]);
                }
            }
            out.print(";");
        }
        out.println();
        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\tproperty \"" + property + "\" ;");
            }
        }
        out.println("}");
    }

    /* *************************************************************** */
    /* Methods that allow basic manipulation of non-public variables   */
    /* *************************************************************** */
    /**
     * Get the properties of the current ProbabilityFunction.
     *
     * @return
     */
    public ArrayList get_properties()
    {
        return (properties);
    }

    /**
     * Set the properties.
     *
     * @param prop
     */
    public void set_properties(ArrayList prop)
    {
        properties = prop;
    }

    /**
     * Get an Iterator with the properties of the current
     * ProbabilityFunction.
     *
     * @return
     */
    public ArrayList get_enumerated_properties()
    {
        return (properties);
    }

    /**
     * Add a property to the current ProbabilityFunction.
     *
     * @param prop
     */
    public void add_property(String prop)
    {
        if (properties == null)
        {
            properties = new ArrayList();
        }
        properties.add(prop);
    }

    /**
     * Remove a property in the current ProbabilityFunction.
     *
     * @param prop
     */
    public void remove_property(String prop)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(prop);
    }

    /**
     * Remove a property in a given position in the current ProbabilityFunction.
     *
     * @param i Position of the property.
     */
    public void remove_property(int i)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(i);
    }
}
