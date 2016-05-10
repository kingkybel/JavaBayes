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
package InferenceGraphs;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import CredalSets.QBProbabilityFunction;
import CredalSets.VertexSet;
import java.awt.Point;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public final class InferenceGraphNode
{

    private static final Logger LOGGER =
                                Logger.getLogger(InferenceGraphNode.class.
                                        getName());

    InferenceGraph ig;

    ProbabilityVariable pv;
    ProbabilityFunction pf;

    ArrayList parents = new ArrayList();
    ArrayList children = new ArrayList();

    Point pos;

    private final String defaultInferenceGraphNodeValues[] =
    {
        "true", "false"
    };
    private final BayesNet defaultInferenceGraphNodeBayesNet = null;
    private final ArrayList defaultInferenceGraphNodeProperties = null;

    /*
     * Default constructor for an InferenceGraphNode.
     */
    InferenceGraphNode(InferenceGraph iG, String name)
    {
        this(iG, name, new Point(100, 100));
    }

    /*
     * Constructor for a InferenceGraphNode object. The created
     * node is in an incomplete state; the constructor assumes the
     * node is new and not necessarily attached to the current
     * network in the InferenceGraph; no parents nor
     * children are defined for such a node.
     */
    InferenceGraphNode(InferenceGraph iG, String name, Point position)
    {
        ig = iG;

        // Initialize the variable
        pv = new ProbabilityVariable(defaultInferenceGraphNodeBayesNet,
                                     name, BayesNet.INVALID_INDEX,
                                     defaultInferenceGraphNodeValues,
                                     defaultInferenceGraphNodeProperties);
        // Initialize the probability function
        initDists();
        // Initialize the position of the node
        pos = position;
    }

    /*
     * Constructor for a InferenceGraphNode object.
     * Note that parents and children are not properly set here.
     */
    InferenceGraphNode(InferenceGraph iG,
                       ProbabilityVariable pV, ProbabilityFunction pF)
    {
        ig = iG;
        pv = pV;
        pf = pF;
        pos = parsePosition(pV);
    }

    /*
     * Constructor for a InferenceGraphNode object.
     * Note that parents and children are not properly set here.
     */
    InferenceGraphNode(InferenceGraph iG,
                       ProbabilityVariable pV, ProbabilityFunction pF,
                       Point position)
    {
        ig = iG;
        pv = pV;
        pf = pF;
        pos = position;
    }

    /*
     * Initialization for the probability function
     * in the InferenceGraphNode.
     */
    void initDists()
    {
        int i, totalValues;
        double newValue;
        InferenceGraphNode pnode;

        // Create the probabilityVariables
        ProbabilityVariable pvs[] =
                              new ProbabilityVariable[parents.size() + 1];
        pvs[0] = pv;

        totalValues = pv.numberValues();
        newValue = 1.0 / ((double) (totalValues));

        i = 1;
        for (Object e : parents)
        {
            pnode = (InferenceGraphNode) (e);
            pvs[i] = pnode.pv;
            totalValues *= pnode.pv.numberValues();
            i++;
        }

        // Compute the default (uniformly distributed) values
        double dists[] = new double[totalValues];
        for (i = 0; i < dists.length; i++)
        {
            dists[i] = newValue;
        }

        // Construct the ProbabilityFunction
        pf = new ProbabilityFunction(defaultInferenceGraphNodeBayesNet,
                                     pvs,
                                     dists,
                                     defaultInferenceGraphNodeProperties);
    }

    /*
     * Update the position property.
     */
    void updatePosition()
    {
        ArrayList properties = pv.getProperties();
        ArrayList propertiesToRemove = new ArrayList();
        String finalProperty = null;
        String s, ss;

        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                ss = (String) e;
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
            for (Object e : propertiesToRemove)
            {
                ss = (String) (e);
                pv.removeProperty(ss);
            }
        }

        // Build the new position property
        finalProperty = "position = (" +
                        pos.x + ", " + pos.y + ")";
        // Insert the new position
        pv.addProperty(finalProperty);
    }

    /*
     * Get the position of a InferenceGraphNode from the
     * properties in the variable.
     */
    private Point parsePosition(ProbabilityVariable pV)
    {
        ArrayList properties = pV.getProperties();
        Point finalPosition = null;
        String s, ss;

        // get position values from the list of properties
        if (properties.isEmpty())
        {
            return (new Point(100, 100));
        }

        try
        {
            for (Object e : properties)
            {
                ss = (String) e;
                s = ss.trim();

                // If property is not position, skip it
                if (!s.startsWith("position"))
                {
                    continue;
                }

                // Parse the position property
                StreamTokenizer st =
                                new StreamTokenizer(new StringBufferInputStream(
                                                s));
                st.parseNumbers();
                int tok;
                int x = -1, y = 0;
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
                        y = (int) st.nval;
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

        return (finalPosition);
    }

    /**
     * Get a single value of the probability function in the node given a list
     * of pairs (Variable Value). The list specifies which element of the
     * function is referred to.
     *
     * @param variableValuePairs
     * @param indexExtremePoint
     * @return
     */
    public double getFunctionValue(String variableValuePairs[][],
                                   int indexExtremePoint)
    {
        if (pf instanceof VertexSet)
        {
            return (((VertexSet) pf).evaluate(variableValuePairs,
                                              indexExtremePoint));
        }
        else
        {
            return (pf.evaluate(variableValuePairs));
        }
    }

    /**
     * Get an array containing probability values.
     *
     * @return
     */
    public double[] getFunctionValues()
    {
        if (pf instanceof VertexSet)
        {
            double[][] ep = ((VertexSet) pf).getExtremePoints();
            return (ep[0]);
        }
        else
        {
            return (pf.getValues());
        }
    }

    /**
     * Get an array containing probability values; if credal set, return the
     * first extreme point.
     *
     * @param index
     * @return
     */
    public double[] getFunctionValues(int index)
    {
        if (pf instanceof VertexSet)
        {
            double[][] ep = ((VertexSet) pf).getExtremePoints();
            return (ep[index]);
        }
        else
        {
            return (pf.getValues());
        }
    }

    /**
     * Set an array containing probability values; if credal set, insert the
     * array in the first extreme point.
     *
     * @param fv
     */
    public void setFunctionValues(double[] fv)
    {
        if (pf instanceof VertexSet)
        {
            ((VertexSet) pf).setExtremePoint(0, fv);
        }
        else
        {
            pf.setValues(fv);
        }
    }

    /**
     * Set an array containing an extreme point of the credal set.
     *
     * @param iep
     * @param fv
     */
    public void setFunctionValues(int iep, double[] fv)
    {
        if (pf instanceof VertexSet)
        {
            ((VertexSet) pf).setExtremePoint(iep, fv);
        }
        else
        {
            if (iep == 0)
            {
                pf.setValues(fv);
            }
        }
    }

    /**
     * Get a single value of the probability function in the node given the
     * index of the value and the index of the extreme point.
     */
//    public double getFunctionValue(int index, int indexExtremePoint) {
//        if (pf instanceof VertexQBProbabilityFunction)
//            return( ((VertexQBProbabilityFunction)pf).getValue(index, indexExtremePoint) );
//        else
//            return(pf.getValue(index));
//    }
    /**
     * Get a single value of the probability function in the node given the
     * index of the value.
     */
//    public double getFunctionValue(int index) {
//        if (pf instanceof VertexQBProbabilityFunction)
//            return( ((VertexQBProbabilityFunction)pf).getValue(index, 0) );
//        else
//            return(pf.getValue(index));
//    }
    /**
     * Set a single value of the probability function in the node given a list
     * of pairs (Variable Value). The list specifies which element of the
     * function is referred to.
     *
     * @param variableValuePairs
     * @param indexExtremePoint
     * @param val
     */
    public void setFunctionValue(String variableValuePairs[][], double val,
                                 int indexExtremePoint)
    {
        if (pf instanceof VertexSet)
        {
            ((VertexSet) pf).setValue(variableValuePairs, val,
                                      indexExtremePoint);
        }
        else
        {
            pf.setValue(variableValuePairs, val);
        }
    }

    /* ******************** Public methods ******************** */
    /**
     * Return the name of the variable in the node.
     *
     * @return
     */
    public String getName()
    {
        return (pv.getName());
    }

    /**
     * Set the name of the variable.
     *
     * @param n
     */
    public void setName(String n)
    {
        pv.setName(n);
    }

    /**
     * Get the name of all variables in the probability function.
     *
     * @return
     */
    public String[] getAllNames()
    {
        String[] ns = new String[pf.numberVariables()];
        for (int i = 0; i < ns.length; i++)
        {
            ns[i] = pf.getVariable(i).getName();
        }
        return (ns);
    }

    /**
     * Return the values of the variable in the node.
     *
     * @return
     */
    public String[] getValues()
    {
        return (pv.getValues());
    }

    /**
     * Get all values for variables in the function in the node.
     *
     * @return
     */
    public String[][] getAllValues()
    {
        int i, j;
        String allValues[][] = new String[pf.numberVariables()][];
        DiscreteVariable dv;
        for (i = 0; i < pf.numberVariables(); i++)
        {
            dv = pf.getVariable(i);
            allValues[i] = new String[dv.numberValues()];
            for (j = 0; j < allValues[i].length; j++)
            {
                allValues[i][j] = dv.getValue(j);
            }
        }
        return (allValues);
    }

    /**
     * Return the number of values in the variable in the node.
     *
     * @return
     */
    public int getNumberValues()
    {
        return (pv.numberValues());
    }

    /**
     * Indicate whether the node has parents.
     *
     * @return
     */
    public boolean hasParent()
    {
        return (pf.numberVariables() > 1);
    }

    /**
     * Return the parents of a node as an Iterator object.
     *
     * @return
     */
    public ArrayList getParents()
    {
        return (parents);
    }

    /**
     * Return the children of a node as an Iterator object.
     *
     * @return
     */
    public ArrayList getChildren()
    {
        return (children);
    }

    /**
     * Indicate whether the variable in the node is observed.
     *
     * @return
     */
    public boolean isObserved()
    {
        return (pv.isObserved());
    }

    /**
     * Indicate whether the variable in the node is an explanatory variable.
     *
     * @return
     */
    public boolean isExplanation()
    {
        return (pv.isExplanation());
    }

    /**
     * Return the observed value for the variable in the node.
     *
     * @return
     */
    public int getObservedValue()
    {
        return (pv.getObservedIndex());
    }

    /**
     * Return the X position of the node.
     *
     * @return
     */
    public int getPosX()
    {
        return (pos.x);
    }

    /**
     * Return the Y position of the node.
     *
     * @return
     */
    public int getPosY()
    {
        return (pos.y);
    }

    /**
     * Return the variable properties
     *
     * @return
     */
    public ArrayList getVariableProperties()
    {
        return (pv.getProperties());
    }

    /**
     * Set the variable properties.
     *
     * @param prop
     */
    public void setVariableProperties(ArrayList prop)
    {
        pv.setProperties(prop);
    }

    /**
     * Return the function properties.
     *
     * @return
     */
    public ArrayList getFunctionProperties()
    {
        return (pf.getProperties());
    }

    /**
     * Set the function properties.
     *
     * @param prop
     */
    public void setFunctionProperties(ArrayList prop)
    {
        pf.setProperties(prop);
    }

    /**
     * Whether or not the node represents a convex set of distributions (credal
     * set).
     *
     * @return
     */
    public boolean isCredalSet()
    {
        if (pf instanceof QBProbabilityFunction)
        {
            return (true);
        }
        else
        {
            return (false);
        }
    }

    /**
     * Number of distributions that are represented by a node.
     *
     * @return
     */
    public int numberExtremeDistributions()
    {
        if (pf instanceof VertexSet)
        {
            return (((VertexSet) pf).getExtremePoints().length);
        }
        else
        {
            return (1);
        }
    }

    /**
     * Make sure the node represents a single distribution.
     */
    public void setNoLocalCredalSet()
    {
        if (pf instanceof QBProbabilityFunction)
        {
            if (pf instanceof VertexSet)
            {
                ((VertexSet) pf).composeValues();
            }
            pf = new ProbabilityFunction(pf, pf.getValues());
        }
    }

    /**
     * Make sure the node represents a VertexSet a given number of extreme
     * distributions.
     *
     * @param numberExtremePoints
     */
    public void setLocalCredalSet(int numberExtremePoints)
    {
        if (!(pf instanceof VertexSet))
        {
            pf = new VertexSet(pf);
        }
        ((VertexSet) pf).setLocalCredalSet(numberExtremePoints);
    }

    /**
     * Make sure the node represents a VertexSet.
     */
    public void setLocalCredalSet()
    {
        if (!(pf instanceof VertexSet))
        {
            pf = new VertexSet(pf);
        }
    }

    /**
     * Set the observation for the node.
     *
     * @param value
     */
    public void setObservationValue(String value)
    {
        pv.setObservedValue(value);
    }

    /**
     * Clear the observation for the node.
     */
    public void clearObservation()
    {
        pv.setInvalidObservedIndex();
    }

    /**
     * Set the explanatory status of the node.
     *
     * @param flag
     */
    public void setExplanation(boolean flag)
    {
        if (flag == true)
        {
            pv.setExplanationValue(0);
        }
        else
        {
            pv.setExplanationValue(BayesNet.INVALID_INDEX);
        }
    }

    /**
     * Remove a property from a variable.
     *
     * @param index
     */
    public void removeVariableProperty(int index)
    {
        pv.removeProperty(index);
    }

    /**
     * Remove a property from a function.
     *
     * @param index
     */
    public void removeFunctionProperty(int index)
    {
        pf.removeProperty(index);
    }

    /**
     * Add a property to a variable.
     *
     * @param s
     */
    public void addVariableProperty(String s)
    {
        pv.addProperty(s);
        updatePositionFromProperty(s);
    }

    /*
     * Update the position of a node given a property.
     */
    /**
     *
     * @param s
     */
    public void updatePositionFromProperty(String s)
    {
        // If property is position:
        if (s.startsWith("position"))
        {
            Point finalPosition = null;
            // Parse the position property
            try
            {
                StreamTokenizer st =
                                new StreamTokenizer(new StringBufferInputStream(
                                                s));
                st.parseNumbers();
                int tok;
                int x = -1, y = 0;
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
                        y = (int) st.nval;
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
            pos = finalPosition;
        }
    }

    /**
     * Add a property from to function.
     *
     * @param prop
     */
    public void addFunctionProperty(String prop)
    {
        pf.addProperty(prop);
    }

}
