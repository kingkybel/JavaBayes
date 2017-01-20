/*
 * InferenceGraphNode.java
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
package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import CredalSets.QBProbabilityFunction;
import CredalSets.VertexSet;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public final class InferenceGraphNode
{

    private static final Class CLAZZ = InferenceGraphNode.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    InferenceGraph inferenceGraph;

    ProbabilityVariable probVar;
    ProbabilityFunction probFunc;

    ArrayList<InferenceGraphNode> parents = new ArrayList<>();
    ArrayList<InferenceGraphNode> children = new ArrayList<>();

    Point coordinates;

    private final String defaultInferenceGraphNodeValues[] =
    {
        "true", "false"
    };
    private final BayesNet defaultInferenceGraphNodeBayesNet = null;
    private final ArrayList<String> defaultInferenceGraphNodeProperties = null;

    /**
     * Default constructor for an InferenceGraphNode.
     *
     * @param inferenceGraph the graph this node belongs to
     * @param name           the name of the node
     */
    InferenceGraphNode(InferenceGraph inferenceGraph, String name)
    {
        this(inferenceGraph, name, new Point(100, 100));
    }

    /**
     * Constructor for a InferenceGraphNode object. The created node is in an
     * incomplete state; the constructor assumes the node is new and not
     * necessarily attached to the current network in the InferenceGraph; no
     * parents nor children are defined for such a node.
     *
     * @param inferenceGraph the graph this node belongs to
     * @param name           the name of the node
     * @param coordinates    x/y coordinates of the node
     */
    InferenceGraphNode(InferenceGraph inferenceGraph,
                       String name,
                       Point coordinates)
    {
        this.inferenceGraph = inferenceGraph;

        // Initialize the variable
        probVar = new ProbabilityVariable(defaultInferenceGraphNodeBayesNet,
                                          name, BayesNet.INVALID_INDEX,
                                          defaultInferenceGraphNodeValues,
                                          defaultInferenceGraphNodeProperties);
        // Initialize the probability function
        initDists();
        // Initialize the position of the node
        this.coordinates = coordinates;
    }

    /**
     * Constructor for a InferenceGraphNode object. Note that parents and
     * children are not properly set here.
     *
     * @param inferenceGraph the graph this node belongs to
     * @param probVar        probability variable referring to the new node
     * @param probFunc       probability function referring to the new node
     */
    InferenceGraphNode(InferenceGraph inferenceGraph,
                       ProbabilityVariable probVar,
                       ProbabilityFunction probFunc)
    {
        this.inferenceGraph = inferenceGraph;
        this.probVar = probVar;
        this.probFunc = probFunc;
        coordinates = parseCoordinates(probVar);
    }

    /**
     * Constructor for a InferenceGraphNode object. Note that parents and
     * children are not properly set here. probability variable referring to the
     * new node
     *
     * @param inferenceGraph the graph this node belongs to
     * @param probVar        probability variable referring to the new node
     * @param probFunc       probability function referring to the new node
     * @param coordinates    x/y coordinates of the node
     */
    InferenceGraphNode(InferenceGraph inferenceGraph,
                       ProbabilityVariable probVar,
                       ProbabilityFunction probFunc,
                       Point coordinates)
    {
        this.inferenceGraph = inferenceGraph;
        this.probVar = probVar;
        this.probFunc = probFunc;
        this.coordinates = coordinates;
    }

    /**
     * Initialisation for the probability function in the InferenceGraphNode.
     */
    void initDists()
    {
        int i, totalValues;
        double newValue;

        // Create the probabilityVariables
        ProbabilityVariable probVars[] =
                              new ProbabilityVariable[parents.size() + 1];
        probVars[0] = probVar;

        totalValues = probVar.numberValues();
        newValue = 1.0 / ((double) (totalValues));

        i = 1;
        for (InferenceGraphNode pnode : parents)
        {
            probVars[i] = pnode.probVar;
            totalValues *= pnode.probVar.numberValues();
            i++;
        }

        // Compute the default (uniformly distributed) values
        double dists[] = new double[totalValues];
        for (i = 0; i < dists.length; i++)
        {
            dists[i] = newValue;
        }

        // Construct the ProbabilityFunction
        probFunc = new ProbabilityFunction(defaultInferenceGraphNodeBayesNet,
                                           probVars,
                                           dists,
                                           defaultInferenceGraphNodeProperties);
    }

    /**
     * Update the position property.
     */
    void updatePosition()
    {
        ArrayList<String> properties = probVar.getProperties();
        ArrayList<String> propertiesToRemove = new ArrayList();
        String s;

        if ((properties != null) && (properties.size() > 0))
        {
            for (String ss : properties)
            {
                s = ss.trim();

                // If property is not position, skip it
                if (!s.startsWith("position"))
                {
                    continue;
                }

                //Schedule the current position property for removal
                propertiesToRemove.add(ss);
            }

            // Remove the old position properties
            for (String ss : propertiesToRemove)
            {
                probVar.removeProperty(ss);
            }
        }

        // Build/Insert the new position property
        probVar.addProperty("position = (" + coordinates.x + ", " +
                            coordinates.y + ")");
    }

    /**
     * Get the coordinates of a InferenceGraphNode from the properties in the
     * variable.
     *
     * @param probVar a probability variable
     * @return the coordinates as point
     */
    private Point parseCoordinates(ProbabilityVariable probVar)
    {
        ArrayList<String> properties = probVar.getProperties();
        Point finalPosition = null;
        String s;

        // get position values from the list of properties
        if (properties.isEmpty())
        {
            return new Point(100, 100);
        }

        try
        {
            for (String ss : properties)
            {
                s = ss.trim();

                // If property is not position, skip it
                if (!s.startsWith("position"))
                {
                    continue;
                }

                final ByteArrayInputStream byteArrayInputStream =
                                           new ByteArrayInputStream(s.getBytes());
                Reader reader = new BufferedReader(
                       new InputStreamReader(byteArrayInputStream));
                StreamTokenizer st = new StreamTokenizer(reader);
                st.parseNumbers();
                int tok;
                int x = -1;
                while ((tok = st.nextToken()) != StreamTokenizer.TT_EOF)
                {
                    if (tok != StreamTokenizer.TT_NUMBER)
                    {
                        continue;
                    }
                    if (x == -1)
                    {
                        x = (int) st.nval;
                    }
                    else
                    {
                        int y = (int) st.nval;
                        finalPosition = new Point(x, y);
                    }
                }
                break;
            }
        }
        catch (IOException e)
        {
            finalPosition = new Point(100, 100);
        }
        if (finalPosition == null)
        {
            finalPosition = new Point(100, 100);
        }

        return finalPosition;
    }

    /**
     * Get a single value of the probability function in the node given a list
     * of pairs (Variable Value). The list specifies which element of the
     * function is referred to.
     *
     * @param variableValuePairs variable-value-pairs as arrays of string arrays
     * @param indexExtremePoint  index of the extreme point
     * @return the value at the extreme point
     */
    public double getFunctionValue(String variableValuePairs[][],
                                   int indexExtremePoint)
    {
        if (probFunc instanceof VertexSet)
        {
            return ((VertexSet) probFunc).evaluate(variableValuePairs,
                                                   indexExtremePoint);
        }
        else
        {
            return probFunc.evaluate(variableValuePairs);
        }
    }

    /**
     * Get an array containing probability values.
     *
     * @return the probability values as an array of doubles
     */
    public double[] getFunctionValues()
    {
        if (probFunc instanceof VertexSet)
        {
            double[][] ep = ((VertexSet) probFunc).getExtremePoints();
            return ep[0];
        }
        else
        {
            return probFunc.getValues();
        }
    }

    /**
     * Get an array containing probability values; if credal set, return the
     * first extreme point.
     *
     * @param indexExtremePoint index of the extreme points if the probability
     *                          function is a vertex set, ignored otherwise
     * @return the probability values as an array of doubles
     */
    public double[] getFunctionValues(int indexExtremePoint)
    {
        if (probFunc instanceof VertexSet)
        {
            double[][] ep = ((VertexSet) probFunc).getExtremePoints();
            return ep[indexExtremePoint];
        }
        else
        {
            return probFunc.getValues();
        }
    }

    /**
     * Set an array containing probability values; if credal set, insert the
     * array in the first extreme point.
     *
     * @param probValues the probability values of the function as array of
     *                   doubles
     */
    public void setFunctionValues(double[] probValues)
    {
        if (probFunc instanceof VertexSet)
        {
            ((VertexSet) probFunc).setExtremePoint(0, probValues);
        }
        else
        {
            probFunc.setValues(probValues);
        }
    }

    /**
     * Set an array containing an extreme point of the credal set.
     *
     * @param indexExtremePoint index of the extreme points if the probability
     *                          function is a vertex set, ignored otherwise
     * @param probValues        the probability values of the function as array
     *                          of doubles
     */
    public void setFunctionValues(int indexExtremePoint, double[] probValues)
    {
        if (probFunc instanceof VertexSet)
        {
            ((VertexSet) probFunc).
                    setExtremePoint(indexExtremePoint, probValues);
        }
        else
        {
            if (indexExtremePoint == 0)
            {
                probFunc.setValues(probValues);
            }
        }
    }

