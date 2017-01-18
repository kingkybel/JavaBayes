/*
 * BucketTree.java
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
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * Class that combines a collection of buckets into a tree.
 *
 * @author Fabio G. Cozman
 */
public class BucketTree
{

    private static final Class CLAZZ = BucketTree.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Array of Bucket objects.
    Bucket bucketTree[];

    // BayesNet that contains the variables.
    BayesNet bayesNet;

    // Array that stores the index of variables for minimization.
    int backwardPointers[];

    DiscreteFunction unnormalizedResult;

    Ordering ordering;
    ExplanationType explanationStatus;
    boolean isProducingClusters;

    private int activeBucket;

    /**
     * Simple constructor for BucketTree.
     *
     * @param ordering
     */
    public BucketTree(Ordering ordering)
    {
        this(ordering, false);
    }

    /**
     * Constructor for BucketTree. Does the whole initialization; it should be
     * the only method that deals with symbolic names for variables.
     *
     * @param ordering
     * @param isProducingClusters
     */
    public BucketTree(Ordering ordering, boolean isProducingClusters)
    {
        int i, j, markers[];
        ProbabilityFunction probFunc;
        ProbabilityVariable probVar;
        DiscreteVariable auxPv;
        DiscreteFunction ut;
        String order[];

        this.isProducingClusters = isProducingClusters;
        this.ordering = ordering;

        // Collect information from the Ordering object.
        bayesNet = this.ordering.bayesNet;
        explanationStatus = this.ordering.explanationStatus;
        order = this.ordering.order;

        // Indicate the first bucket to process
        activeBucket = 0;

        // Check the possibility that the query has an observed probVar
        i = bayesNet.indexOfVariable(order[order.length - 1]);
        probVar = bayesNet.getProbabilityVariable(i);
        if (probVar.isObserved() == true)
        {
            probFunc = transformToProbabilityFunction(bayesNet, probVar);
            bucketTree = new Bucket[1];
            bucketTree[0] = new Bucket(this, probVar, this.isProducingClusters);
            insert(probFunc);
        }
        else
        {
            // Initialize the bucket objects
            bucketTree = new Bucket[order.length];
            for (i = 0; i < order.length; i++)
            {
                j = bayesNet.indexOfVariable(order[i]);
                bucketTree[i] =
                new Bucket(this,
                           bayesNet.getProbabilityVariable(j),
                           this.isProducingClusters);
            }
            // Insert the probability functions into the bucketTree;
            // first mark all functions that are actually going
            // into the bucketTree.
            markers = new int[bayesNet.numberVariables()];
            for (i = 0; i < order.length; i++)
            {
                markers[bayesNet.indexOfVariable(order[i])] = 1;
            }
            // Now insert functions that are marked and non-null.
            for (i = 0; i < bayesNet.numberProbabilityFunctions(); i++)
            {
                if (markers[bayesNet.getProbabilityFunction(i).getIndex(0)] == 1)
                {
                    probFunc = checkEvidence(bayesNet.getProbabilityFunction(i));
                    if (probFunc != null)
                    {
                        auxPv = (bayesNet.getProbabilityFunction(i)).
                        getVariable(0);
                        insert(probFunc, !probFunc.isParameter(auxPv.getIndex()));
                    }
                }
            }
            // Insert the utilityFunction.
            ut = bayesNet.getUtilityFunction();
            if (ut != null)
            {
                insert(ut);
            }
        }
    }

    /**
     * Transform an observed ProbabilityVariable into a ProbabilityFunction to
     * handle the case where the query involves an observed variable.
     *
     * @param bayesNet the underlying Bayesian network
     * @param probVar  a probability variable
     * @return the transformed function
     */
    private ProbabilityFunction transformToProbabilityFunction(
            BayesNet bayesNet,
            ProbabilityVariable probVar)
    {
        ProbabilityFunction probFunc = new ProbabilityFunction(bayesNet,
                                                               1,
                                                               probVar.
                                                               numberValues(),
                                                               null);
        probFunc.setVariable(0, probVar);
        int indexOfValue = probVar.getObservedIndex();
        probFunc.setValue(indexOfValue, 1.0);
        return probFunc;
    }

