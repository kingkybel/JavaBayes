/*
 * InferenceGraph.java
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
import QuasiBayesianInferences.QBExpectation;
import QuasiBayesianInferences.QBInference;
import QuasiBayesianNetworks.GlobalNeighbourhood;
import QuasiBayesianNetworks.QuasiBayesNet;
import java.awt.Point;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public final class InferenceGraph
{

    private static final Class CLAZZ = InferenceGraph.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    QuasiBayesNet qbn;
    QBInference qbi;
    QBExpectation qbe;
    ArrayList<InferenceGraphNode> nodes = new ArrayList();
    private final String defaultBayesNetName = "InternalNetwork";

    /**
     * Default constructor for an InferenceGraph.
     *
     */
    public InferenceGraph()
    {
        qbn = new QuasiBayesNet(defaultBayesNetName, 0, 0);
    }

    /**
     * Simple constructor for an InferenceGraph.
     *
     * @param bayesNet the underlying Bayesian network
     */
    public InferenceGraph(BayesNet bayesNet)
    {
        qbn = new QuasiBayesNet(bayesNet);
        convertBayesNet();
    }

    /**
     * Constructor for an InferenceGraph.
     *
     * @param filename name of the file describing the network
     * @throws Exception if file cannot be read or parsed
     */
    public InferenceGraph(String filename) throws Exception
    {
        qbn = new QuasiBayesNet(
        new java.io.DataInputStream(new java.io.FileInputStream(filename)));
        convertBayesNet();
    }

    /**
     * Constructor for an InferenceGraph.
     *
     * @param url the URL where to find the textual description of the Bayes net
     * @throws Exception throw if the Bayes net can not be read from the URL
     */
    public InferenceGraph(URL url) throws Exception
    {
        qbn = new QuasiBayesNet(url);
        convertBayesNet();
    }

    /**
     * Get the contents of the graph.
     *
     * @return the graph converted into a quasi-Bayes net
     */
    public QuasiBayesNet getBayesNet()
    {
        return convertGraph();
    }

    /**
     * Convert a QuasiBayesNet object to the InferenceGraph structure;
     *
     * @return true if the conversion is successful.
     */
    boolean convertBayesNet()
    {

        for (int i = 0; i < qbn.numberVariables(); i++)
        {
            ProbabilityVariable probVar = qbn.getProbabilityVariable(i);
            ProbabilityFunction probFunc = null;
            for (int j = 0; j < qbn.numberProbabilityFunctions(); j++)
            {
                probFunc = qbn.getProbabilityFunction(j);
                if (probFunc.getVariable(0) == probVar)
                {
                    break;
                }
            }
            // The variable does not have a corresponding function
            if (probFunc == null)
            {
                return false;
            }

            nodes.add(new InferenceGraphNode(this, probVar, probFunc));
        }
        generateParentsAndChildren();

        return true;
    }

    /**
     * Generate the parents and children for the nodes.
     */
    private void generateParentsAndChildren()
    {
        int i, j;
        DiscreteVariable variables[];
        ProbabilityFunction probFunc;
        InferenceGraphNode node;

        for (InferenceGraphNode baseNode : nodes)
        {
            probFunc = baseNode.probFunc;
            variables = probFunc.getVariables();

            for (i = 1; i < variables.length; i++)
            {
                node = getNode(variables[i]);
                if (node == null)
                {
                    continue;
                }
                baseNode.parents.add(node);
                node.children.add(baseNode);
            }
        }
    }

    /**
     * Get the node corresponding to a given variable.
     *
     * @return the node
     */
    private InferenceGraphNode getNode(DiscreteVariable dv)
    {
        for (InferenceGraphNode node : nodes)
        {
            if (node.probVar == dv)
            {
                return node;
            }
        }
        return null;
    }

    /**
     * Convert the InferenceGraph structure to a QuasiBayesNet object.
     *
     * @return the quasi Bayes net
     */
    QuasiBayesNet convertGraph()
    {
        int i;

        // Create the arrays of variables and functions
        ProbabilityVariable probVars[] = new ProbabilityVariable[nodes.size()];
        ProbabilityFunction probFuncs[] = new ProbabilityFunction[nodes.size()];

        // Insert the empty arrays
        qbn.setProbabilityVariables(probVars);
        qbn.setProbabilityFunctions(probFuncs);

        // Collect all variables and functions in the nodes
        // into the new QuasiBayesNet
        i = 0;
        for (InferenceGraphNode node : nodes)
        {
            node.updatePosition();
            qbn.setProbabilityVariable(i, node.probVar);
            qbn.setProbabilityFunction(i, node.probFunc);
            i++;
        }

        return qbn;
    }

    /**
     * Generate a valid name for a new variable.
     *
     * @return the name as string
     */
    private String generateName(int index)
    {
        // generate names of the form a..z, a1..z1, a2..z2, etc.
        char namec = (char) ((int) 'a' + index % 26);
        int suffix = index / 26;
        String name;
        if (suffix > 0)
        {
            name = "" + namec + suffix;
        }
        else
        {
            name = "" + namec;
        }
        // check whether there is a variable with this name
        for (InferenceGraphNode node : nodes)
        {
            if (node.getName().equals(name))
            {
                return generateName(index + 1);
            }
        }
        return name;
    }

    /**
     * Get the name of the network.
     *
     * @return the name of the network
     */
    public String getName()
    {
        return qbn.getName();
    }

    /**
     * Set the name of the network.
     *
     * @param name he new name
     */
    public void setName(String name)
    {
        qbn.setName(name);
    }

    /**
     * Get the properties of the network.
     *
     * @return the properties as list of strings
     */
    public ArrayList<String> getNetworkProperties()
    {
        return qbn.getProperties();
    }

    /**
     * Set the properties of the network.
     *
     * @param properties list of properties
     */
    public void setNetworkProperties(ArrayList<String> properties)
    {
        qbn.setProperties(properties);
    }

    /**
     * Get the type of global neighborhood modeled by the network.
     *
     * @return the type of global neighborhood
     */
    public GlobalNeighbourhood getGlobalNeighborhoodType()
    {
        return qbn.getGlobalNeighborhoodType();
    }

    /**
     * Set the global neighborhood type.
     *
     * @param type the type of global neighborhood
     */
    public void setGlobalNeighborhood(GlobalNeighbourhood type)
    {
        qbn.setGlobalNeighborhoodType(type);
    }

    /**
     * Get the parameter for the global neighborhood modeled by the network.
     *
     * @return the global neighborhood parameter
     */
    public double getGlobalNeighborhoodParameter()
    {
        return qbn.getGlobalNeighborhoodParameter();
    }

    /**
     * Set the parameter for the global neighborhood modeled by the network.
     *
     * @param parameter the global neighborhood parameter
     */
    public void setGlobalNeighborhoodParameter(double parameter)
    {
        qbn.setGlobalNeighborhoodParameter(parameter);
    }

    /**
     * Remove a property from the network.
     *
     * @param propertyIndex the index of the property to remove
     */
    public void removeNetworkProperty(int propertyIndex)
    {
        qbn.removeProperty(propertyIndex);
    }

    /**
     * Add a property to the network.
     *
     * @param property property given as string
     */
    public void addNetworkProperty(String property)
    {
        qbn.addProperty(property);
    }

    /**
     * Determine whether or not a name is valid and/or repeated.
     *
     * @param name the name to check for validity
     * @return the checked name if it is valid, null else
     */
    public String checkName(String name)
    {
        String checkedName = makeValidValue(name);
        for (InferenceGraphNode node : nodes)
        {
            if (node.getName().equals(checkedName))
            {
                return null;
            }
        }
        return checkedName;
    }

    /**
     * Replace all spaces with underscores to make a valid name.
     *
     * @param value original value
     * @return the valid name
     */
    public String makeValidValue(String value)
    {
        StringBuilder str = new StringBuilder(value);
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == ' ')
            {
                str.setCharAt(i, '_');
            }
        }
        return str.toString();
    }

    /**
     * Print the QuasiBayesNet.
     *
     * @param out output print stream
     */
    public void printBayesNet(PrintStream out)
    {
        QuasiBayesNet qbN = getBayesNet();
        qbN.print(out);
    }

    /**
     * Print information about a posterior marginal for the Bayesian network
     * into the given PrintStream.
     *
     * @param out                        output print stream
     * @param queriedVariable            indicates the variable of interest.
     * @param showBucketTree             determines whether or not to present a
     *                                   description of the BucketTree.
     * @param isProducingComputeClusters whether or not clusters should be
     *                                   produced
     */
    public void printMarginal(PrintStream out,
                              String queriedVariable,
                              boolean isProducingComputeClusters,
                              boolean showBucketTree)
    {
        if ((isProducingComputeClusters == false) ||
            (qbi == null) ||
            (qbi.areClustersProduced() == false))
        {
            qbi = new QBInference(getBayesNet(), isProducingComputeClusters);
        }
        qbi.inference(queriedVariable);
        qbi.print(out, showBucketTree);
    }

    /**
     * Reset the QBInference.
     */
    public void resetMarginal()
    {
        qbi = null;
    }

    /**
     * Print information about a posterior expectation for the Bayesian network
     * into the given PrintStream.
     *
     * @param out                        output print stream
     * @param queriedVariable            indicates the variable of interest.
     * @param isProducingComputeClusters whether or not clusters should be
     *                                   produced
     * @param showBucketTree             determines whether or not to present a
     *                                   description of the BucketTree.
     */
    public void printExpectation(PrintStream out,
                                 String queriedVariable,
                                 boolean isProducingComputeClusters,
                                 boolean showBucketTree)
    {
        if ((isProducingComputeClusters == false) ||
            (qbe == null) /*|| // *** Removed as it does not seem to make sense here
                 (qbi.areClustersProduced() == false)*/)
        {
            qbe = new QBExpectation(getBayesNet(), isProducingComputeClusters);
        }
        qbe.expectation(queriedVariable);
        qbe.print(out, showBucketTree);
    }

    /**
     * Reset the QBExpectation.
     */
    public void resetExpectation()
    {
        qbe = null;
    }

    /**
     * Print information about an explanation for the Bayesian network into the
     * given PrintStream.
     *
     * @param out            output print stream
     * @param showBucketTree determines whether or not to present a description
     *                       of the BucketTree.
     */
    public void printExplanation(PrintStream out, boolean showBucketTree)
    {
        Explanation ex = new Explanation(getBayesNet());
        ex.explanation();
        ex.print(out, showBucketTree);
    }

    /**
     * Print information about a full explanation for the Bayesian network into
     * the given PrintStream.
     *
     * @param out            output print stream
     * @param showBucketTree determines whether or not to present a description
     *                       of the BucketTree.
     */
    public void printFullExplanation(PrintStream out,
                                     boolean showBucketTree)
    {
        Explanation fex = new Explanation(getBayesNet());
        fex.fullExplanation();
        fex.print(out, showBucketTree);
    }

    /**
     * Print the metrics for sensitivity analysis of the Bayesian network into
     * the given PrintStream.
     *
     * @param out output print stream
     */
    public void printSensitivityAnalysis(PrintStream out)
    {
        SensitivityAnalysis sa = new SensitivityAnalysis(getBayesNet());
        //sa.compute(queriedVariable);
        sa.print(out);
    }

    /**
     * Save the Bayesian network into a PrintStream in the BIF
     * InterchangeFormat.
     *
     * @param out output print stream
     */
    public void saveBif(PrintStream out)
    {
        QuasiBayesNet qbN = getBayesNet();
        qbN.saveBif(out);
    }

    /**
     * Save the Bayesian network into a PrintStream in the XML
     * InterchangeFormat.
     *
     * @param out output print stream
     */
    public void saveXml(PrintStream out)
    {
        QuasiBayesNet qbN = getBayesNet();
        qbN.saveXml(out);
    }

    /**
     * Save the Bayesian networks in BUGS format into a PrintStream.
     *
     * @param out output print stream
     */
    public void saveBugs(PrintStream out)
    {
        QuasiBayesNet qbN = getBayesNet();
        qbN.saveBugs(out);
    }

    /**
     * Print method for an InferenceGraph.
     */
    public void print()
    {
        print(System.out);
    }

    /**
     * Print method for an InferenceGraph.
     *
     * @param out output print stream
     */
    public void print(PrintStream out)
    {
        QuasiBayesNet qbN = getBayesNet();
        qbN.print(out);
    }

    /**
     * Get the nodes in the network.
     *
     * @return the list if inference graph nodes
     */
    public ArrayList<InferenceGraphNode> getNodes()
    {
        return nodes;
    }

    /**
     * Get the nodes in the network as an Iterator object.
     *
     * @return the list if inference graph nodes
     */
    public ArrayList<InferenceGraphNode> elements()
    {
        return nodes;
    }

    /**
     * Get the number of variables in the network.
     *
     * @return the number of variables
     */
    public int numberNodes()
    {
        return nodes.size();
    }

    /**
     * Create a new node in the network.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void createNode(int x, int y)
    {
        Point coord = new Point(x, y);
        String n = generateName(nodes.size());
        nodes.add(new InferenceGraphNode(this, n, coord));

        // Synchronize the QuasiBayesNet object and the graph.
        convertGraph();
    }

    /**
     * Create an arc from parent to child.
     *
     * @param parent parent node
     * @param child  child node
     * @return true, if arc was created, false otherwise
     */
    public boolean createArc(InferenceGraphNode parent,
                             InferenceGraphNode child)
    {
        // Check whether the given parent is already a parent of the
        // given child.
        for (InferenceGraphNode currentParentNode : child.parents)
        {
            if (parent == currentParentNode)
            {
                return false;
            }
        }

        // First put child into the children of parent
        parent.children.add(child);
        // Second put parent into the parents of child
        child.parents.add(parent);

        // The parent is not further affected by the arc.
        // The child must have its ProbabilityFunction
        // object updated.
        child.initDists();

        // Synchronize the QuasiBayesNet object and the graph.
        convertGraph();

        // Return true.
        return true;
    }

    /**
     * Delete a node in the network.
     *
     * @param node graph node
     */
    public void deleteNode(InferenceGraphNode node)
    {
        // First, remove node from all its childrem
        for (InferenceGraphNode child : node.children)
        {
            child.parents.remove(node);
            child.initDists();
        }

        // Second remove parent into the parents of child
        for (InferenceGraphNode parent : node.parents)
        {
            parent.children.remove(node);
        }

        // Third remove the node itself
        nodes.remove(node);

        // Synchronize the QuasiBayesNet object and the graph.
        convertGraph();
    }

    /**
     * Delete the arc from parent to child.
     *
     * @param parent parent node
     * @param child  child node
     */
    public void deleteArc(InferenceGraphNode parent,
                          InferenceGraphNode child)
    {
        // First remove child into the children of parent
        parent.children.remove(child);
        // Second remove parent into the parents of child
        child.parents.remove(parent);

        // The parent is not further affected by the arc.
        // The child must have its ProbabilityFunction
        // object updated.
        child.initDists();

        // Synchronize the QuasiBayesNet object and the graph.
        convertGraph();
    }

    /**
     * Determines whether the connection of bottomNode to headNode would cause
     * the network to have a cycle.
     *
     * @param bottomNode from node
     * @param headNode   to node
     * @return true if so, false otherwise
     */
    public boolean hasCycle(InferenceGraphNode bottomNode,
                            InferenceGraphNode headNode)
    {
        ArrayList<InferenceGraphNode> children;
        InferenceGraphNode nextNode;

        // Array with enough space to have all nodes
        InferenceGraphNode listedNodes[] =
                             new InferenceGraphNode[nodes.size()];

        // Hashtable for efficient lookup of already listed nodes
        HashMap hashedNodes = new HashMap();

        // Index of last node in listedNodes
        int lastListedNodeIndex = 0;

        // Initialize: headNode is marked and inserted
        int currentListedNodeIndex = 0;
        listedNodes[0] = headNode;
        hashedNodes.put(headNode.probVar.getName(), headNode);

        // Now expand for children until no more children, or
        // when a child is equal to bottomNode
        while (currentListedNodeIndex <= lastListedNodeIndex)
        {
            // Select the next node to be expanded
            nextNode = listedNodes[currentListedNodeIndex];
            // Update the index that indicates nodes to be expanded
            currentListedNodeIndex++;

            // Get all children of the node being expanded
            children = nextNode.children;
            // Expand the node: put all its children into list
            for (InferenceGraphNode childNode : children)
            {
                if (childNode == bottomNode)
                { // Cycle is detected
                    return true;
                }
                if (!hashedNodes.containsKey(childNode.probVar.getName()))
                {
                    hashedNodes.put(childNode.probVar.getName(), childNode);
                    lastListedNodeIndex++;
                    listedNodes[lastListedNodeIndex] = childNode;
                }
            }
        }
        return false;
    }

    /**
     * Change the values of a variable. Note that, if the number of new values
     * is different from the number of current values, this operation resets the
     * probability values of the variable and all its children.
     *
     * @param node   graph node
     * @param values values the variable can assume as string array
     */
    public void changeValues(InferenceGraphNode node, String values[])
    {
        ArrayList<InferenceGraphNode> children;

        if (node.probVar.numberValues() == values.length)
        {
            node.probVar.setValues(values);
            return;
        }

        node.probVar.setValues(values);
        node.initDists();

        children = node.getChildren();
        for (InferenceGraphNode cnode : children)
        {
            cnode.initDists();
        }

        // Synchronize the QuasiBayesNet object and the graph.
        convertGraph();
    }

    /**
     * Set a value for the position of the node.
     *
     * @param node        graph node
     * @param coordinates x/y coordinates of the node
     */
    public void setCoordinates(InferenceGraphNode node, Point coordinates)
    {
        node.coordinates = coordinates;
        convertGraph();
    }
}
