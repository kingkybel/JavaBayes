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
     * @param name          network name
     * @param numberOfFuncs number of functions in the Bayesian network
     * @param numberOfVars  number of variables in the Bayesian network
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
     * @param name       network name
     * @param properties list of properties
     */
    public QuasiBayesNet(String name, ArrayList<String> properties)
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

    @Override
    protected void translate(InterchangeFormat interchangeFmt)
    {
        QBConvertInterchangeFormat qbcbn = new QBConvertInterchangeFormat(
                                   interchangeFmt);
        setName(qbcbn.getName());
        setProperties(qbcbn.getProperties());
        setProbabilityVariables(qbcbn.getProbabilityVariables(this));
        setProbabilityFunctions(qbcbn.getProbabilityFunctions(this));

        qbcbn.getFunctionsAsTables(this);

        // Process QuasiBayesNet properties
        processProperties();

        // Process ProbabilityVariable properties
        for (int i = 0; i < getProbabilityVariables().length; i++)
        {
            processProbabilityVariableProperties(i);
        }

        // Process ProbabilityFunction properties: create QB functions if necessary
        for (int i = 0; i < numberProbabilityFunctions(); i++)
        {
            processProbabilityFunctionProperties(i);
        }
    }

    @Override
    protected void processProperties()
    {
        boolean isPropertyValueAvailable;
        String propertyValue, keyword, token;
        StringTokenizer st;
        String delimiters = " \n\t\r\f";
        ArrayList<String> propertiesToRemove = new ArrayList<>();

        // Go through the properties
        for (String property : getProperties())
        {
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

        for (String prop2Remove : propertiesToRemove)
        {
            removeProperty(prop2Remove);
        }
    }

    /**
     * Indicate whether or not there are local credal sets defined in the
     * network.
     *
     * @return true if so, false otherwise
     */
    public boolean areLocalCredalSetsPresent()
    {
        for (ProbabilityFunction probabilityFunction : getProbabilityFunctions())
        {
            if (probabilityFunction instanceof QBProbabilityFunction)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void print(PrintStream out)
    {
        int i;

        out.println("// Bayesian network ");
        if (getName() != null)
        {
            out.print("network \"" + getName() + "\" {");
        }
        if (numberVariables() != INVALID_INDEX)
        {
            out.print(" //" + numberVariables() + " variables");
        }
        if (numberProbabilityFunctions() != INVALID_INDEX)
        {
            out.print(" and " + numberProbabilityFunctions() +
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
            for (String property : getProperties())
            {
                out.println("\tproperty \"" + property + "\" ;");
            }
        }
        out.println("}");

        if (numberVariables() != INVALID_INDEX)
        {
            for (i = 0; i < numberVariables(); i++)
            {
                getVariable(i).print(out);
            }
        }
        if (numberProbabilityFunctions() != INVALID_INDEX)
        {
            for (i = 0; i < numberProbabilityFunctions(); i++)
            {
                getFunction(i).print(out);
            }
        }
    }

    /**
     * Get the type of global neighbourhood.
     *
     * @return the type of global neighbourhood
     */
    public GlobalNeighbourhood getGlobalNeighborhoodType()
    {
        return globalNeighborhoodType;
    }

    /**
     * Set the type of global neighbourhood.
     *
     * @param type the new type of global neighbourhood
     */
    public void setGlobalNeighborhoodType(GlobalNeighbourhood type)
    {
        globalNeighborhoodType = type;
    }

    /**
     * Get the parameter for the global neighbourhood modeled by the network.
     *
     * @return he parameter for the global neighbourhood
     */
    public double getGlobalNeighborhoodParameter()
    {
        return globalNeighborhoodParameter;
    }

    /**
     * Set the parameter for the global neighbourhood modeled by the network.
     *
     * @param p the parameter for the global neighbourhood
     */
    public void setGlobalNeighborhoodParameter(double p)
    {
        globalNeighborhoodParameter = p;
    }

}
