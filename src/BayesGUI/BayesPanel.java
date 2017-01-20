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

import BayesGUI.BayesGUI.EditMode;
import BayesianInferences.DSeparation;
import BayesianInferences.ExplanationType;
import BayesianInferences.InferenceGraph;
import BayesianInferences.InferenceGraphNode;
import BayesianNetworks.DiscreteVariable;
import JavaBayesInterface.JavaBayesHelpMessages;
import java.awt.Color;
import java.awt.Cursor;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 *
 * @author Dieter J Kybelksties
 */
public class BayesPanel
        extends JPanel
        implements MouseListener
{

    private static final String CLASS_NAME = BayesPanel.class.getName();
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

    private final BayesGUI frame;

    // Store the mode for events in the panel
    // Used for changing mouse cursor
    BayesGUI.EditMode mode = BayesGUI.EditMode.MOVE;

    // The object with the Bayes net
    private InferenceGraph inferenceGraph;

    // The region that is considered the group
    private final Point groupStart;
    private final Point groupEnd;

    // Variables that store quantities shared among event handling functions
    boolean newArc = false;
    Point newArcHead = null;
    boolean modifyGroup = false;
    InferenceGraphNode movenode = null;
    ArrayList<InferenceGraphNode> movingNodes = null;
    InferenceGraphNode arcBottomNode = null;
    InferenceGraphNode arcHeadNode = null;

    // Fonts.
    private final Font roman = new Font("TimesRoman", Font.BOLD, 12);
    private final Font helvetica = new Font("Helvetica", Font.BOLD, 15);
    private final FontMetrics fmetrics = getFontMetrics(roman);
    private final int h = (int) fmetrics.getHeight() / 3;

    // For double buffering.
    private Image offScreenImage;
    private Graphics offScreenGraphics;
    private Dimension offScreenSize = new Dimension(700, 500);

    InferenceGraphNode eventNode = null;
    Point clickLocation;

    /**
     * Creates new form BayesPanel.
     *
     * @param frame back-pointer to the frame
     */
    public BayesPanel(BayesGUI frame)
    {
        initComponents();
        this.frame = frame;

        // Create default InferenceGraph
        inferenceGraph = new InferenceGraph();

        // Create the group object.
        groupStart = new Point(0, 0);
        groupEnd = new Point(0, 0);

        // set initial mode to be MOVE.
        mode = EditMode.MOVE;
        setCursor(getPredefinedCursor(Cursor.MOVE_CURSOR));

        // set color for background
        setBackground(backgroundColor);
        setPreferredSize(offScreenSize);
        nodeMenu = new NodeMenu();
        setComponentPopupMenu(nodeMenu);
        addMouseListener(this);

        setNetworkConfiguration();
    }

    /**
     * Fill the values in the dialog area.
     */
    private void setNetworkConfiguration()
    {
        double globalNeighbouhoodPar;

        // Synchronize the network if necessary.
        inferenceGraph.getBayesNet();

        // Fill the name.
        frame.setNetworkNameInput(inferenceGraph.getName());

        // Fill and store network properties
        frame.setProperties(inferenceGraph.getNetworkProperties());

        // Set global neighborhood
        frame.setGlobalNeighbourhood(inferenceGraph.getGlobalNeighborhoodType());

        globalNeighbouhoodPar = inferenceGraph.getGlobalNeighborhoodParameter();
        frame.setGlobalParameter(globalNeighbouhoodPar);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        clickLocation = e.getPoint();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        clickLocation = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        clickLocation = e.getPoint();
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        clickLocation = e.getPoint();
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        clickLocation = e.getPoint();
    }

    enum NodeMenuActions
    {

        AddNode,
        DeleteNode,
        AddArc,
        DeleteArc,
        EditFunction,
        EditVariable,
        SetObservedNode,
        UnsetObservedNode,
        QueryExpectation,
        QueryExplanation,
        QueryFullExplanation,
        GetSeparation,
        SensitivityAnalysis;

        @Override
        public String toString()
        {
            return this == AddNode ? "Add Node" :
                   this == DeleteNode ? "Delete Node" :
                   this == AddArc ? "Add Arc" :
                   this == DeleteArc ? "Delete Arc" :
                   this == EditFunction ? "Edit Function" :
                   this == EditVariable ? "Edit Variable" :
                   this == SetObservedNode ? "Set observed Node" :
                   this == UnsetObservedNode ? "Unset observed Node" :
                   this == QueryExpectation ? "Query Expectation" :
                   this == QueryExplanation ? "Query Explanation" :
                   this == QueryFullExplanation ? "Query Full Explanation" :
                   this == GetSeparation ? "Get the d-separation" :
                   this == SensitivityAnalysis ? "Sensitivity Analysis" :
                   "<Unknown>";
        }

    }

    class NodeMenu extends JPopupMenu
    {

        NodeMenu()
        {
            ActionListener action = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    eventNode = getHitNode(clickLocation.x, clickLocation.y);
                    NodeMenuActions command =
                                    ((AugmentedMenuItem) e.getSource()).
                                    getMenuAction();
                    switch (command)
                    {
                        case AddNode:
                            createNode(clickLocation.x, clickLocation.y);
                            break;
                        case DeleteNode:
                            deleteNode(eventNode);
                            break;
                        case AddArc:
                            newArc = true;
                            arcBottomNode = eventNode;
                            newArcHead = new Point(clickLocation.x,
                                                   clickLocation.y);
                            mode = BayesGUI.EditMode.CREATE;
                            break;
                        case DeleteArc:
                            if (isArcHit(clickLocation.x, clickLocation.y))
                            {
                                deleteArc();
                                arcHeadNode = null;
                                arcBottomNode = null;
                            }
                            break;
                        case EditFunction:
                            if (eventNode != null)
                            {
                                editFunction(eventNode);
                            }
                            break;
                        case EditVariable:
                            if (eventNode != null)
                            {
                                editVariable(eventNode);
                            }
                            break;
                        case SetObservedNode:
                            setObserved(
                                    eventNode,
                                    true);
                            break;
                        case UnsetObservedNode:
                            setObserved(eventNode,
                                        false);
                            break;
                        case QueryExpectation:
                            processQuery(
                                    inferenceGraph,
                                    eventNode.getName(),
                                    ExplanationType.EXPECTATION);
                            break;
                        case QueryExplanation:
                            processQuery(inferenceGraph,
                                         eventNode.getName(),
                                         ExplanationType.MARKED_VARIABLES_ONLY);
                            break;
                        case QueryFullExplanation:
                            processQuery(inferenceGraph,
                                         eventNode.getName(),
                                         ExplanationType.ALL_NOT_OBSERVED_VARIABLES);
                            break;
                        case GetSeparation:
                            doSeparation(
                                    inferenceGraph,
                                    eventNode.getName());
                            break;
                        case SensitivityAnalysis:
                            processQuery(
                                    inferenceGraph,
                                    eventNode.getName(),
                                    ExplanationType.SENSITIVITY_ANALYSIS);
                            break;
                    }
                }
            };

            for (NodeMenuActions nma : NodeMenuActions.values())
            {
                AugmentedMenuItem menuItem = new AugmentedMenuItem(nma);
                menuItem.addActionListener(action);
                add(menuItem);
            }
        }

        class AugmentedMenuItem extends JMenuItem
        {

            NodeMenuActions action;

            AugmentedMenuItem(NodeMenuActions action)
            {
                super(action.toString());
                this.action = action;
            }

            NodeMenuActions getMenuAction()
            {
                return action;
            }
        }
    }

    NodeMenu nodeMenu;

    /**
     * Determine whether a node was hit by a mouse click and return the hit
     * node.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return the hit node and null otherwise
     */
    private InferenceGraphNode getHitNode(int x, int y)
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
     * Determine whether an arc was hit by a mouse click. Sets the start and end
     * points of the arc as side-effect.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return true if an arc was hit, false otherwise
     */
    boolean isArcHit(int x, int y)
    {
        double sdpa;

        for (InferenceGraphNode hnode : inferenceGraph.elements())
        {
            for (Iterator it = (hnode.getParents()).iterator(); it.hasNext();)
            {
                InferenceGraphNode pnode = (InferenceGraphNode) it.next();
                sdpa = squareDistancePointArc(hnode, pnode, x, y);
                if ((sdpa >= 0.0) && (sdpa <= DISTANCE_HIT_ARC))
                {
                    arcHeadNode = hnode;
                    arcBottomNode = pnode;
                }
            }
        }
        return arcHeadNode != null && arcBottomNode != null;
    }

    /**
     * Determine whether a point is close to the segment between two nodes
     * (hnode and pnode). if the point does not lie over or above the segment
     * return -1.0.
     *
     * @param hnode start node of the the arc to check
     * @param pnode end node of the the arc to check
     * @param x3    X-coordinate
     * @param y3    Y-coordinate
     * @return if the point does not lie over or above the segment return -1.0,
     *         otherwise the square height
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
        if (squareHyp < ((double) ((x3 - x1) * (x3 - x1) +
                                   (y3 - y1) * (y3 - y1))))
        {
            return -1.0;
        }
        // Check second extreme point
        if (squareHyp < ((double) ((x3 - x2) * (x3 - x2) +
                                   (y3 - y2) * (y3 - y2))))
        {
            return -1.0;
        }

        // Requested distance is the height of the triangle
        return squareHeight;
    }

    /**
     * Update the screen with the network.
     *
     * @param g graphics object
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
     * engine in Java2 was proposed by Michael Becke, Nov 21 2000.
     *
     * <code>
     * try {
     *      ((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,
     * RenderingHints.VALUE_ANTIALIAS_ON);
     * }
     * catch (java.lang.NoClassDefFoundError e)
     * { // Does nothing if new engine fails.
     * }
     * </code>
     *
     * Instead of using this Java2-specific code, the solution used here and
     * also proposed by Michael Becke is to fill first a whole oval with the
     * border color and then fill the inside of the circles.
     *
     * @param g the graphics
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        ExplanationType explanationStatus = frame.getCalculationType();

        if (inferenceGraph == null)
        {
            return;
        }

        // Draw a new arc upto current mouse position.
        g.setColor(arcColor);

        if (newArc)
        {
            g.drawLine(arcBottomNode.getXCoordinate(),
                       arcBottomNode.getYCoordinate(),
                       newArcHead.x,
                       newArcHead.y);
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
            if ((node.getXCoordinate()) >= 0)
            {
                g.fillOval((node.getXCoordinate()) - NODE_RADIUS - 1,
                           (node.getYCoordinate()) - NODE_RADIUS - 1,
                           NODE_SIZE + 2,
                           NODE_SIZE + 2);
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

            if ((node.getXCoordinate()) >= 0)
            {
                g.fillOval((node.getXCoordinate()) - NODE_RADIUS,
                           (node.getYCoordinate()) - NODE_RADIUS,
                           NODE_SIZE,
                           NODE_SIZE);
            }

            g.setColor(nodenameColor);
            g.drawString(node.getName(),
                         (node.getXCoordinate()) - SPACE_DRAW_NODE_NAME,
                         (node.getYCoordinate()) + SPACE_DRAW_NODE_NAME);
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
        g.drawRect(groupX,
                   groupY,
                   groupWidth,
                   groupHeight);
        g.setPaintMode();

    }

    /**
     * Auxiliary function that draws an arc.
     *
     * @param g      graphics object
     * @param node   graph node
     * @param parent parent node
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
        nodeX = node.getXCoordinate();
        nodeY = node.getYCoordinate();
        parentX = parent.getXCoordinate();
        parentY = parent.getYCoordinate();

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
     * Return the QuasiBayesNet object displayed int the NetworkPanel.
     *
     * @return the QuasiBayesNet
     */
    InferenceGraph getInferenceGraph()
    {
        return inferenceGraph;
    }

    /**
     * Store the QuasiBayesNet object to be displayed in the NetworkPanel.
     *
     * @param inferenceGraph
     */
    void load(InferenceGraph inferenceGraph)
    {
        this.inferenceGraph = inferenceGraph;
        repaint();
    }

    /**
     * Clear the NetworkPanel.
     */
    void clear()
    {
        inferenceGraph = new InferenceGraph();
        repaint();
    }

    /**
     * Set a node as observed node.
     *
     * @param node       the node that is observed
     * @param isObserved unset observe if false
     */
    void setObserved(InferenceGraphNode node, boolean isObserved)
    {
        if (node == null)
        {
            return;
        }
        if (isObserved)
        {
            inferenceGraph.resetMarginal();

            Dialog d = new SetObservedVarDialog(frame,
                                                true,
                                                node);
            d.setVisible(true);
            if (node.isObserved())
            {
                frame.metaOutput("Observed value of node '" +
                                 node.getName() +
                                 "' is " +
                                 node.getObservedValue());
            }
        }
        else
        {
            node.clearObservation();
            frame.metaOutput("Node '" +
                             node.getName() +
                             "' is no longer observed");
        }
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
     * variables arcBottomNode and arcHeadNode.
     */
    void createArc()
    {
        boolean flagCreated = inferenceGraph.createArc(arcBottomNode,
                                                       arcHeadNode);
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
            movingNodes = new ArrayList();
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
     * @param node the node to delete
     */
    void deleteNode(InferenceGraphNode node)
    {
        ArrayList<InferenceGraphNode> nodesToDelete;

        // Check whether the node is in the group.
        if (!insideGroup(node))
        {
            inferenceGraph.deleteNode(node); // Delete only the movenode.
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
     * @param node graph node
     * @return true if it is, false otherwise
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
     * variables arcBottomNode and arcHeadNode.
     */
    void deleteArc()
    {
        inferenceGraph.deleteArc(arcBottomNode, arcHeadNode);
        inferenceGraph.resetMarginal();
    }

    /**
     * Edit the components of a node.
     *
     * @param node graph node
     */
    void editVariable(InferenceGraphNode node)
    {
        inferenceGraph.resetMarginal();
        Dialog d = new EditVariableDialog(frame,
                                          inferenceGraph,
                                          node);
        d.setVisible(true);
    }

    /**
     * d-separation display routine.
     *
     * @param inferenceGraph  the inference graph on which to base the
     *                        separation
     * @param queriedVariable name of the queried variable
     */
    public void doSeparation(InferenceGraph inferenceGraph,
                             String queriedVariable)
    {
        int varIndex = inferenceGraph.getBayesNet().indexOfVariable(
            queriedVariable);
        DSeparation dsep = new DSeparation(inferenceGraph.getBayesNet());
        frame.queryOutput("Get all d-connected variables of '" +
                          queriedVariable +
                          "'\n");
        ArrayList<DiscreteVariable> conn = dsep.getDConnectedVariables(varIndex);
        String connResult = "";
        for (DiscreteVariable discrVar : conn)
        {
            connResult += discrVar.getName() + "(" + discrVar.getIndex() + ")";
        }
        frame.resultOutput(connResult + "\n");
        frame.queryOutput("get all affecting variables of '" +
                          queriedVariable +
                          "'\n");
        ArrayList<DiscreteVariable> allAff = dsep.getAllAffectingVariables(
                                    varIndex);

        String allResult = "";
        for (DiscreteVariable discrVar : allAff)
        {
            allResult += discrVar.getName() + "(" + discrVar.getIndex() + ")";
        }
        frame.resultOutput(allResult + "\n");
    }

    /**
     * Process a query.
     *
     * @param inferenceGraph
     * @param queriedVariable
     * @param modeMenuChoice
     */
    public void processQuery(InferenceGraph inferenceGraph,
                             String queriedVariable,
                             ExplanationType modeMenuChoice)
    {
        // Check whether inference is possible
        if (inferenceGraph == null)
        {
            frame.errorOutput("No Bayesian Network loaded. Load/create first.");
            return;
        }

        // This makes the whole inference
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream(bstream);
        // Print the Bayes net.
        if (frame.isBayesNetDisplay())
        {
            inferenceGraph.printBayesNet(pstream);
        }

        // Perform inference
        if (modeMenuChoice.isMarginalPosterior())
        {
            frame.queryOutput("Marginal of " + queriedVariable + "\n");
            inferenceGraph.printMarginal(pstream,
                                         queriedVariable,
                                         frame.isComputingClusters(),
                                         frame.isBucketTree());
        }
        else if (modeMenuChoice.isExpectation())
        {
            frame.queryOutput("Expectation of " + queriedVariable + "\n");
            inferenceGraph.printExpectation(pstream,
                                            queriedVariable,
                                            frame.isComputingClusters(),
                                            frame.isBucketTree());
        }
        else if (modeMenuChoice.usesMarkedVariablesOnly())
        {
            frame.queryOutput("Explanation of " +
                              queriedVariable +
                              " with subset of observed nodes" +
                              "\n");
            inferenceGraph.printExplanation(pstream, frame.isBucketTree());
        }
        else if (modeMenuChoice.usesAllNotObservedVariables())
        {
            frame.queryOutput("Full explanation of " +
                              queriedVariable +
                              "\n");
            inferenceGraph.printFullExplanation(pstream, frame.isBucketTree());
        }
        else if (modeMenuChoice.isSensitivityAnalysis())
        {
            frame.queryOutput("Sensitivity analysis of " +
                              queriedVariable +
                              "\n");
            inferenceGraph.printSensitivityAnalysis(pstream);
        }

        // Print results to output control
        frame.resultOutput(bstream.toString());

        // Close streams
        try
        {
            bstream.close();
            pstream.close();
        }
        catch (IOException e)
        {
            frame.errorOutput("Error closing stream: " +
                              e.getLocalizedMessage());
        }
    }

    /**
     * Edit the function in a node.
     *
     * @param node graph node
     */
    void editFunction(InferenceGraphNode node)
    {
        inferenceGraph.resetMarginal();
        Dialog d = new EditProbabilitiesDialog(frame,
                                               inferenceGraph,
                                               node);
        d.setVisible(true);
    }

    /**
     * Edit the network.
     */
    void editNetwork()
    {
//        ig.resetMarginal();
//        Dialog d = new EditNetworkDialog(frame, ig);
//        d.setVisible(true);
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

        setPreferredSize(new java.awt.Dimension(500, 500));
        addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
        {
            public void mouseDragged(java.awt.event.MouseEvent evt)
            {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt)
            {
                formMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 337, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMousePressed
    {//GEN-HEADEREND:event_formMousePressed
        int mouseButton = evt.getButton();
        clickLocation = evt.getPoint();
        eventNode = getHitNode(clickLocation.x, clickLocation.y);
        if (mouseButton == MouseEvent.BUTTON3)
        {
            // handled by popup-menu
        }
        else if (mouseButton == MouseEvent.BUTTON1 &&
                 eventNode != null &&
                 mode == BayesGUI.EditMode.MOVE)
        {
            movenode = eventNode;
            generateMovingNodes();
        }
        else if (mouseButton == MouseEvent.BUTTON1)
        {
            // Start the creation of a group.
            groupStart.setLocation(clickLocation.x, clickLocation.y);
            groupEnd.setLocation(clickLocation.x, clickLocation.y);
            modifyGroup = true;
        }
        repaint();

    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseReleased
    {//GEN-HEADEREND:event_formMouseReleased
        int x = evt.getX();
        int y = evt.getY();
        if (nodeMenu != null)
        {
            remove(nodeMenu);
        }
        if (movenode != null)
        {
            inferenceGraph.setCoordinates(movenode, new Point(x, y));
            movenode = null;
        }
        else if (newArc == true)
        {
            arcHeadNode = getHitNode(x, y);
            if ((arcHeadNode != null) && (arcBottomNode != null))
            {
                if (arcHeadNode == arcBottomNode)
                {
                    frame.errorOutput(JavaBayesHelpMessages.selfarc);
                }
                else if (inferenceGraph.hasCycle(arcBottomNode, arcHeadNode))
                {
                    frame.errorOutput(JavaBayesHelpMessages.circular);
                }
                else
                {
                    createArc();
                }
            }
            arcHeadNode = null;
            arcBottomNode = null;
            newArcHead = null;
            newArc = false;
            mode = BayesGUI.EditMode.MOVE;
        }
        else if (modifyGroup == true)
        {
            modifyGroup = false;
        }

        repaint();
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseDragged
    {//GEN-HEADEREND:event_formMouseDragged
        int x = evt.getX();
        int y = evt.getY();

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
    }//GEN-LAST:event_formMouseDragged

    private void formMouseMoved(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseMoved
    {//GEN-HEADEREND:event_formMouseMoved
        if (mode == BayesGUI.EditMode.CREATE && newArc)
        {
            newArcHead = new Point(evt.getX(), evt.getY());
        }
        repaint();
    }//GEN-LAST:event_formMouseMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
