/*
 * QuasiBayesNet.java
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
package QuasiBayesianNetworks;

import BayesianNetworks.BayesNet;
import BayesianNetworks.ProbabilityFunction;
import CredalSets.QBProbabilityFunction;
import InterchangeFormat.InterchangeFormat;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class QuasiBayesNet extends BayesNet
{

    private static final Class CLAZZ = QuasiBayesNet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // do not set this here; set by translate()
    GlobalNeighbourhood globalNeighborhoodType =
                        GlobalNeighbourhood.NO_CREDAL_SET;
    // do not set this here; set by translate()
    double globalNeighborhoodParameter = 0.0;

    /**
     * Default constructor for a QuasiBayesian network.
     */
    public QuasiBayesNet()
    {
        super();
        globalNeighborhoodType = GlobalNeighbourhood.NO_CREDAL_SET;
        globalNeighborhoodParameter = 0.0;
    }

    /**
     * Simple constructor for a Quasi-Bayesian network: just give it the number
     * of variables/functions and the name of the network.
     *
     * @param name
     * @param numberOfFuncs
     * @param numberOfVars  number of variables
     */
    public QuasiBayesNet(String name, int numberOfVars, int numberOfFuncs)
    {
        super(name, numberOfVars, numberOfFuncs);
        globalNeighborhoodType = GlobalNeighbourhood.NO_CREDAL_SET;
        globalNeighborhoodParameter = 0.0;
    }

    /**
     * Simple constructor for a Quasi-Bayesian network: just give it the name of
     * network and properties.
     *
     * @param name
     * @param properties list of properties
     */
    public QuasiBayesNet(String name, ArrayList properties)
    {
        super(name, properties);
        globalNeighborhoodType = GlobalNeighbourhood.NO_CREDAL_SET;
        globalNeighborhoodParameter = 0.0;
    }

    /**
     * Simple constructor for a Quasi-Bayesian network: just give it a Bayesian
     * Network and it creates a new copy
     *
     * @param bayesNet the underlying Bayesian network
     */
    public QuasiBayesNet(BayesNet bayesNet)
    {
        super(bayesNet);
        if (bayesNet instanceof QuasiBayesNet)
        {
            globalNeighborhoodType =
            ((QuasiBayesNet) bayesNet).globalNeighborhoodType;
            globalNeighborhoodParameter =
            ((QuasiBayesNet) bayesNet).globalNeighborhoodParameter;
        }
    }

    /**
     * Constructor for a Quasi-Bayesian network from a string.
     *
     * @param networkDescription
     * @throws Exception
     */
    public QuasiBayesNet(String networkDescription) throws Exception
    {
        super(networkDescription);
    }

    /**
     * Constructor for a Bayesian network from an input stream.
     *
     * @param istream
     * @throws Exception
     */
    public QuasiBayesNet(InputStream istream) throws Exception
    {
        super(istream);
    }

    /**
     * Constructor for a Bayesian network from a URL.
     *
     * @param context
     * @param spec
     * @throws Exception
     */
    public QuasiBayesNet(URL context, String spec) throws Exception
    {
        super(context, spec);
    }

    /**
     * Constructor for a Bayesian network from a URL.
     *
     * @param url
     * @throws Exception
     */
    public QuasiBayesNet(URL url) throws Exception
    {
        super(url);
    }

    /**
     * Method that translates the contents of a IFBayesNet object into a
     * QuasiBayesNet object (method works by overriding method in BayesNet
     * class). The method makes modifications to the basic objects supported by
     * the InterchangeFormat, so that the full functionality of the
     * BayesianNetworks package can be used. As the InterchangeFormat evolves,
     * probably some of the objects created through extensions will be created
     * directly by the parser as it parses an InterchangeFormat stream. Right
     * now the extensions involve:
     * <ol>
     * <li>Detecting observed variables</li>
     * <li>Detecting explanation variables</li>
     * </ol>
     *
     * @param interchangeFmt
     */
    @Override
    protected void translate(InterchangeFormat interchangeFmt)
    {
        QBConvertInterchangeFormat qbcbn = new QBConvertInterchangeFormat(
                                   interchangeFmt);
        setName(qbcbn.getName());
        setProperties(qbcbn.getProperties());
        setProbabilityVariables(qbcbn.getProbabilityVariables(this));
        probabilityFunctions = qbcbn.getProbabilityFunctions(this);

        qbcbn.getFunctionsAsTables(this);

        // Process QuasiBayesNet properties
        processProperties();

        // Process ProbabilityVariable properties
        for (int i = 0; i < getProbabilityVariables().length; i++)
        {
            processProbabilityVariableProperties(i);
        }

        // Process ProbabilityFunction properties: create QB functions if necessary
        for (int i = 0; i < probabilityFunctions.length; i++)
        {
            processProbabilityFunctionProperties(i);
        }
    }

    /**
     * Method that makes modifications to the QuasiBayesNet based on its
     * properties (works by overriding method in BayesNet).
     */
    @Override
    protected void processProperties()
    {
        boolean isPropertyValueAvailable;
        String property, propertyValue, keyword, token;
        StringTokenizer st;
        String delimiters = " \n\t\r\f";
        ArrayList propertiesToRemove = new ArrayList();

        // Go through the properties
        for (Object e : getProperties())
        {
            property = (String) (e);
            st = new StringTokenizer(property, delimiters);

            // Extension: global neighborhoods
            token = st.nextToken();
            keyword = GlobalNeighbourhood.CREDAL_SET.toString();
            if (!token.equals(keyword))
            {
                continue;
            }

            // The credal-set property is removed
            propertiesToRemove.add(property);

            // Cycle through keywords for global neighborhood type
            token = st.nextToken();
            globalNeighborhoodType = GlobalNeighbourhood.fromString(token);
            isPropertyValueAvailable =
            globalNeighborhoodType != GlobalNeighbourhood.NO_CREDAL_SET;
//            for (int i = 2; i < 6; i++)
//            {
//                keyword = globalNeighborhoodKeywords[i];
//                if (token.equals(keyword))
//                {
//                    globalNeighborhoodType = i;
//                    isPropertyValueAvailable = true;
//                    break;
//                }
//            }

            // Get the property if necessary
            if (isPropertyValueAvailable)
            {
                propertyValue = st.nextToken();
                globalNeighborhoodParameter =
                Double.valueOf(propertyValue);
            }
        }

        for (Object e : propertiesToRemove)
        {
            removeProperty((String) (e));
        }
    }

    /**
     * Indicate whether or not there are local credal sets defined in the
     * network.
     *
     * @return
     */
    public boolean areLocalCredalSetsPresent()
    {
        for (ProbabilityFunction probabilityFunction : probabilityFunctions)
        {
            if (probabilityFunction instanceof QBProbabilityFunction)
            {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Print method for a QuasiBayesNet object.
     *
     * @param out output print stream
     */
    @Override
    public void print(PrintStream out)
    {
        int i;
        String property;

        out.println("// Bayesian network ");
        if (getName() != null)
        {
            out.print("network \"" + getName() + "\" {");
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

        if (globalNeighborhoodType != GlobalNeighbourhood.NO_CREDAL_SET)
        {
            out.println("\tproperty \"" +
                        GlobalNeighbourhood.CREDAL_SET + " " +
                        globalNeighborhoodType +
                        " " +
                        globalNeighborhoodParameter + "\" ;");
        }

        if (hasProperties())
        {
            for (Object e : getProperties())
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
     * Get the type of global neighborhood.
     *
     * @return
     */
    public GlobalNeighbourhood getGlobalNeighborhoodType()
    {
        return (globalNeighborhoodType);
    }

    /**
     * Set the type of global neighborhood.
     *
     * @param type
     */
    public void setGlobalNeighborhoodType(GlobalNeighbourhood type)
    {
        globalNeighborhoodType = type;
    }

    /**
     * Get the parameter for the global neighborhood modeled by the network.
     *
     * @return
     */
    public double getGlobalNeighborhoodParameter()
    {
        return (globalNeighborhoodParameter);
    }

    /**
     * Set the parameter for the global neighborhood modeled by the network.
     *
     * @param p
     */
    public void setGlobalNeighborhoodParameter(double p)
    {
        globalNeighborhoodParameter = p;
    }

}
