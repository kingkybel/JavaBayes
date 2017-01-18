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
     *
     * @return
     */
    public boolean isIgnore()
    {
        return this == IGNORE;
    }

    /**
     *
     * @return
     */
    public boolean usesMarkedVariablesOnly()
    {
        return this == MARKED_VARIABLES_ONLY;
    }

    /**
     *
     * @return
     */
    public boolean usesAllNotObservedVariables()
    {
        return this == ALL_NOT_OBSERVED_VARIABLES;
    }

    /**
     *
     * @return
     */
    public boolean isExpectation()
    {
        return this == EXPECTATION;
    }

    /**
     *
     * @return
     */
    public boolean isMarginalPosterior()
    {
        return this == MARGINAL_POSTERIOR;
    }

    /**
     *
     * @return
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
     *
     * @return
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
