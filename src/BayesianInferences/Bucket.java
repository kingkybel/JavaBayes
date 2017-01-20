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

    // BucketTree that holds the Bucket.
    BucketTree bucketTree;

    // The Bucket variable.
    ProbabilityVariable probVar;

    // The functions in the Bucket.
    ArrayList<DiscreteFunction> discreteFunctions;

    // The pointers used for maximization.
    DiscreteFunction backwardPointers;

    // The function that is sent from a Bucket to another.
    DiscreteFunction separatorFunc;

    // Whether or not to compute distributions for all variables in the Bucket.
    boolean isProducingClusters;

    // The distribution for all variables involved in the Bucket.
    DiscreteFunction clusterFunc;

    // Variables that are not conditioning variables.
    ArrayList<DiscreteVariable> nonConditioningVariables;

    // The parentBuckets of the Bucket in the BucketTree.
    ArrayList<Bucket> parentBuckets;

    // The child of the Bucket in the BucketTree.
    Bucket child;

    Type bucketStatus = Type.EMPTY;

    private DiscreteFunction orderedDfs[];
    private boolean isOrderedDfsReady;

    /**
     * Simple constructor for Bucket. Default behavior is not to build the
     * distributions for clusters of variables.
     *
     * @param bucketTree the BucketTree that holds the bucket.
     * @param probVar    a probability variable (bucket variable) for the bucket
     */
    Bucket(BucketTree bucketTree, ProbabilityVariable probVar)
    {
        this(bucketTree, probVar, false);
    }

    /**
     * Basic constructor for Bucket.
     *
     * @param bucketTree          The BucketTree that holds the bucket.
     * @param probVar             a probability variable. The bucket variable
     *                            for the Bucket.
     * @param isProducingClusters true, if clusters should be produced, false
     *                            otherwise
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
        this.parentBuckets = new ArrayList<>();
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
        if (clusterFunc != null)
        {
            out.println("Cluster:");
            clusterFunc.print(out);
        }
        if (separatorFunc != null)
        {
            out.println("Separator:");
            separatorFunc.print(out);
        }
        if (parentBuckets.size() > 0)
        {
            out.println("\tParents:");
            for (Bucket bucket : parentBuckets)
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
     * is in the Bucket's separatorFunc. Notice that if all functions in a
     * bucket have a single probVar, then the separatorFunc is null.
     */
    void reduce()
    {
        // Order all the probability functions in the bucket
        orderTheFunctions();
        // If the bucket is empty, return null
        if (orderedDfs.length == 0)
        {
            separatorFunc = null;
            return;
        }
        // Create a ProbabilityFunction with the relevant variables
        DiscreteFunction newDf = buildNewFunction(false);
        // If newDf is null, then the only remaining probVar
        // in the Bucket is the bucket probVar. In this case, combine the
        // functions.
        if (newDf == null)
        {
            combine();
            separatorFunc = null;
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
        // Set the separatorFunc.
        separatorFunc = newDf;
    }

    /**
     * Combine a number of functions in the bucket into a single function.
     *
     * @return the combined function
     */
    DiscreteFunction combine()
    {
        int j, current;
        int indexes[] = new int[bucketTree.bayesNet.numberVariables()];
        int valueLengths[] = new int[bucketTree.bayesNet.numberVariables()];
        double maximalVal, combinedVal;

        // Order all the probability functions in the bucket
        orderTheFunctions();
        // If the bucket is empty, return null
        if (orderedDfs.length == 0)
        {
            return null;
        }

        // Create the combined DiscreteFunction object
        DiscreteFunction newDf = buildNewFunction(true);

        // Initialize some necessary values
        for (int varInd = 0;
             varInd < bucketTree.bayesNet.numberVariables();
             varInd++)
        {
            indexes[varInd] = 0;
            valueLengths[varInd] = bucketTree.bayesNet.getProbabilityVariable(
            varInd).numberValues();
        }

        // Build all values for the combined ProbabilityFunction object
        for (int valInd = 0; valInd < newDf.numberValues(); valInd++)
        {
            // Calculate the combined value
            combinedVal = 1.0;
            for (DiscreteFunction orderedDf : orderedDfs)
            {
                combinedVal *= orderedDf.evaluate(
                bucketTree.bayesNet.getProbabilityVariables(),
                indexes);
            }
            int valuePos = newDf.findPositionOfProbabilityValue(
                bucketTree.bayesNet.getProbabilityVariables(),
                indexes);
            newDf.setValue(valuePos, combinedVal);

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
            for (int varInd = 1; varInd < newDf.numberVariables(); varInd++)
            {
                jump *= newDf.getVariable(varInd).numberValues();
            }
            j = 0;
            maximalVal = 0.0;
            backwardPointers = new DiscreteFunction(1, 1);
            backwardPointers.setVariable(0, probVar);
            for (int valInd = 0; valInd < probVar.numberValues(); valInd++)
            {
                if (newDf.getValue(valInd) > maximalVal)
                {
                    maximalVal = newDf.getValue(valInd * jump);
                    j = valInd;
                }
            }
            backwardPointers.setValue(0, j);
        }

        if (isProducingClusters)
        {
            clusterFunc = newDf;
        }

        return newDf;
    }

    /**
     * Sum out all variables in the clusterFunc, except the bucket variable, and
     * put the summation in the bucketTree.result.
     */
    void reduceCluster()
    {
        // Check whether the clusterFunc is null.
        if (clusterFunc == null)
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
        clusterFunc.sumOut(bucketTree.bayesNet.getProbabilityVariables(),
                           markers);
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
            orderedDfs[i] = (DiscreteFunction) discreteFunctions.get(i);
        }
    }

    /**
     * Join the indexes of the Bucket by marking the variable markers with true.
     *
     * @param variableMarkers boolean array to mark indices of variables
     * @return the number of joined Indices
     */
    private int joinIndexes(boolean variableMarkers[])
    {
        int i, j, k, numberJoinedIndices = 0;
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
                    numberJoinedIndices++;
                }
            }
        }
        return numberJoinedIndices;
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
        int i, j = 0, n, newNumberOfValues = 1;
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
            return null;
        }

        // Calculate necessary quantities
        // the new number of values is the product of the cardinalities of the
        // values of all marked variables
        int joinedIndexes[] = new int[n];
        BayesNet bn = bucketTree.bayesNet;
        for (i = 0; i < variableMarkers.length; i++)
        {
            if (variableMarkers[i] == true)
            {
                joinedIndexes[j] = i;
                j++;
                newNumberOfValues *= bn.getProbabilityVariable(i).numberValues();
            }
        }

        // Create new function to be filled with joined variables
        DiscreteFunction newDf = buildNewVariables(
                         newNumberOfValues,
                         joinedIndexes,
                         isBucketVariableIncluded);

        return newDf;

    }

    /**
     * Construct an array of variables that contains the variables in a new
     * function; if the bucket variable is present, it is the first variable.
     *
     * @param numberOfValues           number of values in the function
     * @param joinedIndexes            array of indices of joined variables
     * @param isBucketVariableIncluded whether or not to include the bucket
     *                                 variable
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
     * @param newDiscrFunc new function
     */
    private void sumOut(DiscreteFunction newDiscrFunc)
    {
        DiscreteVariable discrVars[];
        int i, j, k, l, m, current;
        int numVals = probVar.numberValues();
        final int numVariables = bucketTree.bayesNet.numberVariables();
        int indexes[] = new int[numVariables];
        int valueLengths[] = new int[numVariables];

        // Initialize some necessary values.
        discrVars = bucketTree.bayesNet.getProbabilityVariables();
        for (i = 0; i < numVariables; i++)
        {
            indexes[i] = 0;
            valueLengths[i] =
            bucketTree.bayesNet.getProbabilityVariable(i).numberValues();
        }
        if (isProducingClusters)
        { // If necessary, start up the clusterFunc for the Bucket.
            clusterFunc = buildNewFunction(true);
        }

        // Auxiliary variable to hold last valid index.
        int lastValidIndex = newDiscrFunc.numberVariables() - 1;

        // Do the whole summation.
        for (i = 0; i < newDiscrFunc.numberValues(); i++)
        { // Compute all values of the newDf.
            double summedUpValue = 0.0;
            for (l = 0; l < numVals; l++)
            { // For each value of the bucket variable,
                // mark the current value in the indexes,
                indexes[probVar.getIndex()] = l;
                double partialValue = 1.0;
                for (k = 0; k < orderedDfs.length; k++)
                {
                    // loop through the functions in the Bucket.
                    partialValue *= orderedDfs[k].evaluate(discrVars, indexes);
                }
                if (isProducingClusters)
                { // If necessary, insert value in the clusterFunc.
                    int valuePosCluster = clusterFunc.
                        findPositionOfProbabilityValue(
                                discrVars,
                                indexes);
                    clusterFunc.setValue(valuePosCluster, partialValue);
                }

                // Finally, do the summation for each value of the newDf.
                summedUpValue += partialValue;
            }
            // Insert the summation for the value of newDf into newDf.
            int valuePos = newDiscrFunc.
                findPositionOfProbabilityValue(discrVars,
                                               indexes);
            newDiscrFunc.setValue(valuePos, summedUpValue);

            // Update the indexes.
            indexes[newDiscrFunc.getIndex(lastValidIndex)]++; // Increment the last index.
            for (j = lastValidIndex; j > 0; j--)
            { // Now do the updating of all indexes.
                current = newDiscrFunc.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                { // If overflow in an index,
                    indexes[current] = 0;
                    // then update the next index.
                    indexes[newDiscrFunc.getIndex(j - 1)]++;
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
     * @param newDiscrFunc new function
     */
    private void maxOut(DiscreteFunction newDiscrFunc)
    {
        int i, j, k, lowerIndex, upperIndex, last, current;
        int numberOfVals = probVar.numberValues();
        int indexes[] = new int[bucketTree.bayesNet.numberVariables()];
        int valueLengths[] = new int[bucketTree.bayesNet.numberVariables()];
        double lowValue;
        double highValue;

        // Initialize some necessary values
        createBackwardPointers(newDiscrFunc);
        for (i = 0; i < bucketTree.bayesNet.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] =
            bucketTree.bayesNet.getProbabilityVariable(i).numberValues();
        }

        // Run through all the values of the bucket variable
        last = newDiscrFunc.numberVariables() - 1;
        for (i = 0; i < newDiscrFunc.numberValues(); i++)
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
            int valuePos = newDiscrFunc.findPositionOfProbabilityValue(
                bucketTree.bayesNet.getProbabilityVariables(), indexes);
            newDiscrFunc.setValue(valuePos, highValue);
            backwardPointers.setValue(valuePos, (double) upperIndex);

            // Update the indexes
            indexes[newDiscrFunc.getIndex(last)]++;
            for (j = last; j > 0; j--)
            {
                current = newDiscrFunc.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                {
                    indexes[current] = 0;
                    indexes[newDiscrFunc.getIndex(j - 1)]++;
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
     * @param newDiscrFunc new function
     */
    private void createBackwardPointers(DiscreteFunction newDiscrFunc)
    {
        int i;
        DiscreteVariable newDfVariables[] =
                           new DiscreteVariable[newDiscrFunc.numberVariables()];
        double newDfValues[] = new double[newDiscrFunc.numberValues()];

        for (i = 0; i < newDiscrFunc.numberVariables(); i++)
        {
            newDfVariables[i] = newDiscrFunc.getVariable(i);
        }
        for (i = 0; i < newDiscrFunc.numberValues(); i++)
        {
            newDfValues[i] = newDiscrFunc.getValue(i);
        }
        backwardPointers = new DiscreteFunction(newDfVariables, newDfValues);
    }

    /**
     * Enumeration of Bucket-type.
     */
    public enum Type
    {

        /**
         * Empty bucket.
         */
        EMPTY,
        /**
         * Reduced bucket.
         */
        REDUCED,
        /**
         * Distributed bucket.
         */
        DISTRIBUTED
    }

}
