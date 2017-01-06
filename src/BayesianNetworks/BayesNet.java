/*
 * BayesNet.java
 *
 * @author Fabio G. Cozman
 * Copyright 1996 - 1999, Fabio G. Cozman, Carnergie
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

import InterchangeFormat.InterchangeFormat;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class BayesNet
{

    private static final Class CLAZZ = BayesNet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Constant for invalid indices. Used for example in cases when values are
     * not in a collection.
     */
    public static final int INVALID_INDEX = -1;

    private String name;
    private ArrayList<String> properties = new ArrayList<>();
    protected ProbabilityVariable probabilityVariables[];
    protected ProbabilityFunction probabilityFunctions[];
    protected DiscreteFunction utilityFunction;

    /**
     * Default constructor for a BayesNet.
     */
    public BayesNet()
    {
    }

    /**
     * Simple constructor for a BayesNet.
     *
     * @param name          name of the network.
     * @param numberOfVars  number of variables in the network.
     * @param numberOfFuncs number of probability distributions in the network.
     */
    public BayesNet(String name, int numberOfVars, int numberOfFuncs)
    {
        this();
        setName(name);
        setProbabilityVariables(new ProbabilityVariable[numberOfVars]);
        setProbabilityFunctions(new ProbabilityFunction[numberOfFuncs]);
    }

    /**
     * Simple constructor for a BayesNet.
     *
     * @param name       name of the network
     * @param properties list of properties properties of the network
     */
    public BayesNet(String name, ArrayList<String> properties)
    {
        this();
        setName(name);
        setProperties(properties);
    }

    /**
     * Simple constructor for a BayesNet; creates a copy of a given network.
     *
     * @param bayesNet the underlying Bayesian network Network to be copied.
     */
    public BayesNet(BayesNet bayesNet)
    {
        this(bayesNet.name,
             bayesNet.probabilityVariables.length,
             bayesNet.probabilityFunctions.length);

        System.arraycopy(bayesNet.probabilityVariables,
                         0,
                         probabilityVariables,
                         0,
                         bayesNet.probabilityVariables.length);
        System.arraycopy(bayesNet.probabilityFunctions,
                         0, probabilityFunctions,
                         0,
                         bayesNet.probabilityFunctions.length);

        properties = bayesNet.properties;
    }

    /**
     * Construct a BayesNet from a textual description in a string.
     *
     * @param networkDescription
     * @throws Exception
     */
    public BayesNet(String networkDescription) throws Exception
    {
        this();
        ByteArrayInputStream istream = new ByteArrayInputStream(
                             networkDescription.getBytes());
        // Read the BayesNet from the stream
        InterchangeFormat interchangeFmt = new InterchangeFormat(istream);
        interchangeFmt.CompilationUnit();
        // Transfer information from the parser
        translate(interchangeFmt);
    }

    /**
     * Construct a BayesNet from a textual description in a stream.
     *
     * @param istream
     * @throws Exception
     */
    public BayesNet(InputStream istream) throws Exception
    {
        this();

        // Read the BayesNet from the stream
        InterchangeFormat interchangeFmt = new InterchangeFormat(istream);
        interchangeFmt.CompilationUnit();

        // Now transfer information from the parser
        translate(interchangeFmt);
    }

    /**
     * Construct a BayesNet from a textual description in an URL.
     *
     * @param context The URL context as defined in the Java libraries.
     * @param spec    The URL spec as defined in the Java libraries.
     * @throws Exception
     */
    public BayesNet(URL context, String spec) throws Exception
    {
        this();
        URL url = new URL(context, spec);
        // Read the BayesNet from the stream
        try (InputStream istream = url.openStream())
        {
            // Read the BayesNet from the stream
            InterchangeFormat interchangeFmt = new InterchangeFormat(istream);
            interchangeFmt.CompilationUnit();
            // Now transfer information from the parser
            translate(interchangeFmt);
        }
    }

    /**
     * Construct a BayesNet from a textual description in an URL.
     *
     * @param url
     * @throws Exception
     */
    public BayesNet(URL url) throws Exception
    {
        this();
        // Read the BayesNet from the stream
        try (InputStream istream = url.openStream())
        {
            // Read the BayesNet from the stream
            InterchangeFormat interchangeFmt = new InterchangeFormat(istream);
            interchangeFmt.CompilationUnit();
            // Now transfer information from the parser
            translate(interchangeFmt);
        }
    }

    /**
     * Translate the contents of a IFBayesNet object into a BayesNet object.
     *
     * This method makes modifications to the basic objects supported by the
     * InterchangeFormat, so that the full functionality of the BayesianNetworks
     * package can be used. As the InterchangeFormat evolves, probably some of
     * the objects created through extensions will be created directly by the
     * parser as it parses an InterchangeFormat stream. Right now the extensions
     * involve:
     * <ol>
     * <li> Detecting observed variables.</li>
     * <li> Detecting explanation variables.</li>
     * </ol>
     *
     * @param interchangeFmt
     */
    protected void translate(InterchangeFormat interchangeFmt)
    {
        ConvertInterchangeFormat cbn = new ConvertInterchangeFormat(
                                 interchangeFmt);
        setName(cbn.getName());
        setProperties(cbn.getProperties());
        setProbabilityVariables(cbn.getProbabilityVariables(this));
        setProbabilityFunctions(cbn.getProbabilityFunctions(this));

        cbn.getFunctionsAsTables(this);

        // Process BayesNet properties
        processProperties();

        // Process ProbabilityVariable properties
        for (int i = 0; i < probabilityVariables.length; i++)
        {
            processProbabilityVariableProperties(i);
        }

        // Process ProbabilityFunction properties
        for (int i = 0; i < probabilityFunctions.length; i++)
        {
            processProbabilityFunctionProperties(i);
        }
    }

    /**
     * Method that makes modifications to the QuasiBayesNet based on its
     * properties (works by overriding method in BayesNet).
     */
    protected void processProperties()
    {
    }

    /**
     * Process the properties of a ProbabilityVariable.
     *
     * @param index
     */
    protected void processProbabilityVariableProperties(int index)
    {
        probabilityVariables[index].processProperties();
    }

    /**
     * Process the properties of a ProbabilityFunction.
     *
     * @param index
     */
    protected void processProbabilityFunctionProperties(int index)
    {
        probabilityFunctions[index].processProperties();
    }

    /**
     * Find the ProbabilityFunction that corresponds to a given
     * ProbabilityVariable. Note: the index of a variable is used by the
     * function, as it is the only reference to the variable that is guaranteed
     * to identify the variable uniquely.
     *
     * @param probVar a probability variable
     * @return
     */
    public ProbabilityFunction getFunction(ProbabilityVariable probVar)
    {
        for (ProbabilityFunction probabilityFunction : probabilityFunctions)
        {
            if (probVar.index == probabilityFunction.variables[0].index)
            {
                return probabilityFunction;
            }
        }

        return null;
    }

    /**
     * Retrieve the probability function for a variable.
     *
     * @param var the variable identified by its name
     * @return the probability function
     */
    public ProbabilityFunction getFunction(String var)
    {
        return getFunction(getProbabilityVariable(var));
    }

    /**
     * Save a BayesNet object in a stream, in the BIF InterchangeFormat.
     *
     * @param out output print stream
     */
    public void saveBif(PrintStream out)
    {
        int i;

        out.println("// Bayesian network ");
        if (name != null)
        {
            out.print("network \"" + name + "\" {");
        }
        if (probabilityVariables != null)
        {
            out.print(" //" + probabilityVariables.length + " variables");
        }
        if (probabilityFunctions != null)
        {
            out.print(" and " + probabilityFunctions.length +
                      " probability distributions");
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
        if (probabilityVariables != null)
        {
            for (i = 0; i < probabilityVariables.length;
                 i++)
            {
                if (probabilityVariables[i] != null)
                {
                    probabilityVariables[i].print(out);
                }
            }
        }
        if (probabilityFunctions != null)
        {
            for (i = 0; i < probabilityFunctions.length;
                 i++)
            {
                if (probabilityFunctions[i] != null)
                {
                    probabilityFunctions[i].print(out);
                }
            }
        }
    }

    /**
     * Save a BayesNet object in a stream for the EBayes engine.
     *
     * @param out output print stream
     */
    public void saveEmbayes(PrintStream out)
    {
        int i, j;
        out.println("import ebayes.data.*");
        out.println("class " + name + " extends BayesNet {");
        out.println("\tpublic " + name + "() {");
        out.println("\tsetName(\"" + name + "\");");

        for (i = 0; i < probabilityVariables.length; i++)
        {
            out.println("\tCategoricalVariable " +
                        probabilityVariables[i].name + " = ");
            out.println("\t\tnew CategoricalVariable(\"" +
                        probabilityVariables[i].name + "\",");
            out.print("\t\t\tnew String[] {");
            for (j = 0; j < probabilityVariables[i].values.length; j++)
            {
                out.print("\"" + probabilityVariables[i].values[j] + "\"");
                if (j != (probabilityVariables[i].values.length - 1))
                {
                    out.print(",");
                }
            }
            out.println("});\n");
        }
        out.println("\n\n");
        for (i = 0; i < probabilityFunctions.length; i++)
        {
            out.println("\tCategoricalProbability p" + i + " = ");
            out.println("\t\tnew CategoricalProbability(" +
                        probabilityFunctions[i].variables[0].getName() + ",");
            if (probabilityFunctions[i].variables.length > 1)
            {
                out.print("\t\t\tnew CategoricalVariable[] {");
                for (j = 1; j < probabilityFunctions[i].variables.length; j++)
                {
                    out.print(probabilityFunctions[i].variables[j].getName());
                    if (j != (probabilityFunctions[i].variables.length - 1))
                    {
                        out.print(", ");
                    }
                }
                out.println("}, ");
            }
            out.print("\t\t\tnew double[] {");
            for (j = 0; j < probabilityFunctions[i].values.length; j++)
            {
                out.print(probabilityFunctions[i].values[j]);
                if (j != (probabilityFunctions[i].values.length - 1))
                {
                    out.print(", ");
                }
            }
            out.println("});\n");
        }
        out.println("\tsetVariables(");
        out.println("\t\tnew CategoricalVariable[]");
        out.print("\t\t\t{");
        for (i = 0; i < probabilityVariables.length; i++)
        {
            out.print(probabilityVariables[i].getName());
            if (i != (probabilityVariables.length - 1))
            {
                out.print(", ");
            }
        }
        out.println("} );\n");

        out.println("\tsetProbabilities(");
        out.println("\t\tnew CategoricalProbability[]");
        out.print("\t\t\t{");
        for (i = 0; i < probabilityFunctions.length; i++)
        {
            out.print("p" + i);
            if (i != (probabilityFunctions.length - 1))
            {
                out.print(", ");
            }
        }
        out.println("} );\n");

        out.println("\n}");
    }

    /**
     * Save a BayesNet object in a stream, in the XMLBIF format version 0.3
     * (most recent version).
     *
     * @param out output print stream
     */
    public void saveXml(PrintStream out)
    {
        int i;

        // Heading for the file
        out.println("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n\n");
        out.println("<!--");
        out.println(
                "\tBayesian network in XMLBIF v0.3 (BayesNet Interchange Format)");
        out.println(
                "\tProduced by JavaBayes (http://www.cs.cmu.edu/~javabayes/");
        out.println("\tOutput created " + (new Date()));
        out.println("-->\n\n\n");

        out.println("<!-- DTD for the XMLBIF 0.3 format -->");
        out.println("<!DOCTYPE BIF [\n" +
                    "\t<!ELEMENT BIF ( NETWORK )*>\n" +
                    "\t      <!ATTLIST BIF VERSION CDATA #REQUIRED>\n" +
                    "\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n" +
                    "\t<!ELEMENT NAME (#PCDATA)>\n" +
                    "\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n" +
                    "\t      <!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n" +
                    "\t<!ELEMENT OUTCOME (#PCDATA)>\n" +
                    "\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n" +
                    "\t<!ELEMENT FOR (#PCDATA)>\n" +
                    "\t<!ELEMENT GIVEN (#PCDATA)>\n" +
                    "\t<!ELEMENT TABLE (#PCDATA)>\n" +
                    "\t<!ELEMENT PROPERTY (#PCDATA)>\n" +
                    "]>\n\n");

        // Start of Bayes net
        out.println("<BIF VERSION=\"0.3\">");

        // Bayes net description
        out.println("<NETWORK>");
        if (name != null)
        {
            out.println("<NAME>" + name + "</NAME>");
        }
        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println();

        // Variables
        out.println("<!-- Variables -->");
        if (probabilityVariables != null)
        {
            for (i = 0; i < probabilityVariables.length;
                 i++)
            {
                if (probabilityVariables[i] != null)
                {
                    probabilityVariables[i].saveXml_0_3(out);
                }
            }
        }
        out.println();

        // Probability distributions.
        out.println("<!-- Probability distributions -->");
        if (probabilityFunctions != null)
        {
            for (i = 0; i < probabilityFunctions.length;
                 i++)
            {
                if (probabilityFunctions[i] != null)
                {
                    probabilityFunctions[i].saveXml_0_3(out);
                }
            }
        }
        out.println();

        // End of Bayes net description.
        out.println("</NETWORK>");

        // End of Bayes net.
        out.println("</BIF>");
    }

    /**
     * Save a BayesNet object in a stream, in the XMLBIF format version 0.2.
     *
     * @param out output print stream
     */
    public void saveXml_0_2(PrintStream out)
    {
        int i;

        // Heading for the file
        out.println("<?XML VERSION=\"1.0\"?>\n\n");
        out.println("<!--");
        out.println(
                "\tBayesian network in BIF (BayesNet Interchange Format)");
        out.println(
                "\tProduced by JavaBayes (http://www.cs.cmu.edu/~javabayes/");
        out.println("\tOutput created " + (new Date()));
        out.println("-->\n\n\n");

        out.println("<!-- DTD for the BIF format -->");
        out.println("<!DOCTYPE BIF [\n" +
                    "\t<!ELEMENT BIF ( NETWORK )*>\n" +
                    "\t<!ELEMENT PROPERTY (#PCDATA)>\n" +
                    "\t<!ELEMENT TYPE (#PCDATA)>\n" +
                    "\t<!ELEMENT VALUE (#PCDATA)>\n" +
                    "\t<!ELEMENT NAME (#PCDATA)>\n" +
                    "\t<!ELEMENT NETWORK\n" +
                    "\t    ( NAME, ( PROPERTY | VARIABLE | PROBABILITY )* )>\n" +
                    "\t<!ELEMENT VARIABLE ( NAME, TYPE, ( VALUE |  PROPERTY )* ) >\n" +
                    "\t<!ELEMENT PROBABILITY\n" +
                    "\t    ( FOR | GIVEN | TABLE | ENTRY | DEFAULT | PROPERTY )* >\n" +
                    "\t<!ELEMENT FOR (#PCDATA)>\n" +
                    "\t<!ELEMENT GIVEN (#PCDATA)>\n" +
                    "\t<!ELEMENT TABLE (#PCDATA)>\n" +
                    "\t<!ELEMENT DEFAULT (TABLE)>\n" +
                    "\t<!ELEMENT ENTRY ( VALUE* , TABLE )>\n" +
                    "]>\n\n");

        // Start of Bayes net
        out.println("<BIF>");

        // Bayes net description
        out.println("<NETWORK>");
        if (name != null)
        {
            out.println("<NAME>" + name + "</NAME>");
        }
        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        out.println();

        // Variables
        out.println("<!-- Variables -->");
        if (probabilityVariables != null)
        {
            for (i = 0; i < probabilityVariables.length;
                 i++)
            {
                if (probabilityVariables[i] != null)
                {
                    probabilityVariables[i].saveXml(out);
                }
            }
        }
        out.println();

        // Probability distributions.
        out.println("<!-- Probability distributions -->");
        if (probabilityFunctions != null)
        {
            for (i = 0; i < probabilityFunctions.length;
                 i++)
            {
                if (probabilityFunctions[i] != null)
                {
                    probabilityFunctions[i].saveXml(out);
                }
            }
        }
        out.println();

        // End of Bayes net description.
        out.println("</NETWORK>");

        // End of Bayes net.
        out.println("</BIF>");
    }

    /**
     * Save a BayesNet object into a stream, in the BUGS format.
     *
     * @param out output print stream
     */
    public void saveBugs(PrintStream out)
    {
        SaveBugs sb = new SaveBugs(this);
        sb.save(out);
    }

    /**
     * Get all the evidence contained in the network variables.
     *
     * @return
     */
    public String[][] getAllEvidence()
    {
        int i, j, aux;
        ProbabilityVariable probVar;
        ArrayList<ProbabilityVariable> evs = new ArrayList<>();

        for (i = 0; i < probabilityVariables.length; i++)
        {
            probVar = probabilityVariables[i];
            if (probVar.observedIndex != BayesNet.INVALID_INDEX)
            {
                evs.add(probVar);
            }
        }

        String allEvs[][] = new String[evs.size()][];
        for (i = 0; i < allEvs.length; i++)
        {
            allEvs[i] = new String[2];
        }

        j = 0;
        for (ProbabilityVariable evidenceProbVar : evs)
        {
            allEvs[j][0] = evidenceProbVar.name;
            aux = evidenceProbVar.observedIndex;
            allEvs[j][1] = evidenceProbVar.values[aux];
        }

        return allEvs;
    }

    /**
     * Determine the position of a variable given its name.
     *
     * @param searchName name of the variable
     * @return index of the variable if it exists in the network, INVALID_INDEX
     *         else
     */
    public int indexOfVariable(String searchName)
    {
        int i;
        for (i = 0; i < probabilityVariables.length; i++)
        {
            if (probabilityVariables[i].name.equals(searchName))
            {
                return i;
            }
        }
        return INVALID_INDEX;
    }

    /**
     * Print a BayesNet in the standard output.
     */
    public void print()
    {
        print(System.out);
    }

    /**
     * Print a BayesNet in a given stream.
     *
     * @param out output print stream
     */
    public void print(PrintStream out)
    {
        saveBif(out);
    }

    /* *************************************************************** *
     * Methods that allow basic manipulation of non-public variables   *
     * *************************************************************** */
    /**
     * Get the name of the network.
     *
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the network.
     *
     * @param name
     */
    public final void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the properties.
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
    public final void setProperties(ArrayList<String> properties)
    {
        this.properties = properties;
    }

    /**
     * Add a property.
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
     * Remove a property.
     *
     * @param property the property to remove
     */
    public void removeProperty(String property)
    {
        properties.remove(property);
    }

    /**
     * Remove a property by its index.
     *
     * @param index the index to remove
     */
    public void removeProperty(int index)
    {
        properties.remove(index);
    }

    /**
     * Get the number of variables in the network.
     *
     * @return the cardinality of variables if the variables are initialised
     *         (can be 0) or INVALID_INDEX else
     */
    public int numberVariables()
    {
        if (probabilityVariables == null)
        {
            return BayesNet.INVALID_INDEX;
        }
        return probabilityVariables.length;
    }

    /**
     * Get the number of distributions in the network.
     *
     * @return the cardinality of functions if the functions are initialised
     *         (can be 0) or INVALID_INDEX else
     */
    public int numberProbabilityFunctions()
    {
        if (probabilityFunctions == null)
        {
            return BayesNet.INVALID_INDEX;
        }
        return probabilityFunctions.length;
    }

    /**
     * Get the probability variable at a given index.
     *
     * @param index the index to look for
     * @return the probability variable at index if found or null otherwise
     */
    public ProbabilityVariable getProbabilityVariable(int index)
    {
        if (index <= probabilityVariables.length)
        {
            return probabilityVariables[index];
        }
        else
        {
            return null;
        }
    }

    /**
     * Get the probability function at a given index.
     *
     * @param index the index to look for
     * @return the probability function at index if found or null otherwise
     */
    public ProbabilityFunction getProbabilityFunction(int index)
    {
        if (index <= probabilityFunctions.length)
        {
            return probabilityFunctions[index];
        }
        else
        {
            return null;
        }
    }

    /**
     * Retrieve a variable by name.
     *
     * @param varName the name to search for
     * @return the variable with this name if it exists, null otherwise
     */
    public ProbabilityVariable getProbabilityVariable(String varName)
    {
        for (ProbabilityVariable var : probabilityVariables)
        {
            if (var.getName() == null ? varName == null :
                var.getName().equals(varName))
            {
                return var;
            }
        }
        return null;
    }

    /**
     * Get the probability variables.
     *
     * @return the probability variables as array, could be null
     */
    public ProbabilityVariable[] getProbabilityVariables()
    {
        return probabilityVariables;
    }

    /**
     * Get the probability functions.
     *
     * @return the probability functions as array, could be null
     */
    public ProbabilityFunction[] getProbabilityFunctions()
    {
        return probabilityFunctions;
    }

    /**
     * Get the utility function.
     *
     * @return the utility function, could be null
     */
    public DiscreteFunction getUtilityFunction()
    {
        return utilityFunction;
    }

    /**
     * Set a probability variable given its constituents.
     *
     * @param index      index of the variable to set
     * @param name       new name
     * @param values     array of values
     * @param properties list of properties
     * @throws java.lang.Exception if index is out of range
     */
    public void setProbabilityVariable(int index,
                                       String name,
                                       String values[],
                                       ArrayList<String> properties)
            throws Exception
    {
        if (index > 0 && index < probabilityVariables.length)
        {
            probabilityVariables[index] = new ProbabilityVariable(this,
                                                                  name,
                                                                  index,
                                                                  values,
                                                                  properties);
        }
        else
        {
            throw new Exception("Attempt to set variable at index " +
                                index +
                                ": illegal index. Range=[0.." +
                                probabilityVariables.length);
        }
    }

    /**
     * Set a probability function given its constituents.
     *
     * @param index
     * @param variables
     * @param values
     * @param properties list of properties
     * @throws java.lang.Exception if index is out of range
     */
    public void setProbabilityFunction(int index,
                                       ProbabilityVariable[] variables,
                                       double values[],
                                       ArrayList<String> properties) throws
            Exception
    {
        if (index > 0 && index < probabilityFunctions.length)
        {
            probabilityFunctions[index] =
            new ProbabilityFunction(this, variables, values, properties);
        }
        else
        {
            throw new Exception("Attempt to set function at index " +
                                index +
                                ": illegal index. Range=[0.." +
                                probabilityFunctions.length);
        }
    }

    /**
     * Set a probability variable given its index.
     *
     * @param index   index of the probability variable
     * @param probVar the new probability variable
     */
    public void setProbabilityVariable(int index, ProbabilityVariable probVar)
    {
        probVar.setBayesNet(this);
        probVar.setIndex(index);
        probabilityVariables[index] = probVar;
    }

    /**
     * Set a probability variable given its index.
     *
     * @param index    index of the probability function
     * @param probFunc probability function
     */
    public void setProbabilityFunction(int index, ProbabilityFunction probFunc)
    {
        probFunc.bayesNet = this;
        probabilityFunctions[index] = probFunc;
    }

    /**
     * Set the vector of probability variables.
     *
     * @param probVar a probability variables
     */
    public final void setProbabilityVariables(ProbabilityVariable probVars[])
    {
        probabilityVariables = probVars;
    }

    /**
     * Set the vector of probability functions.
     *
     * @param probFuncs probability functions
     */
    public final void setProbabilityFunctions(ProbabilityFunction probFuncs[])
    {
        probabilityFunctions = probFuncs;
    }

    public boolean hasProperties()
    {
        return (properties != null) && (properties.size() > 0);
    }
}
