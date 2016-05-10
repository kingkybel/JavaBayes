/**
 * DSeparation.java
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
package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.ProbabilityFunction;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

class DSeparation
{

    private final static int CONNECTED_VARIABLES = 0;
    private final static int AFFECTING_VARIABLES = 1;
    private static final Logger LOG =
    Logger.getLogger(DSeparation.class.getName());
    BayesNet bn;
    boolean[] above;
    boolean[] below;

    /**
     * Constructor for DSeparation object.
     */
    DSeparation(BayesNet bN)
    {
        bn = bN;
    }

    /**
     * Return a list of all variables that are d-connected to a given variable.
     */
    public ArrayList allConnected(int x)
    {
        return (separation(x, CONNECTED_VARIABLES));
    }

    /**
     * Returns a list of all variables whose distributions can affect the
     * marginal posterior of a given variable.
     */
    public ArrayList allAffecting(int x)
    {
        return (separation(x, AFFECTING_VARIABLES));
    }

    /*
     * Find all d-separation relations.
     */
    private void separationRelations(int x, int flag)
    {
        int nvertices = bn.numberProbabilityFunctions();
        if (flag == AFFECTING_VARIABLES)
        {
            nvertices += nvertices;
        }

        boolean ans = false;

        above = new boolean[nvertices];
        below = new boolean[nvertices];

        int current[] = new int[2];

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
                    if (adj(i, v, flag))
                    {
                        if ((!below[i]) && (!isSeparator(i, flag)))
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
                    if (adj(v, j, flag))
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
                if (isSeparator(v, flag))
                {  // v known
                    for (i = 0; i < nvertices; i++)
                    {
                        if (adj(i, v, flag))
                        {
                            if ((!isSeparator(i, flag)) && !below[i])
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
                    for (j = 0; j < nvertices;
                         j++)
                    {
                        if (adj(v, j, flag))
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

    /*
     * Run the separation algorithm and process its results.
     */
    private ArrayList separation(int x, int flag)
    {
        int i;
        int nvertices = bn.numberProbabilityFunctions();
        ArrayList dSeparatedVariables = new ArrayList();

        // Run algorithm
        separationRelations(x, flag);

        // Process results
        if (flag == CONNECTED_VARIABLES)
        {
            for (i = 0; i < nvertices; i++)
            {
                if (below[i] || above[i])
                {
                    dSeparatedVariables.add(bn.getProbabilityVariable(i));
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
                            bn.getProbabilityVariable(i - nvertices));
                }
            }
        }

        return (dSeparatedVariables);
    }

    /*
     * Check whether the variable given by the index is in the
     * list of separators (i.e., it is observed).
     */
    private boolean isSeparator(int i, int flag)
    {
        if ((flag == CONNECTED_VARIABLES) ||
            ((flag == AFFECTING_VARIABLES) &&
             (i < bn.numberProbabilityFunctions())))
        {
            return (bn.getProbabilityVariable(i).isObserved());
        }
        else
        {
            return (false);
        }
    }

    /*
     * Check whether there is a link from variable indexFrom to
     * variable indexTo.
     */
    private boolean adj(int indexFrom, int indexTo, int flag)
    {
        ProbabilityFunction pf = null;

        if ((flag == CONNECTED_VARIABLES) ||
            ((flag == AFFECTING_VARIABLES) &&
             (indexTo < bn.numberProbabilityFunctions()) &&
             (indexFrom < bn.numberProbabilityFunctions())))
        {

            for (int i = 0; i < bn.numberProbabilityFunctions(); i++)
            {
                if (bn.getProbabilityFunction(i).getIndex(0) == indexTo)
                {
                    pf = bn.getProbabilityFunction(i);
                    break;
                }
            }
            if (pf == null)
            {
                return (false);
            }

            for (int i = 1; i < pf.numberVariables(); i++)
            {
                if (pf.getIndex(i) == indexFrom)
                {
                    return (true);
                }
            }
            return (false);
        }
        else
        {
            if ((indexFrom - indexTo) == bn.numberProbabilityFunctions())
            {
                return (true);
            }
            else
            {
                return (false);
            }
        }
    }
}
