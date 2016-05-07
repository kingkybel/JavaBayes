package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
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
    private static final Logger LOG =
    Logger.getLogger(Inference.class.getName());

    /**
     *
     */
    protected BayesNet bn;

    /**
     *
     */
    protected BucketTree bucket_tree;

    /**
     *
     */
    protected Bucket bucket_for_variable[];

    /**
     *
     */
    protected ArrayList bucket_forest;

    /**
     *
     */
    protected ProbabilityFunction result;

    /**
     *
     */
    protected boolean do_produce_clusters;


    /*
     * Constructor for an Inference.
     */

    /**
     *
     * @param b_n
     * @param dpc
     */
    
    public Inference(BayesNet b_n, boolean dpc)
    {
        bn = b_n;
        bucket_for_variable = new Bucket[b_n.number_variables()];
        bucket_forest = new ArrayList();
        do_produce_clusters = dpc;
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
     * @param queried_variable_name
     */
    protected void inference(String queried_variable_name)
    {
        if (do_produce_clusters)
        { // If clusters are generated:
            int index_queried = bn.index_of_variable(queried_variable_name);
            if (index_queried != BayesNet.INVALID_INDEX)
            { // If the queried_variable_name is valid:
                Bucket buck = bucket_for_variable[index_queried];
                // If the variable has no Bucket or a Bucket without valid cluster:
                if ((buck == null) || (buck.cluster == null))
                {
                    inference(new Ordering(bn, queried_variable_name,
                                           IGNORE_EXPLANATION,
                                           Ordering.MINIMUM_WEIGHT));
                }
                else
                { // If variable already has a Bucket:
                    // Get the BucketTree.
                    bucket_tree = buck.bucket_tree;
                    // Note that the method bucket_tree.distribute() below must return true:
                    //     - the bucket_tree is constructed with IGNORE_EXPLANATION.
                    //     - this block only runs if do_produce_clusters is true.
                    if (buck.bucket_status != Bucket.DISTRIBUTED)
                    {
                        if (buck ==
                            bucket_tree.bucket_tree[bucket_tree.bucket_tree.length -
                                                    1])
                        {
                            bucket_tree.reduce(); // If Bucket is the last bucket, then just reduce;
                        }
                        else
                        {
                            // if not, then distribute.
                            bucket_tree.distribute();
                        }
                    }
                    // Now process the cluster in the Bucket.
                    System.out.println("ARRIVED HERE!");
                    buck.reduce_cluster();
                    // And then get the result
                    System.out.println("AND HERE TOO!");
                    result = bucket_tree.get_normalized_result();
                }
            }
            else
            { // If the queried_variable_name is invalid:
                inference(new Ordering(bn, (String) null,
                                       IGNORE_EXPLANATION,
                                       Ordering.MINIMUM_WEIGHT));
            }
        }
        else
        { // If no cluster is generated:
            inference(new Ordering(bn, queried_variable_name,
                                   IGNORE_EXPLANATION, Ordering.MINIMUM_WEIGHT));
        }
    }

    /**
     * Calculation of marginal posterior distribution using a given ordering,
     * and an arbitrary BayesNet.
     * @param order
     */
    protected void inference(String order[])
    {
        inference(new Ordering(bn, order, IGNORE_EXPLANATION));
    }

    /*
     * Calculation of marginal posterior distribution.
     */
    private void inference(Ordering or)
    {
        // Create the Ordering and the BucketTree.
        bucket_tree = new BucketTree(or, do_produce_clusters);
        // Add the new BucketTree to the bucket_forest and update bucket_for_variable.
        if (do_produce_clusters)
        {
            add_bucket_tree();
        }
        // Generate the result by reducing the BucketTree.
        bucket_tree.reduce();
        result = bucket_tree.get_normalized_result();
    }

    /*
     * Add a BucketTree to the bucket_forest and
     * update the bucket_for_variable array.
     */
    private void add_bucket_tree()
    {
        Bucket buck;
        // Add the current BucketTree to the bucket_forest.
        bucket_forest.add(bucket_tree);
        // Put the buckets in correspondence with the variables.
        for (int i = 0; i < bucket_tree.bucket_tree.length; i++)
        {
            buck = bucket_tree.bucket_tree[i];
            bucket_for_variable[buck.variable.get_index()] = buck;
        }
    }

    /*
     * Generic, auxiliary methods.
     */
    /**
     * Print the Inference.
     */
    public void print()
    {
        print(System.out, (boolean) true);
    }

    /**
     * Print the Inference.
     * @param out
     */
    public void print(PrintStream out)
    {
        print(out, (boolean) true);
    }

    /**
     * Print the Inference.
     * @param should_print_bucket_tree
     */
    public void print(boolean should_print_bucket_tree)
    {
        print(System.out, should_print_bucket_tree);
    }

    /**
     * Print the Inference.
     * @param out
     * @param should_print_bucket_tree
     */
    public void print(PrintStream out, boolean should_print_bucket_tree)
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

        if (should_print_bucket_tree == true)
        {
            bucket_tree.print(out);
        }
        out.println();

        result.print(out);
    }

    /* ************************************************************* */
    /* Methods that allow basic manipulation of non-public variables */
    /* ************************************************************* */
    /**
     * Get the BucketTree.
     * @return 
     */
    public BucketTree get_bucket_tree()
    {
        return (bucket_tree);
    }

    /**
     * Get the BayesNet.
     * @return 
     */
    public BayesNet get_bayes_net()
    {
        return (bn);
    }

    /**
     * Get the current result of the Inference.
     * @return 
     */
    public ProbabilityFunction get_result()
    {
        return (result);
    }

    /**
     * Get the status of the clustering process.
     * @return 
     */
    public boolean areClustersProduced()
    {
        return (do_produce_clusters);
    }
}
