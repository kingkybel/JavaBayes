/*
 * EditNetworkDialog.java
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
import QuasiBayesianNetworks.GlobalNeighbourhood;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.logging.Logger;

class EditNetworkDialog extends Dialog
{

    private static final Class CLAZZ = EditNetworkDialog.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Constants used to construct the dialog.
    private final static int TOP_INSET = 5;
    private final static int LEFT_INSET = 10;
    private final static int RIGHT_INSET = 10;
    private final static int BOTTOM_INSET = 0;

    // Labels for the various elements of the dialog.
    private final static String dialogTitle = "Edit Network";
    private final static String nameLabel = "Name:";
    private final static String networkPropertiesLabel = "Network properties:";
    private final static String nextPropertyLabel = "Next";
    private final static String newPropertyLabel = "New";
    private final static String globalLabel = "Network neighborhood model:";
    private final static String noGlobalLabel = "No global neighborhood";
    private final static String epsilonGlobalLabel =
                                "Epsilon contaminated neighborhood";
    private final static String ratioGlobalLabel =
                                "Constant density ratio neighborhood";
    private final static String totalGlobalLabel =
                                "Total variation neighborhood";
    private final static String boundedGlobalLabel =
                                "Constant density bounded neighborhood";
    private final static String globalParameterLabel =
                                "Global neighborhood parameter:";
    private final static String okLabel = "Apply";
    private final static String dismissLabel = "Dismiss";
    // The InferenceGraph object that contains the network.
    InferenceGraph inferenceGraph;
    // Variables that hold the properties in the dialog.
    PropertyManager propertyManager;
    // Variables used to construct the dialog.
    int displayedNetworkPropertyIndex;
    Panel namePanel;
    Panel networkPropertyPanel;
    Panel globalNeighbourhoodPanel;
    Panel gncp;
    Panel globalNeighbourhoodPropertyPanel;
    Panel parameterPanel;
    Panel okPanel;
    Label name;
    Label networkProperties;
    TextField textName;
    TextField textGlobalParameter;
    Label global;
    Label globalParameter;
    CheckboxGroup globals;
    Checkbox noGlobal;
    Checkbox epsilonGlobal;
    Checkbox ratioGlobal;
    Checkbox totalGlobal;
    Checkbox boundedGlobal;
    Button newNetworkProperty;
    Button nextNetworkProperty;
    TextField networkPropertiesText;
    Button okButton;
    Button dismissButton;

    /**
     * Default constructor for an EditNetworkDialog object.
     *
     * @param parent         parent frame
     * @param inferenceGraph inference graph referring to
     */
    EditNetworkDialog(Frame parent, InferenceGraph inferenceGraph)
    {
        super(parent, dialogTitle, true);
        this.inferenceGraph = inferenceGraph;

        // Compose the whole frame.
        // Panel for the name.
        namePanel = new Panel();
        namePanel.setLayout(new BorderLayout());
        name = new Label(nameLabel);
        textName = new TextField(30);
        namePanel.add("West", name);
        namePanel.add("Center", textName);

        // Network properties.
        networkPropertyPanel = new Panel();
        networkPropertyPanel.setLayout(new BorderLayout());
        networkProperties = new Label(networkPropertiesLabel);
        nextNetworkProperty = new Button(nextPropertyLabel);
        newNetworkProperty = new Button(newPropertyLabel);
        networkPropertiesText = new TextField(40);

        networkPropertyPanel.add("North", networkProperties);
        networkPropertyPanel.add("West", nextNetworkProperty);
        networkPropertyPanel.add("Center", networkPropertiesText);
        networkPropertyPanel.add("East", newNetworkProperty);

        // Global neighborhood parameters
        globalNeighbourhoodPanel = new Panel();
        globalNeighbourhoodPanel.setLayout(new BorderLayout());
        global = new Label(globalLabel);

        gncp = new Panel();
        gncp.setLayout(new GridLayout(5, 1));
        globals = new CheckboxGroup();
        noGlobal = new Checkbox(noGlobalLabel, globals, true);
        epsilonGlobal = new Checkbox(epsilonGlobalLabel, globals, false);
        ratioGlobal = new Checkbox(ratioGlobalLabel, globals, false);
        totalGlobal = new Checkbox(totalGlobalLabel, globals, false);
        boundedGlobal = new Checkbox(boundedGlobalLabel, globals, false);
        gncp.add(noGlobal);
        gncp.add(epsilonGlobal);
        gncp.add(ratioGlobal);
        gncp.add(totalGlobal);
        gncp.add(boundedGlobal);

        globalNeighbourhoodPropertyPanel = new Panel();
        globalNeighbourhoodPropertyPanel.setLayout(new BorderLayout());
        globalParameter = new Label(globalParameterLabel);
        textGlobalParameter = new TextField(10);
        globalNeighbourhoodPropertyPanel.add("West", globalParameter);
        globalNeighbourhoodPropertyPanel.add("Center", textGlobalParameter);

        globalNeighbourhoodPanel.add("North", global);
        globalNeighbourhoodPanel.add("Center", gncp);
        globalNeighbourhoodPanel.add("South", globalNeighbourhoodPropertyPanel);

        // All the network parameters
        parameterPanel = new Panel();
        parameterPanel.setLayout(new BorderLayout());
        parameterPanel.add("North", namePanel);
        parameterPanel.add("Center", networkPropertyPanel);
        parameterPanel.add("South", globalNeighbourhoodPanel);

        // Return buttons
        okPanel = new Panel();
        okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        okButton = new Button(okLabel);
        dismissButton = new Button(dismissLabel);
        okPanel.add(okButton);
        okPanel.add(dismissButton);

        setLayout(new BorderLayout());
        add("North", parameterPanel);
        add("Center", okPanel);

        // Pack the whole window
        pack();

        // Initialize values
        fillDialog();
    }

    /**
     * Customized setVisible method.
     */
    @Override
    public void setVisible(boolean show)
    {
        Rectangle bounds = getParent().getBounds();
        Rectangle abounds = getBounds();

        setLocation(bounds.x + (bounds.width - abounds.width) / 2,
                    bounds.y + (bounds.height - abounds.height) / 2);

        super.setVisible(show);
    }

    @Override
    public Insets getInsets()
    {
        Insets ins = super.getInsets();
        return new Insets(ins.top + TOP_INSET, ins.left + LEFT_INSET,
                          ins.bottom + BOTTOM_INSET, ins.right + RIGHT_INSET);
    }

    @Override
    public boolean handleEvent(Event evt)
    {
        if (evt.id == Event.WINDOW_DESTROY)
        {
            dispose();
        }
        return super.handleEvent(evt);
    }

    /**
     * Fill the values in the dialog area.
     */
    private void fillDialog()
    {
        String values[], allValues = "";
        ArrayList<String> properties;
        String property;
        double par;

        // Synchronize the network if necessary.
        inferenceGraph.getBayesNet();

        // Fill the name.
        textName.setText(inferenceGraph.getName());

        // Fill and store network properties
        propertyManager = new PropertyManager(inferenceGraph.
        getNetworkProperties(),
                                              networkPropertiesText);

        // Set global neighborhood
        switch (inferenceGraph.getGlobalNeighborhoodType())
        {
            case NO_CREDAL_SET:
                globals.setSelectedCheckbox(noGlobal);
                break;
            case CONSTANT_DENSITY_RATIO:
                globals.setSelectedCheckbox(ratioGlobal);
                break;
            case EPSILON_CONTAMINATED:
                globals.setSelectedCheckbox(epsilonGlobal);
                break;
            case CONSTANT_DENSITY_BOUNDED:
                globals.setSelectedCheckbox(boundedGlobal);
                break;
            case TOTAL_VARIATION:
                globals.setSelectedCheckbox(totalGlobal);
                break;
        }

        par = inferenceGraph.getGlobalNeighborhoodParameter();
        textGlobalParameter.setText(String.valueOf(par));
    }

    @Override
    public boolean action(Event evt, Object arg)
    {
        if (evt.target == dismissButton)
        {
            dispose();
        }
        else if (evt.target == okButton)
        {
            updateDialog();
        }
        else if (evt.target == newNetworkProperty)
        {
            propertyManager.newProperty();
        }
        else if (evt.target == nextNetworkProperty)
        {
            propertyManager.nextProperty();
        }
        else if (evt.target == networkPropertiesText)
        {
            propertyManager.updateProperty();
        }
        else
        {
            return super.action(evt, arg);
        }

        return true;
    }

    /**
     * Update the contents of the network when the dialog exits.
     */
    private void updateDialog()
    {
        // Update the name of the network.
        String newNetworkName = textName.getText();
        if (!(newNetworkName.equals(inferenceGraph.getName())))
        {
            newNetworkName = inferenceGraph.checkName(newNetworkName);
            if (newNetworkName != null)
            {
                inferenceGraph.setName(newNetworkName);
            }
        }

        // Update the properties (if necessary).
        ArrayList<String> properties = propertyManager.updatePropertyOnExit();
        if (properties != null)
        {
            inferenceGraph.setNetworkProperties(properties);
        }

        // Update the global neighborhood parameters.
        Checkbox selectedGlobalNeighborhood = globals.getSelectedCheckbox();
        if (selectedGlobalNeighborhood == noGlobal)
        {
            inferenceGraph.setGlobalNeighborhood(
                    GlobalNeighbourhood.NO_CREDAL_SET);
        }
        else if (selectedGlobalNeighborhood == epsilonGlobal)
        {
            inferenceGraph.setGlobalNeighborhood(
                    GlobalNeighbourhood.EPSILON_CONTAMINATED);
        }
        else if (selectedGlobalNeighborhood == ratioGlobal)
        {
            inferenceGraph.setGlobalNeighborhood(
                    GlobalNeighbourhood.CONSTANT_DENSITY_RATIO);
        }
        else if (selectedGlobalNeighborhood == boundedGlobal)
        {
            inferenceGraph.setGlobalNeighborhood(
                    GlobalNeighbourhood.CONSTANT_DENSITY_BOUNDED);
        }
        else if (selectedGlobalNeighborhood == totalGlobal)
        {
            inferenceGraph.setGlobalNeighborhood(
                    GlobalNeighbourhood.TOTAL_VARIATION);
        }

        try
        {
            double par = new Double(textGlobalParameter.getText());
            if (par <= 0.0)
            {
                par = 0.0;
            }
            inferenceGraph.setGlobalNeighborhoodParameter(par);
        }
        catch (NumberFormatException e)
        {
        } // Leave parameter as is if in error.
    }
}
