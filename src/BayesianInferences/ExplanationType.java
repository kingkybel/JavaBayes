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
package BayesianInferences;

import java.util.logging.Logger;

/**
 * Enumeration of tags to specify what kind of explanation should be
 * calculated/displayed.
 *
 * @author Dieter J Kybelksties
 */
public enum ExplanationType
{

    /**
     * Ignore/dummy.
     */
    IGNORE,
    /**
     * Use only marked variables for explanation.
     */
    MARKED_VARIABLES_ONLY,
    /**
     * Us all variables that are not observed.
     */
    ALL_NOT_OBSERVED_VARIABLES,
    /**
     * Calculate the marginal posterior.
     */
    MARGINAL_POSTERIOR,
    /**
     * Calculate an expectation.
     */
    EXPECTATION,
    /**
     * Conduct a sensitivity analysis.
     */
    SENSITIVITY_ANALYSIS;

    private static final Class CLAZZ = ExplanationType.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * I
     * Check whether no explanation is required.
     *
     * @return true if so, false otherwise
     */
    public boolean isIgnore()
    {
        return this == IGNORE;
    }

    /**
     * Check whether marked variables only should be used for explanation.
     *
     * @return true if so, false otherwise
     */
    public boolean usesMarkedVariablesOnly()
    {
        return this == MARKED_VARIABLES_ONLY;
    }

    /**
     * Check whether all not observed variables should be used for explanation.
     *
     * @return true if so, false otherwise
     */
    public boolean usesAllNotObservedVariables()
    {
        return this == ALL_NOT_OBSERVED_VARIABLES;
    }

    /**
     * Check whether expectation should be calculated.
     *
     * @return true if so, false otherwise
     */
    public boolean isExpectation()
    {
        return this == EXPECTATION;
    }

    /**
     * Check whether marginal posterior should be calculated.
     *
     * @return true if so, false otherwise
     */
    public boolean isMarginalPosterior()
    {
        return this == MARGINAL_POSTERIOR;
    }

    /**
     * Check whether sensitivity analysis should be calculated.
     *
     * @return true if so, false otherwise
     */
    public boolean isSensitivityAnalysis()
    {
        return this == SENSITIVITY_ANALYSIS;
    }

    @Override
    public String toString()
    {
        return this == MARKED_VARIABLES_ONLY ?
               "Subset of marked explanantory variables" :
               this == ALL_NOT_OBSERVED_VARIABLES ?
               "All not observed variables are explanatory" :
               this == MARGINAL_POSTERIOR ? "Posterior marginal" :
               this == EXPECTATION ? "Posterior Expectation" :
               this == SENSITIVITY_ANALYSIS ? "" : "<IGNORE>";
    }

    /**
     * Get a list of valid explanation type choices for GUIs. IGNORE can be left
     * out, for example, as it is not a valid choice for calculation.
     *
     * @return valid choices as array
     */
    public static ExplanationType[] validChoices()
    {
        return new ExplanationType[]
        {
            MARKED_VARIABLES_ONLY,
            ALL_NOT_OBSERVED_VARIABLES,
            MARGINAL_POSTERIOR,
            EXPECTATION,
            SENSITIVITY_ANALYSIS
        };
    }
}
