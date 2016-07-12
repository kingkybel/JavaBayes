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
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class NetworkPanel extends Canvas
{

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
    private static final int CREATE_MODE = 1;
    private static final int MOVE_MODE = 2;
    private static final int DELETE_MODE = 3;
    private static final int OBSERVE_MODE = 4;
    private static final int QUERY_MODE = 5;
    private static final int EDIT_VARIABLE_MODE = 6;
    private static final int EDIT_FUNCTION_MODE = 7;
    private static final int EDIT_NETWORK_MODE = 8;
    private static final String CLASS_NAME = NetworkPanel.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private EditorFrame frame; // Used for changing mouse cursor
    // Used for changing mouse cursor
    private ScrollingPanel scrollPanel; // Used to control scrolling
    // Used to control scrolling
    private int mode; // Store the mode for events in the panel
    // Store the mode for events in the panel
    private InferenceGraph ig; // The object with the Bayes net
    // The object with the Bayes net
    private final Point groupStart;
    private final Point groupEnd; // The region that is considered the group
    // The region that is considered the group
    // Variables that store quantities shared among event handling functions
    boolean newArc = false;
    Point newArcHead = null;
    boolean modifyGroup = false;
    InferenceGraphNode movenode = null;
    ArrayList movingNodes = null;
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
     */
    NetworkPanel(EditorFrame frame, ScrollingPanel scroll)
    {
        this.frame = frame;
        this.scrollPanel = scroll;

        // Create default InferenceGraph
        ig = new InferenceGraph();

        // Create the group object.
        groupStart = new Point(0, 0);
        groupEnd = new Point(0, 0);

        // set initial mode to be MOVE.
        mode = MOVE_MODE;
        setCursor(getPredefinedCursor(Cursor.MOVE_CURSOR));

        // set color for background
        setBackground(backgroundColor);
    }

    /**
     * Process mouse down events.
     *
     * @param evt
     * @param y
     * @param x
     * @return
     */
    @Override
    public boolean mouseDown(Event evt, int x, int y)
    {
        x += xScroll;
        y += yScroll;

        InferenceGraphNode node = nodehit(x, y);

        if (node == null)
        { // If no node was clicked on.
            if ((mode == DELETE_MODE) && (archit(x, y)))
            { // Delete arc
                deleteArc();
                archeadnode = null;
                arcbottomnode = null;
            }
            else if (mode == CREATE_MODE)
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
            if (mode == OBSERVE_MODE)
            { // Observe node
                observe(node);
            }
            else if (mode == QUERY_MODE)
            { // Query node
                frame.processQuery(ig, node.getName());
            }
            else if (mode == MOVE_MODE)
            { // Move node
                movenode = node;
                generateMovingNodes();
            }
            else if (mode == DELETE_MODE)
            { // Delete node
                deleteNode(node);
            }
            else if (mode == CREATE_MODE)
            { // Create arc
                newArc = true;
                arcbottomnode = node;
                newArcHead = new Point(x, y);
            }
            else if (mode == EDIT_VARIABLE_MODE)
            { // Edit variable node
                editVariable(node);
            }
            else if (mode == EDIT_FUNCTION_MODE)
            { // Edit function node
                editFunction(node);
            }
        }

        repaint();
        return true;
    }

    /**
     * Process mouse drag events.
     *
     * @param evt
     * @param y
     * @param x
     * @return
     */
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

    /**
     * Process mouse up events.
     *
     * @param evt
     * @param y
     * @param x
     * @return
     */
    @Override
    public boolean mouseUp(Event evt, int x, int y)
    {
        x += xScroll;
        y += yScroll;

        if (movenode != null)
        {
            ig.setPos(movenode, new Point(x, y));
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
                else if (ig.hasCycle(arcbottomnode, archeadnode))
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
     */
    private InferenceGraphNode nodehit(int x, int y)
    {
        InferenceGraphNode node;
        for (Object e : ig.elements())
        {
            node = (InferenceGraphNode) (e);
            if ((x - node.getPosX()) * (x - node.getPosX()) +
                (y - node.getPosY()) * (y - node.getPosY()) <
                NODE_RADIUS * NODE_RADIUS)
            {
                return (node);
            }
        }
        return (null);
    }

    /**
     * Determine whether an arc was hit by a mouse click.
     */
    boolean archit(int x, int y)
    {
        InferenceGraphNode hnode, pnode;
        double sdpa;

        for (Object e : ig.elements())
        {
            hnode = (InferenceGraphNode) (e);
            for (Iterator it = (hnode.getParents()).iterator(); it.hasNext();)
            {
                Object ee = it.next();
                pnode = (InferenceGraphNode) (ee);
                sdpa = squareDistancePointArc(hnode, pnode, x, y);
                if ((sdpa >= 0.0) && (sdpa <= DISTANCE_HIT_ARC))
                {
                    archeadnode = hnode;
                    arcbottomnode = pnode;
                }
            }
        }
        if ((archeadnode != null) && (arcbottomnode != null))
        {
            return true;
        }
        else
        {
            return (false);
        }
    }

    /**
     * Determine whether a point is close to the segment between two nodes
     * (hnode and pnode); if the point does not lie over or above the segment,
     * return -1.0
     */
    double squareDistancePointArc(InferenceGraphNode hnode,
                                  InferenceGraphNode pnode, int x3, int y3)
    {
        int x1, y1, x2, y2;
        double area, squareBase, squareHeight, squareHyp;

        x1 = hnode.getPosX();
        y1 = hnode.getPosY();
        x2 = pnode.getPosX();
        y2 = pnode.getPosY();

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
            return (-1.0);
        }
        // Check second extreme point
        if (squareHyp < ((double) ((x3 - x2) * (x3 - x2) + (y3 - y2) *
                                                           (y3 - y2))))
        {
            return (-1.0);
        }

        // Requested distance is the height of the triangle
        return (squareHeight);
    }

    /**
     * Update the screen with the network.
     */
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
        InferenceGraphNode node, parent;
        ExplanationType explanationStatus = frame.getMode();

        if (ig == null)
        {
            return;
        }

        // Draw a new arc upto current mouse position.
        g.setColor(arcColor);

        if (newArc)
        {
            g.drawLine(arcbottomnode.getPosX() - xScroll,
                       arcbottomnode.getPosY() - yScroll,
                       newArcHead.x - xScroll, newArcHead.y - yScroll);
        }

        // Draw all arcs.
        for (Object e : ig.elements())
        {
            node = (InferenceGraphNode) (e);
            for (Object ee : (node.getParents()))
            {
                parent = (InferenceGraphNode) (ee);
                drawArc(g, node, parent);
            }
        }

        // Draw the nodes.
        g.setFont(helvetica);

        for (Object e : ig.elements())
        {
            node = (InferenceGraphNode) e;

            g.setColor(nodeBorderColor);
            if ((node.getPosX() - xScroll) >= 0)
            {
                g.fillOval((node.getPosX() - xScroll) - NODE_RADIUS - 1,
                           (node.getPosY() - yScroll) - NODE_RADIUS - 1,
                           NODE_SIZE + 2, NODE_SIZE + 2);
            }

            if (explanationStatus.isFull())
            {
                g.setColor(explanationNodeColor);
            }
            else if (explanationStatus.isSubset())
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

            if ((node.getPosX() - xScroll) >= 0)
            {
                g.fillOval((node.getPosX() - xScroll) - NODE_RADIUS,
                           (node.getPosY() - yScroll) - NODE_RADIUS,
                           NODE_SIZE, NODE_SIZE);
            }

            g.setColor(nodenameColor);
            g.drawString(node.getName(),
                         (node.getPosX() - xScroll) - SPACE_DRAW_NODE_NAME,
                         (node.getPosY() - yScroll) + SPACE_DRAW_NODE_NAME);
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
     */
    private void drawArc(Graphics g, InferenceGraphNode node,
                         InferenceGraphNode parent)
    {
        int nodeX, nodeY, parentX, parentY;
        int x1, x2, x3, y1, y2, y3;
        double dirX, dirY, distance;
        double headX, headY, bottomX, bottomY;

        // calculate archead
        nodeX = node.getPosX() - xScroll;
        nodeY = node.getPosY() - yScroll;
        parentX = parent.getPosX() - xScroll;
        parentY = parent.getPosY() - yScroll;

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
     */
    void setMode(String label)
    {
        switch (label)
        {
            case EditorFrame.createLabel:
                mode = CREATE_MODE;
                break;
            case EditorFrame.moveLabel:
                mode = MOVE_MODE;
                break;
            case EditorFrame.deleteLabel:
                mode = DELETE_MODE;
                break;
            case EditorFrame.queryLabel:
                mode = QUERY_MODE;
                break;
            case EditorFrame.observeLabel:
                mode = OBSERVE_MODE;
                break;
            case EditorFrame.editVariableLabel:
                mode = EDIT_VARIABLE_MODE;
                break;
            case EditorFrame.editFunctionLabel:
                mode = EDIT_FUNCTION_MODE;
                break;
            default:
                // default mode;
                mode = CREATE_MODE;
                break;
        }
    }

    /**
     * Return the QuasiBayesNet object displayed int the NetworkPanel.
     */
    InferenceGraph getInferenceGraph()
    {
        return (ig);
    }

    /**
     * Store the QuasiBayesNet object to be displayed in the NetworkPanel.
     */
    void load(InferenceGraph inferenceGraph)
    {
        ig = inferenceGraph;
        repaint();
    }

    /*
     * Clear the NetworkPanel.
     */
    void clear()
    {
        ig = new InferenceGraph();
        repaint();
    }

    /**
     * Observe a node.
     */
    void observe(InferenceGraphNode node)
    {
        ig.resetMarginal();
        Dialog d = new ObserveDialog(this, frame, ig, node);
        d.setVisible(true);
    }

    /**
     * Create a node.
     */
    void createNode(int x, int y)
    {
        ig.createNode(x, y);
        ig.resetMarginal();
    }

    /**
     * Create an arc. The bottom and head nodes of the arc are stored in the
     * variables arcbottomnode and archeadnode.
     */
    void createArc()
    {
        boolean flagCreated = ig.createArc(arcbottomnode, archeadnode);
        if (flagCreated == true)
        {
            ig.resetMarginal();
        }
    }

    /**
     * Make a list of all moving nodes.
     */
    void generateMovingNodes()
    {
        InferenceGraphNode node;

        if (!insideGroup(movenode))
        {
            movingNodes = null;
        }
        else
        {
            movingNodes = new ArrayList();
            for (Object e : ig.elements())
            {
                node = (InferenceGraphNode) e;
                if (insideGroup(node))
                {
                    movingNodes.add(node);
                }
            }
        }
    }

    /**
     * Move a node.
     */
    void moveNode(int x, int y)
    {
        InferenceGraphNode node;
        int deltaX = movenode.getPosX() - x;
        int deltaY = movenode.getPosY() - y;

        // Check whether the movenode is in the group.
        if (movingNodes == null)
        {
            ig.setPos(movenode, new Point(x, y)); // Move only the movenode.
        }
        else
        {
            groupStart.x -= deltaX;
            groupEnd.x -= deltaX;
            groupStart.y -= deltaY;
            groupEnd.y -= deltaY;
            for (Object e : movingNodes)
            {
                node = (InferenceGraphNode) e;
                ig.setPos(node, // Move all nodes in the group.
                          new Point(node.getPosX() - deltaX, node.
                                    getPosY() - deltaY));
            }
        }
    }

    /**
     * Delete a node.
     */
    void deleteNode(InferenceGraphNode node)
    {
        InferenceGraphNode dnode;
        ArrayList nodesToDelete;

        // Check whether the node is in the group.
        if (!insideGroup(node))
        {
            ig.deleteNode(node); // Delete only the movenode.
        }
        else
        {
            nodesToDelete = new ArrayList();
            for (Object e : ig.elements())
            {
                dnode = (InferenceGraphNode) e;
                if (insideGroup(dnode))
                {
                    nodesToDelete.add(dnode);
                }
            }
            for (Object e : nodesToDelete)
            {
                dnode = (InferenceGraphNode) e;
                ig.deleteNode(dnode); // Delete node.
            }
        }
        ig.resetMarginal();
    }

    /**
     * Determine whether a given InferenceGraphNode is inside the group.
     */
    boolean insideGroup(InferenceGraphNode node)
    {
        return ((node.getPosX() > Math.min(groupStart.x, groupEnd.x)) &&
                (node.getPosX() < Math.max(groupStart.x, groupEnd.x)) &&
                (node.getPosY() > Math.min(groupStart.y, groupEnd.y)) &&
                (node.getPosY() < Math.max(groupStart.y, groupEnd.y)));
    }

    /**
     * Delete an arc. The bottom and head nodes of the arc are stored in the
     * variables arcbottomnode and archeadnode.
     */
    void deleteArc()
    {
        ig.deleteArc(arcbottomnode, archeadnode);
        ig.resetMarginal();
    }

    /**
     * Edit the components of a node.
     */
    void editVariable(InferenceGraphNode node)
    {
        ig.resetMarginal();
        Dialog d = new EditVariableDialog(this, frame, ig, node);
        d.setVisible(true);
    }

    /**
     * Edit the function in a node.
     */
    void editFunction(InferenceGraphNode node)
    {
        ig.resetMarginal();
        Dialog d = new EditFunctionDialog(frame, ig, node);
        d.setVisible(true);
    }

    /**
     * Edit the network.
     */
    void editNetwork()
    {
        ig.resetMarginal();
        Dialog d = new EditNetworkDialog(frame, ig);
        d.setVisible(true);
    }

}
