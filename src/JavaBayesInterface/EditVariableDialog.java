/*
 * EditVariableDialog.java
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
import java.util.StringTokenizer;
import java.util.logging.Logger;

class EditVariableDialog extends Dialog
{

    private static final Class CLAZZ = EditVariableDialog.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Constants used to construct the dialog.
    private final static int TOP_INSET = 5;
    private final static int LEFT_INSET = 10;
    private final static int RIGHT_INSET = 10;
    private final static int BOTTOM_INSET = 0;

    // Labels for the various elements of the dialog.
    private final static String nameLabel = "Name:";
    private final static String newValueLabel = "Values:";
    private final static String typeLabel = "Types:";
    private final static String chanceTypeLabel = "Chance node";
    private final static String explanationTypeLabel = "Explanatory node";
    private final static String noLocalCredalSetLabel =
                                "Single distribution";
    private final static String localCredalSetLabel =
                                "Credal set with extreme points";
    private final static String variablePropertiesLabel =
                                "Variable properties:";
    private final static String functionPropertiesLabel =
                                "Function properties:";
    private final static String nextPropertyLabel = "Next";
    private final static String newPropertyLabel = "New";
    private final static String editFunctionLabel = "Edit function";
    private final static String okLabel = "Apply";
    private final static String dismissLabel = "Dismiss";
    // Network panel, used to repaint screen and access dialogs.
    NetworkPanel npan;
    // The InferenceGraph and InferenceGraphNode objects
    // that hold the variable.
    InferenceGraph inferenceGraph;
    InferenceGraphNode inferenceGraphNode;
    // Variables that hold the contents of the dialog.
    int numberExtremePoints;
    PropertyManager variablePropertyManager;
    PropertyManager functionPropertyManager;
    int displayedVariablePropertyIndex;
    int displayedFunctionPropertyIndex;
    Panel np;
    Panel nvp;
    Panel tp;
    Panel ttp;
    Panel vpp;
    Panel fpp;
    Panel npp;
    Panel cbp;
    Panel pp;
    Panel gnp;
    Panel gncp;
    Panel okp;
    Panel qbp;
    Panel qbpp;
    Label name;
    Label newValue;
    Label type;
    Label variableProperties;
    Label functionProperties;
    Label localParameter;
    TextField textName;
    TextField textNewValue;
    TextField textLocalParameter;
    CheckboxGroup types;
    CheckboxGroup functionTypes;
    Checkbox chanceType;
    Checkbox explanationType;
    Checkbox noLocalCredalSetType;
    Checkbox localCredalSetType;
    Button newVariableProperty;
    Button nextVariableProperty;
    Button newFunctionProperty;
    Button nextFunctionProperty;
    TextField variablePropertiesText;
    TextField functionPropertiesText;
    Button distButton;
    Button okButton;
    Button dismissButton;

    /**
     * Default constructor for an EditVariableDialog object.
     *
     * @param networkPanel       the network panel
     * @param parent             parent frame
     * @param inferenceGraph     inference graph referring to
     * @param inferenceGraphNode inferenceGraphNode of the credal set
     */
    EditVariableDialog(NetworkPanel networkPanel,
                       Frame parent,
                       InferenceGraph inferenceGraph,
                       InferenceGraphNode inferenceGraphNode)
    {
        super(parent, "Edit: " + inferenceGraphNode.getName(), true);
        this.npan = networkPanel;
        this.inferenceGraph = inferenceGraph;
        this.inferenceGraphNode = inferenceGraphNode;

        // Compose the frame
        // Panel for name, values and type
        // Panel for the name
        np = new Panel();
        np.setLayout(new BorderLayout());
        name = new Label(nameLabel);
        textName = new TextField(30);
        np.add("West", name);
        np.add("Center", textName);

        // Panel for the values
        nvp = new Panel();
        nvp.setLayout(new BorderLayout());
        newValue = new Label(newValueLabel);
        textNewValue = new TextField(60);
        nvp.add("West", newValue);
        nvp.add("Center", textNewValue);

        // Panel for the type
        tp = new Panel();
        tp.setLayout(new BorderLayout());
        type = new Label(typeLabel);

        ttp = new Panel();
        ttp.setLayout(new GridLayout(2, 1));
        types = new CheckboxGroup();
        chanceType = new Checkbox(chanceTypeLabel, types, true);
        explanationType = new Checkbox(explanationTypeLabel, types, false);
        ttp.add(chanceType);
        ttp.add(explanationType);

        qbp = new Panel();
        qbp.setLayout(new GridLayout(2, 1));
        functionTypes = new CheckboxGroup();
        noLocalCredalSetType = new Checkbox(noLocalCredalSetLabel,
                                            functionTypes, true);
        localCredalSetType = new Checkbox(localCredalSetLabel,
                                          functionTypes, false);

        qbp.add(noLocalCredalSetType);
        qbp.add(localCredalSetType);

        tp.add("North", type);
        tp.add("West", ttp);
        tp.add("East", qbp);

        // Finish panel for name, values and type
        cbp = new Panel();
        cbp.setLayout(new BorderLayout(10, 10));
        cbp.add("North", np);
        cbp.add("Center", nvp);
        cbp.add("South", tp);

        // Panel for properties (variable, function and network)
        pp = new Panel();
        pp.setLayout(new BorderLayout());

        // Variable properties
        vpp = new Panel();
        vpp.setLayout(new BorderLayout());
        variableProperties = new Label(variablePropertiesLabel);
        nextVariableProperty = new Button(nextPropertyLabel);
        newVariableProperty = new Button(newPropertyLabel);
        variablePropertiesText = new TextField(40);
        vpp.add("North", variableProperties);
        vpp.add("West", nextVariableProperty);
        vpp.add("Center", variablePropertiesText);
        vpp.add("East", newVariableProperty);

        // Function properties
        fpp = new Panel();
        fpp.setLayout(new BorderLayout());
        functionProperties = new Label(functionPropertiesLabel);
        nextFunctionProperty = new Button(nextPropertyLabel);
        newFunctionProperty = new Button(newPropertyLabel);
        functionPropertiesText = new TextField(40);
        fpp.add("North", functionProperties);
        fpp.add("West", nextFunctionProperty);
        fpp.add("Center", functionPropertiesText);
        fpp.add("East", newFunctionProperty);

        // Finish panel for properties
        pp.add("North", vpp);
        pp.add("Center", fpp);

        // Return buttons
        okp = new Panel();
        okp.setLayout(new FlowLayout(FlowLayout.CENTER));
        distButton = new Button(editFunctionLabel);
        okp.add(distButton);
        okButton = new Button(okLabel);
        dismissButton = new Button(dismissLabel);
        okp.add(okButton);
        okp.add(dismissButton);
        setLayout(new BorderLayout());
        add("North", cbp);
        add("Center", pp);
        add("South", okp);

        // Pack the whole window
        pack();

        // Initialize values
        fillDialog();
    }

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

        // Synchronize the network if necessary.
        inferenceGraph.getBayesNet();

        // Fill name
        textName.setText(inferenceGraphNode.getName());

        // Fill values
        values = inferenceGraphNode.getValues();
        for (int i = 0; i < values.length; i++)
        {
            allValues += values[i];
            if (i != (values.length - 1))
            {
                allValues += ", ";
            }
        }
        textNewValue.setText(allValues);

        // Set type: explanatory or chance.
        if (inferenceGraphNode.isExplanation())
        {
            types.setSelectedCheckbox(explanationType);
        }
        else
        {
            types.setSelectedCheckbox(chanceType);
        }

        // Set type: standard or credal.
        if (inferenceGraphNode.isCredalSet())
        {
            functionTypes.setSelectedCheckbox(localCredalSetType);
        }
        else
        {
            functionTypes.setSelectedCheckbox(noLocalCredalSetType);
        }

        // Fill and store properties
        variablePropertyManager =
        new PropertyManager(inferenceGraphNode.getVariableProperties(),
                            variablePropertiesText);
        functionPropertyManager =
        new PropertyManager(inferenceGraphNode.getFunctionProperties(),
                            functionPropertiesText);
    }

    @Override
    public boolean action(Event evt, Object arg)
    {
        ArrayList properties;
        String values[], property, checkedName;

        if (evt.target == dismissButton)
        {
            dispose();
        }
        else if (evt.target == okButton)
        {
            updateDialog();
        }
        else if (evt.target == newVariableProperty)
        {
            variablePropertyManager.newProperty();
        }
        else if (evt.target == nextVariableProperty)
        {
            variablePropertyManager.nextProperty();
        }
        else if (evt.target == newFunctionProperty)
        {
            functionPropertyManager.newProperty();
        }
        else if (evt.target == nextFunctionProperty)
        {
            functionPropertyManager.nextProperty();
        }
        else if (evt.target == variablePropertiesText)
        {
            variablePropertyManager.updateProperty();
        }
        else if (evt.target == functionPropertiesText)
        {
            functionPropertyManager.updateProperty();
        }
        else if (evt.target == distButton)
        {
            npan.editFunction(inferenceGraphNode);
        }
        else
        {
            return super.action(evt, arg);
        }

        return true;
    }

    /**
     * Parse the values stated in the values TextField.
     *
     * @param allValues all values in a single string
     * @return a string array containing the parsed strings
     */
    private String[] parseValues(String allValues)
    {
        String token = null, delimiters = " ,\n\t\r";
        StringTokenizer st = new StringTokenizer(allValues, delimiters);
        String vals[] = new String[st.countTokens()];
        int i = 0;

        while (st.hasMoreTokens())
        {
            vals[i] = inferenceGraph.makeValidValue(st.nextToken());
            i++;
        }
        return vals;
    }

    /**
     * Update the contents of the network when the dialog exits.
     */
    private void updateDialog()
    {
        // Update the name of the variable.
        String checkedName = inferenceGraph.checkName(textName.getText());
        if (checkedName != null)
        {
            inferenceGraphNode.setName(checkedName);
        }
        // Update the values of the variable.
        String[] values = parseValues(textNewValue.getText());
        if (values != null)
        {
            inferenceGraph.changeValues(inferenceGraphNode, values);
        }
        // Update the explanatory/chance type.
        if (types.getSelectedCheckbox() == chanceType)
        {
            inferenceGraphNode.setExplanation(false);
        }
        else
        {
            inferenceGraphNode.setExplanation(true);
        }
        npan.repaint();
        // Update the standard/credal type.
        if (functionTypes.getSelectedCheckbox() == noLocalCredalSetType)
        {
            inferenceGraphNode.setNoLocalCredalSet();
        }
        else
        {
            inferenceGraphNode.setLocalCredalSet();
        }
        // Update the variable properties (if necessary).
        ArrayList<String> vprop = variablePropertyManager.updatePropertyOnExit();
        if (vprop != null)
        {
            inferenceGraphNode.setVariableProperties(vprop);
            for (String property : vprop)
            {
                inferenceGraphNode.updateCoordinatesFromProperty(property);
            }
        }
        // Update the function properties (if necessary).
        ArrayList<String> fprop = functionPropertyManager.updatePropertyOnExit();
        if (fprop != null)
        {
            inferenceGraphNode.setFunctionProperties(fprop);
        }
    }
}
