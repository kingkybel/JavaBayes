/*
 * Inference.java
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
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class Inference
{

    /**
     *
     */
    protected static final int IGNORE_EXPLANATION = 0;

    /**
     *
     */
    protected static final int EXPLANATION = 1;

    /**
     *
     */
    protected static final int FULL_EXPLANATION = 2;
    private static final String CLASS_NAME = Inference.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     *
     */
    protected BayesNet bn;

    /**
     *
     */
    protected BucketTree bucketTree;

    /**
     *
     */
    protected Bucket bucketForVariable[];

    /**
     *
     */
    protected ArrayList bucketForest;

    /**
     *
     */
    protected ProbabilityFunction result;

    /**
     *
     */
    protected boolean doProduceClusters;

    /**
     * Constructor for an Inference.
     *
     * @param bN
     * @param dpc
     */
    public Inference(BayesNet bN, boolean dpc)
    {
        bn = bN;
        bucketForVariable = new Bucket[bN.numberVariables()];
        bucketForest = new ArrayList();
        doProduceClusters = dpc;
    }

    /**
     * Calculation of marginal posterior distribution.
     */
    public void inference()
    {
        inference((String) null);
    }

    /**
     * Calculation of marginal posterior distribution for an arbitrary BayesNet.
     *
     * @param queriedVariableName
     */
    protected void inference(String queriedVariableName)
    {
        if (doProduceClusters)
        { // If clusters are generated:
            int indexQueried = bn.indexOfVariable(queriedVariableName);
            if (indexQueried != BayesNet.INVALID_INDEX)
            { // If the queriedVariableName is valid:
                Bucket buck = bucketForVariable[indexQueried];
                // If the variable has no Bucket or a Bucket without valid cluster:
                if ((buck == null) || (buck.cluster == null))
                {
                    inference(new Ordering(bn, queriedVariableName,
                                           IGNORE_EXPLANATION,
                                           Ordering.MINIMUM_WEIGHT));
                }
                else
                { // If variable already has a Bucket:
                    // Get the BucketTree.
                    bucketTree = buck.bucketTree;
                    // Note that the method bucketTree.distribute() below must return true:
                    //     - the bucketTree is constructed with IGNORE_EXPLANATION.
                    //     - this block only runs if doProduceClusters is true.
                    if (buck.bucketStatus != Bucket.DISTRIBUTED)
                    {
                        if (buck ==
                            bucketTree.bucketTree[bucketTree.bucketTree.length -
                                                  1])
                        {
                            bucketTree.reduce(); // If Bucket is the last bucket, then just reduce;
                        }
                        else
                        {
                            // if not, then distribute.
                            bucketTree.distribute();
                        }
                    }
                    // Now process the cluster in the Bucket.
                    System.out.println("ARRIVED HERE!");
                    buck.reduceCluster();
                    // And then get the result
                    System.out.println("AND HERE TOO!");
                    result = bucketTree.getNormalizedResult();
                }
            }
            else
            { // If the queriedVariableName is invalid:
                inference(new Ordering(bn, (String) null,
                                       IGNORE_EXPLANATION,
                                       Ordering.MINIMUM_WEIGHT));
            }
        }
        else
        { // If no cluster is generated:
            inference(new Ordering(bn, queriedVariableName,
                                   IGNORE_EXPLANATION, Ordering.MINIMUM_WEIGHT));
        }
    }

    /**
     * Calculation of marginal posterior distribution using a given ordering,
     * and an arbitrary BayesNet.
     *
     * @param order
     */
    protected void inference(String order[])
    {
        inference(new Ordering(bn, order, IGNORE_EXPLANATION));
    }

    /**
     * Calculation of marginal posterior distribution.
     */
    private void inference(Ordering or)
    {
        // Create the Ordering and the BucketTree.
        bucketTree = new BucketTree(or, doProduceClusters);
        // Add the new BucketTree to the bucketForest and update bucketForVariable.
        if (doProduceClusters)
        {
            addBucketTree();
        }
        // Generate the result by reducing the BucketTree.
        bucketTree.reduce();
        result = bucketTree.getNormalizedResult();
    }

    /**
     * Add a BucketTree to the bucketForest and update the bucketForVariable
     * array.
     */
    private void addBucketTree()
    {
        Bucket buck;
        // Add the current BucketTree to the bucketForest.
        bucketForest.add(bucketTree);
        // Put the buckets in correspondence with the variables.
        for (int i = 0; i < bucketTree.bucketTree.length; i++)
        {
            buck = bucketTree.bucketTree[i];
            bucketForVariable[buck.variable.getIndex()] = buck;
        }
    }

    /**
     * Print the Inference.
     */
    public void print()
    {
        print(System.out, (boolean) true);
    }

    /**
     * Print the Inference.
     *
     * @param out
     */
    public void print(PrintStream out)
    {
        print(out, (boolean) true);
    }

    /**
     * Print the Inference.
     *
     * @param shouldPrintBucketTree
     */
    public void print(boolean shouldPrintBucketTree)
    {
        print(System.out, shouldPrintBucketTree);
    }

    /**
     * Print the Inference.
     *
     * @param out
     * @param shouldPrintBucketTree
     */
    public void print(PrintStream out, boolean shouldPrintBucketTree)
    {
        int i, bp[];
        ProbabilityVariable pv;

        // Do inference if Inference is null.
        if (result == null)
        {
            inference();
        }

        // Print it all.
        out.print("Posterior distribution:");

        if (shouldPrintBucketTree == true)
        {
            bucketTree.print(out);
        }
        out.println();

        result.print(out);
    }

    /* ************************************************************* */
    /* Methods that allow basic manipulation of non-public variables */
    /* ************************************************************* */
    /**
     * Get the BucketTree.
     *
     * @return
     */
    public BucketTree getBucketTree()
    {
        return (bucketTree);
    }

    /**
     * Get the BayesNet.
     *
     * @return
     */
    public BayesNet getBayesNet()
    {
        return (bn);
    }

    /**
     * Get the current result of the Inference.
     *
     * @return
     */
    public ProbabilityFunction getResult()
    {
        return (result);
    }

    /**
     * Get the status of the clustering process.
     *
     * @return
     */
    public boolean areClustersProduced()
    {
        return (doProduceClusters);
    }
}
