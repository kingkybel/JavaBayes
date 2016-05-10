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
 * @author kybelksd
 */
public class BucketTree
{

    static final int MAX_OUT = 2;
    static final int SUM_OUT = 1;
    private static final String CLASS_NAME = BucketTree.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    Bucket bucketTree[]; // Array of Bucket objects.
    BayesNet bn; // BayesNet that contains the variables.

    int backwardPointers[]; // Array that stores the index of variables for minimization.

    DiscreteFunction unnormalizedResult;

    Ordering ordering;
    int explanationStatus;
    boolean doProduceClusters;

    private int activeBucket;

    /**
     * Simple constructor for BucketTree.
     *
     * @param ord
     */
    public BucketTree(Ordering ord)
    {
        this(ord, false);
    }

    /**
     * Constructor for BucketTree. Does the whole initialization; it should be
     * the only method that deals with symbolic names for variables.
     *
     * @param ord
     * @param dpc
     */
    public BucketTree(Ordering ord, boolean dpc)
    {
        int i, j, markers[];
        ProbabilityFunction pf;
        ProbabilityVariable pv;
        DiscreteVariable auxPv;
        DiscreteFunction ut;
        String order[];

        doProduceClusters = dpc;
        ordering = ord;

        // Collect information from the Ordering object.
        bn = ord.bn;
        explanationStatus = ord.explanationStatus;
        order = ord.order;

        // Indicate the first bucket to process
        activeBucket = 0;

        // Check the possibility that the query has an observed variable
        i = bn.indexOfVariable(order[order.length - 1]);
        pv = bn.getProbabilityVariable(i);
        if (pv.isObserved() == true)
        {
            pf = transformToProbabilityFunction(bn, pv);
            bucketTree = new Bucket[1];
            bucketTree[0] = new Bucket(this, pv, doProduceClusters);
            insert(pf);
        }
        else
        {
            // Initialize the bucket objects
            bucketTree = new Bucket[order.length];
            for (i = 0; i < order.length; i++)
            {
                j = bn.indexOfVariable(order[i]);
                bucketTree[i] =
                new Bucket(this, bn.getProbabilityVariable(j),
                           doProduceClusters);
            }
            // Insert the probability functions into the bucketTree;
            // first mark all functions that are actually going
            // into the bucketTree.
            markers = new int[bn.numberVariables()];
            for (i = 0; i < order.length; i++)
            {
                markers[bn.indexOfVariable(order[i])] = 1;
            }
            // Now insert functions that are marked and non-null.
            for (i = 0; i < bn.numberProbabilityFunctions(); i++)
            {
                if (markers[bn.getProbabilityFunction(i).getIndex(0)] == 1)
                {
                    pf = checkEvidence(bn.getProbabilityFunction(i));
                    if (pf != null)
                    {
                        auxPv = (bn.getProbabilityFunction(i)).
                        getVariable(0);
                        insert(pf, !pf.memberOf(auxPv.getIndex()));
                    }
                }
            }
            // Insert the utilityFunction.
            ut = bn.getUtilityFunction();
            if (ut != null)
            {
                insert(ut);
            }
        }
    }

    /*
     * Transform an observed ProbabilityVariable into a ProbabilityFunction
     * to handle the case where the query involves an observed variable.
     */
    private ProbabilityFunction
            transformToProbabilityFunction(BayesNet bn,
                                           ProbabilityVariable pv)
    {
        ProbabilityFunction pf = new ProbabilityFunction(bn, 1,
                                                         pv.numberValues(),
                                                         null);
        pf.setVariable(0, pv);
        int indexOfValue = pv.getObservedIndex();
        pf.setValue(indexOfValue, 1.0);
        return (pf);
    }

