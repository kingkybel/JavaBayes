/*
 * ProbabilityVariable.java
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
public class ProbabilityVariable extends DiscreteVariable
{

    private static final Class CLAZZ = ProbabilityVariable.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);


    static final String observedPropertyName = "observed";
    static final String explanationPropertyName = "explanation";

    private Type type = Type.CHANCE;
    private int observedIndex = BayesNet.INVALID_INDEX;
    private int explanationIndex = BayesNet.INVALID_INDEX;
    private ArrayList<String> properties;
    private BayesNet bayesNet;

    /**
     * Default constructor for a ProbabilityVariable.
     */
    public ProbabilityVariable()
    {
        super();
    }

    /**
     * Constructor for ProbabilityVariable.
     *
     * @param bayesNet   the underlying Bayesian network
     * @param name       name of the new variable
     * @param properties list of properties
     */
    public ProbabilityVariable(BayesNet bayesNet,
                               String name,
                               ArrayList<String> properties)
    {
        super(name);
        this.properties = properties;
        this.bayesNet = bayesNet;
    }

    /**
     * Constructor for ProbabilityVariable.
     *
     * @param bayesNet   the underlying Bayesian network
     * @param name       name of the new variable
     * @param index      given index for the variable
     * @param values     values the variable can assume as string array
     * @param properties list of properties
     */
    public ProbabilityVariable(BayesNet bayesNet,
                               String name,
                               int index,
                               String values[],
                               ArrayList<String> properties)
    {
        super(name, index, values);
        this.properties = properties;
        this.bayesNet = bayesNet;
    }

    /**
     * Copy constructor for ProbabilityVariable.
     *
     * @param probVar a probability variable
     */
    public ProbabilityVariable(ProbabilityVariable probVar)
    {
        super(probVar);

        observedIndex = probVar.observedIndex;
        explanationIndex = probVar.explanationIndex;
        type = probVar.type;

        properties = probVar.properties;
        bayesNet = probVar.bayesNet;
    }

    /**
     * Constructor for ProbabilityVariable.
     *
     * @param bayesNet the underlying Bayesian network
     * @param probVar  a probability variable
     */
    public ProbabilityVariable(BayesNet bayesNet, ProbabilityVariable probVar)
    {
        super(probVar);

        observedIndex = probVar.observedIndex;
        explanationIndex = probVar.explanationIndex;
        type = probVar.type;

        properties = probVar.properties;
        this.bayesNet = bayesNet;
    }

    /**
     * Process the variables properties. Determine:
     * <ol>
     * <li> whether a variable is observed </li>
     * <li> whether a variable is a explanation variable </li>
     * </ol>
     */
    void processProperties()
    {
        String property, propertyValue, keyword;
        ArrayList<String> propertiesToRemove = new ArrayList<>();

        // Get all properties one by one
        for (String pp : properties)
        {
            property = pp.trim();
            // If the property is an "observed" property
            keyword = observedPropertyName;
            if ((property.startsWith(keyword)) || (property.equals(keyword)))
            {
                propertiesToRemove.add(pp);
                propertyValue = property.substring(keyword.length()).trim();
                observedIndex = indexOfValue(propertyValue);
                continue;
            }
            // If the property is a "explanation" property
            keyword = explanationPropertyName;
            if ((property.startsWith(keyword)) || (property.equals(keyword)))
            {
                propertiesToRemove.add(pp);
                propertyValue = property.substring(keyword.length()).trim();
                explanationIndex = indexOfValue(propertyValue);
                if (explanationIndex == BayesNet.INVALID_INDEX)
                {
                    explanationIndex = 0;
                }
            }
        }

        for (String prop2Remove : propertiesToRemove)
        {
            properties.remove(prop2Remove);
        }
    }

    /**
     * Save the contents of a ProbabilityVariable object into a PrintStream
     * using the XMLBIF v0.3 format.
     *
     * @param out output print stream
     */
    public void saveXml_0_3(PrintStream out)
    {

        if (this == null)
        {
            return;
        }
        out.println("<VARIABLE TYPE=\"nature\">");
        if (name != null)
        {
            out.println("\t<NAME>" + name + "</NAME>");
        }

        if (values != null)
        {
            for (String value : values)
            {
                out.println("\t<OUTCOME>" + value + "</OUTCOME>");
            }
        }

        if (isExplanation())
        {
            out.println("\t<PROPERTY>" + explanationPropertyName + " " +
                        values[explanationIndex] + "</PROPERTY>");
        }

        if (isObserved())
        {
            out.println("\t<PROPERTY>" + observedPropertyName + " " +
                        values[observedIndex] + "</PROPERTY>");
        }

        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</VARIABLE>\n");
    }

    /**
     * Save the contents of a ProbabilityVariable object into a PrintStream.
     *
     * @param out output print stream
     */
    public void saveXml(PrintStream out)
    {
        if (this == null)
        {
            return;
        }
        out.println("<VARIABLE>");
        if (name != null)
        {
            out.println("\t<NAME>" + name + "</NAME>");
        }

        if (values != null)
        {
            out.println("\t<TYPE>discrete</TYPE>");
            for (String value : values)
            {
                out.println("\t<VALUE>" + value + "</VALUE>");
            }
        }

        if (isExplanation())
        {
            out.println("\t<PROPERTY>" + explanationPropertyName + " " +
                        values[explanationIndex] + "</PROPERTY>");
        }

        if (isObserved())
        {
            out.println("\t<PROPERTY>" + observedPropertyName + " " +
                        values[observedIndex] + "</PROPERTY>");
        }

        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</VARIABLE>\n");
    }

    @Override
    public void print(PrintStream out)
    {
        if (this == null)
        {
            return;
        }
        out.print("variable ");
        if (name != null)
        {
            out.print(" \"" + name + "\" ");
        }
        out.print("{");
        if (values != null)
        {
            out.println(" //" + numberValues() + " values");
            out.print("\ttype discrete[" + numberValues() + "] { ");
            for (String value : values)
            {
                out.print(" \"" + value + "\" ");
            }
            out.println("};");
        }

        if (isExplanation())
        {
            out.println("\tproperty \"" + explanationPropertyName + " " +
                        values[explanationIndex] + "\" ;");
        }

        if (isObserved())
        {
            out.println("\tproperty \"" + observedPropertyName + " " +
                        values[observedIndex] + "\" ;");
        }

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
     * Get the type of the ProbabilityVariable.
     *
     * @return CHANCE or TRANSPARENT
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Indicate whether the current ProbabilityVariable is an explanatory
     * variable or not.
     *
     * @return true if so, false otherwise
     */
    public boolean isExplanation()
    {
        return explanationIndex != BayesNet.INVALID_INDEX;
    }

    /**
     * Indicate whether the current ProbabilityVariable has been observed or
     * not.
     *
     * @return true if so, false otherwise
     */
    public boolean isObserved()
    {
        return observedIndex != BayesNet.INVALID_INDEX;
    }

    /**
     * Set a value of the current ProbabilityVariable as observed.
     *
     * @param value Observed value.
     */
    public void setObservedValue(String value)
    {
        int foundIndex = indexOfValue(value);
        if (foundIndex == BayesNet.INVALID_INDEX)
        {
            return;
        }
        observedIndex = foundIndex;
    }

    /**
     * Set the variable as explanatory with a given value.
     *
     * @param valIndex Index of the value that is assigned to the variable.
     */
    public void setExplanationValue(int valIndex)
    {
        explanationIndex = valIndex;
    }

    /**
     * Add a property to the current ProbabilityVariable.
     *
     * @param property property given as string
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
     * Remove a property from the current ProbabilityVariable.
     *
     * @param property property given as string
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
     * Remove a property from the current ProbabilityVariable given the position
     * of the property.
     *
     * @param propIndex index of the property
     */
    public void removeProperty(int propIndex)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(propIndex);
    }

    /**
     * Get the index of the observed value.
     *
     * @return the index of the observed value
     */
    public int getObservedIndex()
    {
        return observedIndex;
    }

    /**
     * Get the index of the assigned value in the variable.
     *
     * @return the index of the explanation value
     */
    public int getExplanationIndex()
    {
        return explanationIndex;
    }

    /**
     * Get the properties.
     *
     * @return the properties as list of strings
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
     * Get an list with the properties.
     *
     * @return properties as list of strings
     */
    public ArrayList<String> getEnumeratedProperties()
    {
        return properties;
    }

    /**
     * Set the index of the variable.
     *
     * @param index
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * Set the index of the current ProbabilityVariable as invalid (variable is
     * not observed).
     */
    public void setInvalidIndex()
    {
        index = BayesNet.INVALID_INDEX;
    }

    /**
     * Set the ProbabilityVariable as not observed..
     */
    public void setInvalidObservedIndex()
    {
        observedIndex = BayesNet.INVALID_INDEX;
    }

    /**
     * Set the type of the current ProbabilityVariable.
     *
     * @param type
     */
    public void setType(Type type)
    {
        this.type = type;
    }

    /**
     * Set a new Bayes net.
     *
     * @param bayesNet the new Bayes net
     */
    public void setBayesNet(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    /**
     * Enumeration to distinguish types of probability variables.
     */
    public enum Type
    {

        /**
         * Chance variable.
         */
        CHANCE, /**
         * Transparent variable.
         */
        TRANSPARENT
    }
}
