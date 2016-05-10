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
 * @author Fabio G. Cozman
 */
public class QBProbabilityFunction extends ProbabilityFunction
{

    private static final Logger LOGGER =
                                Logger.getLogger(QBProbabilityFunction.class.
                                        getName());

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
     * @param bayesNet
     * @param properties
     * @param nVb
     * @param nVl
     */
    public QBProbabilityFunction(BayesNet bayesNet,
                                 int nVb,
                                 int nVl,
                                 ArrayList properties)
    {
        super(bayesNet, nVb, nVl, properties);
        lowerEnvelope = new double[nVl];
        upperEnvelope = new double[nVl];
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param bayesNet
     * @param properties
     * @param dvs
     * @param up
     * @param v
     * @param lp
     */
    public QBProbabilityFunction(BayesNet bayesNet,
                                 DiscreteVariable dvs[],
                                 double v[],
                                 double lp[],
                                 double up[],
                                 ArrayList properties)
    {
        super(bayesNet, dvs, v, properties);
        lowerEnvelope = lp;
        upperEnvelope = up;
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param discrFunc
     * @param newUp
     * @param newValues
     * @param newLp
     */
    public QBProbabilityFunction(DiscreteFunction discrFunc,
                                 double newValues[],
                                 double newLp[],
                                 double newUp[])
    {
        super(discrFunc, newValues);
        lowerEnvelope = newLp;
        upperEnvelope = newUp;
    }

    /**
     * Print QBProbabilityFunction.
     */
    @Override
    public void print()
    {
        print(System.out);
    }

    /**
     * Print QBProbabilityFunction.
     *
     * @param out
     */
    @Override
    public void print(PrintStream out)
    {
        int j;
        String property;

        if (variables != null)
        {
            out.print(" envelope ( ");
            for (j = 0; j < variables.length; j++)
            {
                out.print(" \"" + variables[j].getName() + "\" ");
            }
            out.print(") {");
            if (lowerEnvelope != null)
            {
                out.println(" //" + variables.length +
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
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\tproperty \"" + property + "\" ;");
            }
        }
        out.println("}");
    }

    /**
     * Get the lowerEnvelope array.
     *
     * @return
     */
    public double[] getLowerEnvelope()
    {
        return (lowerEnvelope);
    }

    /**
     * Get the upperEnvelope array.
     *
     * @return
     */
    public double[] getUpperEnvelope()
    {
        return (upperEnvelope);
    }
}