    /*
     * Eliminates all variables defined as evidence.
     * The order of the variables that are not eliminated is
     * the same order in the original function.
     */
    private ProbabilityFunction checkEvidence(ProbabilityFunction pf)
    {
        int i, j, k, v, auxI;
        boolean markers[] = new boolean[bn.numberVariables()];
        int n = buildEvidenceMarkers(pf, markers);

        // Handle special cases
        if (n == 0)
        {
            return (null); // No variable remains
        }
        if (n == pf.numberVariables())
        {
            return (pf); // No relevant evidence
        }

        // Calculate necessary quantities in such a
        // way that the order of variables in the original
        // function is not altered.
        int joinedIndexes[] = new int[n];
        for (i = 0, j = 0, v = 1; i < pf.numberVariables(); i++)
        {
            auxI = pf.getVariable(i).getIndex();
            if (markers[auxI] == true)
            {
                joinedIndexes[j] = auxI;
                j++;
                v *= bn.getProbabilityVariable(auxI).numberValues();
            }
        }

        // Create new function to be filled with joined variables
        ProbabilityFunction newPf = new ProbabilityFunction(bn, n, v, null);
        for (i = 0; i < n; i++)
        {
            newPf.setVariable(i, bn.
                              getProbabilityVariable(joinedIndexes[i]));
        }

        // Loop through the values
        checkEvidenceLoop(newPf, pf);

        return (newPf);
    }

