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
    protected BayesNet bayesNet;

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
    protected boolean isProducingClusters;

    /**
     *
     */
    protected DiscreteFunction currentFunction;

    /**
     * Constructor for an Expectation.
     *
     * @param bayesNet
     * @param isProducingClusters
     */
    public Expectation(BayesNet bayesNet, boolean isProducingClusters)
    {
        this.bayesNet = bayesNet;
        this.isProducingClusters = isProducingClusters;
        initializeInference();
    }

    /**
     * Initialize the Inference object.
     */
    private void initializeInference()
    {
        inference = new Inference(bayesNet, isProducingClusters);
    }

    /**
     * Calculation of Expectation. Expectations in all possible ways: 1)
     * Specifying or not the queried variable and the ordering 2) Specifying or
     * not the moment order
     */
    public void expectation()
    {
        // Construct the function with the values.
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(0);
        DiscreteFunction discrFunc = constructValues(probVar,
                                                     Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(discrFunc);
    }

    /**
     * Calculation of Expectation.
     *
     * @param queriedVariableName
     */
    public void expectation(String queriedVariableName)
    {
        // Construct the function with the values
        int index = bayesNet.indexOfVariable(queriedVariableName);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(index);
        DiscreteFunction discrFunc = constructValues(probVar,
                                                     Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(discrFunc, queriedVariableName);
    }

    /**
     * Perform calculation of expectation given order.
     *
     * @param order
     */
    public void expectation(String order[])
    {
        // Construct the function with the values
        int index = bayesNet.indexOfVariable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(index);
        DiscreteFunction discrFunc = constructValues(probVar,
                                                     Expectation.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(discrFunc, order);
    }

    /**
     * Calculation of Expectation.
     *
     * @param momentOrder
     */
    public void expectation(int momentOrder)
    {
        // Construct the function with the values
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(0);
        DiscreteFunction discrFunc = constructValues(probVar, momentOrder);
        // Calculate expectation.
        expectation(discrFunc);
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
        int index = bayesNet.indexOfVariable(queriedVariableName);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(index);
        DiscreteFunction discrFunc = constructValues(probVar, momentOrder);
        // Calculate expectation.
        expectation(discrFunc, queriedVariableName);
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
        int index = bayesNet.indexOfVariable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(index);
        DiscreteFunction discrFunc = constructValues(probVar, momentOrder);
        // Calculate expectation.
        expectation(discrFunc, order);
    }

    /**
     * Do the Expectation, assuming the input DiscreteFunction is a function
     * only of the queried variable.
     *
     * @param discrFunc
     */
    public void expectation(DiscreteFunction discrFunc)
    {
        inference.inference();
        doExpectationFromInference(discrFunc);
    }

    /**
     * Do the Expectation, assuming the input DiscreteFunction is a function
     * only of the queried variable.
     *
     * @param discrFunc
     * @param queriedVariableName
     */
    public void expectation(DiscreteFunction discrFunc,
                            String queriedVariableName)
    {
        inference.inference(queriedVariableName);
        doExpectationFromInference(discrFunc);
    }

    /**
     * Do the Expectation given order, assuming the input DiscreteFunction is a
     * function only of the queried variable.
     *
     * @param discrFunc
     * @param order
     */
    public void expectation(DiscreteFunction discrFunc, String order[])
    {
        inference.inference(order);
        doExpectationFromInference(discrFunc);
    }

    /**
     * Construct the utility function that produces the requested moment.
     */
    private DiscreteFunction constructValues(ProbabilityVariable probVar,
                                             int momentOrder)
    {
        DiscreteFunction discrFunc = probVar.getNumericValues();
        if (momentOrder > 1)
        {
            for (int i = 0; i < discrFunc.numberValues(); i++)
            {
                discrFunc.setValue(i, Math.pow(discrFunc.getValue(i),
                                               momentOrder));
            }
        }
        return (discrFunc);
    }

    /**
     * Do the expectations discrFunc inference.
     *
     * @param discrFunc
     */
    protected void doExpectationFromInference(DiscreteFunction discrFunc)
    {
        currentFunction = discrFunc;

        ProbabilityFunction res = inference.getResult();
        results = new double[1];
        results[0] = res.expectedValue(discrFunc);
    }

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
        ProbabilityVariable probVar;

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
