/*
 * @author  Dieter J Kybelksties
 * @date Jun 1, 2016
 *
 */
package BayesianInferences;

import java.util.logging.Logger;

/**
 *
 * @author Dieter J Kybelksties
 */
public enum ExplanationType
{

    IGNORE,
    MARKED_VARIABLES_ONLY,
    ALL_NOT_OBSERVED_VARIABLES,
    MARGINAL_POSTERIOR,
    EXPECTATION,
    SENSITIVITY_ANALYSIS;

    private static final Class CLAZZ = ExplanationType.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public boolean isIgnore()
    {
        return this == IGNORE;
    }

    public boolean usesMarkedVariablesOnly()
    {
        return this == MARKED_VARIABLES_ONLY;
    }

    public boolean usesAllNotObservedVariables()
    {
        return this == ALL_NOT_OBSERVED_VARIABLES;
    }

    public boolean isExpectation()
    {
        return this == EXPECTATION;
    }

    public boolean isMarginalPosterior()
    {
        return this == MARGINAL_POSTERIOR;
    }

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