    /*
     * Build an array of markers. The marker for a
     * variable is true only if the variable is present in the
     * input ProbabilityFunction pf and is not observed.
     * Even explanatory variables can be observed and taken as
     * evidence.
     */
    private int buildEvidenceMarkers(ProbabilityFunction pf,
                                     boolean markers[])
    {
        int i, n;
        // Initialize the markers
        for (i = 0; i < markers.length; i++)
        {
            markers[i] = false;
        }
        // Insert the variables of the ProbabilityFunction
        for (i = 0; i < pf.numberVariables(); i++)
        {
            markers[pf.getIndex(i)] = true;
        }
        // Take the evidence out
        for (i = 0; i < bn.numberVariables(); i++)
        {
            if (bn.getProbabilityVariable(i).isObserved())
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

    /*
     * Obtain the values for the evidence plus function.
     */
    private void checkEvidenceLoop(ProbabilityFunction newPf,
                                   ProbabilityFunction pf)
    {
        int i, j, k, l, m, p, last, current;
        int indexes[] = new int[bn.numberVariables()];
        int valueLengths[] = new int[bn.numberVariables()];

        for (i = 0; i < bn.numberVariables(); i++)
        {
            indexes[i] = 0;
            valueLengths[i] = bn.getProbabilityVariable(i).numberValues();
        }
        for (i = 0; i < bn.numberVariables(); i++)
        {
            if (bn.getProbabilityVariable(i).isObserved())
            {
                indexes[i] = bn.getProbabilityVariable(i).getObservedIndex();
            }
        }
        last = newPf.numberVariables() - 1;
        for (i = 0; i < newPf.numberValues(); i++)
        {
            p = newPf.getPositionFromIndexes(indexes);
            newPf.setValue(p, pf.evaluate(indexes));

            indexes[newPf.getIndex(last)]++;
            for (j = last; j > 0; j--)
            {
                current = newPf.getIndex(j);
                if (indexes[current] >= valueLengths[current])
                {
                    indexes[current] = 0;
                    indexes[newPf.getIndex(j - 1)]++;
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
        boolean markNonConditioning[] = new boolean[bn.numberVariables()];

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
        // Third, this method is used only if doProduceClusters is true.
        if (doProduceClusters == false)
        {
            return (false);
        }
        // Fourth, this method is use only if no explanatory variable was max'ed out.
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
            // necessary. It will count as "conditioning" any variable in the cluster
            // except the bucket variable. This will imply that some variables in the
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
            bucketTree[i].separator = bucketTree[i].child.cluster.sumOut(bn.
            getProbabilityVariables(),
                                                                         markNonConditioning);

            // Compute cluster using new separator (note that if separator
            // is null, the cluster had all variables already processed).
            if (bucketTree[i].separator != null)
            {
                // OBS: the method here should normalize with respect to more
                // than one variable, to allow this algorithm to be more efficient!
                bucketTree[i].cluster.normalizeFirst();
                // Now combine the cluster and the separator.
                bucketTree[i].cluster =
                bucketTree[i].cluster.multiply(bn.getProbabilityVariables(),
                                               bucketTree[i].separator);
            }

            // Mark the Bucket as DISTRIBUTED.
            bucketTree[i].bucketStatus = Bucket.DISTRIBUTED;
        }
        // Indicate success.
        return (true);
    }

    /*
     * Recover the maximizing variables going back through the
     * maximizing bucketTree; the variables are returned as an array
     * of markers (non-explanation variables get INVALID_INDEX).
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
        int backwardMarkers[] = new int[bn.numberVariables()];
        for (i = 0; i < backwardMarkers.length; i++)
        {
            backwardMarkers[i] = BayesNet.INVALID_INDEX;
        }

        // Initialize the marker for the last bucket
        backwardMarkers[b.variable.getIndex()] =
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
            // since it can be a bucket with only the bucket variable left
            if (backDf.numberValues() == 1)
            {
                backwardMarkers[bucketTree[i].variable.getIndex()] =
                (int) (backDf.getValue(0) + 0.5);
                continue;
            }
            // Process the bucket
            j = backDf.
            getPositionFromIndexes(bn.getProbabilityVariables(),
                                   backwardMarkers);
            backwardMarkers[bucketTree[i].variable.getIndex()] =
            (int) (backDf.getValue(j) + 0.5);
        }

        return (backwardMarkers);
    }

    /*
     * Put the separator function of a Bucket buck
     * into the BucketTree beyond the current
     * activeBucket.
     */
    private void insert(Bucket buck)
    {
        int i, index;
        Bucket b;

        if (buck.separator == null)
        {
            return;
        }

        for (i = activeBucket; i < bucketTree.length; i++)
        {
            // Get the index for current Bucket's variable.
            index = bucketTree[i].variable.getIndex();
            // If separator contains a variable in the current Bucket, then join buckets.
            if (buck.separator.memberOf(index))
            {
                // Add separator to bucket.
                bucketTree[i].discreteFunctions.add(buck.separator);
                // Update the nonConditioning variables.
                // Go through the non-conditioning variables in the inserted Bucket.
                for (Object e : buck.nonConditioningVariables)
                {
                    bucketTree[i].nonConditioningVariables.add(e);
                }
                // Take the inserted Bucket variable out by making it CONDITIONING:
                // Must take the variable out as it has been eliminated already.
                bucketTree[i].nonConditioningVariables.remove(
                        buck.variable);
                // Mark parent/child relationship.
                buck.child = bucketTree[i];
                bucketTree[i].parents.add(buck);
                return;
            }
        }
    }

    /*
     * Put a DiscreteFunction into the BucketTree beyond the current
     * activeBucket.
     */
    private void insert(DiscreteFunction df)
    {
        insert(df, false);
    }

    /*
     * Put a DiscreteFunction into the BucketTree beyond the current
     * activeBucket. If wasFirstVariableCancelledByEvidence is true,
     * then mark the bucket accordingly.
     */
    private void insert(DiscreteFunction df,
                        boolean wasFirstVariableCancelledByEvidence)
    {
        int i, index;
        Bucket b;
        for (i = activeBucket; i < bucketTree.length; i++)
        {
            index = bucketTree[i].variable.getIndex();
            if (df.memberOf(index))
            {
                bucketTree[i].discreteFunctions.add(df);
                // If the function is a ProbabilityFunction, store its
                // first variable appropriately (assuming for now that
                // the first variable is the only possible non-conditioning variable).
                if ((df instanceof ProbabilityFunction) &&
                    (!wasFirstVariableCancelledByEvidence))
                {
                    bucketTree[i].nonConditioningVariables.add(df.
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
                            new ProbabilityFunction(unnormalizedResult, bn);
        auxPf.normalize();
        return (auxPf);
    }

    /* *************************************************************** */
    /*  Methods that allow basic manipulation of non-public variables  */
    /* *************************************************************** */
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
