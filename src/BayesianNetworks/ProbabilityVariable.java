/**
 * ProbabilityVariable.java
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
 *
 * @author kybelksd
 */
public class ProbabilityVariable extends DiscreteVariable
{

    /**
     *
     */
    public static final int CHANCE = 0;

    /**
     *
     */
    public static final int TRANSPARENT = 1;
    static final String observedPropertyName = "observed";
    static final String explanationPropertyName = "explanation";
    private static final Logger LOG =
    Logger.getLogger(ProbabilityVariable.class.getName());

    /**
     *
     */
    protected int type = ProbabilityVariable.CHANCE;

    /**
     *
     */
    protected int observedIndex = BayesNet.INVALID_INDEX;

    /**
     *
     */
    protected int explanationIndex = BayesNet.INVALID_INDEX;

    /**
     *
     */
    protected ArrayList properties;

    /**
     *
     */
    protected BayesNet bn;

    /**
     * Default constructor for a ProbabilityVariable.
     */
    public ProbabilityVariable()
    {
        super();
    }

    /**
     * Constructor for ProbabilityVariable.
     * @param bN
     * @param p
     * @param nVb
     */
    public ProbabilityVariable(BayesNet bN, String nVb, ArrayList p)
    {
        super(nVb);
        properties = p;
        bn = bN;
    }

    /**
     * Constructor for ProbabilityVariable.
     * @param bN
     * @param p
     * @param nVb
     * @param vl
     * @param vi
     */
    public ProbabilityVariable(BayesNet bN, String nVb, int vi,
                               String vl[], ArrayList p)
    {
        super(nVb, vi, vl);
        properties = p;
        bn = bN;
    }

    /**
     * Constructor for ProbabilityVariable.
     * @param pv
     */
    public ProbabilityVariable(ProbabilityVariable pv)
    {
        super(pv);

        observedIndex = pv.observedIndex;
        explanationIndex = pv.explanationIndex;
        type = pv.type;

        properties = pv.properties;
        bn = pv.bn;
    }

    /**
     * Constructor for ProbabilityVariable.
     * @param bN
     * @param pv
     */
    public ProbabilityVariable(BayesNet bN, ProbabilityVariable pv)
    {
        super(pv);

        observedIndex = pv.observedIndex;
        explanationIndex = pv.explanationIndex;
        type = pv.type;

        properties = pv.properties;
        bn = bN;
    }

    /**
     * Determine: 1) whether a variable is observed 2) whether a variable is a
     * explanation variable
     */
    void processProperties()
    {
        int indexOfObservedValue, indexOfExplanationValue;
        String pp, property, propertyValue, keyword;
        ArrayList propertiesToRemove = new ArrayList();

        // Get all properties one by one
        for (Object e : properties)
        {
            pp = (String) (e);
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
                continue;
            }
        }

        for (Object e : propertiesToRemove)
        {
            property = (String) (e);
            properties.remove(property);
        }
    }

    /**
     * Save the contents of a ProbabilityVariable object into a PrintStream
     * using the XMLBIF v0.3 format.
     * @param out
     */
    public void saveXml_0_3(PrintStream out)
    {
        String property;

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
            for (int i = 0; i < values.length; i++)
            {
                out.println("\t<OUTCOME>" + values[i] + "</OUTCOME>");
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
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</VARIABLE>\n");
    }

    /**
     * Save the contents of a ProbabilityVariable object into a PrintStream.
     * @param out
     */
    public void saveXml(PrintStream out)
    {
        String property;

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
            for (int i = 0; i < values.length; i++)
            {
                out.println("\t<VALUE>" + values[i] + "</VALUE>");
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
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println("</VARIABLE>\n");
    }

    /**
     * Print method for ProbabilityVariable.
     */
    @Override
    public void print(PrintStream out)
    {
        String property;

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
            out.println(" //" + values.length + " values");
            out.print("\ttype discrete[" + values.length + "] { ");
            for (int i = 0; i < values.length; i++)
            {
                out.print(" \"" + values[i] + "\" ");
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
     * Get the type of the ProbabilityVariable.
     * @return 
     */
    public int getType()
    {
        return (type);
    }

    /**
     * Indicate whether the current ProbabilityVariable is an explanatory
     * variable or not.
     * @return 
     */
    public boolean isExplanation()
    {
        return (explanationIndex != BayesNet.INVALID_INDEX);
    }

    /**
     * Indicate whether the current ProbabilityVariable has been observed or
     * not.
     * @return 
     */
    public boolean isObserved()
    {
        return (observedIndex != BayesNet.INVALID_INDEX);
    }

    /**
     * Set a value of the current ProbabilityVariable as observed.
     *
     * @param v Observed value.
     */
    public void setObservedValue(String v)
    {
        int index = indexOfValue(v);
        if (index == BayesNet.INVALID_INDEX)
        {
            return;
        }
        observedIndex = index;
    }

    /**
     * Set the variable as explanatory with a given value.
     *
     * @param i Index of the value that is assigned to the variable.
     */
    public void setExplanationValue(int i)
    {
        explanationIndex = i;
    }

    /**
     * Add a property to the current ProbabilityVariable.
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
     * Remove a property from the current ProbabilityVariable.
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
     * Remove a property from the current ProbabilityVariable given the position
     * of the property.
     * @param i
     */
    public void removeProperty(int i)
    {
        if (properties == null)
        {
            return;
        }
        properties.remove(i);
    }

    /**
     * Get the index of the observed value.
     * @return 
     */
    public int getObservedIndex()
    {
        return (observedIndex);
    }

    /**
     * Get the index of the assigned value in the variable.
     * @return 
     */
    public int getExplanationIndex()
    {
        return (explanationIndex);
    }

    /**
     * Get the properties.
     * @return 
     */
    public ArrayList getProperties()
    {
        return (properties);
    }

    /**
     * Set the properties.
     * @param prop
     */
    public void setProperties(ArrayList prop)
    {
        properties = prop;
    }

    /**
     * Get an Iterator with the properties.
     * @return 
     */
    public ArrayList getEnumeratedProperties()
    {
        return (properties);
    }

    /**
     * Set the index of the variable.
     * @param ind
     */
    public void setIndex(int ind)
    {
        index = ind;
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
     * @param t
     */
    public void setType(int t)
    {
        type = t;
    }
}
