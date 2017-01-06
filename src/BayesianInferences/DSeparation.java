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

    public enum ConnectionType
    {

        CONNECTED_VARIABLES, AFFECTING_VARIABLES;
    }
    BayesNet bayesNet;
    boolean[] above;
    boolean[] below;

    /**
     * Constructor for DSeparation object.
     *
     * @param bayesNet
     */
    public DSeparation(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    /**
     * Return a list of all variables that are d-connected to a given variable.
     *
     * @param x index of the variable to test
     * @return
     */
    public ArrayList<DiscreteVariable> getDConnectedVariables(int x)
    {
        return separation(x, ConnectionType.CONNECTED_VARIABLES);
    }

    /**
     * Returns a list of all variables whose distributions can affect the
     * marginal posterior of a given variable.
     *
     * @param x index of the variable to test
     * @return
     */
    public ArrayList<DiscreteVariable> getAllAffectingVariables(int x)
    {
        return separation(x, ConnectionType.AFFECTING_VARIABLES);
    }

    /**
     * Find all d-separation relations.
     *
     * @param x              index of the variable to test
     * @param connectionType
     */
    private void separationRelations(int x, ConnectionType connectionType)
    {
        int nvertices = bayesNet.numberProbabilityFunctions();
        if (connectionType == ConnectionType.AFFECTING_VARIABLES)
        {
            nvertices += nvertices;
        }

        above = new boolean[nvertices];
        below = new boolean[nvertices];

        int current[];

        int i, j, v, subscript;

        for (i = 0; i < nvertices; i++)
        {
            above[i] = false;
            below[i] = false;
        }

        Stack stack = new Stack();

        int Xabove[] =
        {
            x, 1
        };
        int Xbelow[] =
        {
            x, -1
        };

        stack.push(Xabove);
        stack.push(Xbelow);

        below[x] = true;
        above[x] = true;

        while (!stack.empty())
        {
            current = (int[]) stack.pop();
            v = current[0];
            subscript = current[1];

            if (subscript < 0)
            {
                for (i = 0; i < nvertices; i++)
                {
                    if (adj(i, v, connectionType))
                    {
                        if ((!below[i]) && (!isSeparator(i, connectionType)))
                        {
                            below[i] = true;
                            int Vbelow[] =
                            {
                                i, -1
                            };
                            stack.push(Vbelow);
                        }
                    }
                }
                for (j = 0; j < nvertices; j++)
                {
                    if (adj(v, j, connectionType))
                    {
                        if (!above[j])
                        {
                            above[j] = true;
                            int Tabove[] =
                            {
                                j, 1
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
                    for (i = 0; i < nvertices; i++)
                    {
                        if (adj(i, v, connectionType))
                        {
                            if ((!isSeparator(i, connectionType)) && !below[i])
                            {
                                below[i] = true;
                                int Tbelow[] =
                                {
                                    i, -1
                                };
                                stack.push(Tbelow);
                            }
                        }
                    }
                }
                else
                {
                    for (j = 0; j < nvertices; j++)
                    {
                        if (adj(v, j, connectionType))
                        {
                            if (!above[j])
                            {
                                above[j] = true;
                                int Sabove[] =
                                {
                                    j, 1
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
     * @param x              index of the variable to test
     * @param connectionType
     * @return
     */
    private ArrayList<DiscreteVariable> separation(int x,
                                                   ConnectionType connectionType)
    {
        int i;
        int nvertices = bayesNet.numberProbabilityFunctions();
        ArrayList<DiscreteVariable> dSeparatedVariables = new ArrayList<>();

        // Run algorithm
        separationRelations(x, connectionType);

        // Process results
        if (connectionType == ConnectionType.CONNECTED_VARIABLES)
        {
            for (i = 0; i < nvertices; i++)
            {
                if (below[i] || above[i])
                {
                    dSeparatedVariables.add(bayesNet.getProbabilityVariable(i));
                }
            }
        }
        else
        {
            for (i = nvertices; i < (nvertices + nvertices); i++)
            {
                if (below[i] || above[i])
                {
                    dSeparatedVariables.add(
                            bayesNet.getProbabilityVariable(i - nvertices));
                }
            }
        }

        return dSeparatedVariables;
    }

    /**
     * Check whether the variable given by the index is in the list of
     * separators (i.e., it is observed).
     *
     * @param varIndex
     * @param connectionType
     * @return
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
     * @return
     */
    private boolean adj(int indexFrom,
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
            return (indexFrom - indexTo) == bayesNet.
                   numberProbabilityFunctions();
        }
    }
}
