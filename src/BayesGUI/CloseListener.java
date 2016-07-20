
/*
 * @author  Dieter J Kybelksties
 * @date Jul 20, 2016
 *
 */
package BayesGUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Dieter J Kybelksties
 */
public class CloseListener extends WindowAdapter
{

    private static final String CLASS_NAME = CloseListener.class.getName();
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
