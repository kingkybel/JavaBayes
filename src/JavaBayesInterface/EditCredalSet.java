/*
 * EditCredalSet.java
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

import BayesianInferences.InferenceGraph;
import BayesianInferences.InferenceGraphNode;
import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Event;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.logging.Logger;

class EditCredalSet extends EditFunctionPanel
{

    private static final Class CLAZZ = EditCredalSet.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Constants used to construct the panel.
    private static final String credalSetSpecification =
                                "Credal set specification";
    private static final String credalSet = "Index of extreme distribution:";
    private static final String numberExtremePointsLabel =
                                "Number of extreme points:";
    // The graph and node that contain the probability function.
    private final InferenceGraph ig;
    private final InferenceGraphNode node;

    // Variables that hold the relevant information from the node.
    private final String allVariableNames[];
    private final String allVariableValues[][];
    private double allProbabilityValues[][];
    private int indexExtremePoint;

    // Components used to construct the panel.
    private final FunctionTablePanel probabilityTable;
    private Panel csp, ics, qbpp;
    private Choice credalSetChoice;
    private Label localParameter;
    private TextField textLocalParameter;

    /**
     * Default constructor for an EditCredalSet.
     */
    EditCredalSet(InferenceGraph iG, InferenceGraphNode iGN)
    {
        this.ig = iG;
        this.node = iGN;

        // Copy the probability values in the node.
        copyProbabilityValues();

        // Get the variable names.
        allVariableNames = node.getAllNames();

        // Get the variable values.
        allVariableValues = node.getAllVariableValues();

        // Construct the name of the probability function.
        Label probabilityName = createCredalSetName();

        // Construct the table of probability values.
        indexExtremePoint = 0;
        probabilityTable = new FunctionTablePanel(allVariableNames,
                                                  allVariableValues,
                                                  allProbabilityValues[indexExtremePoint]);

        // Credal set panel.
        generateCredalSetPanel();

        // Set the final layout
        setLayout(new BorderLayout());
        add("North", probabilityName);
        add("Center", probabilityTable);
        add("South", csp);
    }

    /**
     * Copy the probability values into internal variables.
     */
    private void copyProbabilityValues()
    {
        double originalProbabilityValues[];
        allProbabilityValues =
        new double[node.numberExtremeDistributions()][];
        for (int i = 0; i < allProbabilityValues.length; i++)
        {
            originalProbabilityValues = node.getFunctionValues(i);
            allProbabilityValues[i] =
            new double[originalProbabilityValues.length];
            System.arraycopy(originalProbabilityValues, 0,
                             allProbabilityValues[i], 0,
                             allProbabilityValues[i].length);
        }
    }

    /*
     * Create a Label containing a description of the credal set.
     */
    private Label createCredalSetName()
    {
        StringBuilder name = new StringBuilder("K(");
        name.append(node.getName());
        if (node.hasParent())
        {
            name.append(" |");
            ArrayList<InferenceGraphNode> parents = node.getParents();
            for (InferenceGraphNode parent : parents)
            {
                name.append(" ").append((parent).getName()).append(",");
            }
            name.setCharAt(name.length() - 1, ')');
        }
        else
        {
            name.append(")");
        }
        return new Label(name.toString(), Label.CENTER);
    }

    @Override
    void accept()
    {
        int i, k;
        allProbabilityValues[indexExtremePoint] = probabilityTable.
        getTable();
        for (i = 0; i < allProbabilityValues.length; i++)
        {
            node.setFunctionValues(i, allProbabilityValues[i]);
        }
        // Update the number of extreme points.
        try
        {
            int oldNumberExtremePoints = allProbabilityValues.length;
            int numberExtremePoints =
                (new Integer(textLocalParameter.getText()));
            if (numberExtremePoints != allProbabilityValues.length)
            {
                node.setLocalCredalSet(numberExtremePoints);
                copyProbabilityValues();
                if (indexExtremePoint >= numberExtremePoints)
                {
                    indexExtremePoint = numberExtremePoints - 1;
                }
                probabilityTable.insertTable(
                        allProbabilityValues[indexExtremePoint]);
                if (numberExtremePoints > oldNumberExtremePoints)
                {
                    for (k = oldNumberExtremePoints; k <
                                                     numberExtremePoints;
                         k++)
                    {
                        credalSetChoice.add(String.valueOf(k));
                    }
                }
                if (oldNumberExtremePoints > numberExtremePoints)
                {
                    for (k = (oldNumberExtremePoints - 1);
                         k >= numberExtremePoints;
                         k--)
                    {
                        credalSetChoice.remove(k);
                    }
                }
                credalSetChoice.select(indexExtremePoint);
            }
        }
        catch (NumberFormatException ex)
        {
        }
    }

    @Override
    void dismiss()
    {
        // No-op.
    }

    /**
     * Generate a panel for credal set.
     */
    private void generateCredalSetPanel()
    {
        csp = new Panel();
        csp.setLayout(new BorderLayout());

        Label credalSetSpecificationLabel =
              new Label(credalSetSpecification, Label.CENTER);

        ics = new Panel();
        ics.setLayout(new BorderLayout());
        Label credalSetLabel = new Label(credalSet);
        credalSetChoice = new Choice();
        for (int i = 0; i < node.numberExtremeDistributions(); i++)
        {
            credalSetChoice.add(String.valueOf(i));
        }
        ics.add("West", credalSetLabel);
        ics.add("Center", credalSetChoice);

        qbpp = new Panel();
        qbpp.setLayout(new BorderLayout());
        localParameter = new Label(numberExtremePointsLabel);
        textLocalParameter = new TextField(5);
        int numberExtremePoints = node.numberExtremeDistributions();
        textLocalParameter.setText(String.valueOf(numberExtremePoints));
        qbpp.add("West", localParameter);
        qbpp.add("Center", textLocalParameter);

        csp.add("North", credalSetSpecificationLabel);
        csp.add("Center", qbpp);
        csp.add("South", ics);
    }

    /**
     * Handle the events.
     */
    @Override
    public boolean action(Event evt, Object arg)
    {
        if (evt.target == credalSetChoice)
        {
            allProbabilityValues[indexExtremePoint] = probabilityTable.
            getTable();
            indexExtremePoint = credalSetChoice.getSelectedIndex();
            probabilityTable.insertTable(
                    allProbabilityValues[indexExtremePoint]);
            return true;
        }
        return super.action(evt, arg);
    }
}
