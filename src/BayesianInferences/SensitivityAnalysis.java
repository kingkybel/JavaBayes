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
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class SensitivityAnalysis
{

    private static final Class CLAZZ = SensitivityAnalysis.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Constructor for an Expectation.
     *
     * @param bayesNet the underlying Bayesian network
     */
    public SensitivityAnalysis(BayesNet bayesNet)
    {
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
     * Print Sensitivity analysis.
     *
     * @param out                   output print stream
     * @param shouldPrintBucketTree if true, then print the bucket tree, if
     *                              false don't
     */
    public void print(PrintStream out, boolean shouldPrintBucketTree)
    {
        out.print("Sensitivity analysis not implemented yet!");
    }
}
