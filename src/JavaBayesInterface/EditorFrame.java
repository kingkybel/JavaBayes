/*
 * EditorFrame.java
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

import BayesianInferences.ExplanationType;
import BayesianInferences.InferenceGraph;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Cursor;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class EditorFrame extends Frame
{

    // Constants
    /**
     *
     */
    final static public int BIF_FORMAT = 0;

    /**
     *
     */
    final static public int XML_FORMAT = 1;

    /**
     *
     */
    final static public int BUGS_FORMAT = 2;

    /**
     *
     */
    final static public int ALGORITHM_VARIABLE_ELIMINATION = 0;

    /**
     *
     */
    final static public int ALGORITHM_BUCKET_TREE = 1;

    // constants for caption text of all buttons
    /**
     *
     */
    public static final String createLabel = "Create";

    /**
     *
     */
    public static final String moveLabel = "Move";

    /**
     *
     */
    public static final String deleteLabel = "Delete";

    /**
     *
     */
    public static final String queryLabel = "Query";

    /**
     *
     */
    public static final String observeLabel = "Observe";

    /**
     *
     */
    public static final String editVariableLabel = "Edit Variable";

    /**
     *
     */
    public static final String editFunctionLabel = "Edit Function";

    /**
     *
     */
    public static final String editNetworkLabel = "Edit Network";
    private static final String CLASS_NAME = EditorFrame.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    JavaBayes jb;
    Panel cmdPanel;
    Panel editPanel;
    /**
     *
     */
    public ScrollingPanel scrollPanel;
    // Options (controlled by menus in JavaBayesConsoleFrame)
    ExplanationType modeMenuChoice = ExplanationType.MARGINAL_POSTERIOR;
    boolean whatToShowBayesianNetworkState = false;
    boolean whatToShowBucketTreeState = false;
    int saveFormat = BIF_FORMAT;
    private String currentSaveFilename;
    private int algorithmType = ALGORITHM_VARIABLE_ELIMINATION;

    /**
     * Default constructor for an EditorFrame.
     *
     * @param javaBayes
     * @param title
     */
    public EditorFrame(JavaBayes javaBayes, String title)
    {
        super(title);

        jb = javaBayes;

        scrollPanel = new ScrollingPanel(this);

        cmdPanel = new Panel();
        cmdPanel.setLayout(new GridLayout(1, 5));
        cmdPanel.add(new Button(createLabel));
        cmdPanel.add(new Button(moveLabel));
        cmdPanel.add(new Button(deleteLabel));
        cmdPanel.add(new Button(queryLabel));
        cmdPanel.add(new Button(observeLabel));

        editPanel = new Panel();
        editPanel.setLayout(new GridLayout(1, 3));
        editPanel.add(new Button(editVariableLabel));
        editPanel.add(new Button(editFunctionLabel));
        editPanel.add(new Button(editNetworkLabel));

        setLayout(new BorderLayout(0, 0));
        add("North", cmdPanel);
        add("Center", scrollPanel);
        add("South", editPanel);

        // Resize the frame.
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        d.width /= 2;
        d.height = d.height * 3 / 4;
        setSize(d);
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
        return (super.handleEvent(evt));
    }

    /**
     * Handle button events.
     *
     * @param evt
     * @param arg
     * @return
     */
    @Override
    public boolean action(Event evt, Object arg)
    {
        if (evt.target instanceof Button)
        {
            String label = ((Button) (evt.target)).getLabel();

            switch ((String) arg)
            {
                case createLabel:
                    scrollPanel.netPanel.setMode(label);
                    JavaBayesHelpMessages.show(
                            JavaBayesHelpMessages.createMessage);
                    setCursor(getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case moveLabel:
                    scrollPanel.netPanel.setMode(label);
                    JavaBayesHelpMessages.
                            show(JavaBayesHelpMessages.moveMessage);
                    setCursor(getPredefinedCursor(Cursor.MOVE_CURSOR));
                    break;
                case deleteLabel:
                    scrollPanel.netPanel.setMode(label);
                    JavaBayesHelpMessages.show(
                            JavaBayesHelpMessages.deleteMessage);
                    setCursor(getPredefinedCursor(Cursor.HAND_CURSOR));
                    break;
                case queryLabel:
                    setQueryMode();
                    break;
                case observeLabel:
                    setObserveMode();
                    break;
                case editVariableLabel:
                    setEditVariableMode();
                    break;
                case editFunctionLabel:
                    setEditFunctionMode();
                    break;
                case editNetworkLabel:
                    setEditNetworkMode();
                    break;
            }
        }
        return true;
    }

    /**
     * Open a file and read the network in it.
     *
     * @param filename
     * @return
     */
    public boolean open(String filename)
    {
        InferenceGraph inferenceGraph;

        try
        {
            if (jb.isApplet)
            {
                return (false);
            }
            else
            {
                jb.appendText("\nLoading " + filename + "\n");
                inferenceGraph = new InferenceGraph(filename);
            }
        }
        catch (Exception e)
        {
            jb.appendText(e + "\n");
            return (false);
        }

        // Put the network into the graphical interface
        setInferenceGraph(inferenceGraph);

        return (true);
    }

    /**
     * Open a URL and read the network in it.
     *
     * @param filename
     * @return
     */
    public boolean openUrl(String filename)
    {
        InferenceGraph ig;

        try
        {
            jb.appendText("\nLoading " + filename + "\n");
            ig = new InferenceGraph(new URL(filename));
        }
        catch (Exception e)
        {
            jb.appendText("Exception: " + e + "\n");
            return (false);
        }

        // Put the network into the graphical interface
        setInferenceGraph(ig);

        return (true);
    }

    /**
     * Save the network.
     *
     * @return
     */
    public boolean save()
    {
        return (save(currentSaveFilename));
    }

    /**
     * Save the network.
     *
     * @param filename
     * @return
     */
    public boolean save(String filename)
    {
        InferenceGraph ig = getInferenceGraph();

        if (filename == null)
        {
            jb.appendText("\n Filename invalid!");
            return (false);
        }

        if (ig == null)
        {
            jb.appendText("\n No Bayesian network to be saved.\n\n");
            return (false);
        }

        try
        {
            FileOutputStream fileout = new FileOutputStream(filename);
            PrintStream out = new PrintStream(fileout);
            switch (saveFormat)
            {
                case BIF_FORMAT:
                    ig.saveBif(out);
                    break;
                case XML_FORMAT:
                    ig.saveXml(out);
                    break;
                case BUGS_FORMAT:
                    ig.saveBugs(out);
                    break;
            }
            out.close();
            fileout.close();
        }
        catch (IOException e)
        {
            jb.appendText("Exception: " + e + "\n");
            return (false);
        }
        return (true);
    }

    /**
     * Clear the network screen.
     */
    public void clear()
    {
        scrollPanel.netPanel.clear();
    }

    /**
     * Process a query.
     *
     * @param ig
     * @param queriedVariable
     */
    public void processQuery(InferenceGraph ig, String queriedVariable)
    {
        // Check whether inference is possible
        if (ig == null)
        {
            jb.appendText("\nLoad Bayesian network.\n\n");
            return;
        }

        // This makes the whole inference
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream(bstream);

        // Print the Bayes net.
        if (whatToShowBayesianNetworkState)
        {
            printBayesNet(pstream, ig);
        }

        // Perform inference
        if (modeMenuChoice.isMarginalPosterior())
        {
            printMarginal(pstream, ig, queriedVariable);
        }
        else if (modeMenuChoice.isExpectation())
        {
            printExpectation(pstream, ig, queriedVariable);
        }
        else if (modeMenuChoice.isSubset())
        {
            printExplanation(pstream, ig);
        }
        else if (modeMenuChoice.isFull())
        {
            printFullExplanation(pstream, ig);
        }
        else if (modeMenuChoice.isSensitivityAnalysis())
        {
            printSensitivityAnalysis(pstream, ig);
        }

        // Print results to test window
        jb.appendText(bstream.toString());

        // Close streams
        try
        {
            bstream.close();
            pstream.close();
        }
        catch (IOException e)
        {
        }
    }

    /**
     * Print the QuasiBayesNet in the InferenceGraph.
     *
     * @param pstream
     * @param ig
     */
    protected void printBayesNet(PrintStream pstream, InferenceGraph ig)
    {
        ig.printBayesNet(pstream);
    }

    /**
     * Compute and print a posterior marginal distribution for the
     * InferenceGraph.
     *
     * @param pstream
     * @param queriedVariable
     * @param ig
     */
    protected void printMarginal(PrintStream pstream, InferenceGraph ig,
                                 String queriedVariable)
    {
        if (algorithmType == ALGORITHM_VARIABLE_ELIMINATION)
        {
            ig.printMarginal(pstream, queriedVariable, false,
                             whatToShowBucketTreeState);
        }
        else if (algorithmType == ALGORITHM_BUCKET_TREE)
        {
            ig.printMarginal(pstream, queriedVariable, true,
                             whatToShowBucketTreeState);
        }
    }

    /**
     * Compute and print a posterior expectation for the InferenceGraph.
     *
     * @param pstream
     * @param queriedVariable
     * @param ig
     */
    protected void printExpectation(PrintStream pstream, InferenceGraph ig,
                                    String queriedVariable)
    {
        if (algorithmType == ALGORITHM_VARIABLE_ELIMINATION)
        {
            ig.printExpectation(pstream, queriedVariable, false,
                                whatToShowBucketTreeState);
        }
        else if (algorithmType == ALGORITHM_BUCKET_TREE)
        {
            ig.printExpectation(pstream, queriedVariable, true,
                                whatToShowBucketTreeState);
        }
    }

    /**
     * Compute and print an explanation for the InferenceGraph.
     *
     * @param pstream
     * @param ig
     */
    protected void printExplanation(PrintStream pstream, InferenceGraph ig)
    {
        ig.printExplanation(pstream, whatToShowBucketTreeState);
    }

    /**
     * Compute and print a full explanation for the InferenceGraph.
     *
     * @param pstream
     * @param ig
     */
    protected void printFullExplanation(PrintStream pstream, InferenceGraph ig)
    {
        ig.printFullExplanation(pstream, whatToShowBucketTreeState);
    }

    /**
     * Compute and print the metrics for sensitivity analysis of the
     * InferenceGraph.
     *
     * @param pstream
     * @param ig
     */
    protected void printSensitivityAnalysis(PrintStream pstream,
                                            InferenceGraph ig)
    {
        ig.printSensitivityAnalysis(pstream);
    }

    /**
     * Get the InferenceGraph in the NetworkPanel.
     *
     * @return
     */
    public InferenceGraph getInferenceGraph()
    {
        return (scrollPanel.netPanel.getInferenceGraph());
    }

    /**
     * Load an InferenceGraph.
     *
     * @param ig
     */
    public void setInferenceGraph(InferenceGraph ig)
    {
        scrollPanel.netPanel.load(ig);
    }

    /**
     * Interact with menu options: observe variables.
     */
    public void setObserveMode()
    {
        setCursor(getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        scrollPanel.netPanel.setMode(observeLabel);
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.observeMessage);
    }

    /**
     * Interact with menu options: edit variable.
     */
    public void setEditVariableMode()
    {
        setCursor(getPredefinedCursor(Cursor.TEXT_CURSOR));
        scrollPanel.netPanel.setMode(editVariableLabel);
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.editMessage);
    }

    /**
     * Interact with menu options: edit function.
     */
    public void setEditFunctionMode()
    {
        setCursor(getPredefinedCursor(Cursor.TEXT_CURSOR));
        scrollPanel.netPanel.setMode(editFunctionLabel);
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.editMessage);
    }

    /**
     * Interact with menu options: edit network.
     */
    public void setEditNetworkMode()
    {
        scrollPanel.netPanel.editNetwork();
    }

    /**
     * Interact with menu options: queries are processed.
     */
    public void setQueryMode()
    {
        setCursor(getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        scrollPanel.netPanel.setMode(queryLabel);
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.queryMessage);
    }

    /**
     * Return the mode.
     *
     * @return
     */
    public ExplanationType getMode()
    {
        return (modeMenuChoice);
    }

    /**
     * Get the current filename for saving.
     *
     * @return
     */
    public String getCurrentSaveFilename()
    {
        return (currentSaveFilename);
    }

    /**
     * Set the current filename for saving.
     *
     * @param csf
     */
    public void setCurrentSaveFilename(String csf)
    {
        currentSaveFilename = csf;
    }

    /**
     * Interact with menu options: whether to show BucketTree.
     *
     * @param whatToShowBucketTree
     */
    public void whatToShowBucketTreeAction(boolean whatToShowBucketTree)
    {
        whatToShowBucketTreeState = whatToShowBucketTree;
    }

    /**
     * Interact with menu options: whether to show bayesian networks.
     *
     *
     * @param whatToShowBayesianNetwork
     */
    public void whatToShowBayesianNetworkAction(
            boolean whatToShowBayesianNetwork)
    {
        whatToShowBayesianNetworkState =
        whatToShowBayesianNetwork;
    }

    /**
     * Inferences produce expectations.
     */
    public void posteriorExpectationAction()
    {
        modeMenuChoice = ExplanationType.EXPECTATION;
        scrollPanel.netPanel.repaint();
    }

    /**
     * Inferences produce posterior marginals.
     */
    public void posteriorMarginalAction()
    {
        modeMenuChoice = ExplanationType.MARGINAL_POSTERIOR;
        scrollPanel.netPanel.repaint();
    }

    /**
     * Estimate explanation variables.
     */
    public void estimateExplanationVariablesAction()
    {
        modeMenuChoice = ExplanationType.SUBSET;
        scrollPanel.netPanel.repaint();
    }

    /**
     * Produce the estimates for the best configuration.
     */
    public void estimateBestConfigurationAction()
    {
        modeMenuChoice = ExplanationType.FULL;
        scrollPanel.netPanel.repaint();
    }

    /**
     * Produce the metrics for sensitivity analysis.
     */
    public void sensitivityAnalysisAction()
    {
        modeMenuChoice = ExplanationType.SENSITIVITY_ANALYSIS;
        scrollPanel.netPanel.repaint();
    }

    /**
     * Set the format for saving.
     *
     * @param sf
     */
    public void setSaveFormat(int sf)
    {
        saveFormat = sf;
    }

    /**
     * Set the algorithm type.
     *
     * @param type
     */
    public void setAlgorithm(int type)
    {
        algorithmType = type;
    }
}
