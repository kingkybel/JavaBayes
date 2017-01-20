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

    private static final Class CLAZZ = Expectation.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

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

    /**
     * Set the expected results array.
     *
     * @param expectedValue only the expected value in an array of size 1
     */
    public void setResults(double expectedValue)
    {
        results = new double[1];
        results[0] = expectedValue;
    }

    /**
     * Set the expected results array.
     *
     * @param min minimum expectation
     * @param max maximum expectation
     */
    public void setResults(double min, double max)
    {
        results = new double[2];
        results[0] = min;
        results[1] = max;
    }

    /**
     * Set the expected results array.
     *
     * @param results arbitrary length array of results
     */
    public void setResults(double[] results)
    {
        this.results = new double[results.length];
        System.arraycopy(results, 0, this.results, 0, results.length);
    }

    /**
     * Retrieve the Bayes net.
     *
     * @return the Bayes net
     */
    public BayesNet getBayesNet()
    {
        return bayesNet;
    }

    /**
     * Set a new Bayes net.
     *
     * @param bayesNet new Bayes net
     */
    public void setBayesNet(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    /**
     * Retrieve the inference object.
     *
     * @return the inference
     */
    public Inference getInference()
    {
        return inference;
    }

    /**
     * Set s new inference object.
     *
     * @param inference the new inference
     */
    public void setInference(Inference inference)
    {
        this.inference = inference;
    }

    /**
     * Is the produce Cluster flag set?
     *
     * @return true if so, false otherwise
     */
    public boolean isProducingClusters()
    {
        return isProducingClusters;
    }

    /**
     * Set whether to produce clusters or not.
     *
     * @param isProducingClusters true/false
     */
    public void setProducingClusters(boolean isProducingClusters)
    {
        this.isProducingClusters = isProducingClusters;
    }

    /**
     * Retrieve the current function.
     *
     * @return the current function
     */
    public DiscreteFunction getCurrentFunction()
    {
        return currentFunction;
    }

    /**
     * Set a new discrete function as current.
     *
     * @param currentFunction the new discrete function to set as current
     */
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
     * @param order order of variables given as array of their names
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
     * @param order  order of variables given as array of their names
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
     * @param discrFunc discrete function of the queried variable
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
     * @param discrFunc           discrete function of the queried variable
     * @param queriedVariableName the queried variable
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
     * @param discrFunc discrete function of the queried variable
     * @param order     order of variables given as array of their names
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
        return discrFunc;
    }

    /**
     * Do the expectations function's inference.
     *
     * @param discrFunc discrete function of the queried variable
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
     * @param shouldPrintBucketTree if true, then print the bucket tree, if
     *                              false don't
     */
    public void print(boolean shouldPrintBucketTree)
    {
        print(System.out, shouldPrintBucketTree);
    }

    /**
     * Print Expectation.
     *
     * @param out                   output print stream
     * @param shouldPrintBucketTree if true, then print the bucket tree, if
     *                              false don't
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

        inference.printBucketTree(out, shouldPrintBucketTree);
    }

    /**
     * Get the results of Expectation.
     *
     * @return the results as array of doubles
     */
    public double[] getResults()
    {
        return results;
    }

    /**
     * Type of expectation to calculate.
     */
    public enum Type
    {

        /**
         * Expected value.
         */
        EXPECTED_VALUE(1),
        /**
         * Second moment expectation (derivate).
         */
        SECOND_MOMENT(2),
        /**
         * Third moment expectation.
         */
        THIRD_MOMENT(3),
        /**
         * Fourth moment expectation.
         */
        FOURTH_MOMENT(4);

        private Type(int value)
        {
            this.value = value;
        }

        /**
         * Retrieve the order of expectation from the enum value.
         *
         * @return the order (currently 1,2, 3 or 4
         */
        public int order()
        {
            return value;
        }
        private final int value;
    }
}
