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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Class to listen to windowClose-events. Use in combination with ExitAction.
 *
 * @author Dieter J Kybelksties
 */
public class CloseListener extends WindowAdapter
{

    public static final Class CLAZZ = CloseListener.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    String prompt = "Are you sure you want to exit the application";
    String title = "Exit Application";

    public CloseListener()
    {
    }

    public CloseListener(String prompt, String title)
    {
        this.prompt = prompt;
        this.title = title;
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        JFrame frame = (JFrame) e.getSource();
        int result = JOptionPane.showConfirmDialog(
            frame,
            prompt,
            title,
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION)
        {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

}
