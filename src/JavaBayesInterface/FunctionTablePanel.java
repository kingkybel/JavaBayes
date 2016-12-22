/*
 * FunctionTablePanel.java
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

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public final class FunctionTablePanel extends Panel
{

    private static final Class CLAZZ = FunctionTablePanel.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final Color colorParents = Color.red;
    private static final String parentsAnnounceLabel = "Values for parents:";
    private final String allVariableNames[];
    private final String allVariableValues[][];
    private double tableValues[];
    private int parentsCurrentChoices[];

    private Panel ap, dp, pp;
    private Label parentsAnnounce;
    private Label parentsLabels[];
    private Label separatorLabel;
    private Choice parentChoices[];
    private TextField fields[][];
    private Label firstParentNameLabel;
    private Label firstParentValuesLabels[];
    private Label nodeValuesLabels[];

    /**
     * Default constructor for an FunctionTablePanel.
     *
     * @param avn
     * @param tv
     * @param avv
     */
    public FunctionTablePanel(String avn[], String avv[][], double tv[])
    {
        this.allVariableNames = avn;
        this.allVariableValues = avv;
        this.tableValues = tv;

        // Construct the relevant panels.
        buildPanels();

        // Store the initial choices.
        if (allVariableNames.length > 2)
        {
            parentsCurrentChoices = new int[parentChoices.length];
            for (int i = 0; i < parentsCurrentChoices.length; i++)
            {
                parentsCurrentChoices[i] = parentChoices[i].
                getSelectedIndex();
            }
        }

        // Finally set the panel with all choices and fields.
        setLayout(new BorderLayout());
        add("North", ap);
        add("Center", dp);
    }

    /**
     * Return the current table.
     *
     * @return
     */
    public double[] getTable()
    {
        updateTableValues();
        return (tableValues);
    }

    /**
     * Insert a new table.
     *
     * @param newTable
     */
    public void insertTable(double newTable[])
    {
        tableValues = newTable;

        int i, j, k;
        if (allVariableNames.length == 1)
        {
            for (j = 0; j < allVariableValues[0].length; j++)
            {
                fields[j][0].setText(String.valueOf(tableValues[j]));
            }
        }
        else if (allVariableNames.length == 2)
        {
            for (i = 0; i < allVariableValues[0].length; i++)
            {
                for (j = 0; j < allVariableValues[1].length;
                     j++)
                {
                    fields[i][j].setText(String.valueOf(tableValues[i *
                                                                    allVariableValues[1].length +
                                                                    j]));
                }
            }
        }
        else
        {
            double valueSet = 0.0;
            int parentIndexes[] = new int[allVariableNames.length];
            for (k = 0; k < parentChoices.length; k++)
            {
                parentIndexes[k + 2] = parentChoices[k].getSelectedIndex();
            }
            for (i = 0; i < allVariableValues[0].length; i++)
            {
                for (j = 0; j < allVariableValues[1].length; j++)
                {
                    parentIndexes[0] = i;
                    parentIndexes[1] = j;
                    fields[i][j].setText(String.valueOf(
                            tableValues[getLocationFromIndexes(
                                    parentIndexes)]));
                }
            }
            for (k = 0; k < parentsCurrentChoices.length; k++)
            {
                parentsCurrentChoices[k] = parentChoices[k].
                getSelectedIndex();
            }
        }
    }

    /**
     * Build the table panel.
     */
    void buildPanels()
    {
        buildParentsPanel();
        buildDistributionPanel();
    }

    /**
     * Build parents panel.
     */
    private void buildParentsPanel()
    {
        if (allVariableNames.length <= 2)
        {
            ap = new Panel();
            return;
        }
        // Construct the panel with the values of the parents.
        pp = new Panel();
        pp.setLayout(new GridLayout(allVariableNames.length - 2, 2));
        parentsLabels = new Label[allVariableNames.length - 2];
        parentChoices = new Choice[allVariableNames.length - 2];

        // Fill the labels and choices for the values of the parents.
        for (int i = 2; i < allVariableNames.length; i++)
        {
            // Get a parent
            parentsLabels[i - 2] = new Label(allVariableNames[i]);
            parentsLabels[i - 2].setForeground(colorParents);
            parentChoices[i - 2] = new Choice();
            parentChoices[i - 2].setForeground(colorParents);
            // Fill the choice item with the values for the parent.
            for (String allVariableValue : allVariableValues[i])
            {
                parentChoices[i - 2].add(allVariableValue);
            }
            // Insert the label and choice
            pp.add(parentsLabels[i - 2]);
            pp.add(parentChoices[i - 2]);
        }
        // Just use a panel to put the title in the parent choices.
        ap = new Panel();
        ap.setLayout(new BorderLayout());
        parentsAnnounce = new Label(parentsAnnounceLabel, Label.CENTER);
        parentsAnnounce.setForeground(colorParents);
        ap.add("North", parentsAnnounce);
        ap.add("Center", pp);
        separatorLabel = new Label("");
        ap.add("South", separatorLabel);
    }

    /**
     * Build distribution panel.
     */
    private void buildDistributionPanel()
    {
        int i, j;
        dp = new Panel();
        nodeValuesLabels = new Label[allVariableValues[0].length];
        if (allVariableNames.length == 1)
        {
            // For a node with no parent, go directly to the distribution panel.
            dp.setLayout(new GridLayout(allVariableValues[0].length, 2));
            fields = new TextField[allVariableValues[0].length][1];
            for (j = 0; j < allVariableValues[0].length; j++)
            {
                nodeValuesLabels[j] = new Label(allVariableValues[0][j]);
                dp.add(nodeValuesLabels[j]);
                fields[j][0] = new TextField();
                fields[j][0].setText(String.valueOf(tableValues[j]));
                dp.add(fields[j][0]);
            }
        }
        else
        {
            // For one or more parents, create a two-dimensional table.
            dp.setLayout(new GridLayout(allVariableValues[0].length + 1,
                                        allVariableValues[1].length + 1));
            fields =
            new TextField[allVariableValues[0].length][allVariableValues[1].length];
            firstParentNameLabel = new Label(allVariableNames[1]);
            firstParentNameLabel.setForeground(colorParents);
            dp.add(firstParentNameLabel);
            firstParentValuesLabels =
            new Label[allVariableValues[1].length];
            for (i = 0; i < allVariableValues[1].length; i++)
            {
                firstParentValuesLabels[i] = new Label(
                allVariableValues[1][i]);
                firstParentValuesLabels[i].setForeground(colorParents);
                dp.add(firstParentValuesLabels[i]);
            }
            // Auxiliary jump; used to compute location of table values.
            int jump = 1;
            if (allVariableNames.length > 1)
            {
                for (i = 2; i < allVariableNames.length; i++)
                {
                    jump *= allVariableValues[i].length;
                }
            }
            for (j = 0; j < allVariableValues[0].length; j++)
            {
                nodeValuesLabels[j] = new Label(allVariableValues[0][j]);
                dp.add(nodeValuesLabels[j]);
                for (i = 0; i < allVariableValues[1].length; i++)
                {
                    fields[j][i] = new TextField();
                    fields[j][i].setText(String.valueOf(
                            tableValues[(j * allVariableValues[1].length + i) *
                                        jump]));
                    dp.add(fields[j][i]);
                }
            }
        }
    }

    /**
     * Handle events in the panel.
     *
     * @param evt
     * @param arg
     * @return
     */
    @Override
    public boolean action(Event evt, Object arg)
    {
        int i, j, k;
        int startingIndex;
        double valueSet = 0.0;

        // In case the node has more than one
        // parent, check whether parent values
        // have been changed.
        if (allVariableNames.length > 2)
        {
            for (i = 0; i < parentChoices.length; i++)
            {
                if (evt.target == parentChoices[i])
                {
                    updateTableForParents();
                    return (true);
                }
            }
        }
        return (super.action(evt, arg));
    }

    /**
     * Update the table when the parents change.
     */
    private void updateTableForParents()
    {
        int i, j, k;
        if (allVariableNames.length == 1)
        {
            for (j = 0; j < allVariableValues[0].length; j++)
            {
                fields[j][0].setText(String.valueOf(tableValues[j]));
            }
        }
        else if (allVariableNames.length == 2)
        {
            for (i = 0; i < allVariableValues[0].length; i++)
            {
                for (j = 0; j < allVariableValues[1].length;
                     j++)
                {
                    fields[i][j].setText(String.valueOf(tableValues[i *
                                                                    allVariableValues[1].length +
                                                                    j]));
                }
            }
        }
        else
        {
            double valueSet = 0.0;
            int parentIndexes[] = new int[allVariableNames.length];
            for (k = 0; k < parentChoices.length; k++)
            {
                parentIndexes[k + 2] = parentsCurrentChoices[k];
            }
            for (i = 0; i < fields.length; i++)
            {
                for (j = 0; j < fields[i].length; j++)
                {
                    try
                    {
                        valueSet =
                        (Double.parseDouble(fields[i][j].getText()));
                        parentIndexes[0] = i;
                        parentIndexes[1] = j;
                        tableValues[getLocationFromIndexes(parentIndexes)] =
                        valueSet;
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
            for (k = 0; k < parentChoices.length; k++)
            {
                parentIndexes[k + 2] = parentChoices[k].getSelectedIndex();
            }
            for (i = 0; i < allVariableValues[0].length; i++)
            {
                for (j = 0; j < allVariableValues[1].length; j++)
                {
                    parentIndexes[0] = i;
                    parentIndexes[1] = j;
                    fields[i][j].setText(String.valueOf(
                            tableValues[getLocationFromIndexes(
                                    parentIndexes)]));
                }
            }
            for (k = 0; k < parentsCurrentChoices.length; k++)
            {
                parentsCurrentChoices[k] = parentChoices[k].
                getSelectedIndex();
            }
        }
    }

    /**
     * Update the table values.
     */
    private void updateTableValues()
    {
        int i, j, k;
        if (allVariableNames.length == 1)
        {
            for (i = 0; i < fields.length; i++)
            {
                try
                {
                    tableValues[i] =
                    (Double.parseDouble(fields[i][0].getText()));
                }
                catch (NumberFormatException e)
                {
                }
            }
        }
        else if (allVariableNames.length == 2)
        {
            for (i = 0; i < fields.length; i++)
            {
                for (j = 0; j < fields[i].length; j++)
                {
                    try
                    {
                        tableValues[i * allVariableValues[1].length + j] =
                        (Double.parseDouble(fields[i][j].getText()));
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
        }
        else
        {
            double valueSet = 0.0;
            int parentIndexes[] = new int[allVariableNames.length];
            for (k = 0; k < parentChoices.length; k++)
            {
                parentIndexes[k + 2] = parentChoices[k].getSelectedIndex();
            }
            for (i = 0; i < fields.length; i++)
            {
                for (j = 0; j < fields[i].length; j++)
                {
                    try
                    {
                        valueSet =
                        (Double.parseDouble(fields[i][j].getText()));
                        parentIndexes[0] = i;
                        parentIndexes[1] = j;
                        tableValues[getLocationFromIndexes(parentIndexes)] =
                        valueSet;
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
        }
    }

    /**
     * Determine the index of a given location.
     */
    private int getLocationFromIndexes(int indexes[])
    {
        int pos = 0, jump = 1;
        for (int i = (allVariableNames.length - 1); i >= 0; i--)
        {
            pos += indexes[i] * jump;
            jump *= allVariableValues[i].length;
        }
        return (pos);
    }
}
