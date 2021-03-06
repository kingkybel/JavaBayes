/*
 * Expectation.java
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
package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * There are two cases to be considered: EXPLANATION or FULL_EXPLANATION the
 * only difference is which variables are considered explanation variables.
 *
 * In ALL_NOT_OBSERVED_VARIABLES all variables are explanation variables except
 * variables that are observed.
 *
 * In MARKED_VARIABLES_ONLY only variables that are marked as explanation
 * variables are used in the maximisation; an observed variable is not used,
 * even if it is marked as explanation variable. Note that in EXPLANATION mode,
 * if there are no variables marked as explanation variables, the final result
 * is the posterior marginal.
 */
public class Explanation
{

    private static final Class CLAZZ = Explanation.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    BayesNet bayesNet;
    BucketTree bucketTree;
    ProbabilityFunction results[];

    /**
     * Constructor for an Explanation.
     *
     * @param bayesNet the underlying Bayesian network
     */
    public Explanation(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    /**
     * Calculation of an Explanation.
     */
    public void explanation()
    {
        explanation(ExplanationType.MARKED_VARIABLES_ONLY);
    }

    /**
     * Calculation of a full Explanation.
     */
    public void fullExplanation()
    {
        explanation(ExplanationType.ALL_NOT_OBSERVED_VARIABLES);
    }

    /**
     * Calculation of an Explanation accordingly to the flag explanationStatus.
     *
     * @param explanationStatus what to use for explanation
     */
    public void explanation(ExplanationType explanationStatus)
    {
        bucketTree = new BucketTree(new Ordering(bayesNet,
                                                 (String) null,
                                                 explanationStatus,
                                                 Ordering.Type.MINIMUM_WEIGHT));
        doInferenceFromBucketTree();
    }

    /**
     * Calculation of an Explanation given order.
     *
     * @param order order of variables given as array of their names
     */
    public void explanation(String order[])
    {
        explanation(order, ExplanationType.MARKED_VARIABLES_ONLY);
    }

    /**
     * Calculation of a full Explanation given order.
     *
     * @param order order of variables given as array of their names
     */
    public void fullExplanation(String order[])
    {
        explanation(order, ExplanationType.ALL_NOT_OBSERVED_VARIABLES);
    }

    /**
     * Calculation of an Explanation accordingly to the flag explanationStatus.
     *
     * @param order             order of variables given as array of their names
     * @param explanationStatus what to use for explanation
     */
    public void explanation(String order[],
                            ExplanationType explanationStatus)
    {
        bucketTree =
        new BucketTree(new Ordering(bayesNet, order, explanationStatus));
        doInferenceFromBucketTree();
    }

    /**
     * Do the Explanation.
     */
    void doInferenceFromBucketTree()
    {
        results = new ProbabilityFunction[1];
        bucketTree.reduce();
        results[0] = bucketTree.getNormalizedResult();
    }

    /**
     * Print Explanation.
     */
    public void print()
    {
        print(System.out, (boolean) true);
    }

    /**
     * Print Explanation.
     *
     * @param out output print stream
     */
    public void print(PrintStream out)
    {
        print(out, (boolean) true);
    }

    /**
     * Print Explanation.
     *
     * @param shouldPrintBucketTree if true, then print the bucket tree, if
     *                              false don't
     */
    public void print(boolean shouldPrintBucketTree)
    {
        print(System.out, shouldPrintBucketTree);
    }

    /**
     * Print Explanation.
     *
     * @param out                   output print stream
     * @param shouldPrintBucketTree if true, then print the bucket tree, if
     *                              false don't
     */
    public void print(PrintStream out, boolean shouldPrintBucketTree)
    {
        ProbabilityVariable probVar;

        // Do explanation if Explanation is null.
        if (results == null)
        {
            explanation();
        }

        // Print it all.
        out.println("Explanation:");

        if (shouldPrintBucketTree == true)
        {
            bucketTree.print(out);
        }

        if (bucketTree.backwardPointers == null)
        {
            out.println("No explanatory variable; posterior distribution:");
            for (ProbabilityFunction result : results)
            {
                result.print(out);
            }
        }
        else
        {
            int bp[] = bucketTree.backwardPointers;
            for (int i = 0; i < bp.length; i++)
            {
                if (bp[i] != BayesNet.INVALID_INDEX)
                {
                    probVar = bayesNet.getProbabilityVariable(i);
                    out.println("Variable " + probVar.getName() + ": " +
                                probVar.getValue(bp[i]));
                }
            }
        }
    }

    /**
     * Get the results in the Explanation.
     *
     * @return the results
     */
    public ProbabilityFunction[] getResults()
    {
        return results;
    }
}
