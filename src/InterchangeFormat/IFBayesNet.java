/*
 * IFBayesNet.java
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
package InterchangeFormat;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class IFBayesNet
{

    private static final Class CLAZZ = IFBayesNet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    String name;
    ArrayList<String> properties;
    ArrayList<IFProbabilityVariable> probVars;
    ArrayList<IFProbabilityFunction> probFuncs;

    /**
     * Basic constructor.
     *
     * @param name
     * @param properties list of properties
     */
    public IFBayesNet(String name, ArrayList<String> properties)
    {
        this.name = name;
        this.properties = properties;
        this.probVars = new ArrayList<>();
        this.probFuncs = new ArrayList<>();
    }

    /**
     * Retrieve the name of the Bayes net.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieve the properties as String list.
     *
     * @return the properties
     */
    public ArrayList<String> getProperties()
    {
        return properties;
    }

    /**
     * Retrieve the probability variables as String list.
     *
     * @return the probability variables as String list
     */
    public ArrayList<IFProbabilityVariable> getProbabilityVariables()
    {
        return probVars;
    }

    /**
     * Retrieve the probability functions as String list.
     *
     * @return the probability functions as String list
     */
    public ArrayList<IFProbabilityFunction> getProbabilityFunctions()
    {
        return probFuncs;
    }

    /**
     * Method that adds a ProbabilityVariable object to the vector of variables.
     *
     * @param probVar a probability variable
     */
    public void add(IFProbabilityVariable probVar)
    {
        probVars.add(probVar);
    }

    /**
     * Method that adds a IFProbabilityFunction object to the vector of
     * functions.
     *
     * @param probFunc probability function
     */
    public void add(IFProbabilityFunction probFunc)
    {
        probFuncs.add(probFunc);
    }

    /**
     * Method that inverts the tables in the ProbabilityFunction objects;
     * necessary for formats that put the GIVEN variables as the lowest running
     * indexes in the tables.
     */
    public void invertProbabilityTables()
    {
        for (IFProbabilityFunction probFunc : probFuncs)
        {
            probFunc.invertTables(this);
        }
    }
}
