package CredalSets;

import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;

/*
 * This abstract class provides the infra-structure for calculation
 * of posterior marginals, generalized Choquet integrals and
 * posterior expectations for two-monotone capacities.
 * Particular classes of capacities must provide implementations
 * for the abstract methods in this class.
 */
/**
 *
 * @author kybelksd
 */
public abstract class TwoMonotoneCapacity
        extends FinitelyGeneratedSet
        implements MappingDouble
{

    private final static int LOWER_EXPECTATION_BRACKET = 0;
    private final static int UPPER_EXPECTATION_BRACKET = 1;

    private final static double ACCURACY = 10E-8;
    // Auxiliary variable that holds a discrete function for bracketing
    private DiscreteFunction temporaryDiscreteFunction;

    /**
     * Constructor for an TwoMonotoneCapacity ProbabilityFunction object and
     * given epsilon.
     * @param pf
     */
    public TwoMonotoneCapacity(ProbabilityFunction pf)
    {
        super(pf, pf.getValues());
    }

    /**
     * Obtain the lower probability of an event given the base probability for
     * the event.
     * @param p
     * @return 
     */
    public abstract double getLowerProbabilityFromBase(double p);

    /**
     * ************************************************************
     */
    /* Obtain the upper probability of an event given the base     */
    /* probability for the event.                                  */
    /**
     * ************************************************************
     * @param p
     * @return 
     */
    public abstract double getUpperProbabilityFromBase(double p);

    /**
     * ************************************************************
     */
    /* Get a base probability value for an atom.                   */
    /**
     * ************************************************************
     * @param index
     * @return 
     */
    public abstract double getAtomProbability(int index);

    /**
     * ************************************************************
     */
    /* Perform calculation of marginal posterior distributions for */
    /* a total variation global neighborhood                       */
    /**
     * ************************************************************
     * @return 
     */
    public ProbabilityFunction posteriorMarginal()
    {
        double lowerValues[] = new double[values.length];
        double upperValues[] = new double[values.length];
        DiscreteFunction df = new DiscreteFunction(1, values.length);

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
            double total = 0.0; // Probability p(e)
            for (int i = 0; i < values.length; i++)
            {
                total += values[i];
            }

            for (int i = 0; i < values.length; i++)
            {
                lowerValues[i] =
                (getLowerProbabilityFromBase(values[i]) /
                 (getLowerProbabilityFromBase(values[i]) +
                  getUpperProbabilityFromBase(total - values[i])));
            }
            for (int i = 0; i < values.length; i++)
            {
                upperValues[i] =
                (getUpperProbabilityFromBase(values[i]) /
                 (getUpperProbabilityFromBase(values[i]) +
                  getLowerProbabilityFromBase(total - values[i])));
            }
        }

        return (new QBProbabilityFunction(this, (double[]) null,
                                          lowerValues, upperValues));
    }

    /**
     * ************************************************************
     */
    /* Perform calculation of expected value for density ratio     */
    /**
     * ************************************************************
     * @param df
     * @return 
     */
    public double[] expectedValues(DiscreteFunction df)
    {
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

     // Else, apply the marginalization property and the
        // fact that total variation neighborhoods are 2-monotone.
        GeneralizedChoquetIntegral gci =
                                   new GeneralizedChoquetIntegral(this, df);

        return (gci.results);
    }

    /**
     * ************************************************************
     */
    /* Perform calculation of posterior expected value.            */
    /* Assumes that the probability values are not                 */
    /* normalized; probability values are p(x, e) where e is       */
    /* the fixed evidence                                          */
    /**
     * ************************************************************
     * @param df
     * @return 
     */
    public double[] posteriorExpectedValues(DiscreteFunction df)
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

    // Else, apply the marginalization property and the
        // fact that total variation neighborhoods are 2-monotone.
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
     * ************************************************************
     */
    /* To conform to the MappingDouble interface demanded by the   */
    /* Bracketing class, the method map() must be present.         */
    /**
     * ************************************************************
     */
    @Override
    public double map(int mapType, double mapInput)
    {
        // Get temporaryDiscreteFunction
        DiscreteFunction tdf = temporaryDiscreteFunction;

        // Create new discrete function
        double newValues[] = new double[tdf.numberValues()];
        for (int i = 0; i < newValues.length; i++)
        {
            newValues[i] = tdf.getValue(i) - mapInput;
        }
        DiscreteFunction mtdf = new DiscreteFunction(tdf.getVariables(),
                                                     newValues);

        // Obtain Walley's formula for lower and upper expectations
        GeneralizedChoquetIntegral gci = new GeneralizedChoquetIntegral(this,
                                                                        mtdf);

        if (mapType == LOWER_EXPECTATION_BRACKET)
        {
            return (gci.results[0]); // LOWER_EXPECTATION_BRACKET
        }
        else
        {
            return (gci.results[1]); // UPPER_EXPECTATION_BRACKET
        }
    }
}
