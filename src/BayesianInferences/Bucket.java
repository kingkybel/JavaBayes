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

    private static final Class CLAZZ = Bucket.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public enum Type
    {

        EMPTY, REDUCED, DISTRIBUTED;
    }
    // BucketTree that holds the Bucket.
    BucketTree bucketTree;

    // The Bucket variable.
    ProbabilityVariable probVar;

    // The functions in the Bucket.
    ArrayList<DiscreteFunction> discreteFunctions;

    // The pointers used for maximization.
    DiscreteFunction backwardPointers;

    // The function that is sent from a Bucket to another.
    DiscreteFunction separator;

    // Whether or not to compute distributions for all variables in the Bucket.
    boolean isProducingClusters;

    // The distribution for all variables involved in the Bucket.
    DiscreteFunction cluster;

    // Variables that are not conditioning variables.
    ArrayList<DiscreteVariable> nonConditioningVariables;

    // The parents of the Bucket in the BucketTree.
    ArrayList<Bucket> parents;

    // The child of the Bucket in the BucketTree.
    Bucket child;

    Type bucketStatus = Type.EMPTY;

    private DiscreteFunction orderedDfs[];
    private boolean isOrderedDfsReady;

    /**
     * Simple constructor for Bucket. Default behavior is not to build the
     * distributions for clusters of variables.
     *
     * @param bucketTree The BucketTree that holds the bucket.
     * @param probVar    a probability variable The bucket variable for the
     *                   Bucket.
     */
    Bucket(BucketTree bucketTree, ProbabilityVariable probVar)
    {
        this(bucketTree, probVar, false);
    }

    /**
     * Basic constructor for Bucket.
     *
     * @param bucketTree          The BucketTree that holds the bucket.
     * @param probVar             a probability variable The bucket variable for
     *                            the Bucket.
     * @param isProducingClusters Flag that indicates whether distributions for
     *                            clusters of variables are to be computed or
     *                            not.
     */
    Bucket(BucketTree bucketTree,
           ProbabilityVariable probVar,
           boolean isProducingClusters)
    {
        this.bucketTree = bucketTree;
        this.probVar = probVar;
        this.discreteFunctions = new ArrayList<>();
        this.isProducingClusters = isProducingClusters;
        this.nonConditioningVariables = new ArrayList<>();
        this.parents = new ArrayList<>();
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
     *
     * @param out output print stream
     */
    void print(PrintStream out)
    {
        boolean isExplanationFlag = false;

        if (isExplanation())
        {
            isExplanationFlag = true;
        }

        if (isExplanationFlag)
        {
            out.print("MAP");
        }
        out.println("Bucket; variable " + probVar.getName() +
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
        for (DiscreteFunction discrFunc : discreteFunctions)
        {
            discrFunc.print(out);
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
            for (Bucket bucket : parents)
            {
                out.println("\t" + bucket.probVar.getName());
            }
        }
        if (child != null)
        {
            out.println("\tChild:");
            out.println("\t" + child.probVar.getName());
        }
    }

    /**
     * Reduce the Bucket, either by summation or maximisation. The final result
     * is in the Bucket's separator. Notice that if all functions in a bucket
     * have a single probVar, then the separator is null.
     */
    void reduce()
    {
        // Order all the probability functions in the bucket
        orderTheFunctions();
        // If the bucket is empty, return null
        if (orderedDfs.length == 0)
        {
            separator = null;
            return;
        }
        // Create a ProbabilityFunction with the relevant variables
        DiscreteFunction newDf = buildNewFunction(false);
        // If newDf is null, then the only remaining probVar
        // in the Bucket is the bucket probVar. In this case, combine the functions.
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
        bucketStatus = Type.REDUCED;
        // Set the separator.
        separator = newDf;
    }

    /**
     * Combine a number of functions in the bucket into a single function.
     *
     * @return the combined function
     */
    DiscreteFunction combine()
    {
        int i, j, k, m, p, current;
        int indexes[] = new int[bucketTree.bayesNet.numberVariables()];
        int valueLengths[] = new int[bucketTree.bayesNet.numberVariables()];
        double t, v;

        // Order all the probability functions in the bucket
        orderTheFunctions();
        // If the bucket is empty, return null
        if (orderedDfs.length == 0)
        {
            return (null);
        }

        // Create the combined DiscreteFunction object
        DiscreteFunction newDf = buildNewFunction(true);

        // Initialize some necessary values
        for (i = 0; i < bucketTree.bayesNet.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] = bucketTree.bayesNet.getProbabilityVariable(i).
            numberValues();
        }

        // Build all values for the combined ProbabilityFunction object
        for (i = 0; i < newDf.numberValues(); i++)
        {
            // Calculate the combined value
            v = 1.0;
            for (m = 0; m < orderedDfs.length; m++)
            {
                v *= orderedDfs[m].evaluate(bucketTree.bayesNet.
                getProbabilityVariables(),
                                            indexes);
            }
            p = newDf.getPositionFromIndexes(bucketTree.bayesNet.
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
            backwardPointers.setVariable(0, probVar);
            for (i = 0; i < probVar.numberValues(); i++)
            {
                if (newDf.getValue(i) > t)
                {
                    t = newDf.getValue(i * jump);
                    j = i;
                }
            }
            backwardPointers.setValue(0, j);
        }

        if (isProducingClusters)
        {
            cluster = newDf;
        }
        return (newDf);
    }

    /**
     * Sum out all variables in the cluster, except the bucket probVar, and put
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
        boolean markers[] = new boolean[bucketTree.bayesNet.numberVariables()];
        for (int i = 0; i < markers.length; i++)
        {
            markers[i] = true;
        }
        markers[probVar.getIndex()] = false;
        // Fill in the bucketTree.result.
        bucketTree.unnormalizedResult =
        cluster.sumOut(bucketTree.bayesNet.getProbabilityVariables(), markers);
    }

    /**
     * Detect whether the bucket variable is an explanatory variable.
     *
     * @return true, if the bucket - tree is a (full) explanatory variable,
     *         false otherwise
     */
    boolean isExplanation()
    {
        return bucketTree.isIgnoreExplanation() ? false :
               bucketTree.isFullExplanation() ? true :
               probVar.isExplanation();
    }

    /**
     * Order the probability functions in the Bucket.
     */
    private void orderTheFunctions()
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
     *
     * @param variableMarkers
     * @return the number of joined Indices
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
     *
     * @param isBucketVariableIncluded whether or not to include the bucket
     *                                 variable
     * @return the new bucket function
     */
    private DiscreteFunction buildNewFunction(boolean isBucketVariableIncluded)
    {
        int i, j = 0, n, v = 1;
        boolean variableMarkers[] =
                  new boolean[bucketTree.bayesNet.numberVariables()];

        // Join the indexes in the bucket
        n = joinIndexes(variableMarkers);
        if (isBucketVariableIncluded == false)
        {
            n--;
            variableMarkers[probVar.getIndex()] = false;
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
                v *= bucketTree.bayesNet.getProbabilityVariable(i).
                numberValues();
            }
        }

        // Create new function to be filled with joined variables
        DiscreteFunction newDf = buildNewVariables(
                         v,
                         joinedIndexes,
                         isBucketVariableIncluded);

        return (newDf);

    }

    /**
     * Construct an array of variables that contains the variables in a new
     * function; if the bucket variable is present, it is the first variable.
     *
     * @param numberOfValues
     * @param joinedIndexes
     * @param isBucketVariableIncluded
     */
    private DiscreteFunction buildNewVariables(int numberOfValues,
                                               int joinedIndexes[],
                                               boolean isBucketVariableIncluded)
    {
        DiscreteFunction newDf = new DiscreteFunction(joinedIndexes.length,
                                                      numberOfValues);
        // Bucket probVar comes first if present
        if (isBucketVariableIncluded == true)
        {
            for (int i = 0, j = 1; i < joinedIndexes.length; i++)
            {
                if (joinedIndexes[i] == probVar.getIndex())
                {
                    newDf.setVariable(0,
                                      bucketTree.bayesNet.
                                      getProbabilityVariable(probVar.getIndex()));
                }
                else
                {
                    newDf.setVariable(
                            j,
                            bucketTree.bayesNet.getProbabilityVariable(
                                    joinedIndexes[i]));
                    j++;
                }
            }
        }
        else
        {
            for (int i = 0; i < joinedIndexes.length; i++)
            {
                newDf.setVariable(
                        i,
                        bucketTree.bayesNet.getProbabilityVariable(
                                joinedIndexes[i]));
            }
        }

        return newDf;
    }

    /**
     * Obtain the values for the reducedFunction. Attention: the array
     * orderedDfs is supposed to be ready!
     *
     * @param newDf new function
     */
    private void sumOut(DiscreteFunction newDf)
    {
        DiscreteVariable dvs[];
        int i, j, k, l, m, p, pCluster, last, current;
        int numVals = probVar.numberValues();
        final int numVariables = bucketTree.bayesNet.numberVariables();
        int indexes[] = new int[numVariables];
        int valueLengths[] = new int[numVariables];
        double t, v;

        // Initialize some necessary values.
        dvs = bucketTree.bayesNet.getProbabilityVariables();
        for (i = 0; i < numVariables; i++)
        {
            indexes[i] = 0;
            valueLengths[i] =
            bucketTree.bayesNet.getProbabilityVariable(i).numberValues();
        }
        if (isProducingClusters)
        { // If necessary, start up the cluster for the Bucket.
            cluster = buildNewFunction(true);
        }

        // Auxiliary variable to hold last valid index.
        last = newDf.numberVariables() - 1;

        // Do the whole summation.
        for (i = 0; i < newDf.numberValues(); i++)
        { // Compute all values of the newDf.
            v = 0.0;
            for (l = 0; l < numVals; l++)
            { // For each value of the bucket variable,
                // mark the current value in the indexes,
                indexes[probVar.getIndex()] = l;
                t = 1.0;
                for (k = 0; k < orderedDfs.length; k++)
                {
                    // loop through the functions in the Bucket.
                    t *= orderedDfs[k].evaluate(dvs, indexes);
                }
                if (isProducingClusters)
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
     * Obtain the values for the reducedFunction through maximisation.
     * Attention: the array orderedDfs is supposed to be ready!
     *
     * @param newDf new function
     */
    private void maxOut(DiscreteFunction newDf)
    {
        int i, j, k, lowerIndex, pos, upperIndex, last, current;
        int numberOfVals = probVar.numberValues();
        int indexes[] = new int[bucketTree.bayesNet.numberVariables()];
        int valueLengths[] = new int[bucketTree.bayesNet.numberVariables()];
        double lowValue;
        double highValue;

        // Initialize some necessary values
        createBackwardPointers(newDf);
        for (i = 0; i < bucketTree.bayesNet.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] =
            bucketTree.bayesNet.getProbabilityVariable(i).numberValues();
        }

        // Run through all the values of the bucket probVar
        last = newDf.numberVariables() - 1;
        for (i = 0; i < newDf.numberValues(); i++)
        {
            highValue = 0.0;
            upperIndex = BayesNet.INVALID_INDEX;
            for (lowerIndex = 0; lowerIndex < numberOfVals; lowerIndex++)
            {
                lowValue = 1.0;
                indexes[probVar.getIndex()] = lowerIndex;
                // Combine the values through all the functions in the bucket
                for (k = 0; k < orderedDfs.length; k++)
                {
                    lowValue *= orderedDfs[k].evaluate(
                    bucketTree.bayesNet.getProbabilityVariables(),
                    indexes);
                }
                // Perform the maximization
                if (highValue <= lowValue)
                {
                    highValue = lowValue;
                    upperIndex = lowerIndex;
                }
            }
            // Update functions
            pos = newDf.getPositionFromIndexes(
            bucketTree.bayesNet.getProbabilityVariables(), indexes);
            newDf.setValue(pos, highValue);
            backwardPointers.setValue(pos, (double) upperIndex);

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
     * Allocate and initialise the backwardPointers in the Bucket.
     *
     * @param newDf new function
     */
    private void createBackwardPointers(DiscreteFunction newDf)
    {
        int i;
        DiscreteVariable newDfVariables[] =
                           new DiscreteVariable[newDf.numberVariables()];
        double newDfValues[] = new double[newDf.numberValues()];

        for (i = 0; i < newDf.numberVariables(); i++)
        {
            newDfVariables[i] = newDf.getVariable(i);
        }
        for (i = 0; i < newDf.numberValues(); i++)
        {
            newDfValues[i] = newDf.getValue(i);
        }
        backwardPointers = new DiscreteFunction(newDfVariables, newDfValues);
    }

}
