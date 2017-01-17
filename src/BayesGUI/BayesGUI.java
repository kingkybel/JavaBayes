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

import BayesianInferences.ExplanationType;
import BayesianInferences.InferenceGraph;
import BayesianInferences.InferenceGraphNode;
import QuasiBayesianNetworks.GlobalNeighbourhood;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Dieter J Kybelksties
 */
public class BayesGUI extends javax.swing.JFrame
{

    private static final Class<BayesGUI> CLAZZ = BayesGUI.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final String RESULT = "result";
    private static final String QUERY = "query";
    Style metaStyle;
    Style queryStyle;
    Style resultStyle;
    Style errorStyle;
    StyleContext styleContext = new StyleContext();
    StyledDocument doc;
    String bayesNetModelFile = "";

    ExplanationType getCalculationType()
    {
        return (ExplanationType) calculationTypeComboBox.getSelectedItem();
    }

    boolean isBayesNetDisplay()
    {
        return bayesNetRadioButton.isSelected();
    }

    boolean isComputingClusters()
    {
        return computeClustersCheckBox.isSelected();
    }

    boolean isBucketTree()
    {
        return bucketTreeRadioButton.isSelected();
    }

    void setNetworkNameInput(String name)
    {
        networkNameInput.setText(name);
    }

    void setGlobalNeighbourhood(GlobalNeighbourhood globalNeighborhoodType)
    {
        globalNeighbourhoodComboBox.setSelectedItem(globalNeighborhoodType);
    }

    void setProperties(ArrayList<String> networkProperties)
    {
        propertiesTable.setModel(new PropertiesTableModel(networkProperties));
    }

    void setGlobalParameter(double paramValue)
    {
        globalNeighbourhoodParameterSpinner.setValue(paramValue);
    }

    // Network editing modes.
    enum EditMode
    {

        CREATE,
        MOVE,
        DELETE,
        OBSERVE,
        QUERY,
        EDIT_VARIABLE,
        EDIT_FUNCTION,
        EDIT_NETWORK;

        @Override
        public String toString()
        {
            return this == CREATE ? "Create New Bayes Net" :
                   this == MOVE ? "Move Elements" :
                   this == DELETE ? "Delete Node/Arc" :
                   this == OBSERVE ? "Set observed variables" :
                   this == QUERY ? "Query the Bayes Net" :
                   this == EDIT_VARIABLE ? "Edit variables" :
                   this == EDIT_FUNCTION ? "Edit functions" : "<UNKNOWN>";
        }
    }
    EditMode mode = EditMode.MOVE;
    boolean newArc = false;
    Point newArcHead = null;
    boolean modifyGroup = false;
    InferenceGraphNode movenode = null;
    InferenceGraphNode arcbottomnode = null;
    InferenceGraphNode archeadnode = null;
    int xScroll;
    int yScroll;
    InferenceGraph ig;
    BayesPanel bayesPanel;

    private final Font roman = new Font("TimesRoman", Font.BOLD, 12);
    private final Font helvetica = new Font("Helvetica", Font.BOLD, 15);
    private final FontMetrics fmetrics = getFontMetrics(roman);
    private final int h = (int) fmetrics.getHeight() / 3;

    OutputPanel outputPanel;

