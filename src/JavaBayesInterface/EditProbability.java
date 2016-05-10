/*
 * EditProbability.java
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
package JavaBayesInterface;

import InferenceGraphs.InferenceGraph;
import InferenceGraphs.InferenceGraphNode;
import java.awt.BorderLayout;
import java.awt.Label;
import java.util.ArrayList;
import java.util.logging.Logger;

class EditProbability extends EditFunctionPanel
{

    private static final String CLASS_NAME = EditProbability.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    EditFunctionDialog parentDialog;

    // The graph and node that contain the probability function.
    InferenceGraph ig;
    InferenceGraphNode node;

    // Variables that hold the relevant information from the node.
    String allVariableNames[];
    String allVariableValues[][];
    double probabilityValues[];

    FunctionTablePanel probabilityTable;

    /**
     * Default constructor for an EditProbability.
     */
    EditProbability(EditFunctionDialog parentDialog,
                    InferenceGraph iG, InferenceGraphNode iGN)
    {
        this.parentDialog = parentDialog;
        this.ig = iG;
        this.node = iGN;

        // Copy the probability values in the node.
        double originalProbabilityValues[] = node.getFunctionValues();
        probabilityValues = new double[originalProbabilityValues.length];
        System.arraycopy(originalProbabilityValues, 0, probabilityValues, 0,
                         probabilityValues.length);

        // Get the variable names.
        allVariableNames = node.getAllNames();

        // Get the variable values.
        allVariableValues = node.getAllValues();

        // Construct the name of the probability function.
        Label probabilityName = createProbabilityName();

        // Construct the table of probability values.
        probabilityTable = new FunctionTablePanel(allVariableNames,
                                                  allVariableValues,
                                                  probabilityValues);

        // Set the final layout
        setLayout(new BorderLayout());
        add("North", probabilityName);
        add("Center", probabilityTable);
    }

    /*
     * Create a Label containing a description of the probability function.
     */
    private Label createProbabilityName()
    {
        StringBuilder name = new StringBuilder("p(");
        name.append(node.getName());
        if (node.hasParent())
        {
            name.append(" |");
            ArrayList parents = node.getParents();
            for (Object e : parents)
            {
                name.append(" ").
                        append(((InferenceGraphNode) (e)).
                                getName()).
                        append(",");
            }
            name.setCharAt(name.length() - 1, ')');
        }
        else
        {
            name.append(")");
        }
        return (new Label(name.toString(), Label.CENTER));
    }

    @Override
    void accept()
    {
        double EPSILON = 1e-6;
        // Get the values from the table.
        probabilityValues = probabilityTable.getTable();
        // Check whether things add up to one.
        int numberValues = node.getNumberValues();
        int numberConditioningValues =
            probabilityValues.length / numberValues;
        double verificationCounters[] =
                 new double[numberConditioningValues];
        for (int i = 0; i < probabilityValues.length; i++)
        {
            verificationCounters[i % numberConditioningValues] +=
            probabilityValues[i];
        }
        for (int j = 0; j < verificationCounters.length; j++)
        {
            if (Math.abs(verificationCounters[j] - 1.0) >= EPSILON)
            {
                EditorFrame ef;
                if (parentDialog.parent instanceof EditorFrame)
                {
                    ef = (EditorFrame) (parentDialog.parent);
                    ef.jb.appendText("Some of the probability values " +
                                     "you have edited add up to " +
                                     verificationCounters[j] +
                                     ". Please check it.\n\n");
                }
            }
        }
        // Set the values.
        node.setFunctionValues(probabilityValues);
    }

    @Override
    void dismiss()
    {
        // No-op.
    }
}
