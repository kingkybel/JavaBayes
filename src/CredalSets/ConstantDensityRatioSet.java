package CredalSets;

import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public class ConstantDensityRatioSet
        extends FinitelyGeneratedSet
        implements MappingDouble
{

    private final static int LOWER_EXPECTATION_BRACKET = 0;
    private final static int UPPER_EXPECTATION_BRACKET = 1;

    private final static double ACCURACY = 10E-8;
    private static final String CLASS_NAME =
                                ConstantDensityRatioSet.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private double k;
    // Auxiliary variable that holds a discrete function for bracketing.
    private DiscreteFunction temporaryDiscreteFunction;

    /**
     * Constructor for an ConstantDensityRatioSet ProbabilityFunction object and
     * given constant.
     *
     * @param pf
     * @param kk
     */
    public ConstantDensityRatioSet(ProbabilityFunction pf, double kk)
    {
        super(pf, pf.getValues());
        k = kk;
        if (k <= 0.0)
        {
            k = 1.0;
        }
        else
        {
            if (k < 1.0)
            {
                k = 1.0 / k;
            }
        }
    }

    /**
     * Perform calculation of marginal posterior distributions for. a density
     * ratio global neighborhood.
     *
     * @return
     */
    public ProbabilityFunction posteriorMarginal()
    {
        double lowerValues[] = new double[values.length];
        double upperValues[] = new double[values.length];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((variables[0] instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) variables[0]).isObserved() == true))
        {
            for (int i = 0; i < values.length; i++)
            {
                lowerValues[i] = values[i];
                upperValues[i] = values[i];
            }
        } // Else, apply the marginalization property.
        else
        {
            double total = 0.0;
            for (int i = 0; i < values.length; i++)
            {
                total += values[i];
            }
            for (int i = 0; i < values.length; i++)
            {
                lowerValues[i] =
                (values[i] / k) /
                ((values[i] / k) + k * (total - values[i]));
            }
            for (int i = 0; i < values.length; i++)
            {
                upperValues[i] =
                (k * values[i]) /
                (k * values[i] + (total - values[i]) / k);
            }
        }

        return (new QBProbabilityFunction(bn, variables, values,
                                          lowerValues, upperValues, properties));
    }

    /**
     * Perform calculation of expected value for density ratio.
     *
     * @param df
     * @return
     */
    public double[] expectedValues(DiscreteFunction df)
    {
        Bracketing bracket = new Bracketing();
        double results[] = new double[2];

        // Check the possibility that the query has an observed variable,
        // in which case the marginalization property does not apply.
        if ((variables[0] instanceof ProbabilityVariable) &&
            (((ProbabilityVariable) variables[0]).isObserved() == true))
        {
            results[0] =
            df.getValue(((ProbabilityVariable) variables[0]).
                    getObservedIndex());
            results[1] = results[0];
            return (results);
        }
     // Else, apply the marginalization property.

        // Obtain the maximum and minimum of functions
        double maxDfValue = df.getValue(0);
        double minDfValue = df.getValue(0);
        for (int i = 1; i < df.numberValues(); i++)
        {
            if (maxDfValue < df.getValue(i))
            {
                maxDfValue = df.getValue(i);
            }
            if (minDfValue > df.getValue(i))
            {
                minDfValue = df.getValue(i);
            }
        }

        // Prepare the temporaryDiscreteFunction variable for bracketing
        temporaryDiscreteFunction = df;

        // Bracket the lower expectation
        double lowerExpectation =
               bracket.perform(this, LOWER_EXPECTATION_BRACKET,
                               minDfValue, maxDfValue, ACCURACY);

        // Bracket the upper expectation
        double upperExpectation =
               bracket.perform(this, UPPER_EXPECTATION_BRACKET,
                               minDfValue, maxDfValue, ACCURACY);

        // Calculate the values
        results[0] = lowerExpectation;
        results[1] = upperExpectation;

        return (results);
    }

    /**
     * Perform calculation of posterior expected value. Assumes that the
     * probability values are not normalized; probability values are p(x, e)
     * where e is the fixed evidence
     *
     * @param df
     * @return
     */
    public double[] posteriorExpectedValues(DiscreteFunction df)
    {
        return (expectedValues(df));
    }

    /**
     * To conform to the Mapping interface demanded by the Bracketing class, the
     * method map() must be present.
     */
    @Override
    public double map(int mapType, double mapInput)
    {
        int i;
        double aux;
        double mapOutputUpper = 0.0;
        double mapOutputLower = 0.0;
        double mapOutput = 0.0;
        DiscreteFunction tdf = temporaryDiscreteFunction;

        switch (mapType)
        {
            case LOWER_EXPECTATION_BRACKET:
                for (i = 0; i < values.length; i++)
                {
                    aux = tdf.getValue(i) - mapInput;
                    mapOutputUpper += (k * values[i]) * (-Math.max(-aux, 0.0));
                    mapOutputLower += (values[i] / k) * (Math.max(aux, 0.0));
                }
                break;
            case UPPER_EXPECTATION_BRACKET:
                for (i = 0; i < values.length; i++)
                {
                    aux = tdf.getValue(i) - mapInput;
                    mapOutputUpper += (k * values[i]) * (Math.max(aux, 0.0));
                    mapOutputLower += (values[i] / k) * (-Math.max(-aux, 0.0));
                }
                break;
        }
        mapOutput = mapOutputUpper + mapOutputLower;
        return (mapOutput);
    }
}
