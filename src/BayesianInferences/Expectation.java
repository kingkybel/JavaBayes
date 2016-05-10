/*
 * Expectation.java
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
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class Expectation
{

    static final int EXPECTED_VALUE = 1;
    static final int SECOND_MOMENT = 2;
    static final int THIRD_MOMENT = 3;
    static final int FOURTH_MOMENT = 4;
    private static final String CLASS_NAME = Expectation.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

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
    protected boolean doProduceClusters;

    /**
     *
     */
    protected DiscreteFunction currentFunction;

    /**
     * Constructor for an Expectation.
     *
     * @param bN
     * @param dpc
     */
    public Expectation(BayesNet bN, boolean dpc)
    {
        bn = bN;
        doProduceClusters = dpc;
        initializeInference();
    }

    /**
     * Initialize the Inference object.
     */
    private void initializeInference()
    {
        inference = new Inference(bn, doProduceClusters);
    }

    /**
     * Calculation of Expectation. Expectations in all possible ways: 1)
     * Specifying or not the queried variable and the ordering 2) Specifying or
     * not the moment order
     */
    public void expectation()
    {
        // Construct the function with the values.
        ProbabilityVariable pv = bn.getProbabilityVariable(0);
        DiscreteFunction df = constructValues(pv, Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(df);
    }

    /**
     * Calculation of Expectation.
     *
     * @param queriedVariableName
     */
    public void expectation(String queriedVariableName)
    {
        // Construct the function with the values
        int index = bn.indexOfVariable(queriedVariableName);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.getProbabilityVariable(index);
        DiscreteFunction df = constructValues(pv, Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(df, queriedVariableName);
    }

    /**
     * Perform calculation of expectation given order.
     *
     * @param order
     */
    public void expectation(String order[])
    {
        // Construct the function with the values
        int index = bn.indexOfVariable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.getProbabilityVariable(index);
        DiscreteFunction df = constructValues(pv, Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(df, order);
    }

    /**
     * Calculation of Expectation.
     *
     * @param momentOrder
     */
    public void expectation(int momentOrder)
    {
        // Construct the function with the values
        ProbabilityVariable pv = bn.getProbabilityVariable(0);
        DiscreteFunction df = constructValues(pv, momentOrder);
        // Calculate expectation.
        expectation(df);
    }

    /**
     * Calculation of Expectation.
     *
     * @param momentOrder
     * @param queriedVariableName
     */
    public void expectation(int momentOrder, String queriedVariableName)
    {
        // Construct the function with the values
        int index = bn.indexOfVariable(queriedVariableName);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.getProbabilityVariable(index);
        DiscreteFunction df = constructValues(pv, momentOrder);
        // Calculate expectation.
        expectation(df, queriedVariableName);
    }

    /**
     * Calculation of expectation given order.
     *
     * @param momentOrder
     * @param order
     */
    public void expectation(int momentOrder, String order[])
    {
        // Construct the function with the values
        int index = bn.indexOfVariable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable pv = bn.getProbabilityVariable(index);
        DiscreteFunction df = constructValues(pv, momentOrder);
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
        doExpectationFromInference(df);
    }

    /**
     * Do the Expectation, assuming the input DiscreteFunction is a function
     * only of the queried variable.
     *
     * @param df
     * @param queriedVariableName
     */
    public void expectation(DiscreteFunction df,
                            String queriedVariableName)
    {
        inference.inference(queriedVariableName);
        doExpectationFromInference(df);
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
        doExpectationFromInference(df);
    }

    /*
     * Construct the utility function that produces the
     * requested moment.
     */
    private DiscreteFunction constructValues(ProbabilityVariable pv,
                                             int momentOrder)
    {
        DiscreteFunction df = pv.getNumericValues();
        if (momentOrder > 1)
        {
            for (int i = 0; i < df.numberValues(); i++)
            {
                df.setValue(i, Math.pow(df.getValue(i), momentOrder));
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
    protected void doExpectationFromInference(DiscreteFunction df)
    {
        currentFunction = df;

        ProbabilityFunction res = inference.getResult();
        results = new double[1];
        results[0] = res.expectedValue(df);
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
     * @param shouldPrintBucketTree
     */
    public void print(boolean shouldPrintBucketTree)
    {
        print(System.out, shouldPrintBucketTree);
    }

    /**
     * Print Expectation.
     *
     * @param out
     * @param shouldPrintBucketTree
     */
    public void print(PrintStream out, boolean shouldPrintBucketTree)
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
        currentFunction.print(out);
        out.println();

        if (shouldPrintBucketTree == true)
        {
            inference.bucketTree.print(out);
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
    public double[] getResults()
    {
        return (results);
    }
}
