/*
 * VertexSet.java
 * @author Fabio G. Cozman
 * Copyright 1996 - 1999, Fabio G. Cozman,
 *          Carnergie Mellon University, Universidade de Sao Paulo
 * fgcozman@usp.br, http://www.cs.cmu.edu/~fgcozman/home.html
 *
 * The JavaBayes distribution is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation (either
 * version 2 of the License or, at your option, any later version),
 * provided that this notice and the name of the author appear in all
 * copies. Upon request to the author, some of the packages in the
 * JavaBayes distribution can be licensed under the GNU Lesser General
 * Public License as published by the Free Software Foundation (either
 * version 2 of the License, or (at your option) any later version).
 * If you're using the software, please notify fgcozman@usp.br so
 * that you can receive updates and patches. JavaBayes is distributed
 * "as is", in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with the JavaBayes distribution. If not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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
 * @author Fabio G. Cozman
 */
public final class VertexSet
        extends FinitelyGeneratedSet
{

    private static final Class CLAZZ = VertexSet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Variable that indicates which extreme point is active
    ProbabilityVariable auxiliaryVariable;

    // The set of extreme points; the first coordinate indexes
    // a set of double[] arrays (each array contains the values
    // for an extreme point).
    double extremePoints[][];

    /**
     * Default constructor for a VertexQBProbabilityFunction.
     *
     * @param bayesNet      the underlying Bayesian network
     * @param properties list of properties
     * @param variables
     * @param extremePoints
     */
    public VertexSet(BayesNet bayesNet,
                     ProbabilityVariable variables[],
                     double extremePoints[][],
                     ArrayList properties)
    {
        // Call the super constructor with extremePoints[0].
        super(bayesNet, variables, extremePoints[0], properties);

        // Now replace ep[0] with a new array to avoid wrong
        // cross-references among arrays.
        double[] vals = new double[extremePoints[0].length];
        values = vals;

        // Update the extremePoints and the values.
        this.extremePoints = extremePoints;
        composeValues();
    }

    /**
     * Constructor for a VertexQBProbabilityFunction.
     *
     * @param bayesNet      the underlying Bayesian network
     * @param extremePoints
     * @param probVars      probability variables
     * @param properties list of properties
     * @param values
     */
    public VertexSet(BayesNet bayesNet,
                     ProbabilityVariable probVars[],
                     double values[],
                     ArrayList properties,
                     double extremePoints[][])
    {
        super(bayesNet,
              probVars,
              values,
              (double[]) null,
              (double[]) null,
              properties);
        this.extremePoints = extremePoints;
    }

    /**
     * Constructor for a VertexQBProbabilityFunction.
     *
     * @param probFunc
     */
    public VertexSet(ProbabilityFunction probFunc)
    {
        super(probFunc, probFunc.getValues());
        if (probFunc instanceof VertexSet)
        {
            extremePoints = ((VertexSet) probFunc).extremePoints;
        }
        else
        {
            extremePoints = new double[1][];
            extremePoints[0] = probFunc.getValues();
        }
    }

    /**
     * Constructor for a VertexQBProbabilityFunction from a ProbabilityFunction
     * object and new values.
     *
     * @param probFunc
     * @param values
     */
    public VertexSet(ProbabilityFunction probFunc, double values[])
    {
        super(probFunc, values);
        if (probFunc instanceof VertexSet)
        {
            extremePoints = ((VertexSet) probFunc).extremePoints;
            auxiliaryVariable = ((VertexSet) probFunc).auxiliaryVariable;
        }
        else
        {
            extremePoints = new double[1][];
            extremePoints[0] = probFunc.getValues();
        }
    }

    /**
     * Put together all the values for the possible vertices of credal set and
     * create an auxiliary variable to indicate which vertex to consider There
     * are three things to do:
     * <ol>
     * <li>Create an auxiliaryVariable with correct values. </li>
     * <li> Combine the values into a new array.</li>
     * <li> Insert the auxiliaryVariable into the variables array.</li>
     * </ol>
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
        newQbpf.bayesNet = transformedBn;
        newQbpf.auxiliaryVariable = auxv;
        newQbpf.variables = newVariables;

        return (newQbpf);
    }

    /**
     * Create a new array of values that combines extreme points.
     *
     * @param transformedBn *unused*
     *
     * @return the new array of doubles
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
     * Create an auxiliary variable to indicate the vertices.
     */
    private ProbabilityVariable
            createAuxiliaryVariable(BayesNet transformedBn)
    {
        int i;

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
        ProbabilityVariable probVar;

        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bayesNet.numberVariables()];

        // Fill the array of markers.
        for (String[] variableValuePair : variableValuePairs)
        {
            index = bayesNet.indexOfVariable(variableValuePair[0]);
            probVar = bayesNet.getProbabilityVariable(index);
            valueIndexes[index] = probVar.indexOfValue(variableValuePair[1]);
        }

        // Now evaluate
        int position =
            getPositionFromIndexes(bayesNet.getProbabilityVariables(),
                                   valueIndexes);
        return (extremePoints[indexExtremePoint][position]);
    }

    /**
     * Set a single value of the probability function.
     *
     * @param variableValuePairs
     * @param indexExtremePoint
     * @param value
     */
    public void setValue(String variableValuePairs[][],
                         double value,
                         int indexExtremePoint)
    {
        int index;
        ProbabilityVariable probVar;

        // Initialize with zeros an array of markers.
        int valueIndexes[] = new int[bayesNet.numberVariables()];

        // Fill the array of markers.
        for (String[] variableValuePair : variableValuePairs)
        {
            index = bayesNet.indexOfVariable(variableValuePair[0]);
            probVar = bayesNet.getProbabilityVariable(index);
            valueIndexes[index] = probVar.indexOfValue(variableValuePair[1]);
        }

        // Get the position of the value in the array of values
        int pos = getPositionFromIndexes(bayesNet.getProbabilityVariables(),
                                         valueIndexes);
        // Set the value.
        extremePoints[indexExtremePoint][pos] = value;
        composeValues();
    }

    /**
     * Print method.
     *
     * @param out output print stream
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
            for (double[] extremePoint : extremePoints)
            {
                aux += extremePoint[i];
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
     * @param extremePoints
     */
    public void setExtremePoint(int index, double extremePoints[])
    {
        this.extremePoints[index] = extremePoints;
    }

    /**
     * Retrieve the auxiliary variable.
     *
     * @return
     */
    public ProbabilityVariable getAuxiliaryVariable()
    {
        return (auxiliaryVariable);
    }

    /**
     * Retrieve the extreme points.
     *
     * @return
     */
    public double[][] getExtremePoints()
    {
        return (extremePoints);
    }
}
