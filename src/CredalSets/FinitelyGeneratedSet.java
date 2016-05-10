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

abstract class FinitelyGeneratedSet
        extends QBProbabilityFunction
{

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
                         int nVb,
                         int nVl,
                         ArrayList properties)
    {
        super(bayesNet, nVb, nVl, properties);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(BayesNet bayesNet,
                         DiscreteVariable pvs[],
                         double v[],
                         double lp[],
                         double up[],
                         ArrayList properties)
    {
        super(bayesNet, pvs, v, lp, up, properties);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(BayesNet bayesNet,
                         DiscreteVariable pvs[],
                         double v[],
                         ArrayList properties)
    {
        this(bayesNet, pvs, v, (double[]) null, (double[]) null, properties);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(DiscreteFunction discrFunc,
                         double newValues[],
                         double newLp[],
                         double newUp[])
    {
        super(discrFunc, newValues, newLp, newUp);
    }

    /**
     * Constructor for FinitelyGeneratedSet.
     */
    FinitelyGeneratedSet(DiscreteFunction discrFunc, double newValues[])
    {
        super(discrFunc, newValues, (double[]) null, (double[]) null);
    }
}
