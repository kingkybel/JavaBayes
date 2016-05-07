package JavaBayesInterface;

import InferenceGraphs.InferenceGraph;
import InferenceGraphs.InferenceGraphNode;
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

    // Constants used to construct the dialog.
    private final static int TOP_INSET = 5;
    private final static int LEFT_INSET = 10;
    private final static int RIGHT_INSET = 10;
    private final static int BOTTOM_INSET = 0;

    // Labels for the various elements of the dialog.
    private final static String ok_label = "Apply";
    private final static String dialog_title = "Edit Function";
    private final static String dismiss_label = "Dismiss";
    private static final Logger LOG =
    Logger.getLogger(EditFunctionDialog.class.getName());
    // Variables used to construct the dialog.
    Frame parent;
    EditFunctionPanel efp;
    Panel buttons;
    Button ok_button;
    Button dismiss_button;

    /**
     * Default constructor for an EditFunctionDialog.
     */
    EditFunctionDialog(Frame parent, InferenceGraph ig,
                       InferenceGraphNode ign)
    {
        super(parent, dialog_title, true);
        this.parent = parent;
        setLayout(new BorderLayout());
        efp = dispatch(ig, ign);
        buttons = new Panel();
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
        ok_button = new Button(ok_label);
        dismiss_button = new Button(dismiss_label);
        buttons.add(ok_button);
        buttons.add(dismiss_button);
        add("Center", efp);
        add("South", buttons);
        pack();
    }

    /*
     * Create the appropriate instance of EditFunctionPanel,
     * based on the function in the node.
     */
    private EditFunctionPanel dispatch(InferenceGraph ig, InferenceGraphNode ign)
    {
        if (ign.is_credal_set())
        {
            return (new EditCredalSet(ig, ign));
        }
        else
        {
            return (new EditProbability(this, ig, ign));
        }
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
     * Customize insets method.
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

    /**
     * Handle events in the dialog.
     */
    @Override
    public boolean action(Event evt, Object arg)
    {
        // Check whether to dismiss
        if (evt.target == dismiss_button)
        {
            efp.dismiss();
            dispose();
            return (true);
        }
        else if (evt.target == ok_button)
        {
            efp.accept();
            return (true);
        }
        return (super.action(evt, arg));
    }
}
