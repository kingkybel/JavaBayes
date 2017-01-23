/*
 * ObserveDialog.java
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
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.util.logging.Logger;

class ObserveDialog extends Dialog
{

    private static final Class CLAZZ = ObserveDialog.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    NetworkPanel networkPanel;

    InferenceGraph inferenceGraph;
    InferenceGraphNode inferenceGraphNode;

    boolean observed;

    Checkbox observedBox;
    List valuesList;

    /**
     * Default constructor for an ObserveDialog object.
     *
     * @param networkPanel       the network panel
     * @param parent             parent frame
     * @param inferenceGraph     inference graph referring to
     * @param inferenceGraphNode inferenceGraphNode of the credal set
     */
    ObserveDialog(NetworkPanel networkPanel,
                  Frame parent,
                  InferenceGraph inferenceGraph,
                  InferenceGraphNode inferenceGraphNode)
    {
        super(parent, "Set Observe Value", true);
        this.inferenceGraph = inferenceGraph;
        this.inferenceGraphNode = inferenceGraphNode;
        this.networkPanel = networkPanel;

        Panel cbp = new Panel();
        cbp.setLayout(new FlowLayout(FlowLayout.CENTER));

        observed = inferenceGraphNode.isObserved();
        observedBox = new Checkbox("Observed", null, observed);
        cbp.add(observedBox);

        Panel listp = new Panel();
        listp.setLayout(new GridLayout(1, 1));
        valuesList = new List(6, false);

        String[] values = inferenceGraphNode.getValues();
        for (String value : values)
        {
            valuesList.add(value);
        }

        if (observed)
        {
            valuesList.select(inferenceGraphNode.getObservedIndex());
        }

        listp.add(valuesList);

        Panel okp = new Panel();
        okp.setLayout(new FlowLayout(FlowLayout.CENTER));
        okp.add(new Button("Ok"));
        okp.add(new Button("Cancel"));

        setLayout(new BorderLayout());
        add("North", cbp);
        add("Center", listp);
        add("South", okp);
        pack();
    }

    @Override
    public boolean action(Event evt, Object arg)
    {
        if (evt.target == observedBox)
        {
            observed = observedBox.getState();
            if (observed)
            {
                valuesList.select(0);	// select first value by default
            }
            else
            {
                // clear any selection
                valuesList.deselect(valuesList.getSelectedIndex());
            }
            return super.action(evt, arg);
        }
        else if (evt.target == valuesList)
        {
            if (!observed)
            {
                observed = true;
                observedBox.setState(observed);
            }
            return super.action(evt, arg);
        }
        else if (arg.equals("Ok"))
        {
            observed = observedBox.getState();
            String selValue = valuesList.getSelectedItem();
            if (observed && selValue == null)
            {
                JavaBayesHelpMessages.show(JavaBayesHelpMessages.observeError);
                return true; // do not close this dialog box
            }
            if (observed)
            {
                inferenceGraphNode.setObservationValue(selValue);
            }
            else
            {
                inferenceGraphNode.clearObservation();
            }
            networkPanel.repaint();
            dispose();
        }
        else if (arg.equals("Cancel"))
        {
            dispose();
        }
        else
        {
            return super.action(evt, arg);
        }
        return true;
    }

}