    /**
     * Eliminates all variables defined as evidence. The order of the variables
     * that are not eliminated is the same order in the original function.
     *
     * @param probFunc probability function
     * @return
     */
    private ProbabilityFunction checkEvidence(ProbabilityFunction probFunc)
    {
        int varIndex, markedVarIndex, newNumberOfValues, auxI;
        boolean markers[] = new boolean[bayesNet.numberVariables()];
        int numMarked = buildEvidenceMarkers(probFunc, markers);

        // Handle special cases
        if (numMarked == 0)
        {
            return null; // No probVar remains
        }
        if (numMarked == probFunc.numberVariables())
        {
            return probFunc; // No relevant evidence
        }

        // Calculate necessary quantities in such a way that the order of
        // variables in the original function is not altered.
        int joinedIndexes[] = new int[numMarked];
        for (varIndex = 0, markedVarIndex = 0, newNumberOfValues = 1;
             varIndex < probFunc.numberVariables();
             varIndex++)
        {
            auxI = probFunc.getVariable(varIndex).getIndex();
            if (markers[auxI] == true)
            {
                joinedIndexes[markedVarIndex] = auxI;
                markedVarIndex++;
                newNumberOfValues *= bayesNet.getProbabilityVariable(auxI).
                numberValues();
            }
        }

        // Create new function to be filled with joined variables
        ProbabilityFunction newPf =
                            new ProbabilityFunction(bayesNet,
                                                    numMarked,
                                                    newNumberOfValues,
                                                    null);
        for (varIndex = 0; varIndex < numMarked; varIndex++)
        {
            newPf.setVariable(varIndex,
                              bayesNet.getProbabilityVariable(
                                      joinedIndexes[varIndex]));
        }

        // Loop through the values
        checkEvidenceLoop(newPf, probFunc);

        return newPf;
    }

    /**
     * Build an array of markers. The marker for a variable is true only if the
     * variable is present in the input ProbabilityFunction probFunc and is not
     * observed. Even explanatory variables can be observed and taken as
     * evidence.
     *
     * @param probFunc probability function
     * @param markers  a boolean vector of cardinality of number of variables
     * @return the number of marked evidence variables
     */
    private int buildEvidenceMarkers(ProbabilityFunction probFunc,
                                     boolean markers[])
    {
        int varIndex, numMarkedVars;
        // Initialize the markers
        for (varIndex = 0; varIndex < markers.length; varIndex++)
        {
            markers[varIndex] = false;
        }
        // Insert the variables of the ProbabilityFunction
        for (varIndex = 0; varIndex < probFunc.numberVariables(); varIndex++)
        {
            markers[probFunc.getIndex(varIndex)] = true;
        }
        // Take the evidence out
        for (varIndex = 0; varIndex < bayesNet.numberVariables(); varIndex++)
        {
            if (bayesNet.getProbabilityVariable(varIndex).isObserved())
            {
                markers[varIndex] = false;
            }
        }
        // Count how many variables remain
        numMarkedVars = 0;
        for (varIndex = 0; varIndex < markers.length; varIndex++)
        {
            if (markers[varIndex] == true)
            {
                numMarkedVars++;
            }
        }
        return numMarkedVars;
    }

