/*
 * Bucket.java
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
package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Logger;

class Bucket
{

    static final int EMPTY = 0;
    static final int REDUCED = 1;
    static final int DISTRIBUTED = 2;
    private static final String CLASS_NAME = Bucket.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    BucketTree bucketTree; // BucketTree that holds the Bucket.

    ProbabilityVariable variable; // The Bucket variable.
    ArrayList discreteFunctions;    // The functions in the Bucket.

    DiscreteFunction backwardPointers; // The pointers used for maximization.

    DiscreteFunction separator; // The function that is sent from a Bucket to another.
    boolean doProduceClusters; // Whether or not to compute distributions for all variables in the Bucket.
    DiscreteFunction cluster; // The distribution for all variables involved in the Bucket.

    ArrayList nonConditioningVariables; // Variables that are not conditioning variables.

    ArrayList parents; // The parents of the Bucket in the BucketTree.
    Bucket child; // The child of the Bucket in the BucketTree.

    int bucketStatus = EMPTY;

    private DiscreteFunction orderedDfs[];
    private boolean isOrderedDfsReady;

    /**
     * Simple constructor for Bucket. Default behavior is not to build the
     * distributions for clusters of variables.
     *
     * @param bs The BucketTree that holds the bucket.
     * @param pv The bucket variable for the Bucket.
     */
    Bucket(BucketTree bs, ProbabilityVariable pv)
    {
        this(bs, pv, false);
    }

    /**
     * Basic constructor for Bucket.
     *
     * @param bs              The BucketTree that holds the bucket.
     * @param pv              The bucket variable for the Bucket.
     * @param produceClusters Flag that indicates whether distributions for
     *                        clusters of variables are to be computed or not.
     */
    Bucket(BucketTree bs, ProbabilityVariable pv, boolean dpc)
    {
        bucketTree = bs;
        variable = pv;
        discreteFunctions = new ArrayList();
        doProduceClusters = dpc;
        nonConditioningVariables = new ArrayList();
        parents = new ArrayList();
    }

    /**
     * Print method for Bucket.
     */
    void print()
    {
        print(System.out);
    }

    /**
     * Print method for Bucket.
     */
    void print(PrintStream out)
    {
        boolean isExplanationFlag = false;
        DiscreteFunction dF;

        if (isExplanation())
        {
            isExplanationFlag = true;
        }

        if (isExplanationFlag)
        {
            out.print("MAP");
        }
        out.println("Bucket; variable " + variable.getName() +
                    " with " + discreteFunctions.size() +
                    " function(s).");
        switch (bucketStatus)
        {
            case EMPTY:
                out.println("Bucket is empty.");
                break;
            case REDUCED:
                out.println("Bucket has been reduced.");
                break;
            case DISTRIBUTED:
                out.println("Bucket has been distributed.");
                break;
        }
        for (Object e : discreteFunctions)
        {
            dF = (DiscreteFunction) (e);
            dF.print(out);
        }
        if (isExplanationFlag && (backwardPointers != null))
        {
            out.println("Backward pointers:");
            backwardPointers.print(out);
        }
        if (cluster != null)
        {
            out.println("Cluster:");
            cluster.print(out);
        }
        if (separator != null)
        {
            out.println("Separator:");
            separator.print(out);
        }
        if (parents.size() > 0)
        {
            out.println("\tParents:");
            for (Object e : parents)
            {
                out.println("\t" + ((Bucket) (e)).variable.
                            getName());
            }
        }
        if (child != null)
        {
            out.println("\tChild:");
            out.println("\t" + child.variable.getName());
        }
    }

    /**
     * Reduce the Bucket, either by summation or maximization. The final result
     * is in the Bucket's separator. Notice that if all functions in a bucket
     * have a single variable, then the separator is null.
     */
    void reduce()
    {
        // Order all the probability functions in the bucket
        orderDfs();
        // If the bucket is empty, return null
        if (orderedDfs.length == 0)
        {
            separator = null;
            return;
        }
        // Create a ProbabilityFunction with the relevant variables
        DiscreteFunction newDf = buildNewFunction(false);
        // If newDf is null, then the only remaining variable
        // in the Bucket is the bucket variable. In this case, combine the functions.
        if (newDf == null)
        {
            combine();
            separator = null;
            return;
        }
        // Either sum out or maximize out the bucket variable.
        if (isExplanation())
        {
            maxOut(newDf);
        }
        else
        {
            sumOut(newDf);
        }

        // Mark the Bucket as REDUCED;
        bucketStatus = REDUCED;
        // Set the separator.
        separator = newDf;
    }

    /**
     * Combine a number of functions in the bucket into a single function.
     */
    DiscreteFunction combine()
    {
        int i, j, k, m, p, current;
        int indexes[] = new int[bucketTree.bn.numberVariables()];
        int valueLengths[] = new int[bucketTree.bn.numberVariables()];
        double t, v;

        // Order all the probability functions in the bucket
        orderDfs();
        // If the bucket is empty, return null
        if (orderedDfs.length == 0)
        {
            return (null);
        }

        // Create the combined DiscreteFunction object
        DiscreteFunction newDf = buildNewFunction(true);

        // Initialize some necessary values
        for (i = 0; i < bucketTree.bn.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] = bucketTree.bn.getProbabilityVariable(i).
            numberValues();
        }

        // Build all values for the combined ProbabilityFunction object
        for (i = 0; i < newDf.numberValues(); i++)
        {
            // Calculate the combined value
            v = 1.0;
            for (m = 0; m < orderedDfs.length; m++)
            {
                v *= orderedDfs[m].evaluate(bucketTree.bn.
                getProbabilityVariables(),
                                            indexes);
            }
            p = newDf.getPositionFromIndexes(bucketTree.bn.
            getProbabilityVariables(),
                                             indexes);
            newDf.setValue(p, v);

            // Update the indexes
            indexes[newDf.getIndex(newDf.numberVariables() - 1)]++;
            for (j = (newDf.numberVariables() - 1); j > 0; j--)
            {
                current = newDf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                {
                    indexes[current] = 0;
                    indexes[newDf.getIndex(j - 1)]++;
                }
                else
                {
                    break;
                }
            }
        }

        // Maximize if necessary. If the combined function
        // has conditioning variables, only the first
        // combination of conditioning variables is analyzed.
        if (isExplanation())
        {
            int jump = 1;
            for (i = 1; i < newDf.numberVariables(); i++)
            {
                jump *= newDf.getVariable(i).numberValues();
            }
            j = 0;
            t = 0.0;
            backwardPointers = new DiscreteFunction(1, 1);
            backwardPointers.setVariable(0, variable);
            for (i = 0; i < variable.numberValues(); i++)
            {
                if (newDf.getValue(i) > t)
                {
                    t = newDf.getValue(i * jump);
                    j = i;
                }
            }
            backwardPointers.setValue(0, j);
        }

        if (doProduceClusters)
        {
            cluster = newDf;
        }
        return (newDf);
    }

    /**
     * Sum out all variables in the cluster, except the bucket variable, and put
     * the summation in the bucketTree.result.
     */
    void reduceCluster()
    {
        // Check whether the cluster is null.
        if (cluster == null)
        {
            bucketTree.unnormalizedResult = null;
            return;
        }
        // Construct the markers.
        boolean markers[] = new boolean[bucketTree.bn.numberVariables()];
        for (int i = 0; i < markers.length; i++)
        {
            markers[i] = true;
        }
        markers[variable.getIndex()] = false;
        // Fill in the bucketTree.result.
        bucketTree.unnormalizedResult =
        cluster.sumOut(bucketTree.bn.getProbabilityVariables(), markers);
    }

    /**
     * Detect whether the bucket variable is an explanatory variable.
     */
    boolean isExplanation()
    {
        if (bucketTree.explanationStatus == Inference.IGNORE_EXPLANATION)
        {
            return (false);
        }
        if (bucketTree.explanationStatus == Inference.FULL_EXPLANATION)
        {
            return (true);
        }
        return (variable.isExplanation());
    }

    /**
     * Order the probability functions in the Bucket.
     */
    private void orderDfs()
    {
        if (isOrderedDfsReady == true)
        {
            return;
        }
        isOrderedDfsReady = true;
        orderedDfs = new DiscreteFunction[discreteFunctions.size()];
        for (int i = 0; i < orderedDfs.length; i++)
        {
            orderedDfs[i] =
            (DiscreteFunction) (discreteFunctions.get(i));
        }
    }

    /**
     * Join the indexes of the Bucket by marking the variable markers with true.
     */
    private int joinIndexes(boolean variableMarkers[])
    {
        int i, j, k, n = 0;
        for (i = 0; i < variableMarkers.length; i++)
        {
            variableMarkers[i] = false;
        }
        for (i = 0; i < orderedDfs.length; i++)
        {
            for (j = 0; j < orderedDfs[i].numberVariables(); j++)
            {
                k = orderedDfs[i].getIndex(j);
                if (variableMarkers[k] == false)
                {
                    variableMarkers[k] = true;
                    n++;
                }
            }
        }
        return (n);
    }

    /**
     * Construct a DiscreteFunction which holds all the variables in the Bucket
     * (maybe with the exception of the bucket variable).
     */
    private DiscreteFunction buildNewFunction(
            boolean isBucketVariableIncluded)
    {
        int i, j = 0, n, v = 1;
        boolean variableMarkers[] = new boolean[bucketTree.bn.
                  numberVariables()];

        // Join the indexes in the bucket
        n = joinIndexes(variableMarkers);
        if (isBucketVariableIncluded == false)
        {
            n--;
            variableMarkers[variable.getIndex()] = false;
        }

        // If the only variable is the bucket variable, then ignore
        if (n == 0)
        {
            return (null);
        }

        // Calculate necessary quantities
        int joinedIndexes[] = new int[n];
        for (i = 0; i < variableMarkers.length; i++)
        {
            if (variableMarkers[i] == true)
            {
                joinedIndexes[j] = i;
                j++;
                v *= bucketTree.bn.getProbabilityVariable(i).numberValues();
            }
        }

        // Create new function to be filled with joined variables
        DiscreteFunction newDf = new DiscreteFunction(n, v);
        buildNewVariables(newDf,
                          joinedIndexes,
                          isBucketVariableIncluded, n);

        return (newDf);

    }

    /**
     * Construct an array of variables that contains the variables in a new
     * function; if the bucket variable is present, it is the first variable.
     */
    private void buildNewVariables(DiscreteFunction newDf,
                                   int joinedIndexes[],
                                   boolean isBucketVariableIncluded,
                                   int n)
    {
        // Bucket variable comes first if present
        if (isBucketVariableIncluded == true)
        {
            for (int i = 0, j = 1; i < n; i++)
            {
                if (joinedIndexes[i] == variable.getIndex())
                {
                    newDf.setVariable(0,
                                      bucketTree.bn.
                                      getProbabilityVariable(
                                              variable.getIndex()));
                }
                else
                {
                    newDf.setVariable(j,
                                      bucketTree.bn.
                                      getProbabilityVariable(
                                              joinedIndexes[i]));
                    j++;
                }
            }
        }
        else
        {
            for (int i = 0; i < n; i++)
            {
                newDf.setVariable(i,
                                  bucketTree.bn.
                                  getProbabilityVariable(
                                          joinedIndexes[i]));
            }
        }
    }

    /**
     * Obtain the values for the reducedFunction. Attention: the array
     * orderedDfs is supposed to be ready!
     */
    private void sumOut(DiscreteFunction newDf)
    {
        DiscreteVariable dvs[];
        int i, j, k, l, m, p, pCluster, last, current;
        int n = variable.numberValues();
        int indexes[] = new int[bucketTree.bn.numberVariables()];
        int valueLengths[] = new int[bucketTree.bn.numberVariables()];
        double t, v;

        // Initialize some necessary values.
        dvs = bucketTree.bn.getProbabilityVariables();
        for (i = 0; i < bucketTree.bn.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] = bucketTree.bn.
            getProbabilityVariable(i).numberValues();
        }
        if (doProduceClusters)
        { // If necessary, start up the cluster for the Bucket.
            cluster = buildNewFunction(true);
        }

        // Do the whole summation.
        last = newDf.numberVariables() - 1; // Auxiliary variable to hold last valid index.
        for (i = 0; i < newDf.numberValues(); i++)
        { // Compute all values of the newDf.
            v = 0.0;
            for (l = 0; l < n; l++)
            { // For each value of the bucket variable,
                indexes[variable.getIndex()] = l; // mark the current value in the indexes,
                t = 1.0;
                for (m = 0; m < orderedDfs.length; m++)
                {
                    // loop through the functions in the Bucket.
                    t *= orderedDfs[m].evaluate(dvs, indexes);
                }
                if (doProduceClusters)
                { // If necessary, insert value in the cluster.
                    pCluster = cluster.getPositionFromIndexes(dvs,
                                                              indexes);
                    cluster.setValue(pCluster, t);
                }
                v += t; // Finally, do the summation for each value of the newDf.
            }
            // Insert the summation for the value of newDf into newDf.
            p = newDf.getPositionFromIndexes(dvs, indexes);
            newDf.setValue(p, v);

            // Update the indexes.
            indexes[newDf.getIndex(last)]++; // Increment the last index.
            for (j = last; j > 0; j--)
            { // Now do the updating of all indexes.
                current = newDf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                { // If overflow in an index,
                    indexes[current] = 0;
                    indexes[newDf.getIndex(j - 1)]++; // then update the next index.
                }
                else
                {
                    break;
                }
            }
        }
    }

    /**
     * Obtain the values for the reducedFunction through maximization.
     * Attention: the array orderedDfs is supposed to be ready!
     */
    private void maxOut(DiscreteFunction newDf)
    {
        int i, j, k, l, m, p, u, last, current;
        int n = variable.numberValues();
        int indexes[] = new int[bucketTree.bn.numberVariables()];
        int valueLengths[] = new int[bucketTree.bn.numberVariables()];
        double t, v = 0.0;

        // Initialize some necessary values
        createBackwardPointers(newDf);
        for (i = 0; i < bucketTree.bn.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] = bucketTree.bn.
            getProbabilityVariable(i).numberValues();
        }

        // Run through all the values of the bucket variable
        last = newDf.numberVariables() - 1;
        for (i = 0; i < newDf.numberValues(); i++)
        {
            v = 0.0;
            u = BayesNet.INVALID_INDEX;
            for (l = 0; l < n; l++)
            {
                t = 1.0;
                indexes[variable.getIndex()] = l;
                // Combine the values through all the functions in the bucket
                for (m = 0; m < orderedDfs.length; m++)
                {
                    t *= orderedDfs[m].evaluate(bucketTree.bn.
                    getProbabilityVariables(),
                                                indexes);
                }
                // Perform the maximization
                if (v <= t)
                {
                    v = t;
                    u = l;
                }
            }
            // Update functions
            p = newDf.getPositionFromIndexes(bucketTree.bn.
            getProbabilityVariables(),
                                             indexes);
            newDf.setValue(p, v);
            backwardPointers.setValue(p, (double) u);

            // Update the indexes
            indexes[newDf.getIndex(last)]++;
            for (j = last; j > 0; j--)
            {
                current = newDf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                {
                    indexes[current] = 0;
                    indexes[newDf.getIndex(j - 1)]++;
                }
                else
                {
                    break;
                }
            }
        }
    }

    /**
     * Allocate and initialize the backwardPointers in the Bucket.
     */
    private void createBackwardPointers(DiscreteFunction newDf)
    {
        int i;
        DiscreteVariable newDfVariables[] =
                           new DiscreteVariable[newDf.
                           numberVariables()];
        double newDfValues[] = new double[newDf.numberValues()];

        for (i = 0; i < newDf.numberVariables(); i++)
        {
            newDfVariables[i] = newDf.getVariable(i);
        }
        for (i = 0; i < newDf.numberValues(); i++)
        {
            newDfValues[i] = newDf.getValue(i);
        }
        backwardPointers =
        new DiscreteFunction(newDfVariables, newDfValues);
    }

}
