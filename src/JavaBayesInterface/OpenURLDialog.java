/*
 * OpenURLDialog.java
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
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class OpenURLDialog extends Dialog
{

    private static final String CLASS_NAME = OpenURLDialog.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    JavaBayes jb;

    java.awt.TextField textURL;
    java.awt.Panel panelButtons;
    java.awt.Button okButton;
    java.awt.Button cancelButton;

    /**
     *
     * @param parent
     * @param javaBayes
     * @param modal
     */
    public OpenURLDialog(Frame parent, JavaBayes javaBayes, boolean modal)
    {
        super(parent, true);

        //{{INIT_CONTROLS
        setLayout(new BorderLayout());
        addNotify();
        setSize(getInsets().left + getInsets().right + 295, getInsets().top +
                                                            getInsets().bottom +
                                                            92);

        textURL = new TextField();
        add("North", textURL);

        panelButtons = new Panel();
        panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        okButton = new java.awt.Button("Ok");
        panelButtons.add(okButton);
        cancelButton = new java.awt.Button("Cancel");
        panelButtons.add(cancelButton);
        add("South", panelButtons);

        setResizable(false);
        //}}

        // Insert the JavaBayes object
        jb = javaBayes;
    }

    /**
     *
     * @param parent
     * @param javaBayes
     * @param title
     * @param modal
     */
    public OpenURLDialog(Frame parent, JavaBayes javaBayes,
                         String title, boolean modal)
    {
        this(parent, javaBayes, modal);
        setTitle(title);
    }

    void OkButtonClicked(Event event)
    {
        jb.openUrl(textURL.getText());
        dispose();
    }

    void CancelButtonClicked(Event event)
    {
        dispose();
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
    public boolean handleEvent(Event event)
    {
        if (event.target == cancelButton && event.id == Event.ACTION_EVENT)
        {
            CancelButtonClicked(event);
        }
        if (event.target == okButton && event.id == Event.ACTION_EVENT)
        {
            OkButtonClicked(event);
        }
        return super.handleEvent(event);
    }
}