    /**
     * Obtain the values for the evidence plus function.
     *
     * @param newProbFunc
     * @param probFunc    probability function
     */
    private void checkEvidenceLoop(ProbabilityFunction newProbFunc,
                                   ProbabilityFunction probFunc)
    {
        int i, j, k, l, m, p, last, current;
        int indexes[] = new int[bayesNet.numberVariables()];
        int valueLengths[] = new int[bayesNet.numberVariables()];

        for (i = 0; i < bayesNet.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] = bayesNet.getProbabilityVariable(i).numberValues();
        }
        for (i = 0; i < bayesNet.numberVariables(); i++)
        {
            if (bayesNet.getProbabilityVariable(i).isObserved())
            {
                indexes[i] = bayesNet.getProbabilityVariable(i).
                getObservedIndex();
            }
        }
        last = newProbFunc.numberVariables() - 1;
        for (i = 0; i < newProbFunc.numberValues(); i++)
        {
            p = newProbFunc.getPositionFromIndexes(indexes);
            newProbFunc.setValue(p, probFunc.evaluate(indexes));

            indexes[newProbFunc.getIndex(last)]++;
            for (j = last; j > 0; j--)
            {
                current = newProbFunc.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                {
                    indexes[current] = 0;
                    indexes[newProbFunc.getIndex(j - 1)]++;
                }
                else
                {
                    break;
                }
            }
        }
    }

    /**
     * Variable elimination in the BucketTree.
     */
    public void reduce()
    {
        int i;
        // Reduce all Bucket objects.
        for (i = 0; i < (bucketTree.length - 1); i++)
        {
            activeBucket = i;
            bucketTree[i].reduce();
            insert(bucketTree[i]);
        }
        // Now reduce the last Bucket.
        unnormalizedResult = bucketTree[i].combine();
        // Mark the last Bucket as DISTRIBUTED.
        bucketTree[i].bucketStatus = Bucket.Type.DISTRIBUTED;
        // Generate the backwardPointers if necessary.
        backwardPointers = backwardMaximization();
    }

    /**
     * Distribute evidence in the BucketTree.
     *
     * @return true if successful; false if not.
     */
    public boolean distribute()
    {
        int i, j;
        boolean markNonConditioning[] = new boolean[bayesNet.numberVariables()];

        // First make sure the BucketTree has been reduced.
        if (unnormalizedResult == null)
        {
            reduce();
        }
        // Second make sure there is more than one Bucket in the BucketTree.
        int last = bucketTree.length - 1;
        if (last < 1)
        {
            return true;
        }
        // Third, this method is used only if isProducingClusters is true.
        if (isProducingClusters == false)
        {
            return false;
        }
        // Fourth, this method is use only if no explanatory probVar was max'ed out.
        if (backwardPointers != null)
        {
            return false;
        }

        // Go through the Bucket objects, from bottom to top,
        // to compute the new separatorFunc and clusterFunc for each bucket.
        for (i = (last - 1); i >= 0; i--)
        { // Start from (last-1); last does not have child.
            // Check whether the Bucket has any valid content.
            if (bucketTree[i].clusterFunc == null)
            {
                break;
            }
            // Take the non-conditioning variables in a boolean array.
            for (j = 0; j < markNonConditioning.length; j++)
            {
                markNonConditioning[j] = true;
            }
            // OBS: The following piece of code will actually be less efficient than
            // necessary. It will count as "conditioning" any variable in the clusterFunc
            // except the bucket variable. This will imply that some variables in the
            // separatorFunc will be normalized over without need, and the separatorFunc will
            // be larger than necessary.
            // OBS: this code was contributed by Wei Zhou (wei@cs.ualberta.ca),
            // who also detected the problem with the original code.
            // if (bucketTree[i].clusterFunc.numberVariables() > bucketTree[i].nonConditioningVariables.size())
            for (j = 1; j < bucketTree[i].clusterFunc.numberVariables(); j++)
            {
                markNonConditioning[(bucketTree[i].clusterFunc.getVariables())[j].
                        getIndex()] = false;
            }

            // The following piece of code does the right thing (compared to the
            // piece of code above): it selects the
            // minimum number of non-conditioning variables. To use this piece
            // of code, it will be necessary to create a "normalize" method that
            // normalizes with respect to a number of variables at a time.
       /*
             for (j=0; j<bucketTree[i].clusterFunc.numberVariables(); j++) {
             markNonConditioning[ (bucketTree[i].clusterFunc.getVariables())[j].getIndex() ] = false;
             }
             for (Iterator e = bucketTree[i].nonConditioningVariables.elements(); e.hasMoreElements(); ) {
             ProbabilityVariable pv = (ProbabilityVariable)(e.nextElement());
             markNonConditioning[pv.getIndex() ] = true;
             } */
            // Update the separatorFunc.
            bucketTree[i].separatorFunc =
            bucketTree[i].child.clusterFunc.sumOut(
                    bayesNet.getProbabilityVariables(),
                    markNonConditioning);

            // Compute clusterFunc using new separatorFunc (note that if separatorFunc
            // is null, the clusterFunc had all variables already processed).
            if (bucketTree[i].separatorFunc != null)
            {
                // OBS: the method here should normalize with respect to more
                // than one probVar, to allow this algorithm to be more efficient!
                bucketTree[i].clusterFunc.normalizeFirst();
                // Now combine the clusterFunc and the separatorFunc.
                bucketTree[i].clusterFunc =
                bucketTree[i].clusterFunc.multiply(bayesNet.
                        getProbabilityVariables(),
                                                   bucketTree[i].separatorFunc);
            }

            // Mark the Bucket as DISTRIBUTED.
            bucketTree[i].bucketStatus = Bucket.Type.DISTRIBUTED;
        }
        // Indicate success.
        return true;
    }

    /**
     * Recover the maximizing variables going back through the maximizing
     * bucketTree; the variables are returned as an array of markers
     * (non-explanation variables get INVALID_INDEX).
     *
     * @return
     */
    private int[] backwardMaximization()
    {
        int i;
        int bi = bucketTree.length - 1;
        DiscreteFunction backDf;
        Bucket b = bucketTree[bi];

        // If there are no explanation variables in the BayesNet, return null
        if (b.backwardPointers == null)
        {
            return null;
        }

        // Initialize the markers for backward pointers with INVALID_INDEX
        int backwardMarkers[] = new int[bayesNet.numberVariables()];
        for (i = 0; i < backwardMarkers.length; i++)
        {
            backwardMarkers[i] = BayesNet.INVALID_INDEX;
        }

        // Initialize the marker for the last bucket
        backwardMarkers[b.probVar.getIndex()] =
        (int) (b.backwardPointers.getValue(0) + 0.5);

        // Go backwards through the bucketTree
        for (i = (bi - 1); i >= 0; i--)
        {
            if (!bucketTree[i].isExplanation())
            {
                break;
            }
            backDf = bucketTree[i].backwardPointers;
            // Skip null pointers (caused by evidence)
            if (backDf == null)
            {
                continue;
            }
            // Special treatment for bucket with only one value,
            // since it can be a bucket with only the bucket probVar left
            if (backDf.numberValues() == 1)
            {
                backwardMarkers[bucketTree[i].probVar.getIndex()] =
                (int) (backDf.getValue(0) + 0.5);
                continue;
            }
            // Process the bucket
            int valuePos = backDf.
                getPositionFromIndexes(bayesNet.getProbabilityVariables(),
                                       backwardMarkers);
            backwardMarkers[bucketTree[i].probVar.getIndex()] =
            (int) (backDf.getValue(valuePos) + 0.5);
        }

        return backwardMarkers;
    }

    /**
     * Put the separatorFunc function of a Bucket buck into the BucketTree
     * beyond the current activeBucket.
     *
     * @param bucket
     */
    private void insert(Bucket bucket)
    {
        if (bucket.separatorFunc != null)
        {
            for (int i = activeBucket; i < bucketTree.length; i++)
            {
                // Get the index for current Bucket's probVar.
                int varIndex = bucketTree[i].probVar.getIndex();
                // If separatorFunc contains a probVar in the current Bucket, then join buckets.
                if (bucket.separatorFunc.isParameter(varIndex))
                {
                    // Add separatorFunc to bucket.
                    bucketTree[i].discreteFunctions.add(bucket.separatorFunc);
                    // Update the nonConditioning variables.
                    // Go through the non-conditioning variables in the inserted
                    // Bucket.
                    for (DiscreteVariable probVar
                                 : bucket.nonConditioningVariables)
                    {
                        bucketTree[i].nonConditioningVariables.add(probVar);
                    }
                    // Take the inserted Bucket probVar out by making it
                    // CONDITIONING:
                    // Must take the probVar out as it has been eliminated already.
                    bucketTree[i].nonConditioningVariables.remove(
                            bucket.probVar);
                    // Mark parent/child relationship.
                    bucket.child = bucketTree[i];
                    bucketTree[i].parentBuckets.add(bucket);
                    return; // bail out - we're done
                }
            }
        }
    }

    /**
     * Put a DiscreteFunction into the BucketTree beyond the current
     * activeBucket.
     *
     * @param discrFunc the function we want to insert
     */
    private void insert(DiscreteFunction discrFunc)
    {
        insert(discrFunc, false);
    }

    /**
     * Put a DiscreteFunction into the BucketTree beyond the current
     * activeBucket. If wasFirstVariableCancelledByEvidence is true, then mark
     * the bucket accordingly.
     *
     * @param discrFunc
     * @param wasFirstVariableCancelledByEvidence
     */
    private void insert(DiscreteFunction discrFunc,
                        boolean wasFirstVariableCancelledByEvidence)
    {
        for (int i = activeBucket; i < bucketTree.length; i++)
        {
            int varIndex = bucketTree[i].probVar.getIndex();
            if (discrFunc.isParameter(varIndex))
            {
                bucketTree[i].discreteFunctions.add(discrFunc);
                // If the function is a ProbabilityFunction, store its first
                // variable appropriately (assuming for now that the first
                // variable is the only possible non-conditioning variable).
                if (discrFunc instanceof ProbabilityFunction &&
                    !wasFirstVariableCancelledByEvidence)
                {
                    bucketTree[i].nonConditioningVariables.add(
                            discrFunc.getVariable(0));
                }
                return; // bail out - we're done
            }
        }
    }

    /**
     * Print method for BucketTree.
     */
    public void print()
    {
        print(System.out);
    }

    /**
     * Print method for BucketTree.
     *
     * @param out output print stream
     */
    public void print(PrintStream out)
    {
        out.println("BucketTree:" +
                    "\n\tActive Bucket is " +
                    activeBucket +
                    ".");
        for (Bucket bucket : bucketTree)
        {
            bucket.print(out);
        }
        out.println("Bucket result: ");
        unnormalizedResult.print(out);
    }

    /**
     * Get the normalized result for the BucketTree.
     *
     * @return the normalized result
     */
    public ProbabilityFunction getNormalizedResult()
    {
        ProbabilityFunction auxProbFunc =
                            new ProbabilityFunction(unnormalizedResult, bayesNet);
        auxProbFunc.normalize();
        return auxProbFunc;
    }

    /**
     * Get the unnormalized result for the BucketTree.
     *
     * @return the unnormalized result
     */
    public DiscreteFunction getUnnormalizedResult()
    {
        return unnormalizedResult;
    }

    /**
     * Check whether the flag is set to use all not observed variables for
     * explanation.
     *
     * @return true is so, false otherwise
     */
    boolean isFullExplanation()
    {
        return explanationStatus.usesAllNotObservedVariables();
    }

    /**
     * Check whether we want to ignore the explanation.
     *
     * @return true if so, false otherwise
     */
    boolean isIgnoreExplanation()
    {
        return explanationStatus.isIgnore();
    }
}
