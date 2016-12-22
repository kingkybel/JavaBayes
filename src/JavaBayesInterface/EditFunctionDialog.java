/*
 * EditFunctionDialog.java
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
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.util.logging.Logger;

class EditFunctionDialog extends Dialog
{

    private static final Class CLAZZ = EditFunctionDialog.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Constants used to construct the dialog.
    private final static int TOP_INSET = 5;
    private final static int LEFT_INSET = 10;
    private final static int RIGHT_INSET = 10;
    private final static int BOTTOM_INSET = 0;

    // Labels for the various elements of the dialog.
    private final static String okLabel = "Apply";
    private final static String dialogTitle = "Edit Function";
    private final static String dismissLabel = "Dismiss";
    // Variables used to construct the dialog.
    Frame parent;
    EditFunctionPanel efp;
    Panel buttons;
    Button okButton;
    Button dismissButton;

    /**
     * Default constructor for an EditFunctionDialog.
     */
    EditFunctionDialog(Frame parent, InferenceGraph ig,
                       InferenceGraphNode ign)
    {
        super(parent, dialogTitle, true);
        this.parent = parent;
        setLayout(new BorderLayout());
        efp = dispatch(ig, ign);
        buttons = new Panel();
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
        okButton = new Button(okLabel);
        dismissButton = new Button(dismissLabel);
        buttons.add(okButton);
        buttons.add(dismissButton);
        add("Center", efp);
        add("South", buttons);
        pack();
    }

    /**
     * Create the appropriate instance of EditFunctionPanel, based on the
     * function in the node.
     */
    private EditFunctionPanel dispatch(InferenceGraph ig, InferenceGraphNode ign)
    {
        if (ign.isCredalSet())
        {
            return (new EditCredalSet(ig, ign));
        }
        else
        {
            return (new EditProbability(this, ig, ign));
        }
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

    /**
     * Customize getInsets() method.
     */
    @Override
    public Insets getInsets()
    {
        Insets ins = super.getInsets();
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

    /**
     * Handle events in the dialog.
     */
    @Override
    public boolean action(Event evt, Object arg)
    {
        // Check whether to dismiss
        if (evt.target == dismissButton)
        {
            efp.dismiss();
            dispose();
            return (true);
        }
        else if (evt.target == okButton)
        {
            efp.accept();
            return (true);
        }
        return (super.action(evt, arg));
    }
}
