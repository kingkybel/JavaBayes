/**
 * VertexSet.java
 *
 * @author Fabio G. Cozman Copyright 1996 - 1999, Fabio G. Cozman, Carnergie
 * Mellon University, Universidade de Sao Paulo fgcozman@usp.br,
 * http://www.cs.cmu.edu/~fgcozman/home.html
 *
 * The JavaBayes distribution is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (either version 2 of the License or, at your
 * option, any later version), provided that this notice and the name of the
 * author appear in all copies. Upon request to the author, some of the packages
 * in the JavaBayes distribution can be licensed under the GNU Lesser General
 * Public License as published by the Free Software Foundation (either version 2
 * of the License, or (at your option) any later version). If you're using the
 * software, please notify fgcozman@usp.br so that you can receive updates and
 * patches. JavaBayes is distributed "as is", in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with the JavaBayes distribution. If not, write
 * to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 */
package CredalSets;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public final class VertexSet
        extends FinitelyGeneratedSet
{
    private static final Logger LOG =
    Logger.getLogger(VertexSet.class.getName());

    // Variable that indicates which extreme point is active
    ProbabilityVariable auxiliaryVariable;

    // The set of extreme points; the first coordinate indexes
    // a set of double[] arrays (each array contains the values
    // for an extreme point).
    double extremePoints[][];

    /**
     * Default constructor for a VertexQBProbabilityFunction.
     *
     * @param bN
     * @param prop
     * @param pvs
     * @param ep
     */
    public VertexSet(BayesNet bN, ProbabilityVariable pvs[],
                     double ep[][], ArrayList prop)
    {
        // Call the super constructor with ep[0].
        super(bN, pvs, ep[0], prop);

        // Now replace ep[0] with a new array to avoid wrong
        // cross-references among arrays.
        double[] vals = new double[ep[0].length];
        values = vals;

        // Update the extremePoints and the values.
        extremePoints = ep;
        composeValues();
    }

    /**
     * Constructor for a VertexQBProbabilityFunction.
     *
     * @param bN
     * @param ep
     * @param pvs
     * @param prop
     * @param v
     */
    public VertexSet(BayesNet bN, ProbabilityVariable pvs[],
                     double v[], ArrayList prop, double ep[][])
    {
        super(bN, pvs, v, (double[]) null, (double[]) null, prop);
        extremePoints = ep;
    }

    /**
     * Constructor for a VertexQBProbabilityFunction.
     *
     * @param pf
     */
    public VertexSet(ProbabilityFunction pf)
    {
        super(pf, pf.getValues());
        if (pf instanceof VertexSet)
        {
            extremePoints = ((VertexSet) pf).extremePoints;
        }
        else
        {
            extremePoints = new double[1][];
            extremePoints[0] = pf.getValues();
        }
    }

    /**
     * Constructor for a VertexQBProbabilityFunction from a ProbabilityFunction
     * object and new values.
     *
     * @param pf
     * @param newValues
     */
    public VertexSet(ProbabilityFunction pf, double newValues[])
    {
        super(pf, newValues);
        if (pf instanceof VertexSet)
        {
            extremePoints = ((VertexSet) pf).extremePoints;
            auxiliaryVariable = ((VertexSet) pf).auxiliaryVariable;
        }
        else
        {
            extremePoints = new double[1][];
            extremePoints[0] = pf.getValues();
        }
    }

    /**
     * Put together all the values for the possible vertices of credal set and
     * create an auxiliary variable to indicate which vertex to consider There
     * are three things to do: 1) Create an auxiliaryVariable with correct
     * values. 2) Combine the values into a new array. 3) Insert the
     * auxiliaryVariable into the variables array.
     *
     * @param transformedBn
     * @return
     */
    public VertexSet prepareAuxiliaryVariable(BayesNet transformedBn)
    {
        int i;
        double newValues[];

        // Assume that values and auxiliaryVariable are correct if
        // auxiliaryVariable is non null (cannot happen in current version)
        if (auxiliaryVariable != null)
        {
            return (this);
        }

        // Create the auxiliary variable for this credal set
        ProbabilityVariable auxv = createAuxiliaryVariable(transformedBn);

        // Create the new values for the credal set with auxiliaryVariable
        newValues = createNewValues(transformedBn);

        // Now insert the auxiliaryVariable in the variables array
        DiscreteVariable newVariables[] =
                           new DiscreteVariable[variables.length + 1];
        for (i = 0; i < variables.length; i++)
        {
            newVariables[i] = variables[i];
        }
        newVariables[i] = auxv;

        // Use the newValues array to create a new
        // VertexQBProbabilityFunction that incorporates the auxiliaryVariable
        VertexSet newQbpf = new VertexSet(this, newValues);
        newQbpf.bn = transformedBn;
        newQbpf.auxiliaryVariable = auxv;
        newQbpf.variables = newVariables;

        return (newQbpf);
    }

    /**
     * Create a new array of values that combines extreme points.
     */
    private double[] createNewValues(BayesNet transformedBn)
    {
        int i, j;
        // Combine vertices and the auxiliaryVariable and create new values
        double newValues[] =
                 new double[extremePoints.length * values.length];
        for (i = 0; i < values.length; i++)
        {
            for (j = 0; j < extremePoints.length; j++)
            {
                newValues[j + i * extremePoints.length] =
                extremePoints[j][i];
            }
        }
        return (newValues);
    }

    /**
     * Create an auxiliar variable to indicate the vertices.
     */
    private ProbabilityVariable
            createAuxiliaryVariable(BayesNet transformedBn)
    {
        int i, j;

        // Compose the name of the auxiliary variable
        StringBuffer bufferAuxiliaryVariableName =
                     new StringBuffer("<Transparent:");
        bufferAuxiliaryVariableName.append(variables[0].getName());
        bufferAuxiliaryVariableName.append(">");
        String auxiliaryVariableName =
               new String(bufferAuxiliaryVariableName);

        // Compose the values of the auxiliary variable
        String auxiliaryVariableValues[] = new String[extremePoints.length];
        for (i = 0; i < auxiliaryVariableValues.length; i++)
        {
            auxiliaryVariableValues[i] = String.valueOf(i);
        }

        // Create the auxiliary variable
        ProbabilityVariable auxv =
                            new ProbabilityVariable(transformedBn,
                                                    auxiliaryVariableName,
                                                    BayesNet.INVALID_INDEX,
                                                    auxiliaryVariableValues,
                                                    ((ArrayList) null));
        // Mark the auxiliary variable as auxiliary
        auxv.setType(ProbabilityVariable.TRANSPARENT);

        // Return the created auxiliary variable
        return (auxv);
    }

    /**
     * Evaluate a function given a list of pairs (Variable Value) which
     * specifies a value of the function, and the index of the extreme
     * distribution to consider.
     *
     * @param variableValuePairs
     * @param indexExtremePoint
     * @return
     */
    public double evaluate(String variableValuePairs[][],
                           int indexExtremePoint)
    {
        int index;
        ProbabilityVariable pv;

        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bn.numberVariables()];

        // Fill the array of markers.
        for (int i = 0; i < variableValuePairs.length; i++)
        {
            index = bn.indexOfVariable(variableValuePairs[i][0]);
            pv = bn.getProbabilityVariable(index);
            valueIndexes[index] = pv.indexOfValue(variableValuePairs[i][1]);
        }

        // Now evaluate
        int position = getPositionFromIndexes(bn.getProbabilityVariables(),
                                                 valueIndexes);
        return (extremePoints[indexExtremePoint][position]);
    }

    /**
     * Set a single value of the probability function.
     *
     * @param variableValuePairs
     * @param indexExtremePoint
     * @param val
     */
    public void setValue(String variableValuePairs[][], double val,
                          int indexExtremePoint)
    {
        int index;
        ProbabilityVariable pv;

        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bn.numberVariables()];

        // Fill the array of markers.
        for (int i = 0; i < variableValuePairs.length; i++)
        {
            index = bn.indexOfVariable(variableValuePairs[i][0]);
            pv = bn.getProbabilityVariable(index);
            valueIndexes[index] = pv.indexOfValue(variableValuePairs[i][1]);
        }

        // Get the position of the value in the array of values
        int pos = getPositionFromIndexes(bn.getProbabilityVariables(),
                                            valueIndexes);
        // Set the value.
        extremePoints[indexExtremePoint][pos] = val;
        composeValues();
    }

    /**
     * Print method.
     *
     * @param out
     */
    @Override
    public void print(PrintStream out)
    {
        int i, j;
        String property;

        if (variables != null)
        {
            out.print("probability ( ");
            for (j = 0; j < variables.length; j++)
            {
                out.print(" \"" + variables[j].getName() + "\" ");
            }
            out.print(") {");
            out.println(" //" + variables.length +
                        " variable(s) and " + values.length + " values");
            if (extremePoints != null)
            {
                for (i = 0; i < extremePoints.length; i++)
                {
                    out.print("\ttable ");
                    for (j = 0; j < extremePoints[i].length; j++)
                    {
                        out.print(extremePoints[i][j] + " ");
                    }
                    out.println(";");
                }
                out.print(" // Values: ");
            }
            out.print("\ttable ");
            for (j = 0; j < values.length; j++)
            {
                out.print(values[j] + " ");
            }
            out.print(";");
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

    /**
     * Produce the centroid of all extreme distributions and insert it into the
     * values of the distribution.
     */
    public void composeValues()
    {
        double aux, n;

        if (extremePoints == null)
        {
            return;
        }

        n = (double) (extremePoints.length);

        for (int i = 0; i < values.length; i++)
        {
            aux = 0.0;
            for (int j = 0; j < extremePoints.length; j++)
            {
                aux += extremePoints[j][i];
            }
            values[i] = aux / n;
        }
    }

    /**
     * Set the number of extreme distributions in the credal set.
     *
     * @param numberExtremePoints
     */
    public void setLocalCredalSet(int numberExtremePoints)
    {
        int i, j, k;
        int numberCurrentExtremePoints;
        double newExtremePoints[][];

        // Update the values in case some extreme distributions
        // have changed.
        composeValues();

        if (extremePoints == null)
        {
            numberCurrentExtremePoints = 0;
        }
        else
        {
            numberCurrentExtremePoints = extremePoints.length;

            // If the new size is equal to current size, return.
            if (numberExtremePoints == numberCurrentExtremePoints)
            {
                return;
            }
        }

        // Allocate the new extreme distributions.
        newExtremePoints = new double[numberExtremePoints][values.length];

        // If the new size is larger than the current size.
        if (numberExtremePoints > numberCurrentExtremePoints)
        {
            // First copy what is already there.
            for (i = 0; i < numberCurrentExtremePoints; i++)
            {
                for (j = 0; j < extremePoints[i].length; j++)
                {
                    newExtremePoints[i][j] = extremePoints[i][j];
                }
            }
            // Then fill with copies of values.
            for (k = i; k < newExtremePoints.length; k++)
            {
                for (j = 0; j < values.length; j++)
                {
                    newExtremePoints[k][j] = values[j];
                }
            }
        }
        else
        {
            // If the new size is smaller than the current size.
            for (i = 0; i < newExtremePoints.length; i++)
            {
                for (j = 0; j < values.length; j++)
                {
                    newExtremePoints[i][j] = extremePoints[i][j];
                }
            }
        }

        extremePoints = newExtremePoints;
    }

    /**
     * Set an extreme point of the credal set.
     *
     * @param index
     * @param ep
     */
    public void setExtremePoint(int index, double ep[])
    {
        extremePoints[index] = ep;
    }

    /**
     * Methods that allow basic manipulation of non-public variables.
     *
     * @return
     */
    public ProbabilityVariable getAuxiliaryVariable()
    {
        return (auxiliaryVariable);
    }

    /**
     *
     * @return
     */
    public double[][] getExtremePoints()
    {
        return (extremePoints);
    }
}
