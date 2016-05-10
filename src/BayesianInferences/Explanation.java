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
 * In FULL_EXPLANATION all variables are explanation variables except variables
 * that are observed.
 *
 * In EXPLANATION only variables that are marked as explanation variables are
 * used in the maximization; an observed variable is not used even if it is
 * marked as explanation variable. Note that in EXPLANATION mode, if there are
 * no variables marked as explanation variables, the final result is the
 * posterior marginal.
 */
public class Explanation
{

    private static final String CLASS_NAME = Explanation.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    BayesNet bn;
    BucketTree bucketTree;
    ProbabilityFunction results[];

    /**
     * Constructor for an Explanation.
     *
     * @param bN
     */
    public Explanation(BayesNet bN)
    {
        bn = bN;
    }

    /**
     * Calculation of an Explanation.
     */
    public void explanation()
    {
        explanation(Inference.EXPLANATION);
    }

    /**
     * Calculation of a full Explanation.
     */
    public void fullExplanation()
    {
        explanation(Inference.FULL_EXPLANATION);
    }

    /**
     * Calculation of an Explanation accordingly to the flag explanationStatus.
     *
     * @param explanationStatus
     */
    public void explanation(int explanationStatus)
    {
        bucketTree = new BucketTree(new Ordering(bn, (String) null,
                                                 explanationStatus,
                                                 Ordering.MINIMUM_WEIGHT));
        doInferenceFromBucketTree();
    }

    /**
     * Calculation of an Explanation given order.
     *
     * @param order
     */
    public void explanation(String order[])
    {
        explanation(order, Inference.EXPLANATION);
    }

    /**
     * Calculation of a full Explanation given order.
     *
     * @param order
     */
    public void fullExplanation(String order[])
    {
        explanation(order, Inference.FULL_EXPLANATION);
    }

    /**
     * Calculation of an Explanation accordingly to the flag explanationStatus.
     *
     * @param order
     * @param explanationStatus
     */
    public void explanation(String order[], int explanationStatus)
    {
        bucketTree =
        new BucketTree(new Ordering(bn, order, explanationStatus));
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
     * @param out
     */
    public void print(PrintStream out)
    {
        print(out, (boolean) true);
    }

    /**
     * Print Explanation.
     *
     * @param shouldPrintBucketTree
     */
    public void print(boolean shouldPrintBucketTree)
    {
        print(System.out, shouldPrintBucketTree);
    }

    /**
     * Print Explanation.
     *
     * @param out
     * @param shouldPrintBucketTree
     */
    public void print(PrintStream out, boolean shouldPrintBucketTree)
    {
        int i, bp[];
        ProbabilityVariable pv;

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
            for (i = 0; i < results.length; i++)
            {
                results[i].print(out);
            }
        }
        else
        {
            bp = bucketTree.backwardPointers;
            for (i = 0; i < bp.length; i++)
            {
                if (bp[i] != BayesNet.INVALID_INDEX)
                {
                    pv = bn.getProbabilityVariable(i);
                    out.println("Variable " + pv.getName() + ": " +
                                pv.getValue(bp[i]));
                }
            }
        }
    }

    /* ************************************************************* */
    /* Methods that allow basic manipulation of non-public variables */
    /* ************************************************************* */
    /**
     * Get the results in the Explanation.
     *
     * @return
     */
    public ProbabilityFunction[] getResults()
    {
        return (results);
    }
}
