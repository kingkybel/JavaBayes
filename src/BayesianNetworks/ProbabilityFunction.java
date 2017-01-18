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

    private static final Class CLAZZ = ProbabilityFunction.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     *
     */
    protected ArrayList<String> properties;

    /**
     *
     */
    protected BayesNet bayesNet;

    /**
     * Default constructor for a ProbabilityFunction.
     */
    public ProbabilityFunction()
    {
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param bayesNet       the underlying Bayesian network
     * @param properties     list of properties
     * @param numberOfVars   number of variables
     * @param numberOfValues number of values
     */
    public ProbabilityFunction(BayesNet bayesNet,
                               int numberOfVars,
                               int numberOfValues,
                               ArrayList<String> properties)
    {
        super(numberOfVars, numberOfValues);
        this.properties = properties;
        this.bayesNet = bayesNet;
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param bayesNet   the underlying Bayesian network
     * @param properties list of properties
     * @param variables
     * @param values
     */
    public ProbabilityFunction(BayesNet bayesNet,
                               DiscreteVariable variables[],
                               double values[],
                               ArrayList<String> properties)
    {
        super(variables, values);
        this.properties = properties;
        this.bayesNet = bayesNet;
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param discrFunc
     * @param values
     */
    public ProbabilityFunction(DiscreteFunction discrFunc, double[] values)
    {
        super(discrFunc.variables, values);
        if (discrFunc instanceof ProbabilityFunction)
        {
            bayesNet = ((ProbabilityFunction) discrFunc).bayesNet;
            properties = ((ProbabilityFunction) discrFunc).properties;
        }
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param discrFunc
     * @param bayesNet  the underlying Bayesian network
     */
    public ProbabilityFunction(DiscreteFunction discrFunc, BayesNet bayesNet)
    {
        super(discrFunc.variables, discrFunc.values);
        this.bayesNet = bayesNet;
        if (discrFunc instanceof ProbabilityFunction)
        {
            properties = ((ProbabilityFunction) discrFunc).properties;
        }
    }

    /**
     * Method that processes the properties.
     */
    void processProperties()
    {
    }

    /**
     * Set a single value of the probability function.
     *
     * @param variableValuePairs
     * @param value
     */
    public void setValue(String variableValuePairs[][], double value)
    {
        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bayesNet.numberVariables()];

        // Fill the array of markers.
        for (String[] variableValuePair : variableValuePairs)
        {
            int index = bayesNet.indexOfVariable(variableValuePair[0]);
            ProbabilityVariable probVar = bayesNet.getVariable(index);
            valueIndexes[index] = probVar.indexOfValue(variableValuePair[1]);
        }

        // Get the position of the value in the array of values
        int valuePos =
            getPositionFromIndexes(bayesNet.getProbabilityVariables(),
                                   valueIndexes);
        // Set the value.
        values[valuePos] = value;
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
        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bayesNet.numberVariables()];

        // Fill the array of markers.
        for (String[] variableValuePair : variableValuePairs)
        {
            int index = bayesNet.indexOfVariable(variableValuePair[0]);
            ProbabilityVariable probVar = bayesNet.getVariable(index);
            valueIndexes[index] = probVar.indexOfValue(variableValuePair[1]);
        }

        // Now evaluate
        return evaluate(valueIndexes);
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
        return super.evaluate(bayesNet.getProbabilityVariables(), valueIndexes);
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
        return super.getPositionFromIndexes(bayesNet.getProbabilityVariables(),
                                            variableIndexes);
    }

    /**
     * Obtain the expected value of a DiscreteFunction. The current
     * implementation is very limited; it assumes that both the
     * ProbabilityFunction object and the DiscreteFunctions object have a single
     * variable, and the variable must be the same for both functions.
     *
     * @param discrFunc
     * @return
     */
    public double expectedValue(DiscreteFunction discrFunc)
    {
        double ev = 0.0;
        for (int i = 0; i < discrFunc.numberValues(); i++)
        {
            ev += values[i] * discrFunc.values[i];
        }
        return ev;
    }

    /**
     * Obtain posterior expected value of a DiscreteFunction This assumes that
     * the probability values are unnormalized, equal to p(x, e) where e is the
     * evidence. The current implementation is very limited; it assumes that
     * both the ProbabilityFunction object and the DiscreteFunctions object has
     * a single variable, and the variable must be the same for both functions.
     *
     * @param discrFunc
     * @return
     */
    public double posteriorExpectedValue(DiscreteFunction discrFunc)
    {
        double ev = 0.0;
        double p = 0.0;
        for (int i = 0; i < discrFunc.numberValues(); i++)
        {
            p += values[i];
            ev += values[i] * discrFunc.values[i];
        }
        return ev / p;
    }

    /**
     * Calculate the variance of a DiscreteFunction. The current implementation
     * is very limited; it assumes that both the ProbabilityFunction object and
     * the DiscreteFunctions object has a single variable, and the variable must
     * be the same for both functions.
     *
     * @param discrFunc
     * @return
     */
    public double variance(DiscreteFunction discrFunc)
    {
        double aux, ev = 0.0, evv = 0.0;
        for (int i = 0; i < discrFunc.numberValues(); i++)
        {
            aux = values[i] * discrFunc.values[i];
            ev += aux;
            evv = discrFunc.values[i] * aux;
        }
        return evv - ev * ev;
    }

    /**
     * Save the contents of a ProbabilityFunction object into a PrintStream in
     * the XMLBIF v0.3 format.
     *
     * @param out output print stream
     */
    public void saveXml_0_3(PrintStream out)
    {
        int i, j, sizeOfFirst = 0, sizeOfOthers = 1;

        out.println("<DEFINITION>");

        if (variables != null)
        {
            out.println("\t<FOR>" + variables[0].name + "</FOR>");
            for (j = 1; j < numberVariables(); j++)
            {
                out.println("\t<GIVEN>" + variables[j].name + "</GIVEN>");
            }

            out.print("\t<TABLE>");

            if (numberVariables() > 1)
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
                for (j = 0; j < numberValues(); j++)
                {
                    out.print(values[j] + " ");
                }
            }

            out.println("</TABLE>");
        }

        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</DEFINITION>\n");
    }

    /**
     * Save the contents of a ProbabilityFunction object into a PrintStream.
     *
     * @param out output print stream
     */
    public void saveXml(PrintStream out)
    {
        int j;

        out.println("<PROBABILITY>");

        if (variables != null)
        {
            out.println("\t<FOR>" + variables[0].name + "</FOR>");
            for (j = 1; j < numberVariables(); j++)
            {
                out.println("\t<GIVEN>" + variables[j].name + "</GIVEN>");
            }

            out.print("\t<TABLE>");
            for (j = 0; j < numberValues(); j++)
            {
                out.print(values[j] + " ");
            }
            out.println("</TABLE>");
        }

        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</PROBABILITY>\n");
    }

    /**
     * Print method.
     *
     * @param out output print stream
     */
    @Override
    public void print(PrintStream out)
    {
        int j;

        if (variables != null)
        {
            out.print("probability ( ");
            for (j = 0; j < numberVariables(); j++)
            {
                out.print(" \"" + variables[j].name + "\" ");
            }
            out.print(") {");
            out.println(" //" + numberVariables() +
                        " variable(s) and " + numberValues() + " values");

            out.println("\ttable ");
            if (numberVariables() == 1)
            {
                for (j = 0; j < numberValues(); j++)
                {
                    out.print("\t\t" + values[j]);
                    if (j == (numberValues() - 1))
                    {
                        out.print("; ");
                    }
                    out.print("\t// p(" + variables[0].values[j] +
                              " | evidence )");
                    if (j != (numberValues() - 1))
                    {
                        out.println();
                    }
                }
            }
            else
            {
                out.print("\t\t");
                for (j = 0; j < numberValues(); j++)
                {
                    out.print(" " + values[j]);
                }
            }
            out.print(";");
        }
        out.println();
        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\tproperty \"" + property + "\" ;");
            }
        }
        out.println("}");
    }

    /**
     * Get the properties of the current ProbabilityFunction.
     *
     * @return
     */
    public ArrayList<String> getProperties()
    {
        return properties;
    }

    /**
     * Set the properties.
     *
     * @param properties list of properties
     */
    public void setProperties(ArrayList<String> properties)
    {
        this.properties = properties;
    }

    /**
     * Get an Iterator with the properties of the current ProbabilityFunction.
     *
     * @return
     */
    public ArrayList<String> getEnumeratedProperties()
    {
        return properties;
    }

    /**
     * Add a property to the current ProbabilityFunction.
     *
     * @param property
     */
    public void addProperty(String property)
    {
        if (properties == null)
        {
            properties = new ArrayList<>();
        }
        properties.add(property);
    }

    /**
     * Remove a property in the current ProbabilityFunction.
     *
     * @param property
     */
    public void removeProperty(String property)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(property);
    }

    /**
     * Remove a property in a given position in the current ProbabilityFunction.
     *
     * @param index Position of the property.
     */
    public void removeProperty(int index)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(index);
    }
}
