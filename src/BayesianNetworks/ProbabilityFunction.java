/*
 * ProbabilityFunction.java
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
 * @author Fabio G. Cozman
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
     * @param bN
     * @param prop
     * @param nVb
     * @param nVl
     */
    public ProbabilityFunction(BayesNet bN, int nVb, int nVl, ArrayList prop)
    {
        super(nVb, nVl);
        properties = prop;
        bn = bN;
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param bN
     * @param prop
     * @param pvs
     * @param v
     */
    public ProbabilityFunction(BayesNet bN, DiscreteVariable pvs[],
                               double v[], ArrayList prop)
    {
        super(pvs, v);
        properties = prop;
        bn = bN;
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param df
     * @param newValues
     */
    public ProbabilityFunction(DiscreteFunction df, double[] newValues)
    {
        super(df.variables, newValues);
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
     * @param bN
     */
    public ProbabilityFunction(DiscreteFunction df, BayesNet bN)
    {
        super(df.variables, df.values);
        bn = bN;
        if (df instanceof ProbabilityFunction)
        {
            properties = ((ProbabilityFunction) df).properties;
        }
    }

    /* ************************************************************* */
    /* Method that processes the properties                          */
    /* ************************************************************* */
    void processProperties()
    {
    }

    /**
     * Set a single value of the probability function.
     *
     * @param variableValuePairs
     * @param val
     */
    public void setValue(String variableValuePairs[][], double val)
    {
        int index;
        ProbabilityVariable pv;

        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bn.probabilityVariables.length];

        // Fill the array of markers.
        for (int i = 0; i < variableValuePairs.length; i++)
        {
            index = bn.indexOfVariable(variableValuePairs[i][0]);
            pv = bn.probabilityVariables[index];
            valueIndexes[index] = pv.indexOfValue(variableValuePairs[i][1]);
        }

        // Get the position of the value in the array of values
        int pos = getPositionFromIndexes(bn.probabilityVariables,
                                         valueIndexes);
        // Set the value.
        values[pos] = val;
    }

    /**
     * Evaluate a function given a list of pairs (Variable Value) which
     * specifies a value of the function.
     *
     * @param variableValuePairs
     * @return
     */
    public double evaluate(String variableValuePairs[][])
    {
        int index;
        ProbabilityVariable pv;

        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bn.probabilityVariables.length];

        // Fill the array of markers.
        for (int i = 0; i < variableValuePairs.length; i++)
        {
            index = bn.indexOfVariable(variableValuePairs[i][0]);
            pv = bn.probabilityVariables[index];
            valueIndexes[index] = pv.indexOfValue(variableValuePairs[i][1]);
        }

        // Now evaluate
        return (evaluate(valueIndexes));
    }

    /**
     * Evaluate a function given a (possibly partial) instantiation of variables
     * through the markers. The markers indicate which variables are present in
     * the function to be evaluated.
     *
     * @param valueIndexes
     * @return
     */
    public double evaluate(int valueIndexes[])
    {
        return (super.evaluate(bn.probabilityVariables, valueIndexes));
    }

    /**
     * Get position in a function from a (possibly partial) instantiation of
     * variables through the indexes.
     *
     * @param variableIndexes
     * @return
     */
    public int getPositionFromIndexes(int variableIndexes[])
    {
        return (super.getPositionFromIndexes(bn.probabilityVariables,
                                             variableIndexes));
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
    public double expectedValue(DiscreteFunction df)
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
    public double posteriorExpectedValue(DiscreteFunction df)
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
    public void saveXml_0_3(PrintStream out)
    {
        int i, j, sizeOfFirst = 0, sizeOfOthers = 1;
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
                sizeOfFirst = variables[0].numberValues();
                for (j = 1; j < variables.length; j++)
                {
                    sizeOfOthers *= variables[j].numberValues();
                }
                for (i = 0; i < sizeOfOthers; i++)
                {
                    for (j = 0; j < sizeOfFirst;
                         j++)
                    {
                        out.print(values[j * sizeOfOthers + i] + " ");
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
    public void saveXml(PrintStream out)
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
    public ArrayList getProperties()
    {
        return (properties);
    }

    /**
     * Set the properties.
     *
     * @param prop
     */
    public void setProperties(ArrayList prop)
    {
        properties = prop;
    }

    /**
     * Get an Iterator with the properties of the current ProbabilityFunction.
     *
     * @return
     */
    public ArrayList getEnumeratedProperties()
    {
        return (properties);
    }

    /**
     * Add a property to the current ProbabilityFunction.
     *
     * @param prop
     */
    public void addProperty(String prop)
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
    public void removeProperty(String prop)
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
    public void removeProperty(int i)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(i);
    }
}
