/*
 * FinitelyGeneratedSet.java
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
import java.util.ArrayList;
import java.util.logging.Logger;

abstract class FinitelyGeneratedSet
        extends QBProbabilityFunction
{

    private static final Class CLAZZ = FinitelyGeneratedSet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Default constructor for a FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet()
    {
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(BayesNet bayesNet,
                         int numberOfVars,
                         int numberOfValues,
                         ArrayList properties)
    {
        super(bayesNet, numberOfVars, numberOfValues, properties);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(BayesNet bayesNet,
                         DiscreteVariable variables[],
                         double values[],
                         double lowerEnvelope[],
                         double upperEnvelope[],
                         ArrayList properties)
    {
        super(bayesNet,
              variables,
              values,
              lowerEnvelope,
              upperEnvelope,
              properties);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(BayesNet bayesNet,
                         DiscreteVariable variables[],
                         double values[],
                         ArrayList properties)
    {
        this(bayesNet,
             variables,
             values,
             (double[]) null,
             (double[]) null,
             properties);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(DiscreteFunction discrFunc,
                         double values[],
                         double lowerEnvelope[],
                         double upperEnvelope[])
    {
        super(discrFunc, values, lowerEnvelope, upperEnvelope);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(DiscreteFunction discrFunc, double values[])
    {
        super(discrFunc, values, (double[]) null, (double[]) null);
    }
}
