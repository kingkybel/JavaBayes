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

    private static final Class CLAZZ = EditorFrame.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Enumeration of formats in which to save the net.
     */
    public enum SaveFormat
    {

        /**
         * Bayes interchange format.
         */
        BIF_FORMAT,
        /**
         * XML representation.
         */
        XML_FORMAT,
        /**
         * BUGS save format.
         */
        BUGS_FORMAT
    }

    /**
     * Enumeration of the algorithms available.
     */
    public enum AlgorithmType
    {

        /**
         * Variable elimination
         */
        VARIABLE_ELIMINATION,
        /**
         * Bucket tree algorithm.
         */
        BUCKET_TREE
    }

    // constants for caption text of all buttons
    static final String createLabel = "Create";
    static final String moveLabel = "Move";
    static final String deleteLabel = "Delete";
    static final String queryLabel = "Query";
    static final String observeLabel = "Observe";
    static final String editVariableLabel = "Edit Variable";
    static final String editFunctionLabel = "Edit Function";
    private static final String editNetworkLabel = "Edit Network";
    JavaBayes javaBayes;
    Panel cmdPanel;
    Panel editPanel;
    /**
     * The scroll panel for the graph.
     */
    public ScrollingPanel scrollPanel;
    // Options (controlled by menus in JavaBayesConsoleFrame)
    ExplanationType modeMenuChoice = ExplanationType.MARGINAL_POSTERIOR;
    boolean whatToShowBayesianNetworkState = false;
    boolean whatToShowBucketTreeState = false;
    SaveFormat saveFormat = SaveFormat.BIF_FORMAT;
    private String currentSaveFilename;
    private AlgorithmType algorithmType = AlgorithmType.VARIABLE_ELIMINATION;

    /**
     * Default constructor for an EditorFrame.
     *
     * @param javaBayes back-pointer to the main class
     * @param title     title of the frame
     */
    public EditorFrame(JavaBayes javaBayes, String title)
    {
        super(title);

        this.javaBayes = javaBayes;

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

    @Override
    public boolean handleEvent(Event evt)
    {
        if (evt.id == Event.WINDOW_DESTROY)
        {
            if (javaBayes != null)
            {
                (new QuitDialog(this, javaBayes, "Quit JavaBayes?", false)).setVisible(
                        true);
            }
        }
        return super.handleEvent(evt);
    }

    @Override
    public boolean action(Event evt, Object arg)
    {
        if (evt.target instanceof Button)
        {
            String label = ((Button) evt.target).getLabel();

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
     * @param filename name of the file describing the network
     * @return true if successful, false otherwise
     */
    public boolean open(String filename)
    {
        InferenceGraph inferenceGraph;

        try
        {
            if (javaBayes.isApplet)
            {
                return false;
            }
            else
            {
                javaBayes.appendText("\nLoading " + filename + "\n");
                inferenceGraph = new InferenceGraph(filename);
            }
        }
        catch (Exception e)
        {
            javaBayes.appendText(e + "\n");
            return false;
        }

        // Put the network into the graphical interface
        setInferenceGraph(inferenceGraph);

        return true;
    }

    /**
     * Open a URL and read the network in it.
     *
     * @param filename name of the file describing the network
     * @return true if successful, false otherwise
     */
    public boolean openUrl(String filename)
    {
        InferenceGraph ig;

        try
        {
            javaBayes.appendText("\nLoading " + filename + "\n");
            ig = new InferenceGraph(new URL(filename));
        }
        catch (Exception e)
        {
            javaBayes.appendText("Exception: " + e + "\n");
            return false;
        }

        // Put the network into the graphical interface
        setInferenceGraph(ig);

        return true;
    }

    /**
     * Save the network.
     *
     * @return true if successful, false otherwise
     */
    public boolean save()
    {
        return save(currentSaveFilename);
    }

    /**
     * Save the network.
     *
     * @param filename name of the file describing the network
     * @return true if successful, false otherwise
     */
    public boolean save(String filename)
    {
        InferenceGraph ig = getInferenceGraph();

        if (filename == null)
        {
            javaBayes.appendText("\n Filename invalid!");
            return false;
        }

        if (ig == null)
        {
            javaBayes.appendText("\n No Bayesian network to be saved.\n\n");
            return false;
        }

        try (FileOutputStream fileout = new FileOutputStream(filename);
             PrintStream out = new PrintStream(fileout))
        {
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
        }
        catch (IOException e)
        {
            javaBayes.appendText("Exception: " + e + "\n");
            return false;
        }

        return true;
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
     * @param inferenceGraph  the underlying inference graph
     * @param queriedVariable name of the queried variable
     */
    public void processQuery(InferenceGraph inferenceGraph,
                             String queriedVariable)
    {
        // Check whether inference is possible
        if (inferenceGraph == null)
        {
            javaBayes.appendText("\nLoad Bayesian network.\n\n");
            return;
        }

        // This makes the whole inference
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream(bstream);

        // Print the Bayes net.
        if (whatToShowBayesianNetworkState)
        {
            printBayesNet(pstream, inferenceGraph);
        }

        // Perform inference
        if (modeMenuChoice.isMarginalPosterior())
        {
            printMarginal(pstream, inferenceGraph, queriedVariable);
        }
        else if (modeMenuChoice.isExpectation())
        {
            printExpectation(pstream, inferenceGraph, queriedVariable);
        }
        else if (modeMenuChoice.usesMarkedVariablesOnly())
        {
            printExplanation(pstream, inferenceGraph);
        }
        else if (modeMenuChoice.usesAllNotObservedVariables())
        {
            printFullExplanation(pstream, inferenceGraph);
        }
        else if (modeMenuChoice.isSensitivityAnalysis())
        {
            printSensitivityAnalysis(pstream, inferenceGraph);
        }

        // Print results to test window
        javaBayes.appendText(bstream.toString());

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
     * @param out            output print stream
     * @param inferenceGraph the underlying inference graph
     */
    protected void printBayesNet(PrintStream out, InferenceGraph inferenceGraph)
    {
        inferenceGraph.printBayesNet(out);
    }

    /**
     * Compute and print a posterior marginal distribution for the
     * InferenceGraph.
     *
     * @param out             output print stream
     * @param queriedVariable name of the queried variable
     * @param inferenceGraph  the underlying inference graph
     */
    protected void printMarginal(PrintStream out,
                                 InferenceGraph inferenceGraph,
                                 String queriedVariable)
    {
        if (algorithmType == AlgorithmType.VARIABLE_ELIMINATION)
        {
            inferenceGraph.printMarginal(out,
                                         queriedVariable,
                                         false,
                                         whatToShowBucketTreeState);
        }
        else if (algorithmType == AlgorithmType.BUCKET_TREE)
        {
            inferenceGraph.printMarginal(out,
                                         queriedVariable,
                                         true,
                                         whatToShowBucketTreeState);
        }
    }

    /**
     * Compute and print a posterior expectation for the InferenceGraph.
     *
     * @param out             output print stream
     * @param queriedVariable name of the queried variable
     * @param inferenceGraph  the underlying inference graph
     */
    protected void printExpectation(PrintStream out,
                                    InferenceGraph inferenceGraph,
                                    String queriedVariable)
    {
        if (algorithmType == AlgorithmType.VARIABLE_ELIMINATION)
        {
            inferenceGraph.printExpectation(out, queriedVariable, false,
                                            whatToShowBucketTreeState);
        }
        else if (algorithmType == AlgorithmType.BUCKET_TREE)
        {
            inferenceGraph.printExpectation(out, queriedVariable, true,
                                            whatToShowBucketTreeState);
        }
    }

    /**
     * Compute and print an explanation for the InferenceGraph.
     *
     * @param out            output print stream
     * @param inferenceGraph the underlying inference graph
     */
    protected void printExplanation(PrintStream out,
                                    InferenceGraph inferenceGraph)
    {
        inferenceGraph.printExplanation(out, whatToShowBucketTreeState);
    }

    /**
     * Compute and print a full explanation for the InferenceGraph.
     *
     * @param out            output print stream
     * @param inferenceGraph the underlying inference graph
     */
    protected void printFullExplanation(PrintStream out,
                                        InferenceGraph inferenceGraph)
    {
        inferenceGraph.printFullExplanation(out, whatToShowBucketTreeState);
    }

    /**
     * Compute and print the metrics for sensitivity analysis of the
     * InferenceGraph.
     *
     * @param out            output print stream
     * @param inferenceGraph the underlying inference graph
     */
    protected void printSensitivityAnalysis(PrintStream out,
                                            InferenceGraph inferenceGraph)
    {
        inferenceGraph.printSensitivityAnalysis(out);
    }

    /**
     * Get the InferenceGraph in the NetworkPanel.
     *
     * @return the InferenceGraph
     */
    public InferenceGraph getInferenceGraph()
    {
        return scrollPanel.netPanel.getInferenceGraph();
    }

    /**
     * Load an InferenceGraph.
     *
     * @param inferenceGraph the new inference graph
     */
    public void setInferenceGraph(InferenceGraph inferenceGraph)
    {
        scrollPanel.netPanel.load(inferenceGraph);
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
     * @return the mode
     */
    public ExplanationType getMode()
    {
        return modeMenuChoice;
    }

    /**
     * Get the current filename for saving.
     *
     * @return the current filename
     */
    public String getCurrentSaveFilename()
    {
        return currentSaveFilename;
    }

    /**
     * Set the current filename for saving.
     *
     * @param currentSaveFilename new save filename
     */
    public void setCurrentSaveFilename(String currentSaveFilename)
    {
        this.currentSaveFilename = currentSaveFilename;
    }

    /**
     * Interact with menu options: whether to show BucketTree.
     *
     * @param whatToShowBucketTree true if bucket tree should be shown, false
     *                             otherwise
     */
    public void whatToShowBucketTreeAction(boolean whatToShowBucketTree)
    {
        whatToShowBucketTreeState = whatToShowBucketTree;
    }

    /**
     * Interact with menu options: whether to show Bayesian networks.
     *
     * @param whatToShowBayesianNetwork true if Bayes net should be shown, false
     *                                  otherwise
     */
    public void whatToShowBayesianNetworkAction(
            boolean whatToShowBayesianNetwork)
    {
        whatToShowBayesianNetworkState = whatToShowBayesianNetwork;
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
        modeMenuChoice = ExplanationType.MARKED_VARIABLES_ONLY;
        scrollPanel.netPanel.repaint();
    }

    /**
     * Produce the estimates for the best configuration.
     */
    public void estimateBestConfigurationAction()
    {
        modeMenuChoice = ExplanationType.ALL_NOT_OBSERVED_VARIABLES;
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
     * @param saveFormat format to save the net in
     */
    public void setSaveFormat(SaveFormat saveFormat)
    {
        this.saveFormat = saveFormat;
    }

    /**
     * Set the algorithm type.
     *
     * @param algorithmType the new algorithm type
     */
    public void setAlgorithm(AlgorithmType algorithmType)
    {
        this.algorithmType = algorithmType;
    }
}
