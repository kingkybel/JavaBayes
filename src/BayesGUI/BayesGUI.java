/*
 * @author  Dieter J Kybelksties
 * @date May 11, 2016
 *
 */
package BayesGUI;

import BayesianInferences.ExplanationType;
import BayesianInferences.InferenceGraph;
import BayesianInferences.InferenceGraphNode;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Dieter J Kybelksties
 */
public class BayesGUI extends javax.swing.JFrame
{

    private static final String CLASS_NAME = BayesGUI.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    Style metaStyle;
    Style queryStyle;
    Style resultStyle;
    Style errorStyle;
    StyleContext styleContext = new StyleContext();
    StyledDocument doc;

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
    ArrayList movingNodes = null;
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

        doc = outputPanel.getStyledDocument();
        metaStyle = addStyle(styleContext,
                             Color.GRAY,
                             Color.WHITE,
                             "courier",
                             10,
                             false);

        queryStyle = addStyle(styleContext,
                              Color.MAGENTA,
                              Color.WHITE,
                              "courier",
                              10,
                              true);

        resultStyle = addStyle(styleContext,
                               Color.BLUE,
                               Color.WHITE,
                               "courier",
                               10,
                               true);

        errorStyle = addStyle(styleContext,
                              Color.YELLOW,
                              Color.RED,
                              "courier",
                              10,
                              true);

        bayesPanel = new BayesPanel(this);
        graphPanel.add(bayesPanel);
        graphPanel.setViewportView(bayesPanel);

        metaOutput("Started Bayes GUI");
    }

    static Style addStyle(StyleContext sc,
                          Color fgColor,
                          Color bgColor,
                          String fontFamily,
                          Integer fontSize,
                          Boolean bold)
    {
        String stylename = fgColor + "_" +
                           bgColor + "_" +
                           fontFamily + "_" +
                           fontSize.toString() + "_" +
                           (bold ? "bold" : "normal");
        Style newStyle = sc.addStyle(stylename, null);
        newStyle.addAttribute(StyleConstants.Foreground, fgColor);
        newStyle.addAttribute(StyleConstants.Background, bgColor);
        newStyle.addAttribute(StyleConstants.FontSize, fontSize);
        newStyle.addAttribute(StyleConstants.FontFamily, fontFamily);
        newStyle.addAttribute(StyleConstants.Bold, bold);

        return newStyle;
    }

    /**
     * Place text in the text area.
     *
     * @param text
     */
    static void appendToDocument(StyledDocument doc, Style style, String line)
    {
        try
        {
            doc.insertString(doc.getEndPosition().getOffset(), line, style);
        }
        catch (BadLocationException ex)
        {
            LOGGER.log(Level.INFO, ex.toString());
        }
    }

    final void metaOutput(String message)
    {
        if (verboseCheckBox.isSelected())
        {
            appendToDocument(doc, metaStyle, "// " + message + "\n");
        }
    }

    final void queryOutput(String message)
    {
        appendToDocument(doc, queryStyle, message);
    }

    final void resultOutput(String message)
    {
        appendToDocument(doc, resultStyle, message);
    }

    final void errorOutput(String message)
    {
        appendToDocument(doc, errorStyle, "!!! " + message + "!!!\n");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

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
        textScrollPane = new javax.swing.JScrollPane();
        outputPanel = new javax.swing.JTextPane();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadBayesMenuItem = new javax.swing.JMenuItem();
        loadFromURLMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        dumpConsoleMenuItem = new javax.swing.JMenuItem();
        fileSeparator = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        mainTabbedPane.setPreferredSize(new java.awt.Dimension(500, 400));
        mainTabbedPane.addTab("Graph", graphPanel);

        optionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        optionPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bucketTreeRadioButton.setText("Bucket Tree");
        optionPanel.add(bucketTreeRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 24, -1, -1));

        bayesNetRadioButton.setSelected(true);
        bayesNetRadioButton.setText("Bayes Net");
        optionPanel.add(bayesNetRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, -1, -1));

        variableEliminationRadioButton.setSelected(true);
        variableEliminationRadioButton.setText("Variable elimination");
        optionPanel.add(variableEliminationRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 0, -1, -1));

        junctionTreeRadioButton.setText("Junction tree");
        optionPanel.add(junctionTreeRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, -1, -1));

        calculationTypeComboBox.setModel(new DefaultComboBoxModel<>(ExplanationType.validChoices())
        );
        calculationTypeComboBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                calculationTypeComboBoxActionPerformed(evt);
            }
        });
        optionPanel.add(calculationTypeComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 0, 190, -1));

        verboseCheckBox.setSelected(true);
        verboseCheckBox.setText("Verbose output");
        optionPanel.add(verboseCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, -1, -1));

        computeClustersCheckBox.setText("Compute Clusters");
        optionPanel.add(computeClustersCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, -1, -1));

        mainTabbedPane.addTab("Options", optionPanel);

        mainSplitPane.setTopComponent(mainTabbedPane);

        outputPanel.setPreferredSize(new java.awt.Dimension(500, 300));
        textScrollPane.setViewportView(outputPanel);

        mainSplitPane.setRightComponent(textScrollPane);

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
        fileMenu.add(saveAsMenuItem);

        dumpConsoleMenuItem.setText("Dump Console...");
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
        String bayesNetModelFile = "";
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
        // TODO add your handling code here:
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void calculationTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_calculationTypeComboBoxActionPerformed
    {//GEN-HEADEREND:event_calculationTypeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_calculationTypeComboBoxActionPerformed

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
    private javax.swing.JScrollPane graphPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JRadioButton junctionTreeRadioButton;
    private javax.swing.JMenuItem loadBayesMenuItem;
    private javax.swing.JMenuItem loadFromURLMenuItem;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JTextPane outputPanel;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JRadioButton variableEliminationRadioButton;
    private javax.swing.JCheckBox verboseCheckBox;
    // End of variables declaration//GEN-END:variables
}
