/*
 * Copyright (C) 2015 Dieter J Kybelksties
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * @author  Dieter J Kybelksties
 * @date May 11, 2016
 *
 */
package QuasiBayesianNetworks;

import java.util.logging.Logger;

/**
 * Enumeration of global neighbourhood types.
 *
 * @author Dieter J Kybelksties
 */
public enum GlobalNeighbourhood
{

    /**
     * No credal set.
     */
    NO_CREDAL_SET,
    /**
     * Neighbourhood defined by credal set.
     */
    CREDAL_SET,
    /**
     * Neighbourhood defined by constant density ratio.
     */
    CONSTANT_DENSITY_RATIO,
    /**
     * Neighbourhood defined by epsilon contamination.
     */
    EPSILON_CONTAMINATED,
    /**
     * Neighbourhood defined by bounded constant density ratio.
     */
    CONSTANT_DENSITY_BOUNDED,
    /**
     * Neighbourhood defined by total variation.
     */
    TOTAL_VARIATION;

    private static final Class CLAZZ = GlobalNeighbourhood.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Override
    public String toString()
    {
        return this == NO_CREDAL_SET ? "none" :
               this == CREDAL_SET ? "credal-set" :
               this == CONSTANT_DENSITY_RATIO ? "constant-density-ratio" :
               this == EPSILON_CONTAMINATED ? "epsilon-contaminated" :
               this == CONSTANT_DENSITY_BOUNDED ? "constant-density-bounded" :
               this == TOTAL_VARIATION ? "total-variation" : "none";
    }

    /**
     * Convert a string into a global neighbourhood type.
     *
     * @param globalNeighbourhoodStr
     * @return globalNeighbourhood converted from string representation
     */
    static public GlobalNeighbourhood fromString(String globalNeighbourhoodStr)
    {
        String str = globalNeighbourhoodStr.trim().toLowerCase();
        return "none".equals(str) ? NO_CREDAL_SET :
               "credal-set".equals(str) ? CREDAL_SET :
               "constant-density-ratio".equals(str) ? CONSTANT_DENSITY_RATIO :
               "epsilon-contaminated".equals(str) ? EPSILON_CONTAMINATED :
               "constant-density-bounded".equals(str) ? CONSTANT_DENSITY_BOUNDED :
               "total-variation".equals(str) ? TOTAL_VARIATION :
               NO_CREDAL_SET; // none - default
    }

}