    /**
     * Creates new form BayesGUI.
     */
    public BayesGUI()
    {
        initComponents();
        displayButtonGroup.add(bayesNetRadioButton);
        displayButtonGroup.add(bucketTreeRadioButton);

        algorithmButtonGroup.add(variableEliminationRadioButton);
        algorithmButtonGroup.add(junctionTreeRadioButton);

        addWindowListener(new CloseListener());
        outputPanel = new OutputPanel();
        mainSplitPane.setRightComponent(outputPanel);
        doc = outputPanel.getStyledDocument();

        bayesPanel = new BayesPanel(this);
        graphPanel.add(bayesPanel);
        graphPanel.setViewportView(bayesPanel);

        try
        {
            outputPanel.addStyle(QUERY,
                                 Color.MAGENTA,
                                 null,
                                 null,
                                 10,
                                 true,
                                 false);
            outputPanel.addStyle(RESULT,
                                 Color.BLUE,
                                 null,
                                 null,
                                 10,
                                 true,
                                 false);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        outputPanel.writelnMeta("Started Bayes GUI");
    }

    final void metaOutput(String message)
    {
        if (verboseCheckBox.isSelected())
        {
            outputPanel.writelnMeta(message);
        }
    }

    final void queryOutput(String message)
    {
        outputPanel.writeln(QUERY, message);
    }

    final void resultOutput(String message)
    {
        outputPanel.writeln(RESULT, message);
    }

    final void errorOutput(String message)
    {
        outputPanel.writelnError(message);
    }

    /**
     * This method is called from within the constructor to initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        displayButtonGroup = new javax.swing.ButtonGroup();
        algorithmButtonGroup = new javax.swing.ButtonGroup();
        mainSplitPane = new javax.swing.JSplitPane();
        mainTabbedPane = new javax.swing.JTabbedPane();
        graphPanel = new javax.swing.JScrollPane();
        optionPanel = new javax.swing.JPanel();
        bucketTreeRadioButton = new javax.swing.JRadioButton();
        bayesNetRadioButton = new javax.swing.JRadioButton();
        variableEliminationRadioButton = new javax.swing.JRadioButton();
        junctionTreeRadioButton = new javax.swing.JRadioButton();
        calculationTypeComboBox = new javax.swing.JComboBox();
        verboseCheckBox = new javax.swing.JCheckBox();
        computeClustersCheckBox = new javax.swing.JCheckBox();
        networkNameInput = new javax.swing.JTextField();
        networkNameLabel = new javax.swing.JLabel();
        globalNeighbourhoodComboBox = new javax.swing.JComboBox();
        globalNeighbourhoodLabel = new javax.swing.JLabel();
        globalNeighbourhoodParameterSpinner = new javax.swing.JSpinner();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertiesTable = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadBayesMenuItem = new javax.swing.JMenuItem();
        loadFromURLMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        dumpConsoleMenuItem = new javax.swing.JMenuItem();
        fileSeparator = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem(new ExitAction());
        editMenu = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bayes GUI");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        mainTabbedPane.setPreferredSize(new java.awt.Dimension(500, 400));
        mainTabbedPane.addTab("Graph", graphPanel);

        optionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        optionPanel.setLayout(new java.awt.GridBagLayout());

        bucketTreeRadioButton.setSelected(true);
        bucketTreeRadioButton.setText("Bucket Tree");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 1, 0, 0);
        optionPanel.add(bucketTreeRadioButton, gridBagConstraints);

        bayesNetRadioButton.setText("Bayes Net");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 0, 0);
        optionPanel.add(bayesNetRadioButton, gridBagConstraints);

        variableEliminationRadioButton.setSelected(true);
        variableEliminationRadioButton.setText("Variable elimination");
        variableEliminationRadioButton.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 14;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        optionPanel.add(variableEliminationRadioButton, gridBagConstraints);

        junctionTreeRadioButton.setText("Junction tree");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 14;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 45;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionPanel.add(junctionTreeRadioButton, gridBagConstraints);

        calculationTypeComboBox.setModel(new DefaultComboBoxModel<>(ExplanationType.validChoices())
        );
        calculationTypeComboBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                calculationTypeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 94;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 158;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 0, 1);
        optionPanel.add(calculationTypeComboBox, gridBagConstraints);

        verboseCheckBox.setSelected(true);
        verboseCheckBox.setText("Verbose output");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 35;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        optionPanel.add(verboseCheckBox, gridBagConstraints);

        computeClustersCheckBox.setText("Compute Clusters");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 1, 0, 0);
        optionPanel.add(computeClustersCheckBox, gridBagConstraints);

        networkNameInput.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                networkNameInputActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 21;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 246;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 4, 0, 0);
        optionPanel.add(networkNameInput, gridBagConstraints);

        networkNameLabel.setText("Network Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 11, 0, 0);
        optionPanel.add(networkNameLabel, gridBagConstraints);

        globalNeighbourhoodComboBox.setModel(new DefaultComboBoxModel(GlobalNeighbourhood.values()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 34;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 168;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 14, 0, 0);
        optionPanel.add(globalNeighbourhoodComboBox, gridBagConstraints);

        globalNeighbourhoodLabel.setText("Global Neighbourhood Model:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        optionPanel.add(globalNeighbourhoodLabel, gridBagConstraints);

        globalNeighbourhoodParameterSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 57;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 36;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 48;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        optionPanel.add(globalNeighbourhoodParameterSpinner, gridBagConstraints);

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        propertiesScrollPane.setViewportView(propertiesTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 93;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 478;
        gridBagConstraints.ipady = 104;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 1, 0, 0);
        optionPanel.add(propertiesScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.ipadx = 269;
        gridBagConstraints.ipady = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 1, 0, 0);
        optionPanel.add(jSeparator1, gridBagConstraints);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 9;
        gridBagConstraints.ipady = 59;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 24, 0, 0);
        optionPanel.add(jSeparator2, gridBagConstraints);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 7;
        gridBagConstraints.ipady = 59;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 8, 0, 0);
        optionPanel.add(jSeparator3, gridBagConstraints);

        mainTabbedPane.addTab("Options", optionPanel);

        mainSplitPane.setTopComponent(mainTabbedPane);

        getContentPane().add(mainSplitPane);

        fileMenu.setText("File");

        loadBayesMenuItem.setText("Open BayesNet...");
        loadBayesMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                loadBayesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadBayesMenuItem);

        loadFromURLMenuItem.setText("Open BayesNet from URL...");
        fileMenu.add(loadFromURLMenuItem);

        saveMenuItem.setText("Save...");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        dumpConsoleMenuItem.setText("Dump Console...");
        dumpConsoleMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dumpConsoleMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(dumpConsoleMenuItem);
        fileMenu.add(fileSeparator);

        quitMenuItem.setText("Quit");
        fileMenu.add(quitMenuItem);

        mainMenuBar.add(fileMenu);

        editMenu.setText("Edit");

        jMenuItem6.setText("New");
        editMenu.add(jMenuItem6);

        mainMenuBar.add(editMenu);

        helpMenu.setText("Help");
        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadBayesMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadBayesMenuItemActionPerformed
    {//GEN-HEADEREND:event_loadBayesMenuItemActionPerformed
        final JFileChooser fc = new JFileChooser(bayesNetModelFile);
        fc.setCurrentDirectory(
                new File(
                        "/home/kybelksd/NetBeansProjects/JavaBayes2/src/Examples"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.addChoosableFileFilter(
                new FileNameExtensionFilter(
                        "Bayes Model Files (*.bif, *.gif, *.mat, *.xml, *.dnet, *.xml03)",
                        "BIF",
                        "GIF",
                        "MAT",
                        "XML",
                        "dnet",
                        "xml03"));
        int reval = fc.showOpenDialog(null);
        if (reval == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            bayesNetModelFile = file.getAbsolutePath();
        }
        try
        {
            metaOutput("Loading Bayesian Network from file " + bayesNetModelFile);
            ig = new InferenceGraph(bayesNetModelFile);
            metaOutput("Successful");
            bayesPanel.load(ig);
        }
        catch (Exception ex)
        {
            errorOutput("Failed:" + ex);
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_loadBayesMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveMenuItemActionPerformed
    {//GEN-HEADEREND:event_saveMenuItemActionPerformed
        doSave(false);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void doSave(boolean saveAs) throws HeadlessException
    {
        InferenceGraph inferenceGraphToSave = bayesPanel.getInferenceGraph();
        if (inferenceGraphToSave == null)
        {
            errorOutput("No Bayesian network to be saved.");
            return;
        }
        String[] validSaveExtensions = new String[]
         {
             "bif", "bugs", "xml03"
        };
        String typeStr = validSaveExtensions[0];
        if (saveAs || bayesNetModelFile == null || bayesNetModelFile.isEmpty())
        {
            if (!bayesNetModelFile.isEmpty() && bayesNetModelFile.contains("."))
            {
                typeStr = bayesNetModelFile.substring(
                bayesNetModelFile.indexOf('.') + 1);
            }
            final JFileChooser fc = new JFileChooser(bayesNetModelFile);
            fc.setCurrentDirectory(
                    new File(
                            "/home/kybelksd/NetBeansProjects/JavaBayes2/src/Examples"));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            for (String ext : validSaveExtensions)
            {
                FileFilter ff = new FileNameExtensionFilter("*." + ext,
                                                            ext.toUpperCase());
                fc.addChoosableFileFilter(ff);
                if (typeStr.equals(ext))
                {
                    fc.setFileFilter(ff);
                }

            }
            fc.setAcceptAllFileFilterUsed(false);
            int reval = fc.showSaveDialog(null);
            if (reval == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();
                bayesNetModelFile = file.getAbsolutePath();
            }
            else
            {
                return;
            }
            typeStr = fc.getFileFilter().getDescription();
        }
        typeStr = typeStr.substring(typeStr.lastIndexOf('.') + 1);
        if (bayesNetModelFile.contains(".") &&
            !bayesNetModelFile.endsWith("." + typeStr))
        {
            bayesNetModelFile =
            bayesNetModelFile.substring(0,
                                        bayesNetModelFile.
                                        lastIndexOf('.') + 1) + typeStr;
        }
        if (!bayesNetModelFile.endsWith("." + typeStr))
        {
            bayesNetModelFile += "." + typeStr;
        }
        try (
                FileOutputStream fileout = new FileOutputStream(
                                         bayesNetModelFile);
                PrintStream out = new PrintStream(fileout))
        {
            switch (typeStr)
            {
                case "bif":
                    inferenceGraphToSave.saveBif(out);
                    break;
                case "xml03":
                    inferenceGraphToSave.saveXml(out);
                    break;
                case "bugs":
                    inferenceGraphToSave.saveBugs(out);
                    break;
            }
            metaOutput("Saved Bayes net to file '" + bayesNetModelFile + "'");
        }
        catch (IOException e)
        {
            errorOutput("Exception: " + e + "\n");
        }
    }

    private void calculationTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_calculationTypeComboBoxActionPerformed
    {//GEN-HEADEREND:event_calculationTypeComboBoxActionPerformed
        ///ig.getBayesNet().set
    }//GEN-LAST:event_calculationTypeComboBoxActionPerformed

    private void networkNameInputActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_networkNameInputActionPerformed
    {//GEN-HEADEREND:event_networkNameInputActionPerformed
        ig.setName(networkNameInput.getText());
    }//GEN-LAST:event_networkNameInputActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsMenuItemActionPerformed
    {//GEN-HEADEREND:event_saveAsMenuItemActionPerformed
        doSave(true);
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void dumpConsoleMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dumpConsoleMenuItemActionPerformed
    {//GEN-HEADEREND:event_dumpConsoleMenuItemActionPerformed
        final JFileChooser fc = new JFileChooser(bayesNetModelFile);
        fc.setCurrentDirectory(
                new File(
                        "/home/kybelksd/NetBeansProjects/JavaBayes2/src/Examples"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);
        int reval = fc.showSaveDialog(null);
        String dumpFile;
        if (reval == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            dumpFile = file.getAbsolutePath();
        }
        else
        {
            return;
        }
        if (dumpFile == null || dumpFile.isEmpty())
        {
            return;
        }
        StyledDocument docToDump = outputPanel.getStyledDocument();
        try
        {
            FileOutputStream fileout = new FileOutputStream(dumpFile);
            PrintStream out = new PrintStream(fileout);
            out.print(docToDump.getText(0, docToDump.getLength()));
        }
        catch (BadLocationException | FileNotFoundException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_dumpConsoleMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info
                         : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException |
               IllegalAccessException |
               javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(BayesGUI.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new BayesGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup algorithmButtonGroup;
    private javax.swing.JRadioButton bayesNetRadioButton;
    private javax.swing.JRadioButton bucketTreeRadioButton;
    private javax.swing.JComboBox calculationTypeComboBox;
    private javax.swing.JCheckBox computeClustersCheckBox;
    private javax.swing.ButtonGroup displayButtonGroup;
    private javax.swing.JMenuItem dumpConsoleMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPopupMenu.Separator fileSeparator;
    private javax.swing.JComboBox globalNeighbourhoodComboBox;
    private javax.swing.JLabel globalNeighbourhoodLabel;
    private javax.swing.JSpinner globalNeighbourhoodParameterSpinner;
    private javax.swing.JScrollPane graphPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JRadioButton junctionTreeRadioButton;
    private javax.swing.JMenuItem loadBayesMenuItem;
    private javax.swing.JMenuItem loadFromURLMenuItem;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JTextField networkNameInput;
    private javax.swing.JLabel networkNameLabel;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JTable propertiesTable;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JRadioButton variableEliminationRadioButton;
    private javax.swing.JCheckBox verboseCheckBox;
    // End of variables declaration//GEN-END:variables
}
