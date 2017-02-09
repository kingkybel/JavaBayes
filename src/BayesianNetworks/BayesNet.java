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

import BayesianInferences.BayesBall;
import InterchangeFormat.InterchangeFormat;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;
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
    private ProbabilityVariable probabilityVariables[];
    private ProbabilityFunction probabilityFunctions[];
    private DiscreteFunction utilityFunction;

    /**
     * Default constructor for a BayesNet.
     */
    public BayesNet()
    {
    }

    /**
     * Simple constructor for a BayesNet.
     *
     * @param name          name of the network
     * @param numberOfVars  number of variables in the network
     * @param numberOfFuncs number of probability distributions in the network
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
     * @param networkDescription the network given as a string description that
     *                           will be parsed
     * @throws Exception if the string cannot be successfully parsed
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
        doTranslate(interchangeFmt);
    }

    private void doTranslate(InterchangeFormat interchangeFmt)
    {
        translate(interchangeFmt);
    }

    /**
     * Construct a BayesNet from a textual description in a stream.
     *
     * @param istream input stream
     * @throws Exception if the string cannot be successfully parsed
     */
    public BayesNet(InputStream istream) throws Exception
    {
        this();

        // Read the BayesNet from the stream
        InterchangeFormat interchangeFmt = new InterchangeFormat(istream);
        interchangeFmt.CompilationUnit();

        // Now transfer information from the parser
        doTranslate(interchangeFmt);
    }

    /**
     * Construct a BayesNet from a textual description in an URL.
     *
     * @param context the URL context as defined in the Java libraries
     * @param spec    the URL spec as defined in the Java libraries
     * @throws Exception if the string cannot be successfully parsed
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
            doTranslate(interchangeFmt);
        }
    }

    /**
     * Construct a BayesNet from a textual description in an URL.
     *
     * @param url the URL where to find the textual description of the Bayes net
     * @throws Exception if the string cannot be successfully parsed
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
            doTranslate(interchangeFmt);
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
     * @param interchangeFmt a derivative of InterchangeFormat
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
     * @param varIndex index of the variable we want to process
     */
    protected void processProbabilityVariableProperties(int varIndex)
    {
        probabilityVariables[varIndex].processProperties();
    }

    /**
     * Process the properties of a ProbabilityFunction.
     *
     * @param funcIndex index of the probability function we want to process
     */
    protected void processProbabilityFunctionProperties(int funcIndex)
    {
        probabilityFunctions[funcIndex].processProperties();
    }

    /**
     * Find the ProbabilityFunction that corresponds to a given
     * ProbabilityVariable. Note: the index of a variable is used by the
     * function, as it is the only reference to the variable that is guaranteed
     * to identify the variable uniquely.
     *
     * @param probVar a probability variable
     * @return the corresponding function
     */
    public ProbabilityFunction getFunction(ProbabilityVariable probVar)
    {
        for (ProbabilityFunction probabilityFunction : probabilityFunctions)
        {
            if (probVar.index == probabilityFunction.getVariable(0).index)
            {
                return probabilityFunction;
            }
        }

        return null;
    }

    /**
     * Retrieve the probability function for a variable.
     *
     * @param varName the variable identified by its name
     * @return the probability function
     * @throws java.lang.Exception if the function cannot be found
     */
    public ProbabilityFunction getFunction(String varName) throws Exception
    {
        return getFunction(getProbabilityVariable(varName));
    }

    /**
     * Retrieve the probability function for a variable.
     *
     * @param varIndex the variable identified by its name
     * @return the probability function
     */
    public ProbabilityFunction getFunction(int varIndex)
    {
        if (probabilityFunctions == null ||
            varIndex < 0 ||
            varIndex > numberProbabilityFunctions())
        {
            return null;
        }
        return probabilityFunctions[varIndex];
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
            for (j = 0; j < probabilityVariables[i].numberValues(); j++)
            {
                out.print("\"" + probabilityVariables[i].values[j] + "\"");
                if (j != (probabilityVariables[i].numberValues() - 1))
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
                        probabilityFunctions[i].getVariable(0).getName() + ",");
            if (probabilityFunctions[i].numberVariables() > 1)
            {
                out.print("\t\t\tnew CategoricalVariable[] {");
                for (j = 1; j < probabilityFunctions[i].numberVariables(); j++)
                {
                    out.print(probabilityFunctions[i].getVariable(0).getName());
                    if (j != (probabilityFunctions[i].numberVariables() - 1))
                    {
                        out.print(", ");
                    }
                }
                out.println("}, ");
            }
            out.print("\t\t\tnew double[] {");
            for (j = 0; j < probabilityFunctions[i].numberValues(); j++)
            {
                out.print(probabilityFunctions[i].getValue(j));
                if (j != (probabilityFunctions[i].numberValues() - 1))
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
            for (ProbabilityVariable probabilityVariable : probabilityVariables)
            {
                if (probabilityVariable != null)
                {
                    probabilityVariable.saveXml_0_3(out);
                }
            }
        }
        out.println();

        // Probability distributions.
        out.println("<!-- Probability distributions -->");
        if (probabilityFunctions != null)
        {
            for (ProbabilityFunction probabilityFunction : probabilityFunctions)
            {
                if (probabilityFunction != null)
                {
                    probabilityFunction.saveXml_0_3(out);
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
            for (ProbabilityVariable probabilityVariable : probabilityVariables)
            {
                if (probabilityVariable != null)
                {
                    probabilityVariable.saveXml(out);
                }
            }
        }
        out.println();

        // Probability distributions.
        out.println("<!-- Probability distributions -->");
        if (probabilityFunctions != null)
        {
            for (ProbabilityFunction probabilityFunction : probabilityFunctions)
            {
                if (probabilityFunction != null)
                {
                    probabilityFunction.saveXml(out);
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
     * @return a list of all the evidence as an array of String arrays
     */
    public String[][] getAllEvidence()
    {
        ProbabilityVariable probVar;
        ArrayList<ProbabilityVariable> evs = new ArrayList<>();

        for (ProbabilityVariable probabilityVariable : probabilityVariables)
        {
            probVar = probabilityVariable;
            if (probVar.getObservedIndex() != BayesNet.INVALID_INDEX)
            {
                evs.add(probVar);
            }
        }

        String allEvs[][] = new String[evs.size()][];
        for (int i = 0; i < allEvs.length; i++)
        {
            allEvs[i] = new String[2];
        }

        for (int i = 0; i < evs.size(); i++)
        {
            ProbabilityVariable evidenceProbVar = evs.get(i);
            allEvs[i][0] = evidenceProbVar.name;
            allEvs[i][1] =
            evidenceProbVar.values[evidenceProbVar.getObservedIndex()];
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
        for (int i = 0; i < probabilityVariables.length; i++)
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
     * @return the network name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the network.
     *
     * @param name the new name
     */
    public final void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the properties.
     *
     * @return the properties as list
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
     * @param property the property to add as string
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
     * @param propIndex the index of the property to remove
     */
    public void removeProperty(int propIndex)
    {
        properties.remove(propIndex);
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
     * @param varIndex the index of the variable to look for
     * @return the probability variable at index if found or null otherwise
     */
    public ProbabilityVariable getProbabilityVariable(int varIndex)
    {
        if (varIndex <= probabilityVariables.length)
        {
            return probabilityVariables[varIndex];
        }
        else
        {
            return null;
        }
    }

    /**
     * Get the probability function at a given index.
     *
     * @param funcIndex the index of the function to look for
     * @return the probability function at index if found or null otherwise
     */
    public ProbabilityFunction getProbabilityFunction(int funcIndex)
    {
        if (funcIndex > -1 && funcIndex < probabilityFunctions.length)
        {
            return probabilityFunctions[funcIndex];
        }
        else
        {
            throw new IndexOutOfBoundsException(funcIndex +
                                                " is out of range [0.." +
                                                probabilityFunctions.length);
        }
    }

    /**
     * Retrieve a variable by name.
     *
     * @param varName the name to search for
     * @return the variable with this name if it exists, null otherwise
     * @throws java.lang.Exception if there is no variable called varName
     */
    public ProbabilityVariable getProbabilityVariable(String varName)
            throws Exception
    {
        if (varName == null)
        {
            throw new Exception("Cannot get <null> probability variable.");
        }
        for (ProbabilityVariable var : probabilityVariables)
        {
            if (var.getName() == null)
            {
                throw new Exception(
                        "<null> probability variable in probabilityVariables.");
            }
            if (var.getName().equals(varName))
            {
                return var;
            }
        }
        throw new Exception("Cannot get probability variable '" + varName + "'");
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
     * @param varIndex   index of the variable to set
     * @param name       new name
     * @param values     values the variable <b>can assume</b> as string array
     * @param properties list of properties
     * @throws java.lang.Exception if index is out of range
     */
    public void setProbabilityVariable(int varIndex,
                                       String name,
                                       String values[],
                                       ArrayList<String> properties)
            throws Exception
    {
        if (varIndex > 0 && varIndex < probabilityVariables.length)
        {
            probabilityVariables[varIndex] = new ProbabilityVariable(this,
                                                                     name,
                                                                     varIndex,
                                                                     values,
                                                                     properties);
        }
        else
        {
            throw new Exception("Attempt to set variable at index " +
                                varIndex +
                                ": illegal index. Range=[0.." +
                                probabilityVariables.length);
        }
    }

    /**
     * Set a probability function given its constituents.
     *
     * @param funcIndex  index of the probability function we want to change
     * @param variables  an array of discrete variable objects
     * @param probValues the probability values of the function as array of
     *                   doubles
     * @param properties list of properties
     * @throws java.lang.Exception if index is out of range
     */
    public void setProbabilityFunction(int funcIndex,
                                       ProbabilityVariable[] variables,
                                       double probValues[],
                                       ArrayList<String> properties) throws
            Exception
    {
        if (funcIndex > 0 && funcIndex < probabilityFunctions.length)
        {
            probabilityFunctions[funcIndex] =
            new ProbabilityFunction(this, variables, probValues, properties);
        }
        else
        {
            throw new Exception("Attempt to set function at index " +
                                funcIndex +
                                ": illegal index. Range=[0.." +
                                probabilityFunctions.length);
        }
    }

    /**
     * Set a probability variable given its index.
     *
     * @param varIndex index of the probability variable
     * @param probVar  the new probability variable
     */
    public void setProbabilityVariable(int varIndex, ProbabilityVariable probVar)
    {
        probVar.setBayesNet(this);
        probVar.setIndex(varIndex);
        probabilityVariables[varIndex] = probVar;
    }

    /**
     * Set a probability variable given its index.
     *
     * @param funcIndex index of the probability function
     * @param probFunc  probability function
     */
    public void setProbabilityFunction(int funcIndex,
                                       ProbabilityFunction probFunc)
    {
        probFunc.bayesNet = this;
        probabilityFunctions[funcIndex] = probFunc;
    }

    /**
     * Set the vector of probability variables.
     *
     * @param probVars probability variables as array
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

    /**
     * Check whether there are any properties defined.
     *
     * @return true if so, false otherwise
     */
    public boolean hasProperties()
    {
        return (properties != null) && (properties.size() > 0);
    }

    /**
     * Retrieve the variable with at index.
     *
     * @param varIndex index of the variable in the array
     * @return the variable if the index is a valid index in the array, null
     *         otherwise
     */
    public ProbabilityVariable getVariable(int varIndex)
    {
        if (probabilityVariables == null ||
            varIndex < 0 ||
            varIndex >= numberVariables())
        {
            return null;
        }
        return probabilityVariables[varIndex];
    }

    /**
     * Calculate the probability of an event. using the following probability
     * formulas.
     *
     * <h3> Conditional probability definition.</h3>
     * <code>
     * P(A|B) = P(A,B)/P(B)
     * </code><br>
     *
     * <h3> Statistically independent definition.</h3>
     * <code>
     * A and B statistically independend <==> P(A,B) = P(A)P(B)<br>
     * equivalently if P(B) != 0:<br>
     * A and B statistically independend <==> P(A|B) = P(A) <br>
     * equivalently if P(A)!=0:<br>
     * A and B statistically independend <==> P(B|A) = P(B) <br>
     * </code><br>
     *
     * <h3> Bayes formula<br></h3>
     * <table>
     * <tr>
     * <td></td> <td></td> <td >P(L|W)P(W)</td>
     * <td></td>
     * <td>P(L|W)P(W)</td>
     * </tr>
     * <tr>
     * <td>P(W|L)</td> <td >=</td>
     * <td>------------</td><td> =
     * </td><td>-------------------------</td></tr>
     * <tr>
     * <td></td><td></td><td>P(L)</td><td>
     * </td><td> P(L|W)P(W) + P(L|¬W)P(¬W)</td>
     * </tr>
     * </table><br>
     *
     * <h3> Chain rule of probability</h3>
     * <code>
     * P(An,An-1,...,A1)=<br>P(An|An-1,...,A1)P(An-1,...,A1)P(A4,A3,A2,A1)=<br>
     * P(A4|A3,A2,A1)P(A3|A2,A1)P(A2|A1)P(A1)
     * </code><br>
     *
     * <h3> Total probability</h3>
     * <code>
     * P(A) = &sum;(P(A,B<sub>i</sub>)) =
     * &sum;(P(A|B<sub>i</sub>)P(B<sub>i</sub>))
     * </code><br>
     *
     * <h3> Symmetry</h3>
     * <code>
     * P(A | B,C) == P(A | B) [A independent of C]<br>
     * <==><br>
     * P(B | A,C) == P(B | A) [B independent of C]
     * </code><br>
     *
     * @param event
     * @param condition
     * @return
     * @see
     * <a href="https://en.wikipedia.org/wiki/Conditional_probability">
     * Conditional probability</a>
     * @see
     * <a href="https://en.wikipedia.org/wiki/Chain_rule_%28probability%29">
     * Chain rule</a>
     * @see
     * <a href="https://en.wikipedia.org/wiki/Law_of_total_probability">
     * Total probability</a>
     * @see
     * <a href="http://research.cs.queensu.ca/home/xiao/dm.html">
     * Data mining</a>
     *
     */
    public double P(ArrayList<ProbabilityVariable> event,
                    ArrayList<ProbabilityVariable> condition)
    {
        double reval = 0.0;
        TreeSet<ProbabilityVariable> eventSet = new TreeSet<>();
        event.addAll(event);

        TreeSet<ProbabilityVariable> conditionSet = new TreeSet<>();
        conditionSet.addAll(condition);

        BayesBall.ResultType result = BayesBall.run(eventSet, conditionSet);

        return reval;
    }
}
