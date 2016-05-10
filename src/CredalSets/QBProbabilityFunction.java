package CredalSets;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author kybelksd
 */
public class QBProbabilityFunction extends ProbabilityFunction
{
    private static final Logger LOGGER =
    Logger.getLogger(QBProbabilityFunction.class.
            getName());

    double lowerEnvelope[];
    double upperEnvelope[];

    /**
     * Default constructor for a QBProbabilityFunction.
     */
    public QBProbabilityFunction()
    {
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param bN
     * @param prop
     * @param nVb
     * @param nVl
     */
    public QBProbabilityFunction(BayesNet bN, int nVb, int nVl,
                                 ArrayList prop)
    {
        super(bN, nVb, nVl, prop);
        lowerEnvelope = new double[nVl];
        upperEnvelope = new double[nVl];
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param bN
     * @param prop
     * @param dvs
     * @param up
     * @param v
     * @param lp
     */
    public QBProbabilityFunction(BayesNet bN, DiscreteVariable dvs[],
                                 double v[], double lp[], double up[],
                                 ArrayList prop)
    {
        super(bN, dvs, v, prop);
        lowerEnvelope = lp;
        upperEnvelope = up;
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param df
     * @param newUp
     * @param newValues
     * @param newLp
     */
    public QBProbabilityFunction(DiscreteFunction df, double newValues[],
                                 double newLp[], double newUp[])
    {
        super(df, newValues);
        lowerEnvelope = newLp;
        upperEnvelope = newUp;
    }

    /**
     * Print QBProbabilityFunction.
     */
    @Override
    public void print()
    {
        print(System.out);
    }

    /**
     * Print QBProbabilityFunction.
     *
     * @param out
     */
    @Override
    public void print(PrintStream out)
    {
        int j;
        String property;

        if (variables != null)
        {
            out.print(" envelope ( ");
            for (j = 0; j < variables.length; j++)
            {
                out.print(" \"" + variables[j].getName() + "\" ");
            }
            out.print(") {");
            if (lowerEnvelope != null)
            {
                out.println(" //" + variables.length +
                            " variable(s) and " + lowerEnvelope.length +
                            " values");
                out.print("\ttable lower-envelope ");
                for (j = 0; j < lowerEnvelope.length; j++)
                {
                    out.print(lowerEnvelope[j] + " ");
                }
                out.print(";");
            }
            out.println();
            if (upperEnvelope != null)
            {
                out.print("\ttable upper-envelope ");
                for (j = 0; j < upperEnvelope.length; j++)
                {
                    out.print(upperEnvelope[j] + " ");
                }
                out.print(";");
            }
        }
        out.println();
        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                property = (String) (e);
                out.println("\tproperty \"" + property + "\" ;");
            }
        }
        out.println("}");
    }

    /* ************************************************************* */
    /* Methods that allow basic manipulation of non-public variables */
    /* ************************************************************* */
    /**
     * Get the lowerEnvelope array.
     *
     * @return
     */
    public double[] getLowerEnvelope()
    {
        return (lowerEnvelope);
    }

    /**
     * Get the upperEnvelope array.
     *
     * @return
     */
    public double[] getUpperEnvelope()
    {
        return (upperEnvelope);
    }
}
