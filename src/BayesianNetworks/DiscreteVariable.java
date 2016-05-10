package BayesianNetworks;

import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * @author kybelksd****************************************************************
 */
public class DiscreteVariable
{
    private static final Logger LOG =
    Logger.getLogger(DiscreteVariable.class.
            getName());

    /**
     *
     */
    protected String name; // Name of the variable

    /**
     *
     */
    protected int index;   // Index of the variable in a collection of variables

    /**
     *
     */
    protected String values[]; // Values of the variable

    /**
     * Default constructor for a DiscreteVariable.
     */
    public DiscreteVariable()
    {
        name = null;
        index = BayesNet.INVALID_INDEX;
        values = null;
    }

    /**
     * Simple constructor for DiscreteVariable.
     *
     * @param nVb Name of the variable.
     */
    public DiscreteVariable(String nVb)
    {
        name = nVb;
        index = BayesNet.INVALID_INDEX;
        values = null;
    }

    /**
     * Simple constructor for DiscreteVariable.
     *
     * @param vb Name of the variable.
     * @param vi Index of the variable.
     * @param vl Values of the variable.
     */
    public DiscreteVariable(String vb, int vi, String vl[])
    {
        name = vb;
        index = vi;
        values = vl;
    }

    /**
     * Simple constructor for DiscreteVariable.
     *
     * @param dv DiscreteVariable that is copied into current DiscreteVariable.
     */
    public DiscreteVariable(DiscreteVariable dv)
    {
        name = dv.name;
        index = dv.index;
        values = dv.values;
    }

    /**
     * Determine the index of a value given its name; returns INVALID_INDEX if
     * there is no index.
     *
     * @param value
     * @return
     */
    public int indexOfValue(String value)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(value))
            {
                return (i);
            }
        }
        return (BayesNet.INVALID_INDEX);
    }

    /**
     * Produce an array of numeric values for the values of a variable. The
     * values are direct translation of the string values into doubles; if the
     * translation fails for a particular value, that value is replaced by its
     * index.
     *
     * @return
     */
    public DiscreteFunction getNumericValues()
    {
        Double daux;
        DiscreteVariable dvs[] = new ProbabilityVariable[1];
        dvs[0] = this;
        double numericValues[] = new double[values.length];
        for (int i = 0; i < values.length; i++)
        {
            try
            {
                daux = Double.valueOf(values[i]);
                numericValues[i] = daux.doubleValue();
            }
            catch (NumberFormatException e)
            {
                numericValues[i] = (double) i;
            }
        }
        return (new DiscreteFunction(dvs, numericValues));
    }

    /**
     * Print method for DiscreteVariable.
     */
    public void print()
    {
        print(System.out);
    }

    /**
     * Print method for DiscreteVariable.
     *
     * @param out
     */
    public void print(PrintStream out)
    {
        if (this == null)
        {
            return;
        }
        out.print("variable ");
        if (name != null)
        {
            out.print(" \"" + name + "\" ");
        }
        out.print("{");
        if (values != null)
        {
            out.println("//" + values.length + " values");
            out.print("\ttype discrete[" + values.length + "] { ");
            for (int i = 0; i < values.length; i++)
            {
                out.print(" \"" + values[i] + "\" ");
            }
            out.println("};");
        }
        out.println("}");
    }

    /* *************************************************************** */
    /*  Methods that allow basic manipulation of non-public variables  */
    /* *************************************************************** */
    /**
     * Get the name of the current DiscreteVariable.
     *
     * @return
     */
    public String getName()
    {
        return (name);
    }

    /**
     * Set the name of the current DiscreteVariable.
     *
     * @param n
     */
    public void setName(String n)
    {
        name = n;
    }

    /**
     * Get the index of the current DiscreteVariable.
     *
     * @return
     */
    public int getIndex()
    {
        return (index);
    }

    /**
     * Return the number of values in the current DiscreteVariable.
     *
     * @return
     */
    public int numberValues()
    {
        return (values.length);
    }

    /**
     * Get the values of the current DiscreteVariable.
     *
     * @return
     */
    public String[] getValues()
    {
        return (values);
    }

    /**
     * Set the values of the current DiscreteVariable.
     *
     * @param vals
     */
    public void setValues(String vals[])
    {
        values = vals;
    }

    /**
     * Get a value of the current DiscreteVariable.
     *
     * @param i Position of the value in the array of values.
     * @return
     */
    public String getValue(int i)
    {
        return (values[i]);
    }
}