//    /**
//     * Get a single value of the probability function in the node given the
//     * index of the value and the index of the extreme point.
//     */
//    public double getFunctionValue(int index, int indexExtremePoint) {
//        if (probFunc instanceof VertexQBProbabilityFunction)
//            return( ((VertexQBProbabilityFunction)probFunc).getValue(index, indexExtremePoint) );
//        else
//            return(probFunc.getValue(index));
//    }
//    /**
//     * Get a single value of the probability function in the node given the
//     * index of the value.
//     */
//    public double getFunctionValue(int index) {
//        if (probFunc instanceof VertexQBProbabilityFunction)
//            return( ((VertexQBProbabilityFunction)probFunc).getValue(index, 0) );
//        else
//            return(probFunc.getValue(index));
//    }
    /**
     * Set a single value of the probability function in the node given a list
     * of pairs (Variable Value). The list specifies which element of the
     * function is referred to.
     *
     * @param variableValuePairs variable-value-pairs as arrays of string arrays
     * @param probValue          the probability value to set
     * @param indexExtremePoint  index of the extreme points if the probability
     *                           function is a vertex set, ignored otherwise
     */
    public void setFunctionValue(String variableValuePairs[][],
                                 double probValue,
                                 int indexExtremePoint)
    {
        if (probFunc instanceof VertexSet)
        {
            ((VertexSet) probFunc).setValue(variableValuePairs, probValue,
                                            indexExtremePoint);
        }
        else
        {
            probFunc.setValue(variableValuePairs, probValue);
        }
    }

    /**
     * Return the name of the variable in the node.
     *
     * @return the name of the variable
     */
    public String getName()
    {
        return probVar.getName();
    }

    /**
     * Set the name of the variable.
     *
     * @param name the name of the variable
     */
    public void setName(String name)
    {
        probVar.setName(name);
    }

    /**
     * Get the name of all variables in the probability function.
     *
     * @return all variable names as string array
     */
    public String[] getAllNames()
    {
        String[] ns = new String[probFunc.numberVariables()];
        for (int i = 0; i < ns.length; i++)
        {
            ns[i] = probFunc.getVariable(i).getName();
        }
        return ns;
    }

    /**
     * Return the values of the variable in the node.
     *
     * @return all enumerated discrete variable values as string array
     */
    public String[] getValues()
    {
        return probVar.getValues();
    }

    /**
     * Get all values for variables in the function in the node.
     *
     * @return all values for variables in the function
     */
    public String[][] getAllVariableValues()
    {
        int i, j;
        String allValues[][] = new String[probFunc.numberVariables()][];
        DiscreteVariable dv;
        for (i = 0; i < probFunc.numberVariables(); i++)
        {
            dv = probFunc.getVariable(i);
            allValues[i] = new String[dv.numberValues()];
            for (j = 0; j < allValues[i].length; j++)
            {
                allValues[i][j] = dv.getValue(j);
            }
        }
        return allValues;
    }

    /**
     * Return the number of values in the variable in the node.
     *
     * @return the number of values in the variable
     */
    public int getNumberValues()
    {
        return probVar.numberValues();
    }

    /**
     * Indicate whether the node has parents.
     *
     * @return true if so, false otherwise
     */
    public boolean hasParent()
    {
        return probFunc.numberVariables() > 1;
    }

    /**
     * Return the parents of a node as list.
     *
     * @return the parents of a node as list
     */
    public ArrayList<InferenceGraphNode> getParents()
    {
        return parents;
    }

    /**
     * Return the children of a node as list.
     *
     * @return the children of a node as list
     */
    public ArrayList<InferenceGraphNode> getChildren()
    {
        return children;
    }

    /**
     * Indicate whether the variable in the node is observed.
     *
     * @return true if so, false otherwise
     */
    public boolean isObserved()
    {
        return probVar.isObserved();
    }

    /**
     * Indicate whether the variable in the node is an explanatory variable.
     *
     * @return true if so false otherwise
     */
    public boolean isExplanation()
    {
        return probVar.isExplanation();
    }

    /**
     * Return the observed index for the variable in the node.
     *
     * @return the observed index for the variable
     */
    public int getObservedIndex()
    {
        return probVar.getObservedIndex();
    }

    /**
     * Return the observed value for the variable in the node.
     *
     * @return the observed value for the variable
     */
    public String getObservedValue()
    {
        return probVar.getValue(getObservedIndex());
    }

    /**
     * Return the X coordinate of the node.
     *
     * @return the X coordinate
     */
    public int getXCoordinate()
    {
        return coordinates.x;
    }

    /**
     * Return the Y coordinate of the node.
     *
     * @return the Y coordinate
     */
    public int getYCoordinate()
    {
        return coordinates.y;
    }

    /**
     * Return the variable properties.
     *
     * @return the variable properties as list
     */
    public ArrayList<String> getVariableProperties()
    {
        return probVar.getProperties();
    }

    /**
     * Set the variable properties.
     *
     * @param properties list of properties
     */
    public void setVariableProperties(ArrayList<String> properties)
    {
        probVar.setProperties(properties);
    }

    /**
     * Return the function properties.
     *
     * @return the function properties as list
     */
    public ArrayList<String> getFunctionProperties()
    {
        return probFunc.getProperties();
    }

    /**
     * Set the function properties.
     *
     * @param properties list of properties
     */
    public void setFunctionProperties(ArrayList<String> properties)
    {
        probFunc.setProperties(properties);
    }

    /**
     * Whether or not the node represents a convex set of distributions (credal
     * set).
     *
     * @return true if so, false otherwise
     */
    public boolean isCredalSet()
    {
        return probFunc instanceof QBProbabilityFunction;
    }

    /**
     * Number of distributions that are represented by a node.
     *
     * @return number of distributions
     */
    public int numberExtremeDistributions()
    {
        if (probFunc instanceof VertexSet)
        {
            return ((VertexSet) probFunc).getExtremePoints().length;
        }
        else
        {
            return 1;
        }
    }

    /**
     * Make sure the node represents a single distribution.
     */
    public void setNoLocalCredalSet()
    {
        if (probFunc instanceof QBProbabilityFunction)
        {
            if (probFunc instanceof VertexSet)
            {
                ((VertexSet) probFunc).composeValues();
            }
            probFunc = new ProbabilityFunction(probFunc, probFunc.getValues());
        }
    }

    /**
     * Make sure the node represents a VertexSet with a given number of extreme
     * distributions.
     *
     * @param numberExtremePoints number of extreme distributions
     */
    public void setLocalCredalSet(int numberExtremePoints)
    {
        if (!(probFunc instanceof VertexSet))
        {
            probFunc = new VertexSet(probFunc);
        }
        ((VertexSet) probFunc).setLocalCredalSet(numberExtremePoints);
    }

    /**
     * Make sure the node represents a VertexSet.
     */
    public void setLocalCredalSet()
    {
        if (!(probFunc instanceof VertexSet))
        {
            probFunc = new VertexSet(probFunc);
        }
    }

    /**
     * Set the observation for the node.
     *
     * @param value the enumerated string value that was observed in this node
     */
    public void setObservationValue(String value)
    {
        probVar.setObservedValue(value);
    }

    /**
     * Clear the observation for the node.
     */
    public void clearObservation()
    {
        probVar.setInvalidObservedIndex();
    }

    /**
     * Set the explanatory status of the node.
     *
     * @param flag true means this node is explanation, false means it's not
     */
    public void setExplanation(boolean flag)
    {
        if (flag == true)
        {
            probVar.setExplanationValue(0);
        }
        else
        {
            probVar.setExplanationValue(BayesNet.INVALID_INDEX);
        }
    }

    /**
     * Remove a property from a variable.
     *
     * @param propIndex the index of the property to remove
     */
    public void removeVariableProperty(int propIndex)
    {
        probVar.removeProperty(propIndex);
    }

    /**
     * Remove a property from a function.
     *
     * @param propIndex the index of the property to remove
     */
    public void removeFunctionProperty(int propIndex)
    {
        probFunc.removeProperty(propIndex);
    }

    /**
     * Add a property to a variable.
     *
     * @param property property given as string
     */
    public void addVariableProperty(String property)
    {
        probVar.addProperty(property);
        updateCoordinatesFromProperty(property);
    }

    /**
     * Update the coordinate of this node by taking into account the given
     * property.
     *
     * @param property property given as string
     */
    public void updateCoordinatesFromProperty(String property)
    {
        // If property is position:
        if (property.startsWith("position"))
        {
            Point finalPosition = null;
            // Parse the position property
            try
            {
                final ByteArrayInputStream byteArrayInputStream =
                                           new ByteArrayInputStream(property.
                                                   getBytes());
                Reader reader = new BufferedReader(
                       new InputStreamReader(byteArrayInputStream));
                StreamTokenizer st = new StreamTokenizer(reader);
                st.parseNumbers();
                int tok;
                int x = -1;
                while ((tok = st.nextToken()) != StreamTokenizer.TT_EOF)
                {
                    if (tok != StreamTokenizer.TT_NUMBER)
                    {
                        continue;
                    }
                    if (x == -1)
                    {
                        x = (int) st.nval;
                    }
                    else
                    {
                        int y = (int) st.nval;
                        finalPosition = new Point(x, y);
                    }
                }
            }
            catch (IOException e)
            {
                finalPosition = new Point(100, 100);
            }
            if (finalPosition == null)
            {
                finalPosition = new Point(100, 100);
            }
            // Update the position property.
            coordinates = finalPosition;
        }
    }

    /**
     * Add a property from to function.
     *
     * @param property property given as string
     */
    public void addFunctionProperty(String property)
    {
        probFunc.addProperty(property);
    }

}
