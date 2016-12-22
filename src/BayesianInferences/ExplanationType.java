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
    SUBSET,
    FULL,
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

    public boolean isSubset()
    {
        return this == SUBSET;
    }

    public boolean isFull()
    {
        return this == FULL;
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
        return this == SUBSET ? "Subset of explanantory variables" :
               this == FULL ? "Complete explanation" :
               this == MARGINAL_POSTERIOR ? "Posterior marginal" :
               this == EXPECTATION ? "Posterior Expectation" :
               this == SENSITIVITY_ANALYSIS ? "" : "<IGNORE>";
    }

    public static ExplanationType[] validChoices()
    {
        return new ExplanationType[]
        {
            SUBSET,
            FULL,
            MARGINAL_POSTERIOR,
            EXPECTATION,
            SENSITIVITY_ANALYSIS
        };
    }
}
