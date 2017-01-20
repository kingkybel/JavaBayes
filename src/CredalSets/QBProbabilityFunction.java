/*
 * QBProbabilityFunction.java
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
package CredalSets;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Quasi-Bayes-probability function.
 *
 * @author Fabio G. Cozman
 */
public class QBProbabilityFunction extends ProbabilityFunction
{

    private static final Class CLAZZ = QBProbabilityFunction.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    double lowerEnvelope[];
    double upperEnvelope[];

    /**
     * Default constructor for a QBProbabilityFunction.
     */
    public QBProbabilityFunction()
    {
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param bayesNet       the underlying Bayesian network
     * @param numberOfVars   number of variables in the function
     * @param numberOfValues number of values in the function
     * @param properties     list of properties
     */
    public QBProbabilityFunction(BayesNet bayesNet,
                                 int numberOfVars,
                                 int numberOfValues,
                                 ArrayList<String> properties)
    {
        super(bayesNet, numberOfVars, numberOfValues, properties);
        lowerEnvelope = new double[numberOfValues];
        upperEnvelope = new double[numberOfValues];
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param bayesNet      the underlying Bayesian network
     * @param properties    list of properties
     * @param variables     an array of discrete variables
     * @param probValues    the probability values of the function as array of
     *                      doubles
     * @param upperEnvelope the upper envelope as array of doubles
     * @param lowerEnvelope the lower envelope as array of doubles
     */
    public QBProbabilityFunction(BayesNet bayesNet,
                                 DiscreteVariable variables[],
                                 double probValues[],
                                 double lowerEnvelope[],
                                 double upperEnvelope[],
                                 ArrayList<String> properties)
    {
        super(bayesNet, variables, probValues, properties);
        this.lowerEnvelope = lowerEnvelope;
        this.upperEnvelope = upperEnvelope;
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param discrFunc     a discrete function
     * @param probValues    the probability values of the function as array of
     *                      doubles
     * @param lowerEnvelope the lower envelope as array of doubles
     * @param upperEnvelope the upper envelope as array of doubles
     */
    public QBProbabilityFunction(DiscreteFunction discrFunc,
                                 double probValues[],
                                 double lowerEnvelope[],
                                 double upperEnvelope[])
    {
        super(discrFunc, probValues);
        this.lowerEnvelope = lowerEnvelope;
        this.upperEnvelope = upperEnvelope;
    }

    @Override
    public void print()
    {
        print(System.out);
    }

    @Override
    public void print(PrintStream out)
    {
        int j;

        if (numberVariables() != BayesNet.INVALID_INDEX)
        {
            out.print(" envelope ( ");
            for (j = 0; j < numberVariables(); j++)
            {
                out.print(" \"" + getVariable(j).getName() + "\" ");
            }
            out.print(") {");
            if (lowerEnvelope != null)
            {
                out.println(" //" + numberVariables() +
                            " variable(s) and " + lowerEnvelope.length +
                            " values");
                out.print("\ttable lower-envelope ");
                for (j = 0; j < lowerEnvelope.length; j++)
                {
                    out.print(lowerEnvelope[j] + " ");
                }
                out.print(";");
            }
            out.println();
            if (upperEnvelope != null)
            {
                out.print("\ttable upper-envelope ");
                for (j = 0; j < upperEnvelope.length; j++)
                {
                    out.print(upperEnvelope[j] + " ");
                }
                out.print(";");
            }
        }
        out.println();
        if ((properties != null) && (properties.size() > 0))
        {
            for (String property : properties)
            {
                out.println("\tproperty \"" + property + "\" ;");
            }
        }
        out.println("}");
    }

    /**
     * Get the lower envelope array.
     *
     * @return the lower envelope
     */
    public double[] getLowerEnvelope()
    {
        return lowerEnvelope;
    }

    /**
     * Get the upper envelope array.
     *
     * @return the upper envelope
     */
    public double[] getUpperEnvelope()
    {
        return upperEnvelope;
    }
}
