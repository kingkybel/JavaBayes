/*
 * Copyright (C) 2015 Dieter J Kybelksties
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * @author  Dieter J Kybelksties
 * @date May 11, 2016
 *
 */
package BayesGUI;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * This class will create and dispatch a WINDOW_CLOSING event to the active
 * window. As a result any WindowListener that handles the windowClosing event
 * will be executed. Since clicking on the "Close" button of the frame/dialog or
 * selecting the "Close" option from the system menu also invoke the
 * WindowListener, this will provide a common exit point for the application.
 *
 * @author Dieter J Kybelksties
 */
public class ExitAction extends AbstractAction
{

    private static final Class<ExitAction> CLAZZ = ExitAction.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Default constructor.
     */
    public ExitAction()
    {
        super("Exit");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        //  Find the active window before creating and dispatching the event

        Window window = KeyboardFocusManager.getCurrentKeyboardFocusManager().
               getActiveWindow();

        if (window != null)
        {
            WindowEvent windowClosing = new WindowEvent(window,
                                                        WindowEvent.WINDOW_CLOSING);
            window.dispatchEvent(windowClosing);
        }
    }

}
