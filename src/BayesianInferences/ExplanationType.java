/*
 * @author  Dieter J Kybelksties
 * @date Jun 1, 2016
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
