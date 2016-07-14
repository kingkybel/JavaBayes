/*
 * @author  Dieter J Kybelksties
 * @date Jul 14, 2016
 *
 */
package BayesGUI;

import java.awt.Color;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Dieter J Kybelksties
 */
public class OutputPanel extends javax.swing.JPanel
{

    private static final String CLASS_NAME = OutputPanel.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static final String NEWLINE = System.getProperty("line.separator");

    public StyledDocument getStyledDocument()
    {
        return textPane.getStyledDocument();
    }

    public enum Styles
    {

        NORMAL, HIGHLIGHT, META, ERROR
    }
    StyleContext styleContext = new StyleContext();
    StyledDocument doc;
    TreeMap<Comparable, Style> styles = new TreeMap<>();
    private boolean verbose = true;

    /**
     * Get the value of verbose.
     *
     * @return the value of verbose
     */
    public boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Set the value of verbose.
     *
     * @param verbose new value of verbose
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    private String defaultFontFamily = "courier";

    /**
     * Get the value of defaultFontFamily
     *
     * @return the value of defaultFontFamily
     */
    public String getDefaultFontFamily()
    {
        return defaultFontFamily;
    }

    /**
     * Set the value of defaultFontFamily
     *
     * @param defaultFontFamily new value of defaultFontFamily
     */
    public void setDefaultFontFamily(String defaultFontFamily)
    {
        this.defaultFontFamily = defaultFontFamily;
    }

    private int defaultFontSize = 10;

    /**
     * Get the value of defaultFontSize
     *
     * @return the value of defaultFontSize
     */
    public int getDefaultFontSize()
    {
        return defaultFontSize;
    }

    /**
     * Set the value of defaultFontSize
     *
     * @param defaultFontSize new value of defaultFontSize
     */
    public void setDefaultFontSize(int defaultFontSize)
    {
        this.defaultFontSize = defaultFontSize;
    }

    private Color defaultForeground;

    /**
     * Get the value of defaultForeground
     *
     * @return the value of defaultForeground
     */
    public Color getDefaultForeground()
    {
        return defaultForeground;
    }

    /**
     * Set the value of defaultForeground
     *
     * @param defaultForeground new value of defaultForeground
     */
    public void setDefaultForeground(Color defaultForeground)
    {
        this.defaultForeground = defaultForeground;
    }

    private Color defaultBackground;

    /**
     * Get the value of defaultBackground
     *
     * @return the value of defaultBackground
     */
    public Color getDefaultBackground()
    {
        return defaultBackground;
    }

    /**
     * Set the value of defaultBackground
     *
     * @param defaultBackground new value of defaultBackground
     */
    public void setDefaultBackground(Color defaultBackground)
    {
        this.defaultBackground = defaultBackground;
    }

    private boolean defaultBold;

    /**
     * Get the value of defaultBold
     *
     * @return the value of defaultBold
     */
    public boolean isDefaultBold()
    {
        return defaultBold;
    }

    /**
     * Set the value of defaultBold
     *
     * @param bold new value of defaultBold
     */
    public void setDefaultBold(boolean bold)
    {
        this.defaultBold = bold;
    }

    private boolean defaultItalic;

    /**
     * Get the value of defaultItalic
     *
     * @return the value of defaultItalic
     */
    public boolean isDefaultItalic()
    {
        return defaultItalic;
    }

    /**
     * Set the value of defaultItalic
     *
     * @param defaultItalic new value of defaultItalic
     */
    public void setDefaultItalic(boolean defaultItalic)
    {
        this.defaultItalic = defaultItalic;
    }

    /**
     * Creates new form OutputPanel.
     */
    public OutputPanel()
    {
        initComponents();
        reset();
    }

