/*
 * JavaBayesConsoleFrame.java
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

import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public final class JavaBayesConsoleFrame extends Frame
{

    private static final Class CLAZZ = JavaBayesConsoleFrame.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    static final String appletInvalidOperation =
                        "This operation is not allowed in an applet!";
    // Labels
    static final String fileLabel = "File";
    static final String optionsLabel = "Options";
    static final String saveLabel = "Save format";
    static final String bifSaveLabel = "BIF format";
    static final String xmlSaveLabel = "XML format";
    final static String bugsSaveLabel = "BUGS format";
    final static String openDialogTitle = "Open";
    final static String saveDialogTitle = "Save";
    final static String openMenuitemTitle = "Open...";
    final static String openUrlMenuitemTitle = "Open URL...";
    final static String saveMenuitemTitle = "Save";
    final static String saveAsMenuitemTitle = "Save as...";
    final static String clearMenuitemTitle = "Clear";
    final static String dumpConsoleMenuitemTitle = "Dump console...";
    final static String quitMenuitemTitle = "Quit";
    final static String showBayesNetTitle = "Bayesian network";
    final static String showBucketsTitle = "Bucket tree";
    final static String algorithmVariableEliminationTitle =
                        "Variable elimination";
    final static String algorithmBucketTreeTitle = "Junction tree";
    final static String whatToShowTitle = "What to show";
    final static String algorithmTitle = "Algorithm";
    final static String inferenceModeTitle = "Inference mode";
    final static String posteriorMarginalTitle = "Posterior marginal";
    final static String posteriorExpectationTitle = "Posterior expectation";
    final static String explanationTitle = "Estimate explanatory variables";
    final static String bestConfigurationTitle = "Find complete explanation";
    final static String sensitivityAnalysisTitle = "Sensitivity analysis";
    final static String helpTitle = "Help";
    final static String aboutTitle = "About";
    private JavaBayes jb;
    // Declare controls
    FileDialog OpenFileDialog;
    FileDialog SaveFileDialog;
    TextArea textArea1;
    // Declare menus
    MenuBar mainMenuBar;
    Menu menu1;
    Menu menu2;
    Menu menu3;
    Menu menu4;
    Menu menu5;
    Menu menu6;
    Menu menu7;
    CheckboxMenuItem showBuckets;
    CheckboxMenuItem showBayesNet;
    CheckboxMenuItem algorithmVariableElimination;
    CheckboxMenuItem algorithmBucketTree;
    CheckboxMenuItem bifFormat;
    CheckboxMenuItem bugsFormat;
    CheckboxMenuItem xmlFormat;
    CheckboxMenuItem posteriorMarginal;
    CheckboxMenuItem posteriorExpectation;
    CheckboxMenuItem explanation;
    CheckboxMenuItem bestConfiguration;
    CheckboxMenuItem sensitivityAnalysis;

    /**
     * Constructor for JavaBayesConsoleFrame.
     *
     * @param javaBayes
     * @param title
     */
    public JavaBayesConsoleFrame(JavaBayes javaBayes, String title)
    {
        jb = javaBayes;
        setTitle(title);

        // Initialize controls.
        OpenFileDialog = new java.awt.FileDialog(this,
                                                 openDialogTitle,
                                                 FileDialog.LOAD);
        SaveFileDialog = new java.awt.FileDialog(this,
                                                 saveDialogTitle,
                                                 FileDialog.SAVE);
        OpenFileDialog.setFile(
                "/home/kybelksd/NetBeansProjects/JavaBayes2/src/Examples");
        textArea1 = new java.awt.TextArea();
        add("Center", textArea1);

        // Menus.
        mainMenuBar = new java.awt.MenuBar();

        menu1 = new java.awt.Menu(fileLabel);
        menu1.add(openMenuitemTitle);
        menu1.add(openUrlMenuitemTitle);
        menu1.add(saveMenuitemTitle);
        menu1.add(saveAsMenuitemTitle);
        menu1.add(clearMenuitemTitle);
        menu1.add(dumpConsoleMenuitemTitle);
        menu1.addSeparator();
        menu1.add(quitMenuitemTitle);
        mainMenuBar.add(menu1);

        menu4 = new Menu(optionsLabel);

        menu6 = new Menu(whatToShowTitle);
        menu6.add(showBayesNet = new CheckboxMenuItem(showBayesNetTitle));
        menu6.add(showBuckets = new CheckboxMenuItem(showBucketsTitle));
        menu4.add(menu6);

        menu7 = new Menu(algorithmTitle);
        menu7.add(algorithmVariableElimination = new CheckboxMenuItem(
                  algorithmVariableEliminationTitle));
        menu7.add(algorithmBucketTree = new CheckboxMenuItem(
                  algorithmBucketTreeTitle));
        menu4.add(menu7);

        menu2 = new Menu(inferenceModeTitle);
        menu2.add(posteriorMarginal = new CheckboxMenuItem(
                  posteriorMarginalTitle));
        menu2.add(posteriorExpectation = new CheckboxMenuItem(
                  posteriorExpectationTitle));
        menu2.add(explanation = new CheckboxMenuItem(explanationTitle));
        menu2.add(bestConfiguration = new CheckboxMenuItem(
                  bestConfigurationTitle));
        // menu2.add( sensitivityAnalysis = new CheckboxMenuItem(sensitivityAnalysisTitle));
        menu4.add(menu2);

        menu5 = new Menu(saveLabel);
        menu5.add(bifFormat = new CheckboxMenuItem(bifSaveLabel));
        menu5.add(xmlFormat = new CheckboxMenuItem(xmlSaveLabel));
        menu5.add(bugsFormat = new CheckboxMenuItem(bugsSaveLabel));
        menu4.add(menu5);

        mainMenuBar.add(menu4);

        menu3 = new java.awt.Menu(helpTitle);
        menu3.add(aboutTitle);
        // The following try block was contributed
        // by Jason Townsend, Nov 12 2000.
        try
        {
            mainMenuBar.setHelpMenu(menu3);
        }
        catch (Exception e)
        {
            mainMenuBar.add(menu3);
        }
        setMenuBar(mainMenuBar);

        // Initialize the inference menu
        posteriorMarginal.setState(true); // Simulate a true state.
        PosteriorMarginalAction();

        // Initialize the save format menu
        bifFormat.setState(true); // Simulate a true state.
        BifFormatAction();

        // Initialize the algorithm menu
        algorithmVariableElimination.setState(true); // Simulate a true state.
        AlgorithmVariableEliminationAction();

        // Resize the frame.
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        d.width /= 2;
        d.height /= 2;
        setSize(d);
    }

    /**
     * Constructor for JavaBayesConsoleFrame.
     *
     * @param jb
     */
    public JavaBayesConsoleFrame(JavaBayes jb)
    {
        this(jb, ((String) null));
    }

    void BucketTreeAction()
    {
        jb.whatToShowBucketTreeAction(showBuckets.getState());
    }

    void BayesianNetworkAction()
    {
        jb.whatToShowBayesianNetworkAction(showBayesNet.getState());
    }

    void PosteriorExpectationAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu2, posteriorExpectation,
                                            posteriorMarginal);
        if (activeItem == posteriorExpectation)
        {
            jb.posteriorExpectationAction();
        }
        else
        {
            jb.posteriorMarginalAction();
        }
    }

    void PosteriorMarginalAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu2, posteriorMarginal,
                                            posteriorExpectation);
        if (activeItem == posteriorExpectation)
        {
            jb.posteriorExpectationAction();
        }
        else
        {
            jb.posteriorMarginalAction();
        }
    }

    void EstimateBestConfigurationAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu2, bestConfiguration,
                                            posteriorMarginal);
        if (activeItem == posteriorMarginal)
        {
            jb.posteriorMarginalAction();
        }
        else
        {
            jb.estimateBestConfigurationAction();
        }
    }

    void SensitivityAnalysisAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu2, sensitivityAnalysis,
                                            posteriorMarginal);
        if (activeItem == posteriorMarginal)
        {
            jb.posteriorMarginalAction();
        }
        else
        {
            jb.sensitivityAnalysisAction();
        }
    }

    void EstimateExplanationVariablesAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu2, explanation,
                                            posteriorMarginal);
        if (activeItem == posteriorMarginal)
        {
            jb.posteriorMarginalAction();
        }
        else
        {
            jb.estimateExplanationVariablesAction();
        }
    }

    void BifFormatAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu5, bifFormat, xmlFormat);
        if (activeItem == bifFormat)
        {
            jb.bifFormatAction();
        }
        else
        {
            jb.xmlFormatAction();
        }
    }

    void XmlFormatAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu5, xmlFormat, bifFormat);
        if (activeItem == xmlFormat)
        {
            jb.xmlFormatAction();
        }
        else
        {
            jb.bifFormatAction();
        }
    }

    void BugsFormatAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu5, bugsFormat, bifFormat);
        if (activeItem == bugsFormat)
        {
            jb.bugsFormatAction();
        }
        else
        {
            jb.bifFormatAction();
        }
    }

    void AlgorithmVariableEliminationAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu7,
                                            algorithmVariableElimination,
                                            algorithmBucketTree);
        if (activeItem == algorithmVariableElimination)
        {
            jb.setAlgorithmVariableElimination();
        }
        else
        {
            jb.setAlgorithmBucketTree();
        }
    }

    void AlgorithmBucketTreeAction()
    {
        CheckboxMenuItem activeItem =
                         updateCheckboxMenu(menu7, algorithmBucketTree,
                                            algorithmVariableElimination);
        if (activeItem == algorithmBucketTree)
        {
            jb.setAlgorithmBucketTree();
        }
        else
        {
            jb.setAlgorithmVariableElimination();
        }
    }

    void DumpConsoleToFileAction()
    {
        if (jb.isApplet)
        {
            textArea1.setText(appletInvalidOperation);
            return;
        }
        SaveFileDialog.setVisible(true);
        String filename = SaveFileDialog.getFile();
        if (filename == null)
        {
            return;
        }
        filename = SaveFileDialog.getDirectory() + filename;
        try
        {
            FileOutputStream fileout = new FileOutputStream(filename);
            PrintStream out = new PrintStream(fileout);
            String t = textArea1.getText();
            textArea1.setText("");
            out.print(t);
            out.close();
            fileout.close();
        }
        catch (IOException e)
        {
            appendText("Dump aborted: " + e + "\n");
            return;
        }
        appendText("\tConsole dumped.\n\n");
    }

    void ClearAction()
    {
        (new ClearDialog(this, jb, "Clear the Bayesian network?", true)).
                setVisible(true);
    }

    void SaveAction()
    {
        if (jb.isApplet)
        {
            appendText(appletInvalidOperation);
            return;
        }
        if (jb.getCurrentSaveFilename() == null)
        {
            SaveAsAction();
        }
        else
        {
            jb.save();
            appendText("\tFile saved.\n\n");
        }
    }

    void SaveAsAction()
    {
        if (jb.isApplet)
        {
            appendText(appletInvalidOperation);
            return;
        }
        SaveFileDialog.setVisible(true);
        String filename = SaveFileDialog.getFile();
        if (filename == null)
        {
            return;
        }
        filename = SaveFileDialog.getDirectory() + filename;
        if (jb.save(filename) == true)
        {
            appendText("\tFile saved.\n\n");
        }
        else
        {
            appendText("\tFile not saved correctly.\n\n");
        }
        jb.setCurrentSaveFilename(filename);
    }

    void OpenAction()
    {
        if (jb.isApplet)
        {
            textArea1.append(appletInvalidOperation);
            return;
        }
        OpenFileDialog.setVisible(true);
        String filename = OpenFileDialog.getFile();
        if (filename == null)
        {
            return;
        }
        filename = OpenFileDialog.getDirectory() + filename;
        if (jb.open(filename) == true)
        {
            appendText("\tFile loaded.\n\n");
        }
        else
        {
            appendText("\tFile not loaded correctly.\n\n");
        }
    }

    void OpenURL_Action()
    {
        (new OpenURLDialog(this, jb, "Insert URL of network", true)).setVisible(
                true);
    }

    void QuitAction()
    {
        (new QuitDialog(this, jb, "Quit JavaBayes?", false)).setVisible(true);
    }

    void AboutAction()
    {
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.aboutMessage);
    }

    /**
     * Override setVisible() so that Console does not superimpose EditorFrame
     *
     * @param show directly.
     */
    @Override
    public void setVisible(boolean show)
    {
        setLocation(50, 50);
        super.setVisible(show);
    }

    /**
     * Override action() to get events.
     *
     * @param event
     * @param arg
     * @return
     */
    @Override
    public boolean action(Event event, Object arg)
    {
        if (event.target instanceof MenuItem)
        {
            String label = (String) (((MenuItem) event.target).getLabel());
            switch (label)
            {
                case showBucketsTitle:
                    BucketTreeAction();
                    return true;
                case showBayesNetTitle:
                    BayesianNetworkAction();
                    return true;
                case posteriorExpectationTitle:
                    PosteriorExpectationAction();
                    return true;
                case posteriorMarginalTitle:
                    PosteriorMarginalAction();
                    return true;
                case bestConfigurationTitle:
                    EstimateBestConfigurationAction();
                    return true;
                case sensitivityAnalysisTitle:
                    SensitivityAnalysisAction();
                    return true;
                case explanationTitle:
                    EstimateExplanationVariablesAction();
                    return true;
                case bifSaveLabel:
                    BifFormatAction();
                    return true;
                case xmlSaveLabel:
                    XmlFormatAction();
                    return true;
                case bugsSaveLabel:
                    BugsFormatAction();
                    return true;
                case algorithmVariableEliminationTitle:
                    AlgorithmVariableEliminationAction();
                    return true;
                case algorithmBucketTreeTitle:
                    AlgorithmBucketTreeAction();
                    return true;
                case clearMenuitemTitle:
                    ClearAction();
                    return true;
                case dumpConsoleMenuitemTitle:
                    DumpConsoleToFileAction();
                    break;
                case saveMenuitemTitle:
                    SaveAction();
                    return true;
                case saveAsMenuitemTitle:
                    SaveAsAction();
                    return true;
                case openMenuitemTitle:
                    OpenAction();
                    return true;
            }
            switch (label)
            {
                case openUrlMenuitemTitle:
                    OpenURL_Action();
                    return true;
                case quitMenuitemTitle:
                    QuitAction();
                    return true;
                case aboutTitle:
                    AboutAction();
                    return true;
            }
        }
        return super.action(event, arg);
    }

    /**
     * Handle the possible destruction of the window.
     *
     * @param evt
     * @return
     */
    @Override
    public boolean handleEvent(Event evt)
    {
        if (evt.id == Event.WINDOW_DESTROY)
        {
            if (jb != null)
            {
                (new QuitDialog(this, jb, "Quit JavaBayes?", false)).setVisible(
                        true);
            }
        }
        return super.handleEvent(evt);
    }

    /**
     * Place text in the text area.
     *
     * @param text
     */
    public void appendText(String text)
    {
        textArea1.append(text);
    }

    /**
     * Create the "radiobutton" behavior for the checkbox menu items. It returns
     * the checkbox that got on.
     */
    private CheckboxMenuItem updateCheckboxMenu(Menu m, CheckboxMenuItem cur,
                                                CheckboxMenuItem def)
    {
        boolean s = cur.getState();

        if (s == false)
        { // If cur was on, then cur is still off and def is on.
            def.setState(true);
            return def;
        }
        else
        {           // If cur was off, then cur is on and all others are off.
            for (int i = 0; i < m.getItemCount(); i++)
            {
                // Set all menu items to off,
                ((CheckboxMenuItem) (m.getItem(i))).setState(false);
            }
            cur.setState(true); // then set cur back to on.
            return cur;
        }
    }
}
