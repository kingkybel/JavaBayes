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

    private static final String CLASS_NAME = Expectation.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public enum Type
    {

        EXPECTED_VALUE(1),
        SECOND_MOMENT(2),
        THIRD_MOMENT(3),
        FOURTH_MOMENT(4);

        private Type(int value)
        {
            this.value = value;

        }

        public int order()
        {
            return value;
        }
        int value;

    }

    private BayesNet bayesNet;
    private Inference inference;
    private double results[];
    private boolean isProducingClusters;
    private DiscreteFunction currentFunction;

    /**
     * Constructor for an Expectation.
     *
     * @param bayesNet            the underlying Bayesian network
     * @param isProducingClusters
     */
    public Expectation(BayesNet bayesNet, boolean isProducingClusters)
    {
        this.bayesNet = bayesNet;
        this.isProducingClusters = isProducingClusters;
        initializeInference();
    }

    public void setResults(double expectedValue)
    {
        results = new double[1];
        results[0] = expectedValue;
    }

    public void setResults(double min, double max)
    {
        results = new double[2];
        results[0] = min;
        results[1] = max;
    }

    public void setResults(double[] results)
    {
        this.results = results;
    }

    public BayesNet getBayesNet()
    {
        return bayesNet;
    }

    public void setBayesNet(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    public Inference getInference()
    {
        return inference;
    }

    public void setInference(Inference inference)
    {
        this.inference = inference;
    }

    public boolean isProducingClusters()
    {
        return isProducingClusters;
    }

    public void setProducingClusters(boolean isProducingClusters)
    {
        this.isProducingClusters = isProducingClusters;
    }

    public DiscreteFunction getCurrentFunction()
    {
        return currentFunction;
    }

    public void setCurrentFunction(DiscreteFunction currentFunction)
    {
        this.currentFunction = currentFunction;
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
                                                     Type.EXPECTED_VALUE);
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
                                                     Type.EXPECTED_VALUE);
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
                                                     Type.EXPECTED_VALUE);
        // Calculate expectation.
        expectation(discrFunc, order);
    }

    /**
     * Calculation of Expectation.
     *
     * @param moment
     */
    public void expectation(Type moment)
    {
        // Construct the function with the values
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(0);
        DiscreteFunction discrFunc = constructValues(probVar, moment);
        // Calculate expectation.
        expectation(discrFunc);
    }

    /**
     * Calculation of Expectation.
     *
     * @param moment
     * @param queriedVariableName
     */
    public void expectation(Type moment, String queriedVariableName)
    {
        // Construct the function with the values
        int index = bayesNet.indexOfVariable(queriedVariableName);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(index);
        DiscreteFunction discrFunc = constructValues(probVar, moment);
        // Calculate expectation.
        expectation(discrFunc, queriedVariableName);
    }

    /**
     * Calculation of expectation given order.
     *
     * @param moment
     * @param order
     */
    public void expectation(Type moment, String order[])
    {
        // Construct the function with the values
        int index = bayesNet.indexOfVariable(order[order.length - 1]);
        if (index == BayesNet.INVALID_INDEX)
        {
            expectation();
            return;
        }
        ProbabilityVariable probVar = bayesNet.getProbabilityVariable(index);
        DiscreteFunction discrFunc = constructValues(probVar, moment);
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
     *
     * @param probVar a probability variable
     * @param moment  moment type
     * @return discrete utility function
     */
    private DiscreteFunction constructValues(ProbabilityVariable probVar,
                                             Type moment)
    {
        DiscreteFunction discrFunc = probVar.getNumericValues();
        if (moment != Type.EXPECTED_VALUE)
        {
            for (int i = 0; i < discrFunc.numberValues(); i++)
            {
                discrFunc.setValue(i,
                                   Math.pow(discrFunc.getValue(i),
                                            moment.order()));
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
     * @param out output print stream
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
     * @param out                   output print stream
     * @param shouldPrintBucketTree
     */
    public void print(PrintStream out, boolean shouldPrintBucketTree)
    {
        out.print("Posterior expectation: [");
        for (double val : results)
        {
            out.print(val + " ");
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
     * @return the results as array of doubles
     */
    public double[] getResults()
    {
        return (results);
    }
}
