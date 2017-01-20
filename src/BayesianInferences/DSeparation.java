/*
 * DSeparation.java
 *
 * @author Fabio G. Cozman
 * Copyright 1996 - 1999, Fabio G. Cozman, Carnergie Mellon University,
 * Universidade de Sao Paulo fgcozman@usp.br,
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
package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * d-separation is a criterion for deciding, from a given a causal graph,
 * whether a set X of variables is independent of another set Y, given a third
 * set Z. The idea is to associate "dependence" with "connectedness" (i.e., the
 * existence of a connecting path) and "independence" with "unconnectedness" or
 * "separation". The only twist on this simple idea is to define what we mean by
 * "connecting path", given that we are dealing with a system of directed arrows
 * in which some vertices (those residing in Z) correspond to measured
 * variables, whose values are known precisely. To account for the orientations
 * of the arrows we use the terms "d-separated" and "d-connected" (d connotes
 * "directional").
 *
 * http://bayes.cs.ucla.edu/BOOK-2K/d-sep.html
 *
 */
public class DSeparation
{

    private static final Class CLAZZ = DSeparation.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    BayesNet bayesNet;
    boolean[] above;
    boolean[] below;

    /**
     * Constructor for DSeparation object.
     *
     * @param bayesNet the underlying Bayesian network
     */
    public DSeparation(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    /**
     * Return a list of all variables that are d-connected to a given variable.
     *
     * @param varIndex index of the variable to test
     * @return the list of d-connected variables
     */
    public ArrayList<DiscreteVariable> getDConnectedVariables(int varIndex)
    {
        return separation(varIndex, ConnectionType.CONNECTED_VARIABLES);
    }

    /**
     * Returns a list of all variables whose distributions can affect the
     * marginal posterior of a given variable.
     *
     * @param varIndex index of the variable to test
     * @return the list of affecting variables
     */
    public ArrayList<DiscreteVariable> getAllAffectingVariables(int varIndex)
    {
        return separation(varIndex, ConnectionType.AFFECTING_VARIABLES);
    }

    /**
     * Find all d-separation relations.
     *
     * @param index          index of the variable to test
     * @param connectionType type of connection to use
     */
    private void separationRelations(int index, ConnectionType connectionType)
    {
        int nvertices = bayesNet.numberProbabilityFunctions();
        if (connectionType == ConnectionType.AFFECTING_VARIABLES)
        {
            nvertices += nvertices;
        }

        above = new boolean[nvertices];
        below = new boolean[nvertices];

        int current[];

        int v, subscript;

        for (int vertexInd = 0; vertexInd < nvertices; vertexInd++)
        {
            above[vertexInd] = false;
            below[vertexInd] = false;
        }

        Stack stack = new Stack();

        int Xabove[] =
        {
            index, 1
        };
        int Xbelow[] =
        {
            index, -1
        };

        stack.push(Xabove);
        stack.push(Xbelow);

        below[index] = true;
        above[index] = true;

        while (!stack.empty())
        {
            current = (int[]) stack.pop();
            v = current[0];
            subscript = current[1];

            if (subscript < 0)
            {
                for (int vertexInd = 0; vertexInd < nvertices; vertexInd++)
                {
                    if (adjacent(vertexInd, v, connectionType))
                    {
                        if ((!below[vertexInd]) &&
                            (!isSeparator(vertexInd, connectionType)))
                        {
                            below[vertexInd] = true;
                            int Vbelow[] =
                            {
                                vertexInd, -1
                            };
                            stack.push(Vbelow);
                        }
                    }
                }
                for (int vertexInd = 0; vertexInd < nvertices; vertexInd++)
                {
                    if (adjacent(v, vertexInd, connectionType))
                    {
                        if (!above[vertexInd])
                        {
                            above[vertexInd] = true;
                            int Tabove[] =
                            {
                                vertexInd, 1
                            };
                            stack.push(Tabove);
                        }
                    }
                }
                above[v] = true;
            }  // subscript < 0
            else
            {
                if (isSeparator(v, connectionType))
                {  // v known
                    for (int vertexInd = 0; vertexInd < nvertices; vertexInd++)
                    {
                        if (adjacent(vertexInd, v, connectionType))
                        {
                            if (!isSeparator(vertexInd, connectionType) &&
                                !below[vertexInd])
                            {
                                below[vertexInd] = true;
                                int Tbelow[] =
                                {
                                    vertexInd, -1
                                };
                                stack.push(Tbelow);
                            }
                        }
                    }
                }
                else
                {
                    for (int vertexInd = 0; vertexInd < nvertices; vertexInd++)
                    {
                        if (adjacent(v, vertexInd, connectionType))
                        {
                            if (!above[vertexInd])
                            {
                                above[vertexInd] = true;
                                int Sabove[] =
                                {
                                    vertexInd, 1
                                };
                                stack.push(Sabove);
                            }
                        }
                    }
                }
            } // subscript >= 0
        }  // while

    }

    /**
     * Run the separation algorithm and process its results.
     *
     * @param varIndex       index of the variable to test
     * @param connectionType type of connection to use
     * @return a list of discrete separation variables
     */
    private ArrayList<DiscreteVariable> separation(int varIndex,
                                                   ConnectionType connectionType)
    {
        int nvertices = bayesNet.numberProbabilityFunctions();
        ArrayList<DiscreteVariable> dSeparatedVariables = new ArrayList<>();

        // Run algorithm
        separationRelations(varIndex, connectionType);

        // Process results
        if (connectionType == ConnectionType.CONNECTED_VARIABLES)
        {
            for (int vertexInd = 0; vertexInd < nvertices; vertexInd++)
            {
                if (below[vertexInd] || above[vertexInd])
                {
                    dSeparatedVariables.add(
                            bayesNet.getProbabilityVariable(vertexInd));
                }
            }
        }
        else
        {
            for (int vertexInd = nvertices;
                 vertexInd < (nvertices + nvertices);
                 vertexInd++)
            {
                if (below[vertexInd] || above[vertexInd])
                {
                    dSeparatedVariables.add(
                            bayesNet.getProbabilityVariable(vertexInd -
                                                            nvertices));
                }
            }
        }

        return dSeparatedVariables;
    }

    /**
     * Check whether the variable given by the index is in the list of
     * separators (i.e., it is observed).
     *
     * @param varIndex       index of the variable to test
     * @param connectionType type of connection to use
     * @return true if the variable at varIndex is a separator
     */
    private boolean isSeparator(int varIndex, ConnectionType connectionType)
    {
        if ((connectionType == ConnectionType.CONNECTED_VARIABLES) ||
            ((connectionType == ConnectionType.AFFECTING_VARIABLES) &&
             (varIndex < bayesNet.numberProbabilityFunctions())))
        {
            return bayesNet.getProbabilityVariable(varIndex).isObserved();
        }
        else
        {
            return false;
        }
    }

    /**
     * Check whether there is a link from variable indexFrom to variable
     * indexTo.
     *
     * @param indexFrom      index of the source variable
     * @param indexTo        index of the target variable
     * @param connectionType the type of connection we are querying
     * @return true if so, false otherwise
     */
    private boolean adjacent(int indexFrom,
                             int indexTo,
                             ConnectionType connectionType)
    {
        ProbabilityFunction probFunc = null;

        if ((connectionType == ConnectionType.CONNECTED_VARIABLES) ||
            ((connectionType == ConnectionType.AFFECTING_VARIABLES) &&
             (indexTo < bayesNet.numberProbabilityFunctions()) &&
             (indexFrom < bayesNet.numberProbabilityFunctions())))
        {

            for (int i = 0; i < bayesNet.numberProbabilityFunctions(); i++)
            {
                if (bayesNet.getProbabilityFunction(i).getIndex(0) == indexTo)
                {
                    probFunc = bayesNet.getProbabilityFunction(i);
                    break;
                }
            }
            if (probFunc == null)
            {
                return false;
            }

            for (int i = 1; i < probFunc.numberVariables(); i++)
            {
                if (probFunc.getIndex(i) == indexFrom)
                {
                    return true;
                }
            }
            return false;
        }
        else
        {
            return indexFrom - indexTo == bayesNet.numberProbabilityFunctions();
        }
    }

    /**
     * Type of the connection we are investigating.
     */
    public enum ConnectionType
    {

        /**
         * Variables are connected to a (set of) variables.
         */
        CONNECTED_VARIABLES,
        /**
         * Variables are affecting a (set of) variables.
         */
        AFFECTING_VARIABLES
    }
}
