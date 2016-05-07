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
    private static final Logger LOG =
    Logger.getLogger(QBProbabilityFunction.class.
            getName());

    double lower_envelope[];
    double upper_envelope[];

    /**
     * Default constructor for a QBProbabilityFunction.
     */
    public QBProbabilityFunction()
    {
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param b_n
     * @param prop
     * @param n_vb
     * @param n_vl
     */
    public QBProbabilityFunction(BayesNet b_n, int n_vb, int n_vl,
                                 ArrayList prop)
    {
        super(b_n, n_vb, n_vl, prop);
        lower_envelope = new double[n_vl];
        upper_envelope = new double[n_vl];
    }

    /**
     * Constructor for ProbabilityFunction.
     *
     * @param b_n
     * @param prop
     * @param dvs
     * @param up
     * @param v
     * @param lp
     */
    public QBProbabilityFunction(BayesNet b_n, DiscreteVariable dvs[],
                                 double v[], double lp[], double up[],
                                 ArrayList prop)
    {
        super(b_n, dvs, v, prop);
        lower_envelope = lp;
        upper_envelope = up;
    }

    /**
     * Constructor for QBProbabilityFunction.
     *
     * @param df
     * @param new_up
     * @param new_values
     * @param new_lp
     */
    public QBProbabilityFunction(DiscreteFunction df, double new_values[],
                                 double new_lp[], double new_up[])
    {
        super(df, new_values);
        lower_envelope = new_lp;
        upper_envelope = new_up;
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
                out.print(" \"" + variables[j].get_name() + "\" ");
            }
            out.print(") {");
            if (lower_envelope != null)
            {
                out.println(" //" + variables.length +
                            " variable(s) and " + lower_envelope.length +
                            " values");
                out.print("\ttable lower-envelope ");
                for (j = 0; j < lower_envelope.length; j++)
                {
                    out.print(lower_envelope[j] + " ");
                }
                out.print(";");
            }
            out.println();
            if (upper_envelope != null)
            {
                out.print("\ttable upper-envelope ");
                for (j = 0; j < upper_envelope.length; j++)
                {
                    out.print(upper_envelope[j] + " ");
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
     * Get the lower_envelope array.
     *
     * @return
     */
    public double[] get_lower_envelope()
    {
        return (lower_envelope);
    }

    /**
     * Get the upper_envelope array.
     *
     * @return
     */
    public double[] get_upper_envelope()
    {
        return (upper_envelope);
    }
}
