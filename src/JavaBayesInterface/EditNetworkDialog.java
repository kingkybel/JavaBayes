/**
 * EditNetworkDialog.java
 *
 * @author Fabio G. Cozman Original version by Sreekanth Nagarajan, rewritten
 * from scratch by Fabio Cozman. Copyright 1996 - 1999, Fabio G. Cozman,
 * Carnergie Mellon University, Universidade de Sao Paulo fgcozman@usp.br,
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
package JavaBayesInterface;

import InferenceGraphs.InferenceGraph;
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
    private static final Logger LOG =
    Logger.getLogger(EditNetworkDialog.class.getName());
    // The InferenceGraph object that contains the network.
    InferenceGraph ig;
    // Variables that hold the properties in the dialog.
    PropertyManager propertyManager;
    // Variables used to construct the dialog.
    int displayedNetworkPropertyIndex;
    Panel np;
    Panel npp;
    Panel gnp;
    Panel gncp;
    Panel gnpp;
    Panel tp;
    Panel okp;
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
     */
    EditNetworkDialog(Frame parent, InferenceGraph iG)
    {
        super(parent, dialogTitle, true);
        this.ig = iG;

    	// Compose the whole frame.
        // Panel for the name.
        np = new Panel();
        np.setLayout(new BorderLayout());
        name = new Label(nameLabel);
        textName = new TextField(30);
        np.add("West", name);
        np.add("Center", textName);

        // Network properties.
        npp = new Panel();
        npp.setLayout(new BorderLayout());
        networkProperties = new Label(networkPropertiesLabel);
        nextNetworkProperty = new Button(nextPropertyLabel);
        newNetworkProperty = new Button(newPropertyLabel);
        networkPropertiesText = new TextField(40);

        npp.add("North", networkProperties);
        npp.add("West", nextNetworkProperty);
        npp.add("Center", networkPropertiesText);
        npp.add("East", newNetworkProperty);

        // Global neighborhood parameters
        gnp = new Panel();
        gnp.setLayout(new BorderLayout());
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

        gnpp = new Panel();
        gnpp.setLayout(new BorderLayout());
        globalParameter = new Label(globalParameterLabel);
        textGlobalParameter = new TextField(10);
        gnpp.add("West", globalParameter);
        gnpp.add("Center", textGlobalParameter);

        gnp.add("North", global);
        gnp.add("Center", gncp);
        gnp.add("South", gnpp);

        // All the network parameters
        tp = new Panel();
        tp.setLayout(new BorderLayout());
        tp.add("North", np);
        tp.add("Center", npp);
        tp.add("South", gnp);

        // Return buttons
        okp = new Panel();
        okp.setLayout(new FlowLayout(FlowLayout.CENTER));
        okButton = new Button(okLabel);
        dismissButton = new Button(dismissLabel);
        okp.add(okButton);
        okp.add(dismissButton);

        setLayout(new BorderLayout());
        add("North", tp);
        add("Center", okp);

        // Pack the whole window
        pack();

        // Initialize values
        fillDialog();
    }

    /**
     * Customized show method.
     */
    @Override
    public void show()
    {
        Rectangle bounds = getParent().bounds();
        Rectangle abounds = bounds();

        move(bounds.x + (bounds.width - abounds.width) / 2,
             bounds.y + (bounds.height - abounds.height) / 2);

        super.show();
    }

    /**
     * Customized insets method.
     */
    @Override
    public Insets insets()
    {
        Insets ins = super.insets();
        return (new Insets(ins.top + TOP_INSET, ins.left + LEFT_INSET,
                           ins.bottom + BOTTOM_INSET, ins.right + RIGHT_INSET));
    }

    /**
     * Handle the possible destruction of the window.
     */
    @Override
    public boolean handleEvent(Event evt)
    {
        if (evt.id == Event.WINDOW_DESTROY)
        {
            dispose();
        }
        return (super.handleEvent(evt));
    }

    /*
     * Fill the values in the dialog area.
     */
    private void fillDialog()
    {
        String values[], allValues = "";
        ArrayList prop;
        String property;
        double par;

        // Synchronize the network if necessary.
        ig.getBayesNet();

        // Fill the name.
        textName.setText(ig.getName());

        // Fill and store network properties
        propertyManager = new PropertyManager(ig.getNetworkProperties(),
                                               networkPropertiesText);

        // Set global neighborhood
        switch (ig.getGlobalNeighborhoodType())
        {
            case InferenceGraph.NO_CREDAL_SET:
                globals.setCurrent(noGlobal);
                break;
            case InferenceGraph.CONSTANT_DENSITY_RATIO:
                globals.setCurrent(ratioGlobal);
                break;
            case InferenceGraph.EPSILON_CONTAMINATED:
                globals.setCurrent(epsilonGlobal);
                break;
            case InferenceGraph.CONSTANT_DENSITY_BOUNDED:
                globals.setCurrent(boundedGlobal);
                break;
            case InferenceGraph.TOTAL_VARIATION:
                globals.setCurrent(totalGlobal);
                break;
        }

        par = ig.getGlobalNeighborhoodParameter();
        textGlobalParameter.setText(String.valueOf(par));
    }

    /**
     * Handle the possible events.
     */
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

        return (true);
    }

    /*
     * Update the contents of the network when the
     * dialog exits.
     */
    private void updateDialog()
    {
        // Update the name of the network.
        String newNetworkName = textName.getText();
        if (!(newNetworkName.equals(ig.getName())))
        {
            newNetworkName = ig.checkName(newNetworkName);
            if (newNetworkName != null)
            {
                ig.setName(newNetworkName);
            }
        }

        // Update the properties (if necessary).
        ArrayList prop = propertyManager.updatePropertyOnExit();
        if (prop != null)
        {
            ig.setNetworkProperties(prop);
        }

        // Update the global neighborhood parameters.
        Checkbox selectedGlobalNeighborhood = globals.getCurrent();
        if (selectedGlobalNeighborhood == noGlobal)
        {
            ig.setGlobalNeighborhood(InferenceGraph.NO_CREDAL_SET);
        }
        else if (selectedGlobalNeighborhood == epsilonGlobal)
        {
            ig.setGlobalNeighborhood(InferenceGraph.EPSILON_CONTAMINATED);
        }
        else if (selectedGlobalNeighborhood == ratioGlobal)
        {
            ig.setGlobalNeighborhood(InferenceGraph.CONSTANT_DENSITY_RATIO);
        }
        else if (selectedGlobalNeighborhood == boundedGlobal)
        {
            ig.setGlobalNeighborhood(InferenceGraph.CONSTANT_DENSITY_BOUNDED);
        }
        else if (selectedGlobalNeighborhood == totalGlobal)
        {
            ig.setGlobalNeighborhood(InferenceGraph.TOTAL_VARIATION);
        }

        try
        {
            double par =
                   (new Double(textGlobalParameter.getText()).doubleValue());
            if (par <= 0.0)
            {
                par = 0.0;
            }
            ig.setGlobalNeighborhoodParameter(par);
        }
        catch (NumberFormatException e)
        {
        } // Leave parameter as is if in error.
    }
}