    /**
     * Add a new style to be used on this panel.
     *
     * @param key        identifying key to retrieve the style
     * @param fgColor    foreground color
     * @param bgColor    background color
     * @param fontFamily string describing the family
     * @param fontSize   the size of the font
     * @param bold       bold weight if true, normal otherwise
     * @param italic     italic if true, upright (roman) otherwise
     * @return the new style
     * @throws java.lang.Exception thrown if the key is null
     */
    public Style addStyle(Comparable key,
                          Color fgColor,
                          Color bgColor,
                          String fontFamily,
                          Integer fontSize,
                          Boolean bold,
                          Boolean italic) throws Exception
    {
        if (key == null)
        {
            throw new Exception(
                    "Cannot add a style without key to later identify it");
        }
        if (fgColor == null)
        {
            fgColor = getDefaultForeground();
        }
        if (bgColor == null)
        {
            bgColor = getDefaultBackground();
        }
        if (fontFamily == null)
        {
            fontFamily = getDefaultFontFamily();
        }
        if (fontSize == null)
        {
            fontSize = getDefaultFontSize();
        }
        if (bold == null)
        {
            bold = isDefaultBold();
        }
        if (italic == null)
        {
            italic = isDefaultItalic();
        }
        String stylename = fgColor + "_" +
                           bgColor + "_" +
                           fontFamily + "_" +
                           fontSize.toString() + "_" +
                           (bold ? "bold" : "normal") + "_" +
                           (italic ? "italic" : "roman");
        Style newStyle = styleContext.addStyle(stylename, null);
        newStyle.addAttribute(StyleConstants.Foreground, fgColor);
        newStyle.addAttribute(StyleConstants.Background, bgColor);
        newStyle.addAttribute(StyleConstants.FontSize, fontSize);
        newStyle.addAttribute(StyleConstants.FontFamily, fontFamily);
        newStyle.addAttribute(StyleConstants.Bold, bold);
        newStyle.addAttribute(StyleConstants.Italic, italic);
        styles.put(key, newStyle);

        return newStyle;
    }

    public final void reset()
    {
        reset("courier", 10);
    }

    public void reset(String fontFamily, int fontSize)
    {
        try
        {
            doc = textPane.getStyledDocument();

            doc.remove(0, doc.getLength());

            styles = new TreeMap<>();
            styleContext = new StyleContext();

            addStyle(Styles.NORMAL,
                     Color.MAGENTA,
                     Color.WHITE,
                     fontFamily,
                     fontSize,
                     true,
                     false);
            addStyle(Styles.HIGHLIGHT,
                     Color.BLUE,
                     Color.WHITE,
                     fontFamily,
                     fontSize,
                     true,
                     false);
            addStyle(Styles.META,
                     Color.GRAY,
                     Color.WHITE,
                     fontFamily,
                     fontSize,
                     false,
                     false);
            addStyle(Styles.ERROR,
                     Color.YELLOW,
                     Color.RED,
                     fontFamily,
                     fontSize,
                     true,
                     false);

        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Place text in the text area.
     *
     * @param text
     */
    void appendToDocument(Style style, String line)
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

    /**
     *
     * @param message
     */
    public final void writelnMeta(String message)
    {
        if (isVerbose())
        {
            appendToDocument(styles.get(Styles.META), "// " + message + NEWLINE);
        }
    }

    /**
     *
     * @param style
     * @param message
     */
    public final void write(Style style, String message)
    {
        appendToDocument(style, message);
    }

    /**
     *
     * @param style
     * @param message
     */
    public final void writeln(Style style, String message)
    {
        appendToDocument(styles.get(Styles.NORMAL), message + NEWLINE);
    }

    /**
     *
     * @param styleKey
     * @param message
     */
    public final void write(Comparable styleKey, String message)
    {
        appendToDocument(styles.get(styleKey), message);
    }

    /**
     *
     * @param styleKey
     * @param message
     */
    public final void writeln(Comparable styleKey, String message)
    {
        appendToDocument(styles.get(styleKey), message + NEWLINE);
    }

    /**
     *
     * @param message
     */
    public final void write(String message)
    {
        appendToDocument(styles.get(Styles.NORMAL), message);
    }

    /**
     *
     * @param message
     */
    public final void writeln(String message)
    {
        appendToDocument(styles.get(Styles.NORMAL), message + NEWLINE);
    }

    /**
     *
     * @param message
     */
    public final void writeHighlight(String message)
    {
        appendToDocument(styles.get(Styles.HIGHLIGHT), message);
    }

    /**
     *
     * @param message
     */
    public final void writelnHighlight(String message)
    {
        appendToDocument(styles.get(Styles.HIGHLIGHT), message + NEWLINE);
    }

    /**
     *
     * @param message
     */
    public final void writelnError(String message)
    {
        appendToDocument(styles.get(Styles.ERROR),
                         "!!! " + message + "!!!" + NEWLINE);
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

        scrollPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        scrollPane.setViewportView(textPane);

        add(scrollPane);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextPane textPane;
    // End of variables declaration//GEN-END:variables
}
