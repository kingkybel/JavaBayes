/*
 * NetworkPanel.java
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
import BayesianInferences.InferenceGraphNode;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class NetworkPanel extends Canvas
{

    private static final Class CLAZZ = NetworkPanel.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Constants for drawing entities.
    private static final int NODE_SIZE = 26;
    private static final int NODE_RADIUS = 13;
    private static final int SPACE_DRAW_NODE_NAME = 24;
    private static final double ARROW_SIZE = 6.0;
    private static final double ARROW_HALF_SIZE = 3.0;
    private static final double DISTANCE_HIT_ARC = 200.0;

    // Color constants for various graphical elements.
    private static final Color nodeColor = Color.green;
    private static final Color observedNodeColor = Color.blue;
    private static final Color explanationNodeColor = Color.orange;

    private static final Color nodeBorderColor = Color.black;
    private static final Color nodenameColor = Color.black;
    private static final Color arcColor = Color.gray;
    private static final Color backgroundColor = Color.white;

    // Network editing modes.
    enum Mode
    {

        CREATE,
        MOVE,
        DELETE,
        OBSERVE,
        QUERY,
        EDIT_VARIABLE,
        EDIT_FUNCTION,
        EDIT_NETWORK
    }
    private final EditorFrame frame; // Used for changing mouse cursor
    // Used for changing mouse cursor
    private final ScrollingPanel scrollPanel; // Used to control scrolling
    // Used to control scrolling
    private Mode mode; // Store the mode for events in the panel
    // Store the mode for events in the panel
    private InferenceGraph inferenceGraph; // The object with the Bayes net
    // The object with the Bayes net
    private final Point groupStart;
    private final Point groupEnd; // The region that is considered the group
    // The region that is considered the group
    // Variables that store quantities shared among event handling functions
    boolean newArc = false;
    Point newArcHead = null;
    boolean modifyGroup = false;
    InferenceGraphNode movenode = null;
    ArrayList<InferenceGraphNode> movingNodes = null;
    InferenceGraphNode arcbottomnode = null;
    InferenceGraphNode archeadnode = null;
    int xScroll;
    int yScroll;

    // Fonts.
    private final Font roman = new Font("TimesRoman", Font.BOLD, 12);
    private final Font helvetica = new Font("Helvetica", Font.BOLD, 15);
    private final FontMetrics fmetrics = getFontMetrics(roman);
    private final int h = (int) fmetrics.getHeight() / 3;

    // For double buffering.
    private Image offScreenImage;
    private Graphics offScreenGraphics;
    private Dimension offScreenSize;

    /**
     * Default constructor for NetworkPanel.
     *
     * @param frame       the frame for this panel
     * @param scrollPanel scroll supporting panel
     */
    NetworkPanel(EditorFrame frame, ScrollingPanel scrollPanel)
    {
        this.frame = frame;
        this.scrollPanel = scrollPanel;

        // Create default InferenceGraph
        inferenceGraph = new InferenceGraph();

        // Create the group object.
        groupStart = new Point(0, 0);
        groupEnd = new Point(0, 0);

        // set initial mode to be MOVE.
        mode = Mode.MOVE;
        setCursor(getPredefinedCursor(Cursor.MOVE_CURSOR));

        // set color for background
        setBackground(backgroundColor);
    }

    @Override
    public boolean mouseDown(Event evt, int x, int y)
    {
        x += xScroll;
        y += yScroll;

        InferenceGraphNode node = nodehit(x, y);

        if (node == null)
        { // If no node was clicked on.
            if ((mode == Mode.DELETE) && (archit(x, y)))
            { // Delete arc
                deleteArc();
                archeadnode = null;
                arcbottomnode = null;
            }
            else if (mode == Mode.CREATE)
            { // Create a node
                createNode(x, y);
            }
            else
            {
                // Start the creation of a group.
                groupStart.setLocation(x, y);
                groupEnd.setLocation(x, y);
                modifyGroup = true;
            }
        }
        else
        { // If a node was clicked on.
            if (mode == Mode.OBSERVE)
            { // Observe node
                observe(node);
            }
            else if (mode == Mode.QUERY)
            { // Query node
                frame.processQuery(inferenceGraph, node.getName());
            }
            else if (mode == Mode.MOVE)
            { // Move node
                movenode = node;
                generateMovingNodes();
            }
            else if (mode == Mode.DELETE)
            { // Delete node
                deleteNode(node);
            }
            else if (mode == Mode.CREATE)
            { // Create arc
                newArc = true;
                arcbottomnode = node;
                newArcHead = new Point(x, y);
            }
            else if (mode == Mode.EDIT_VARIABLE)
            { // Edit variable node
                editVariable(node);
            }
            else if (mode == Mode.EDIT_FUNCTION)
            { // Edit function node
                editFunction(node);
            }
        }

        repaint();
        return true;
    }

    @Override
    public boolean mouseDrag(Event evt, int x, int y)
    {
        x += xScroll;
        y += yScroll;

        if (movenode != null)
        {
            moveNode(x, y);
        }
        else if (newArc == true)
        {
            newArcHead = new Point(x, y);
        }
        else if (modifyGroup == true)
        {
            groupEnd.setLocation(x, y);
        }

        repaint();
        return true;
    }

    @Override
    public boolean mouseUp(Event evt, int x, int y)
    {
        x += xScroll;
        y += yScroll;

        if (movenode != null)
        {
            inferenceGraph.setCoordinates(movenode, new Point(x, y));
            movenode = null;
        }
        else if (newArc == true)
        {
            archeadnode = nodehit(x, y);
            if ((archeadnode != null) && (arcbottomnode != null))
            {
                if (archeadnode == arcbottomnode)
                {
                    JavaBayesHelpMessages.show(JavaBayesHelpMessages.selfarc);
                }
                else if (inferenceGraph.hasCycle(arcbottomnode, archeadnode))
                {
                    JavaBayesHelpMessages.show(JavaBayesHelpMessages.circular);
                }
                else
                {
                    createArc();
                }
            }
            archeadnode = null;
            arcbottomnode = null;
            newArcHead = null;
            newArc = false;
        }
        else if (modifyGroup == true)
        {
            modifyGroup = false;
        }

        repaint();
        return true;
    }

    /**
     * Determine whether a node was hit by a mouse click.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return the inference graph node if there is one at the location, null
     *         otherwise
     */
    private InferenceGraphNode nodehit(int x, int y)
    {
        for (InferenceGraphNode node : inferenceGraph.elements())
        {
            if ((x - node.getXCoordinate()) * (x - node.getXCoordinate()) +
                (y - node.getYCoordinate()) * (y - node.getYCoordinate()) <
                NODE_RADIUS * NODE_RADIUS)
            {
                return node;
            }
        }
        return null;
    }

    /**
     * Determine whether an arc was hit by a mouse click.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return true if there is an arc at the location, false otherwise
     */
    boolean archit(int x, int y)
    {
        double sdpa;

        for (InferenceGraphNode hnode : inferenceGraph.elements())
        {
            for (InferenceGraphNode pnode : hnode.getParents())
            {
                sdpa = squareDistancePointArc(hnode, pnode, x, y);
                if ((sdpa >= 0.0) && (sdpa <= DISTANCE_HIT_ARC))
                {
                    archeadnode = hnode;
                    arcbottomnode = pnode;
                }
            }
        }
        return (archeadnode != null) && (arcbottomnode != null);
    }

    /**
     * Determine whether a point is close to the segment between two nodes
     * (hnode and pnode).
     *
     * @param hnode the first node
     * @param pnode the second node
     * @param x3    X-coordinate
     * @param y3    Y-coordinate
     * @return if the point does not lie over or above the segment, return -1.0,
     *         the height of the triangle otherwise
     */
    double squareDistancePointArc(InferenceGraphNode hnode,
                                  InferenceGraphNode pnode,
                                  int x3,
                                  int y3)
    {
        int x1, y1, x2, y2;
        double area, squareBase, squareHeight, squareHyp;

        x1 = hnode.getXCoordinate();
        y1 = hnode.getYCoordinate();
        x2 = pnode.getXCoordinate();
        y2 = pnode.getYCoordinate();

        // Area of the triangle defined by the three points
        area = 0.5 * (double) (x1 * y2 + y1 * x3 + x2 * y3 -
                               x3 * y2 - y3 * x1 - x2 * y1);
        // Base of the triangle
        squareBase = (double) ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        // Height of the triangle
        squareHeight = 4.0 * (area * area) / squareBase;

        // Maximum possible distance from point to extreme points
        squareHyp = squareBase + squareHeight;
        // Check first extreme point
        if (squareHyp < ((double) ((x3 - x1) * (x3 - x1) + (y3 - y1) *
                                                           (y3 - y1))))
        {
            return -1.0;
        }
        // Check second extreme point
        if (squareHyp < ((double) ((x3 - x2) * (x3 - x2) + (y3 - y2) *
                                                           (y3 - y2))))
        {
            return -1.0;
        }

        // Requested distance is the height of the triangle
        return squareHeight;
    }

    @Override
    public void update(Graphics g)
    {
        // Prepare new offscreen image, for double buffering.
        Dimension d = getSize();
        MediaTracker tracker;

        if ((offScreenImage == null) ||
            (d.width != offScreenSize.width) ||
            (d.height != offScreenSize.height))
        {
            offScreenImage = createImage(d.width, d.height);
            tracker = new MediaTracker(this);
            try
            { // Wait to image to be constructed.
                tracker.addImage(offScreenImage, 0);
                tracker.waitForID(0, 0);
            }
            catch (InterruptedException e)
            {
            }
            offScreenSize = d;
            offScreenGraphics = offScreenImage.getGraphics();
        }

        // Generate the contents of the image.
        offScreenGraphics.setColor(backgroundColor);
        offScreenGraphics.fillRect(0, 0, d.width, d.height);
        paint(offScreenGraphics);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    /**
     * Paint the network. This is not nearly as efficient as it should be,
     * because the whole graph is redrawn every time there is a call to paint().
     * A much more efficient approach would be to only add/move/delete nodes and
     * arcs as needed, in response to user commands.
     *
     * The rendering engine changed in Java2, and the circles are not drawn
     * correctly. The following workaround for the change in the rendering
     * engine in Java2 was proposed by Michael Becke, Nov 21 2000. try {
     * ((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,
     * RenderingHints.VALUE_ANTIALIAS_ON); } catch
     * (java.lang.NoClassDefFoundError e) { // Does nothing if new engine fails.
     * } Instead of using this Java2-specific code, the solution used here and
     * also proposed by Michael Becke is to fill first a whole oval with the
     * border color and then fill the inside of the circles.
     */
    @Override
    public void paint(Graphics g)
    {
        ExplanationType explanationStatus = frame.getMode();

        if (inferenceGraph == null)
        {
            return;
        }

        // Draw a new arc upto current mouse position.
        g.setColor(arcColor);

        if (newArc)
        {
            g.drawLine(arcbottomnode.getXCoordinate() - xScroll,
                       arcbottomnode.getYCoordinate() - yScroll,
                       newArcHead.x - xScroll, newArcHead.y - yScroll);
        }

        // Draw all arcs.
        for (InferenceGraphNode node : inferenceGraph.elements())
        {
            for (InferenceGraphNode parent : node.getParents())
            {
                drawArc(g, node, parent);
            }
        }

        // Draw the nodes.
        g.setFont(helvetica);

        for (InferenceGraphNode node : inferenceGraph.elements())
        {
            g.setColor(nodeBorderColor);
            if ((node.getXCoordinate() - xScroll) >= 0)
            {
                g.fillOval((node.getXCoordinate() - xScroll) - NODE_RADIUS - 1,
                           (node.getYCoordinate() - yScroll) - NODE_RADIUS - 1,
                           NODE_SIZE + 2, NODE_SIZE + 2);
            }

            if (explanationStatus.usesAllNotObservedVariables())
            {
                g.setColor(explanationNodeColor);
            }
            else if (explanationStatus.usesMarkedVariablesOnly())
            {
                if (node.isExplanation())
                {
                    g.setColor(explanationNodeColor);
                }
                else
                {
                    g.setColor(nodeColor);
                }
            }
            else if (explanationStatus.isSensitivityAnalysis() ||
                     explanationStatus.isMarginalPosterior() ||
                     explanationStatus.isExpectation())
            {
                g.setColor(nodeColor);
            }

            if (node.isObserved())
            {
                g.setColor(observedNodeColor);
            }

            if ((node.getXCoordinate() - xScroll) >= 0)
            {
                g.fillOval((node.getXCoordinate() - xScroll) - NODE_RADIUS,
                           (node.getYCoordinate() - yScroll) - NODE_RADIUS,
                           NODE_SIZE, NODE_SIZE);
            }

            g.setColor(nodenameColor);
            g.drawString(node.getName(),
                         (node.getXCoordinate() - xScroll) -
                         SPACE_DRAW_NODE_NAME,
                         (node.getYCoordinate() - yScroll) +
                         SPACE_DRAW_NODE_NAME);
        }

        // Draw the group.
        g.setXORMode(backgroundColor);
        int groupX, groupY, groupWidth, groupHeight;
        if (groupStart.x < groupEnd.x)
        {
            groupX = groupStart.x;
            groupWidth = groupEnd.x - groupStart.x;
        }
        else
        {
            groupX = groupEnd.x;
            groupWidth = groupStart.x - groupEnd.x;
        }
        if (groupStart.y < groupEnd.y)
        {
            groupY = groupStart.y;
            groupHeight = groupEnd.y - groupStart.y;
        }
        else
        {
            groupY = groupEnd.y;
            groupHeight = groupStart.y - groupEnd.y;
        }
        g.drawRect(groupX - xScroll, groupY - yScroll,
                   groupWidth, groupHeight);
        g.setPaintMode();

        // Resize the scrollbars.
        scrollPanel.setScrollbars(getSize());
    }

    /**
     * Auxiliary function that draws an arc.
     *
     * @param g      graphics
     * @param node   inference graph node
     * @param parent parent graph node
     */
    private void drawArc(Graphics g,
                         InferenceGraphNode node,
                         InferenceGraphNode parent)
    {
        int nodeX, nodeY, parentX, parentY;
        int x1, x2, x3, y1, y2, y3;
        double dirX, dirY, distance;
        double headX, headY, bottomX, bottomY;

        // calculate archead
        nodeX = node.getXCoordinate() - xScroll;
        nodeY = node.getYCoordinate() - yScroll;
        parentX = parent.getXCoordinate() - xScroll;
        parentY = parent.getYCoordinate() - yScroll;

        dirX = (double) (nodeX - parentX);
        dirY = (double) (nodeY - parentY);

        distance = Math.sqrt(dirX * dirX + dirY * dirY);

        dirX /= distance;
        dirY /= distance;

        headX = nodeX - (NODE_RADIUS + ARROW_SIZE) * dirX;
        headY = nodeY - (NODE_RADIUS + ARROW_SIZE) * dirY;

        bottomX = parentX + NODE_RADIUS * dirX;
        bottomY = parentY + NODE_RADIUS * dirY;

        x1 = (int) (headX - ARROW_HALF_SIZE * dirX + ARROW_SIZE * dirY);
        x2 = (int) (headX - ARROW_HALF_SIZE * dirX - ARROW_SIZE * dirY);
        x3 = (int) (headX + ARROW_SIZE * dirX);

        y1 = (int) (headY - ARROW_HALF_SIZE * dirY - ARROW_SIZE * dirX);
        y2 = (int) (headY - ARROW_HALF_SIZE * dirY + ARROW_SIZE * dirX);
        y3 = (int) (headY + ARROW_SIZE * dirY);

        int archeadX[] =
        {
            x1, x2, x3, x1
        };
        int archeadY[] =
        {
            y1, y2, y3, y1
        };

        // draw archead
        g.drawLine((int) bottomX, (int) bottomY,
                   (int) headX, (int) headY);
        g.fillPolygon(archeadX, archeadY, 4);
    }

    /**
     * Set the mode for the NetworkPanel.
     *
     * @param modeLabel
     */
    void setMode(String modeLabel)
    {
        switch (modeLabel)
        {
            case EditorFrame.createLabel:
                mode = Mode.CREATE;
                break;
            case EditorFrame.moveLabel:
                mode = Mode.MOVE;
                break;
            case EditorFrame.deleteLabel:
                mode = Mode.DELETE;
                break;
            case EditorFrame.queryLabel:
                mode = Mode.QUERY;
                break;
            case EditorFrame.observeLabel:
                mode = Mode.OBSERVE;
                break;
            case EditorFrame.editVariableLabel:
                mode = Mode.EDIT_VARIABLE;
                break;
            case EditorFrame.editFunctionLabel:
                mode = Mode.EDIT_FUNCTION;
                break;
            default:
                // default mode;
                mode = Mode.CREATE;
                break;
        }
    }

    /**
     * Return the QuasiBayesNet object displayed int the NetworkPanel.
     *
     * @return the QuasiBayesNet object
     */
    InferenceGraph getInferenceGraph()
    {
        return inferenceGraph;
    }

    /**
     * Store the QuasiBayesNet object to be displayed in the NetworkPanel.
     *
     * @param inferenceGraph new inference graph
     */
    void load(InferenceGraph inferenceGraph)
    {
        this.inferenceGraph = inferenceGraph;
        repaint();
    }

    /*
     * Clear the NetworkPanel.
     */
    void clear()
    {
        inferenceGraph = new InferenceGraph();
        repaint();
    }

    /**
     * Observe a node.
     *
     * @param inferenceGraphNode
     */
    void observe(InferenceGraphNode inferenceGraphNode)
    {
        inferenceGraph.resetMarginal();
        Dialog d = new ObserveDialog(this,
                                     frame,
                                     inferenceGraph,
                                     inferenceGraphNode);
        d.setVisible(true);
    }

    /**
     * Create a node.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    void createNode(int x, int y)
    {
        inferenceGraph.createNode(x, y);
        inferenceGraph.resetMarginal();
    }

    /**
     * Create an arc. The bottom and head nodes of the arc are stored in the
     * variables arcbottomnode and archeadnode.
     */
    void createArc()
    {
        boolean flagCreated = inferenceGraph.createArc(arcbottomnode,
                                                       archeadnode);
        if (flagCreated == true)
        {
            inferenceGraph.resetMarginal();
        }
    }

    /**
     * Make a list of all moving nodes.
     */
    void generateMovingNodes()
    {

        if (!insideGroup(movenode))
        {
            movingNodes = null;
        }
        else
        {
            movingNodes = new ArrayList<>();
            for (InferenceGraphNode node : inferenceGraph.elements())
            {
                if (insideGroup(node))
                {
                    movingNodes.add(node);
                }
            }
        }
    }

    /**
     * Move a node.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    void moveNode(int x, int y)
    {
        int deltaX = movenode.getXCoordinate() - x;
        int deltaY = movenode.getYCoordinate() - y;

        // Check whether the movenode is in the group.
        if (movingNodes == null)
        {
            inferenceGraph.setCoordinates(movenode, new Point(x, y)); // Move only the movenode.
        }
        else
        {
            groupStart.x -= deltaX;
            groupEnd.x -= deltaX;
            groupStart.y -= deltaY;
            groupEnd.y -= deltaY;
            for (InferenceGraphNode node : movingNodes)
            {
                inferenceGraph.setCoordinates(node, // Move all nodes in the group.
                                              new Point(node.getXCoordinate() -
                                                        deltaX,
                                                        node.getYCoordinate() -
                                                        deltaY));
            }
        }
    }

    /**
     * Delete a node.
     *
     * @param inferenceGraphNode
     */
    void deleteNode(InferenceGraphNode inferenceGraphNode)
    {
        ArrayList<InferenceGraphNode> nodesToDelete;

        // Check whether the node is in the group.
        if (!insideGroup(inferenceGraphNode))
        {
            inferenceGraph.deleteNode(inferenceGraphNode); // Delete only the movenode.
        }
        else
        {
            nodesToDelete = new ArrayList<>();
            for (InferenceGraphNode dnode : inferenceGraph.elements())
            {
                if (insideGroup(dnode))
                {
                    nodesToDelete.add(dnode);
                }
            }
            for (InferenceGraphNode dnode : nodesToDelete)
            {
                inferenceGraph.deleteNode(dnode);
            }
        }
        inferenceGraph.resetMarginal();
    }

    /**
     * Determine whether a given InferenceGraphNode is inside the group.
     *
     * @param node inference graph node
     * @return true if so false otherwise
     */
    boolean insideGroup(InferenceGraphNode node)
    {
        return (node.getXCoordinate() > Math.min(groupStart.x, groupEnd.x)) &&
               (node.getXCoordinate() < Math.max(groupStart.x, groupEnd.x)) &&
               (node.getYCoordinate() > Math.min(groupStart.y, groupEnd.y)) &&
               (node.getYCoordinate() < Math.max(groupStart.y, groupEnd.y));
    }

    /**
     * Delete an arc. The bottom and head nodes of the arc are stored in the
     * variables arcbottomnode and archeadnode.
     */
    void deleteArc()
    {
        inferenceGraph.deleteArc(arcbottomnode, archeadnode);
        inferenceGraph.resetMarginal();
    }

    /**
     * Edit the components of a node.
     *
     * @param inferenceGraphNode inference graph node
     */
    void editVariable(InferenceGraphNode inferenceGraphNode)
    {
        inferenceGraph.resetMarginal();
        Dialog d = new EditVariableDialog(this,
                                          frame,
                                          inferenceGraph,
                                          inferenceGraphNode);
        d.setVisible(true);
    }

    /**
     * Edit the function in a node.
     *
     * @param inferenceGraphNode inference graph node
     */
    void editFunction(InferenceGraphNode inferenceGraphNode)
    {
        inferenceGraph.resetMarginal();
        Dialog d = new EditFunctionDialog(frame,
                                          inferenceGraph,
                                          inferenceGraphNode);
        d.setVisible(true);
    }

    /**
     * Edit the network.
     */
    void editNetwork()
    {
        inferenceGraph.resetMarginal();
        Dialog d = new EditNetworkDialog(frame, inferenceGraph);
        d.setVisible(true);
    }

}
