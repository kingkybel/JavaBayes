/*
 * ConstantDensityBoundedSet.java
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

import BayesianNetworks.ProbabilityFunction;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class ConstantDensityBoundedSet
        extends TwoMonotoneCapacity
{

    private static final Class CLAZZ = ConstantDensityBoundedSet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * The "size" of the density bounded class.
     */
    private double k;

    /**
     * Constructor for a ConstantDensityBoundedSet given a ProbabilityFunction
     * object and given constant.
     *
     * @param probFunc probability function
     * @param k        the "size" of the density bounds
     */
    public ConstantDensityBoundedSet(ProbabilityFunction probFunc, double k)
    {
        super(probFunc);

        // this.k will always be >= 1.0
        this.k = (k <= 0.0) ? 1.0 : (k < 1.0) ? 1.0 / k : k;
    }

    /**
     * Obtain the lower probability of an event given the base probability for
     * the event.
     *
     * @param p base probability
     * @return the lower probability for the base
     */
    @Override
    public double getLowerProbabilityFromBase(double p)
    {
        return Math.max(p / k, 1.0 - k * (1.0 - p));
    }

    /**
     * Obtain the upper probability of an event given the base probability for
     * the event.
     *
     * @param p base probability
     * @return the upper probability for the base
     */
    @Override
    public double getUpperProbabilityFromBase(double p)
    {
        return Math.min(k * p, 1.0 - (1.0 - p) / k);
    }

    /**
     * Get a base probability value for an atom.
     *
     * @param valIndex index of the value
     * @return the atom probability for the base
     */
    @Override
    public double getAtomProbability(int valIndex)
    {
        return getValue(valIndex);
    }
}
