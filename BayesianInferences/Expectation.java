package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public class Expectation
{

    static final int EXPECTED_VALUE = 1;
    static final int SECOND_MOMENT = 2;
    static final int THIRD_MOMENT = 3;
    static final int FOURTH_MOMENT = 4;
    private static final Logger LOG =
    Logger.getLogger(Expectation.class.getName());

    /**
     *
     */
    protected BayesNet bn;

    /**
     *
     */
    protected Inference inference;

    /**
     *
     */
    protected double results[];

    /**
     *
     */
    protected boolean do_produce_clusters;

    /**
     *
     */
    protected DiscreteFunction current_function;

    /**
     * Constructor for an Expectation.
     *
     * @param b_n
     * @param dpc
     */
    public Expectation(BayesNet b_n, boolean dpc)
    {
        bn = b_n;
        do_produce_clusters = dpc;
        initialize_inference();
    }

    /**
     * Initialize the Inference object.
     */
    private void initialize_inference()
    {
        inference = new Inference(bn, do_produce_clusters);
    }

    /**
     * Calculation of Expectation. Expectations in all possible ways: 1)
     * Specifying or not the queried variable and the ordering 2) Specifying or
     * not the moment order
     */
    public void expectation()
    {
        // Construct the function with the values.
        ProbabilityVariable pv = bn.get_probability_variable(0);
        DiscreteFunction df = construct_values(pv, Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(df);
    }

    /**
     * Calculation of Expectation.
     *
     * @param queried_variable_name
     */
    public void expectation(String queried_variable_name)
    {
        // Construct the function with the values
        int index = bn.index_of_variable(queried_variable_name);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.get_probability_variable(index);
        DiscreteFunction df = construct_values(pv, Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(df, queried_variable_name);
    }

    /**
     * Perform calculation of expectation given order.
     *
     * @param order
     */
    public void expectation(String order[])
    {
        // Construct the function with the values
        int index = bn.index_of_variable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.get_probability_variable(index);
        DiscreteFunction df = construct_values(pv, Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(df, order);
    }

    /**
     * Calculation of Expectation.
     *
     * @param moment_order
     */
    public void expectation(int moment_order)
    {
        // Construct the function with the values
        ProbabilityVariable pv = bn.get_probability_variable(0);
        DiscreteFunction df = construct_values(pv, moment_order);
        // Calculate expectation.
        expectation(df);
    }

    /**
     * Calculation of Expectation.
     *
     * @param moment_order
     * @param queried_variable_name
     */
    public void expectation(int moment_order, String queried_variable_name)
    {
        // Construct the function with the values
        int index = bn.index_of_variable(queried_variable_name);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.get_probability_variable(index);
        DiscreteFunction df = construct_values(pv, moment_order);
        // Calculate expectation.
        expectation(df, queried_variable_name);
    }

    /**
     * Calculation of expectation given order.
     *
     * @param moment_order
     * @param order
     */
    public void expectation(int moment_order, String order[])
    {
        // Construct the function with the values
        int index = bn.index_of_variable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.get_probability_variable(index);
        DiscreteFunction df = construct_values(pv, moment_order);
        // Calculate expectation.
        expectation(df, order);
    }

    /**
     * Do the Expectation, assuming the input DiscreteFunction is a function
     * only of the queried variable.
     *
     * @param df
     */
    public void expectation(DiscreteFunction df)
    {
        inference.inference();
        do_expectation_from_inference(df);
    }

    /**
     * Do the Expectation, assuming the input DiscreteFunction is a function
     * only of the queried variable.
     *
     * @param df
     * @param queried_variable_name
     */
    public void expectation(DiscreteFunction df,
                            String queried_variable_name)
    {
        inference.inference(queried_variable_name);
        do_expectation_from_inference(df);
    }

    /**
     * Do the Expectation given order, assuming the input DiscreteFunction is a
     * function only of the queried variable.
     *
     * @param df
     * @param order
     */
    public void expectation(DiscreteFunction df, String order[])
    {
        inference.inference(order);
        do_expectation_from_inference(df);
    }

    /*
     * Construct the utility function that produces the
     * requested moment.
     */
    private DiscreteFunction construct_values(ProbabilityVariable pv,
                                              int moment_order)
    {
        DiscreteFunction df = pv.get_numeric_values();
        if (moment_order > 1)
        {
            for (int i = 0; i < df.number_values(); i++)
            {
                df.set_value(i, Math.pow(df.get_value(i), moment_order));
            }
        }
        return (df);
    }

    /*
     * Do the expectations from inference.
     */
    /**
     *
     * @param df
     */
    protected void do_expectation_from_inference(DiscreteFunction df)
    {
        current_function = df;

        ProbabilityFunction res = inference.get_result();
        results = new double[1];
        results[0] = res.expected_value(df);
    }

    /*
     * Generic, auxiliary methods.
     */
    /**
     * Print Expectation.
     */
    public void print()
    {
        print(System.out, (boolean) true);
    }

    /**
     * Print Expectation.
     *
     * @param out
     */
    public void print(PrintStream out)
    {
        print(out, (boolean) true);
    }

    /**
     * Print Expectation.
     *
     * @param should_print_bucket_tree
     */
    public void print(boolean should_print_bucket_tree)
    {
        print(System.out, should_print_bucket_tree);
    }

    /**
     * Print Expectation.
     *
     * @param out
     * @param should_print_bucket_tree
     */
    public void print(PrintStream out, boolean should_print_bucket_tree)
    {
        int i, bp[];
        ProbabilityVariable pv;

        // Print it all.
        out.print("Posterior expectation: [");
        for (i = 0; i < results.length; i++)
        {
            out.print(results[i] + " ");
        }
        out.println("], for function:");
        current_function.print(out);
        out.println();

        if (should_print_bucket_tree == true)
        {
            inference.bucket_tree.print(out);
        }
    }

    /* ************************************************************* */
    /* Methods that allow basic manipulation of non-public variables */
    /* ************************************************************* */
    /**
     * Get the results of Expectation.
     *
     * @return
     */
    public double[] get_results()
    {
        return (results);
    }
}
