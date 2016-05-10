/*
 * SaveBugs.java
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
package BayesianNetworks;

import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class SaveBugs
{

    private static final String CLASS_NAME = SaveBugs.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    BayesNet bayesNet;

    /**
     * Default constructor for a SaveBUGS object.
     *
     * @param bayesNet
     */
    public SaveBugs(BayesNet bayesNet)
    {
        this.bayesNet = bayesNet;
    }

    /**
     * Save a BayesNet in a stream in the BUGS format.
     *
     * @param pstream
     */
    public void save(PrintStream pstream)
    {
        pstream.println("# Bayesian Network in the BUGS Format");
        pstream.println("# Produced by BayesianNetworks package in JavaBayes");
        pstream.println("# Output created " + (new Date()));
        pstream.println("\n");

        saveModel(pstream);

        saveVariables(pstream);

        printDataIn(pstream);

        saveStructure(pstream);

        saveData(pstream);
    }

    /**
     * Save the name of the network in BUGS format.
     */
    private void saveModel(PrintStream pstream)
    {
        pstream.println("model " + bayesNet.name + ";");
    }

    /**
     * Declare all the variables used in the network.
     */
    private void saveVariables(PrintStream pstream)
    {
        ProbabilityFunction probFunc;
        ProbabilityVariable probVar;
        int i, j;

        pstream.println("var");
        for (i = (bayesNet.probabilityVariables.length - 1); i >= 0; i--)
        {
            pstream.println("\t" + bayesNet.probabilityVariables[i].name + ",");
        }

        for (i = (bayesNet.probabilityFunctions.length - 1); i >= 0; i--)
        {
            probFunc = bayesNet.probabilityFunctions[i];
            probVar = (ProbabilityVariable) (probFunc.variables[0]);
            pstream.print("\tp." + probVar.name + "[" + probFunc.values.length +
                          "]");
            if (i > 0)
            {
                pstream.println(",");
            }
        }
        pstream.println(";");
    }

    /**
     * Indicate which file contains the data; note that the data is appended to
     * the given pstream, so this space is left to be filled by the user.
     */
    private void printDataIn(PrintStream pstream)
    {
        pstream.println("data in <user-defined-name-for-data-file>;");
    }

    /**
     * The parenthood relationships in the network.
     */
    private void saveStructure(PrintStream pstream)
    {
        ProbabilityFunction probFunc;
        ProbabilityVariable probVar;
        int i, j;

        pstream.println("{");
        for (i = (bayesNet.probabilityFunctions.length - 1); i >= 0; i--)
        {
            probFunc = bayesNet.probabilityFunctions[i];
            probVar = (ProbabilityVariable) (probFunc.variables[0]);
            pstream.print(probVar.name + "  ~  dcat(p." + probVar.name + "[");
            for (j = 1; j < probFunc.variables.length; j++)
            {
                pstream.print(probFunc.variables[j].name);
                pstream.print(",");
            }
            pstream.println("]);");
        }
        pstream.println("}\n\n");
    }

    /**
     * The numeric values for the distributions, saved in the Splus format
     * understood by BUGS. Note that the user has to place this data in a
     * separate file.
     */
    private void saveData(PrintStream pstream)
    {
        ProbabilityFunction probFunc;
        ProbabilityVariable probVar;
        int i, j, k;
        int step;
        double value;

        pstream.println("list(");
        for (i = (bayesNet.probabilityFunctions.length - 1); i >= 0; i--)
        {
            probFunc = bayesNet.probabilityFunctions[i];
            probVar = (ProbabilityVariable) (probFunc.variables[0]);
            /**
             * ** Put distribution values in the correct format. ***
             */
            pstream.print("\tp." + probVar.name + "  = c(");
            step = 1;
            for (j = 1; j < probFunc.variables.length; j++)
            {
                step *= probFunc.variables[j].values.length;
            }
            for (j = 0; j < step; j++)
            {
                for (k = 0; k < probFunc.variables[0].values.length; k++)
                {
                    value = probFunc.values[k * step + j];
                    pstream.print(" " + value);
                    if (k < (probFunc.variables[0].values.length - 1))
                    {
                        pstream.print(",");
                    }
                }
                if (j < (step - 1))
                {
                    pstream.print(",");
                }
            }
            pstream.print(")");
            if (i > 0)
            {
                pstream.println(",");
            }
        }
        pstream.println(")\n");
    }
}
