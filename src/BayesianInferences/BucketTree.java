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
 *
 * @author Fabio G. Cozman
 */
public class BucketTree
{

    static final int MAX_OUT = 2;
    static final int SUM_OUT = 1;
    private static final String CLASS_NAME = BucketTree.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    Bucket bucketTree[]; // Array of Bucket objects.
    BayesNet bayesNet; // BayesNet that contains the variables.

    int backwardPointers[]; // Array that stores the index of variables for minimization.

    DiscreteFunction unnormalizedResult;

    Ordering ordering;
    int explanationStatus;
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
                new Bucket(this, bayesNet.getProbabilityVariable(j),
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
                        insert(probFunc, !probFunc.memberOf(auxPv.getIndex()));
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
     * handle the case where the query involves an observed probVar.
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
        return (probFunc);
    }

    /**
     * Eliminates all variables defined as evidence. The order of the variables
     * that are not eliminated is the same order in the original function.
     */
    private ProbabilityFunction checkEvidence(ProbabilityFunction probFunc)
    {
        int i, j, k, v, auxI;
        boolean markers[] = new boolean[bayesNet.numberVariables()];
        int n = buildEvidenceMarkers(probFunc, markers);

        // Handle special cases
        if (n == 0)
        {
            return (null); // No probVar remains
        }
        if (n == probFunc.numberVariables())
        {
            return (probFunc); // No relevant evidence
        }

        // Calculate necessary quantities in such a
        // way that the order of variables in the original
        // function is not altered.
        int joinedIndexes[] = new int[n];
        for (i = 0, j = 0, v = 1; i < probFunc.numberVariables(); i++)
        {
            auxI = probFunc.getVariable(i).getIndex();
            if (markers[auxI] == true)
            {
                joinedIndexes[j] = auxI;
                j++;
                v *= bayesNet.getProbabilityVariable(auxI).numberValues();
            }
        }

        // Create new function to be filled with joined variables
        ProbabilityFunction newPf =
                            new ProbabilityFunction(bayesNet, n, v, null);
        for (i = 0; i < n; i++)
        {
            newPf.setVariable(i, bayesNet.
                              getProbabilityVariable(joinedIndexes[i]));
        }

        // Loop through the values
        checkEvidenceLoop(newPf, probFunc);

        return (newPf);
    }

    /**
     * Build an array of markers. The marker for a probVar is true only if the
     * probVar is present in the input ProbabilityFunction probFunc and is not
     * observed. Even explanatory variables can be observed and taken as
     * evidence.
     */
    private int buildEvidenceMarkers(ProbabilityFunction probFunc,
                                     boolean markers[])
    {
        int i, n;
        // Initialize the markers
        for (i = 0; i < markers.length; i++)
        {
            markers[i] = false;
        }
        // Insert the variables of the ProbabilityFunction
        for (i = 0; i < probFunc.numberVariables(); i++)
        {
            markers[probFunc.getIndex(i)] = true;
        }
        // Take the evidence out
        for (i = 0; i < bayesNet.numberVariables(); i++)
        {
            if (bayesNet.getProbabilityVariable(i).isObserved())
            {
                markers[i] = false;
            }
        }
        // Count how many variables remain
        n = 0;
        for (i = 0; i < markers.length; i++)
        {
            if (markers[i] == true)
            {
                n++;
            }
        }
        return (n);
    }

    /**
     * Obtain the values for the evidence plus function.
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
        bucketTree[i].bucketStatus = Bucket.DISTRIBUTED;
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
            return (true);
        }
        // Third, this method is used only if isProducingClusters is true.
        if (isProducingClusters == false)
        {
            return (false);
        }
        // Fourth, this method is use only if no explanatory probVar was max'ed out.
        if (backwardPointers != null)
        {
            return (false);
        }

        // Go through the Bucket objects, from bottom to top,
        // to compute the new separator and cluster for each bucket.
        for (i = (last - 1); i >= 0; i--)
        { // Start from (last-1); last does not have child.
            // Check whether the Bucket has any valid content.
            if (bucketTree[i].cluster == null)
            {
                break;
            }
            // Take the non-conditioning variables in a boolean array.
            for (j = 0; j < markNonConditioning.length; j++)
            {
                markNonConditioning[j] = true;
            }
            // OBS: The following piece of code will actually be less efficient than
            // necessary. It will count as "conditioning" any probVar in the cluster
            // except the bucket probVar. This will imply that some variables in the
            // separator will be normalized over without need, and the separator will
            // be larger than necessary.
            // OBS: this code was contributed by Wei Zhou (wei@cs.ualberta.ca),
            // who also detected the problem with the original code.
            // if (bucketTree[i].cluster.numberVariables() > bucketTree[i].nonConditioningVariables.size())
            for (j = 1; j < bucketTree[i].cluster.numberVariables(); j++)
            {
                markNonConditioning[(bucketTree[i].cluster.getVariables())[j].
                        getIndex()] = false;
            }

       // The following piece of code does the right thing (compared to the
            // piece of code above): it selects the
            // minimum number of non-conditioning variables. To use this piece
            // of code, it will be necessary to create a "normalize" method that
            // normalizes with respect to a number of variables at at time.
       /*
             for (j=0; j<bucketTree[i].cluster.numberVariables(); j++) {
             markNonConditioning[ (bucketTree[i].cluster.getVariables())[j].getIndex() ] = false;
             }
             for (Iterator e = bucketTree[i].nonConditioningVariables.elements(); e.hasMoreElements(); ) {
             ProbabilityVariable pv = (ProbabilityVariable)(e.nextElement());
             markNonConditioning[pv.getIndex() ] = true;
             } */
            // Update the separator.
            bucketTree[i].separator = bucketTree[i].child.cluster.sumOut(
            bayesNet.
            getProbabilityVariables(),
            markNonConditioning);

