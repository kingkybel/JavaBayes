/**
 * BayesNet.java
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

import InterchangeFormat.InterchangeFormat;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public class BayesNet
{

    /**
     *
     */
    public static final int INVALID_INDEX = -1;

    /**
     *
     */
    public static final int BIF = 1;

    /**
     *
     */
    public static final int XML = 2;

    /**
     *
     */
    public static final int BUGS = 3;
    private static final String CLASS_NAME = BayesNet.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     *
     */
    protected String name;

    /**
     *
     */
    protected ArrayList properties;

    /**
     *
     */
    protected ProbabilityVariable probabilityVariables[];

    /**
     *
     */
    protected ProbabilityFunction probabilityFunctions[];

    /**
     *
     */
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
     * @param nN Name of the network.
     * @param nV Number of variables in the network.
     * @param nF Number of probability distributions in the network.
     */
    public BayesNet(String nN, int nV, int nF)
    {
        this();
        name = nN;
        probabilityVariables = new ProbabilityVariable[nV];
        probabilityFunctions = new ProbabilityFunction[nF];
    }

    /**
     * Simple constructor for a BayesNet.
     *
     * @param nN Name of network.
     * @param p  Properties of the network.
     */
    public BayesNet(String nN, ArrayList p)
    {
        this();
        name = nN;
        properties = p;
    }

    /**
     * Simple constructor for a BayesNet; creates a copy of a given network.
     *
     * @param bn Network to be copied.
     */
    public BayesNet(BayesNet bn)
    {
        this(bn.name, bn.probabilityVariables.length,
             bn.probabilityFunctions.length);

        System.arraycopy(bn.probabilityVariables, 0, probabilityVariables, 0,
                         bn.probabilityVariables.length);
        System.arraycopy(bn.probabilityFunctions, 0, probabilityFunctions, 0,
                         bn.probabilityFunctions.length);

        properties = bn.properties;
    }

    /**
     * Construct a BayesNet from a textual description in a string.
     *
     * @param s
     * @throws Exception
     */
    public BayesNet(String s) throws Exception
    {
        this();
        StringBufferInputStream istream = new StringBufferInputStream(s);
        // Read the BayesNet from the stream
        InterchangeFormat ifo = new InterchangeFormat(istream);
        ifo.CompilationUnit();
        // Transfer information from the parser
        translate(ifo);
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
        InterchangeFormat ifo = new InterchangeFormat(istream);
        ifo.CompilationUnit();

        // Now transfer information from the parser
        translate(ifo);
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
        InputStream istream = url.openStream();
        // Read the BayesNet from the stream
        InterchangeFormat ifo = new InterchangeFormat(istream);
        ifo.CompilationUnit();
        // Now transfer information from the parser
        translate(ifo);
        istream.close();
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
        InputStream istream = url.openStream();
        // Read the BayesNet from the stream
        InterchangeFormat ifo = new InterchangeFormat(istream);
        ifo.CompilationUnit();
        // Now transfer information from the parser
        translate(ifo);
        istream.close();
    }

    /*
     * Translate the contents of a
     * IFBayesNet object into a BayesNet object.
     *
     * This method makes modifications to the basic objects
     * supported by the InterchangeFormat, so that the full
     * functionality of the BayesianNetworks package can be used.
     * As the InterchangeFormat evolves, probably some of the
     * objects created through extensions will be created directly
     * by the parser as it parses an InterchangeFormat stream.
     * Right now the extensions involve:
     * 1) Detecting observed variables.
     * 2) Detecting explanation variables.
     */
    /**
     *
     * @param ifo
     */
    protected void translate(InterchangeFormat ifo)
    {
        ConvertInterchangeFormat cbn = new ConvertInterchangeFormat(ifo);
        name = cbn.getName();
        properties = cbn.getProperties();
        probabilityVariables = cbn.getProbabilityVariables(this);
        probabilityFunctions = cbn.getProbabilityFunctions(this);

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

    /*
     * Make modifications to a BayesNet based on the properties of
     * the BayesNet.
     */
    /**
     *
     */
    protected void processProperties()
    {
    }

    /*
     * Process the properties of a ProbabilityVariable.
     */
    /**
     *
     * @param index
     */
    protected void processProbabilityVariableProperties(int index)
    {
        probabilityVariables[index].processProperties();
    }

    /*
     * Process the properties of a ProbabilityFunction.
     */
    /**
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
     * @param pV
     * @return
     */
    public ProbabilityFunction getFunction(ProbabilityVariable pV)
    {
        for (int i = 0; i < probabilityFunctions.length; i++)
        {
            if (pV.index == probabilityFunctions[i].variables[0].index)
            {
                return (probabilityFunctions[i]);
            }
        }

        return (null);
    }

    /**
     * Save a BayesNet object in a stream, in the BIF InterchangeFormat.
     *
     * @param out
     */
    public void saveBif(PrintStream out)
    {
        int i;
        String property;

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
            for (Object e : properties)
            {
                property = (String) (e);
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
     * @param out
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
     * @param pstream
     */
    public void saveXml(PrintStream pstream)
    {
        int i;
        String property;

        // Heading for the file
        pstream.println("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n\n");
        pstream.println("<!--");
        pstream.println(
                "\tBayesian network in XMLBIF v0.3 (BayesNet Interchange Format)");
        pstream.println(
                "\tProduced by JavaBayes (http://www.cs.cmu.edu/~javabayes/");
        pstream.println("\tOutput created " + (new Date()));
        pstream.println("-->\n\n\n");

        pstream.println("<!-- DTD for the XMLBIF 0.3 format -->");
        pstream.println("<!DOCTYPE BIF [\n" +
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
        pstream.println("<BIF VERSION=\"0.3\">");

        // Bayes net description
        pstream.println("<NETWORK>");
        if (name != null)
        {
            pstream.println("<NAME>" + name + "</NAME>");
        }
        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                property = (String) (e);
                pstream.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        pstream.println();

        // Variables
        pstream.println("<!-- Variables -->");
        if (probabilityVariables != null)
        {
            for (i = 0; i < probabilityVariables.length;
                 i++)
            {
                if (probabilityVariables[i] != null)
                {
                    probabilityVariables[i].saveXml_0_3(pstream);
                }
            }
        }
        pstream.println();

        // Probability distributions.
        pstream.println("<!-- Probability distributions -->");
        if (probabilityFunctions != null)
        {
            for (i = 0; i < probabilityFunctions.length;
                 i++)
            {
                if (probabilityFunctions[i] != null)
                {
                    probabilityFunctions[i].saveXml_0_3(pstream);
                }
            }
        }
        pstream.println();

        // End of Bayes net description.
        pstream.println("</NETWORK>");

        // End of Bayes net.
        pstream.println("</BIF>");
    }

    /**
     * Save a BayesNet object in a stream, in the XMLBIF format version 0.2.
     *
     * @param pstream
     */
    public void saveXml_0_2(PrintStream pstream)
    {
        int i;
        String property;

        // Heading for the file
        pstream.println("<?XML VERSION=\"1.0\"?>\n\n");
        pstream.println("<!--");
        pstream.println(
                "\tBayesian network in BIF (BayesNet Interchange Format)");
        pstream.println(
                "\tProduced by JavaBayes (http://www.cs.cmu.edu/~javabayes/");
        pstream.println("\tOutput created " + (new Date()));
        pstream.println("-->\n\n\n");

        pstream.println("<!-- DTD for the BIF format -->");
        pstream.println("<!DOCTYPE BIF [\n" +
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
        pstream.println("<BIF>");

        // Bayes net description
        pstream.println("<NETWORK>");
        if (name != null)
        {
            pstream.println("<NAME>" + name + "</NAME>");
        }
        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                property = (String) (e);
                pstream.println("\t<PROPERTY>" + property + "</PROPERTY>");
            }
        }
        pstream.println();

        // Variables
        pstream.println("<!-- Variables -->");
        if (probabilityVariables != null)
        {
            for (i = 0; i < probabilityVariables.length;
                 i++)
            {
                if (probabilityVariables[i] != null)
                {
                    probabilityVariables[i].saveXml(pstream);
                }
            }
        }
        pstream.println();

        // Probability distributions.
        pstream.println("<!-- Probability distributions -->");
        if (probabilityFunctions != null)
        {
            for (i = 0; i < probabilityFunctions.length;
                 i++)
            {
                if (probabilityFunctions[i] != null)
                {
                    probabilityFunctions[i].saveXml(pstream);
                }
            }
        }
        pstream.println();

        // End of Bayes net description.
        pstream.println("</NETWORK>");

        // End of Bayes net.
        pstream.println("</BIF>");
    }

    /**
     * Save a BayesNet object into a stream, in the BUGS format.
     *
     * @param pstream
     */
    public void saveBugs(PrintStream pstream)
    {
        SaveBugs sb = new SaveBugs(this);
        sb.save(pstream);
    }

    /**
     * Get all the evidence contained in the network variables.
     *
     * @return
     */
    public String[][] getAllEvidence()
    {
        int i, j, aux;
        ProbabilityVariable pv;
        ArrayList evs = new ArrayList();
        String allEvs[][] = null;

        for (i = 0; i < probabilityVariables.length; i++)
        {
            pv = probabilityVariables[i];
            if (pv.observedIndex != BayesNet.INVALID_INDEX)
            {
                evs.add(pv);
            }
        }

        allEvs = new String[evs.size()][];
        for (i = 0; i < allEvs.length; i++)
        {
            allEvs[i] = new String[2];
        }

        j = 0;
        for (Object e : evs)
        {
            pv = (ProbabilityVariable) (e);
            allEvs[j][0] = pv.name;
            aux = pv.observedIndex;
            allEvs[j][1] = pv.values[aux];
        }

        return (allEvs);
    }

    /**
     * Determine the position of a variable given its name.
     *
     * @param nVb
     * @return
     */
    public int indexOfVariable(String nVb)
    {
        int i;
        for (i = 0; i < probabilityVariables.length; i++)
        {
            if (probabilityVariables[i].name.equals(nVb))
            {
                return (i);
            }
        }
        return (-1); // Returns -1 if name is not valid!
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
     * @param out
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
        return (name);
    }

    /**
     * Set the name of the network.
     *
     * @param n
     */
    public void setName(String n)
    {
        name = n;
    }

    /**
     * Get the properties.
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
     * Add a property.
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
     * Remove a property.
     *
     * @param prop
     */
    public void removeProperty(String prop)
    {
        properties.remove(prop);
    }

    /**
     * Remove a property.
     *
     * @param i
     */
    public void removeProperty(int i)
    {
        properties.remove(i);
    }

    /**
     * Get the number of variables in the network.
     *
     * @return
     */
    public int numberVariables()
    {
        if (probabilityVariables == null)
        {
            return (BayesNet.INVALID_INDEX);
        }
        return (probabilityVariables.length);
    }

    /**
     * Get the number of distributions in the network.
     *
     * @return
     */
    public int numberProbabilityFunctions()
    {
        if (probabilityFunctions == null)
        {
            return (BayesNet.INVALID_INDEX);
        }
        return (probabilityFunctions.length);
    }

    /**
     * Get the probability variable at a given index.
     *
     * @param index
     * @return
     */
    public ProbabilityVariable getProbabilityVariable(int index)
    {
        if (index <= probabilityVariables.length)
        {
            return (probabilityVariables[index]);
        }
        else
        {
            return (null);
        }
    }

    /**
     * Get the probability function at a given index.
     *
     * @param index
     * @return
     */
    public ProbabilityFunction getProbabilityFunction(int index)
    {
        if (index <= probabilityFunctions.length)
        {
            return (probabilityFunctions[index]);
        }
        else
        {
            return (null);
        }
    }

    /**
     * Get the probability variables.
     *
     * @return
     */
    public ProbabilityVariable[] getProbabilityVariables()
    {
        return (probabilityVariables);
    }

    /**
     * Get the probability functions.
     *
     * @return
     */
    public ProbabilityFunction[] getProbabilityFunctions()
    {
        return (probabilityFunctions);
    }

    /**
     * Get the utility function.
     *
     * @return
     */
    public DiscreteFunction getUtilityFunction()
    {
        return (utilityFunction);
    }

    /**
     * Set a probability variable given its constituents.
     *
     * @param index
     * @param vec
     * @param name
     * @param v
     */
    public void setProbabilityVariable(int index, String name,
                                       String v[], ArrayList vec)
    {
        if (index <= probabilityVariables.length)
        {
            probabilityVariables[index] =
            new ProbabilityVariable(this, name, index, v, vec);
        }
    }

    /**
     * Set a probability function given its constituents.
     *
     * @param index
     * @param variables
     * @param vec
     * @param values
     */
    public void setProbabilityFunction(int index,
                                       ProbabilityVariable[] variables,
                                       double values[], ArrayList vec)
    {
        if (index <= probabilityFunctions.length)
        {
            probabilityFunctions[index] =
            new ProbabilityFunction(this, variables, values, vec);
        }
    }

    /**
     * Set a probability variable given its index.
     *
     * @param index
     * @param pV
     */
    public void setProbabilityVariable(int index, ProbabilityVariable pV)
    {
        pV.bn = this;
        pV.index = index;
        probabilityVariables[index] = pV;
    }

    /**
     * Set a probability variable given its index.
     *
     * @param index
     * @param pF
     */
    public void setProbabilityFunction(int index, ProbabilityFunction pF)
    {
        pF.bn = this;
        probabilityFunctions[index] = pF;
    }

    /**
     * Set the vector of probability variables.
     *
     * @param pvs
     */
    public void setProbabilityVariables(ProbabilityVariable pvs[])
    {
        probabilityVariables = pvs;
    }

    /**
     * Set the vector of probability functions.
     *
     * @param pfs
     */
    public void setProbabilityFunctions(ProbabilityFunction pfs[])
    {
        probabilityFunctions = pfs;
    }
}
