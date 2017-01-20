/*
 * JavaBayes.java
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

import java.applet.Applet;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class JavaBayes extends Applet
{

    private static final Class CLAZZ = JavaBayes.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Main method for JavaBayes.
     *
     * @param argv
     */
    public static void main(String argv[])
    {
        JavaBayes jb = new JavaBayes();
        jb.construct(false);
        if (argv.length > 0)
        {
            String filename = argv[0];
            System.out.println(filename);
            jb.open(filename);
        }
    }

    // Graphical elements of JavaBayes
    EditorFrame editorFrame;
    JavaBayesConsoleFrame consoleFrame;

    /**
     *
     */
    public boolean isApplet = false;

    /**
     * Init method for JavaBayes operating as an applet.
     */
    @Override
    public void init()
    {
        construct(true);
    }

    /**
     * Stop method for JavaBayes operating as an applet.
     */
    @Override
    public void stop()
    {
        quit();
    }

    /**
     * Do all the initializations for a JavaBayes object.
     */
    private void construct(boolean ia)
    {
        isApplet = ia;

        editorFrame = new EditorFrame(this, "JavaBayes Editor");
        editorFrame.setVisible(true);
        consoleFrame = new JavaBayesConsoleFrame(this, "JavaBayes Console");
        consoleFrame.setVisible(true);
        JavaBayesHelpMessages.insert(this);
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.aboutMessage);
        JavaBayesHelpMessages.show(JavaBayesHelpMessages.startMessage);
    }

    /**
     * Open a file and read the network in it.
     *
     * @param filename the file where the network is described
     * @return true if successful, false otherwise
     */
    public boolean open(String filename)
    {
        return editorFrame.open(filename);
    }

    /**
     * Open a URL and read the network in it.
     *
     * @param filename the file where the network is described
     * @return true if successful, false otherwise
     */
    public boolean openUrl(String filename)
    {
        return editorFrame.openUrl(filename);
    }

    /**
     * Clear the network.
     */
    public void clear()
    {
        editorFrame.clear();
        setCurrentSaveFilename(null);
    }

    /**
     * Save the network.
     *
     * @return true if successful, false otherwise
     */
    public boolean save()
    {
        return editorFrame.save();
    }

    /**
     * Save the network.
     *
     * @param filename
     * @return true if successful, false otherwise
     */
    public boolean save(String filename)
    {
        return editorFrame.save(filename);
    }

    /**
     * Interact with menu options: whether to show BucketTree.
     *
     * @param whatToShowBucketTree true if bucket tree should be shown, false
     *                             otherwise
     */
    public void whatToShowBucketTreeAction(boolean whatToShowBucketTree)
    {
        editorFrame.whatToShowBucketTreeAction(whatToShowBucketTree);
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
        editorFrame.whatToShowBayesianNetworkAction(
                whatToShowBayesianNetwork);
    }

    /**
     * Inferences produce expectations.
     */
    public void posteriorExpectationAction()
    {
        editorFrame.posteriorExpectationAction();
    }

    /**
     * Inferences produce posterior marginals.
     */
    public void posteriorMarginalAction()
    {
        editorFrame.posteriorMarginalAction();
    }

    /**
     * Estimate explanation variables.
     */
    public void estimateExplanationVariablesAction()
    {
        editorFrame.estimateExplanationVariablesAction();
    }

    /**
     * Produce the estimates for the best configuration.
     */
    public void estimateBestConfigurationAction()
    {
        editorFrame.estimateBestConfigurationAction();
    }

    /**
     * Produce sensitivity analysis.
     */
    public void sensitivityAnalysisAction()
    {
        editorFrame.sensitivityAnalysisAction();
    }

    /**
     * Use bif format for saving.
     */
    public void bifFormatAction()
    {
        editorFrame.setSaveFormat(EditorFrame.BIF_FORMAT);
    }

    /**
     * Use XML format for saving.
     */
    public void xmlFormatAction()
    {
        editorFrame.setSaveFormat(EditorFrame.XML_FORMAT);
    }

    /**
     * Use bugs format for saving.
     */
    public void bugsFormatAction()
    {
        editorFrame.setSaveFormat(EditorFrame.BUGS_FORMAT);
    }

    /**
     * Quit gracefully.
     */
    public void quit()
    {
        if (isApplet)
        {
            editorFrame.setVisible(false);
            editorFrame.dispose();
            consoleFrame.setVisible(false);
            consoleFrame.dispose();
        }
        else
        {
            System.exit(0);
        }
    }

    /**
     * Put text in the consoleFrame.
     *
     * @param text text to add to the console frame
     */
    public void appendText(String text)
    {
        consoleFrame.appendText(text);
    }

    /**
     * Get the current filename for saving.
     *
     * @return the current filename
     */
    public String getCurrentSaveFilename()
    {
        return editorFrame.getCurrentSaveFilename();
    }

    /**
     * Set the current filename for saving.
     *
     * @param filename the new filename
     */
    public void setCurrentSaveFilename(String filename)
    {
        editorFrame.setCurrentSaveFilename(filename);
    }

    /**
     * Set the inference algorithm as variable elimination.
     */
    public void setAlgorithmVariableElimination()
    {
        editorFrame.setAlgorithm(EditorFrame.ALGORITHM_VARIABLE_ELIMINATION);
    }

    /**
     * Set the inference algorithm as bucket tree.
     */
    public void setAlgorithmBucketTree()
    {
        editorFrame.setAlgorithm(EditorFrame.ALGORITHM_BUCKET_TREE);
    }

}