            // Compute cluster using new separator (note that if separator
            // is null, the cluster had all variables already processed).
            if (bucketTree[i].separator != null)
            {
                // OBS: the method here should normalize with respect to more
                // than one probVar, to allow this algorithm to be more efficient!
                bucketTree[i].cluster.normalizeFirst();
                // Now combine the cluster and the separator.
                bucketTree[i].cluster =
                bucketTree[i].cluster.multiply(bayesNet.
                        getProbabilityVariables(),
                                               bucketTree[i].separator);
            }

            // Mark the Bucket as DISTRIBUTED.
            bucketTree[i].bucketStatus = Bucket.DISTRIBUTED;
        }
        // Indicate success.
        return (true);
    }

    /**
     * Recover the maximizing variables going back through the maximizing
     * bucketTree; the variables are returned as an array of markers
     * (non-explanation variables get INVALID_INDEX).
     */
    private int[] backwardMaximization()
    {
        int i, j;
        int bi = bucketTree.length - 1;
        DiscreteFunction backDf;
        Bucket b = bucketTree[bi];

        // If there are no explanation variables in the BayesNet, return null
        if (b.backwardPointers == null)
        {
            return (null);
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
            j = backDf.
            getPositionFromIndexes(bayesNet.getProbabilityVariables(),
                                   backwardMarkers);
            backwardMarkers[bucketTree[i].probVar.getIndex()] =
            (int) (backDf.getValue(j) + 0.5);
        }

        return (backwardMarkers);
    }

    /**
     * Put the separator function of a Bucket buck into the BucketTree beyond
     * the current activeBucket.
     */
    private void insert(Bucket bucket)
    {
        int i, index;

        if (bucket.separator == null)
        {
            return;
        }

        for (i = activeBucket; i < bucketTree.length; i++)
        {
            // Get the index for current Bucket's probVar.
            index = bucketTree[i].probVar.getIndex();
            // If separator contains a probVar in the current Bucket, then join buckets.
            if (bucket.separator.memberOf(index))
            {
                // Add separator to bucket.
                bucketTree[i].discreteFunctions.add(bucket.separator);
                // Update the nonConditioning variables.
                // Go through the non-conditioning variables in the inserted Bucket.
                for (Object e : bucket.nonConditioningVariables)
                {
                    bucketTree[i].nonConditioningVariables.add(e);
                }
                // Take the inserted Bucket probVar out by making it CONDITIONING:
                // Must take the probVar out as it has been eliminated already.
                bucketTree[i].nonConditioningVariables.remove(bucket.probVar);
                // Mark parent/child relationship.
                bucket.child = bucketTree[i];
                bucketTree[i].parents.add(bucket);
                return;
            }
        }
    }

    /**
     * Put a DiscreteFunction into the BucketTree beyond the current
     * activeBucket.
     */
    private void insert(DiscreteFunction discrFunc)
    {
        insert(discrFunc, false);
    }

    /**
     * Put a DiscreteFunction into the BucketTree beyond the current
     * activeBucket. If wasFirstVariableCancelledByEvidence is true, then mark
     * the bucket accordingly.
     */
    private void insert(DiscreteFunction discrFunc,
                        boolean wasFirstVariableCancelledByEvidence)
    {
        int i, index;
        for (i = activeBucket; i < bucketTree.length; i++)
        {
            index = bucketTree[i].probVar.getIndex();
            if (discrFunc.memberOf(index))
            {
                bucketTree[i].discreteFunctions.add(discrFunc);
                // If the function is a ProbabilityFunction, store its
                // first probVar appropriately (assuming for now that
                // the first probVar is the only possible non-conditioning probVar).
                if ((discrFunc instanceof ProbabilityFunction) &&
                    (!wasFirstVariableCancelledByEvidence))
                {
                    bucketTree[i].nonConditioningVariables.add(discrFunc.
                            getVariable(0));
                }
                return;
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
     * @param out
     */
    public void print(PrintStream out)
    {
        out.println("BucketTree:" + "\n\tActive Bucket is " + activeBucket +
                    ".");
        for (int i = 0; i < bucketTree.length; i++)
        {
            bucketTree[i].print(out);
        }
        out.println("Bucket result: ");
        unnormalizedResult.print(out);
    }

    /**
     * Get the normalized result for the BucketTree.
     *
     * @return
     */
    public ProbabilityFunction getNormalizedResult()
    {
        ProbabilityFunction auxPf =
                            new ProbabilityFunction(unnormalizedResult, bayesNet);
        auxPf.normalize();
        return (auxPf);
    }

    /**
     * Get the unnormalizedResult for the BucketTree.
     *
     * @return
     */
    public DiscreteFunction getUnnormalizedResult()
    {
        return (unnormalizedResult);
    }
}
