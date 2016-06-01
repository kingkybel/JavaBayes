/*
 * @author  Dieter J Kybelksties
 * @date Jun 1, 2016
 *
 */
package BayesianInferences;

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
}
