/*
 * @author  Dieter J Kybelksties
 * @date Jun 13, 2016
 *
 */
package BayesGUI;

import BayesGUI.BayesGUI.EditMode;
import BayesianInferences.ExplanationType;
import BayesianInferences.InferenceGraph;
import BayesianInferences.InferenceGraphNode;
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
public class BayesPanel extends JPanel
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

    private final BayesGUI frame; // Used for changing mouse cursor
    // Used for changing mouse cursor
    BayesGUI.EditMode mode = BayesGUI.EditMode.MOVE; // Store the mode for events in the panel
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
    ArrayList movingNodes = null;
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

    /**
     * Creates new form BayesPanel.
     *
     * @param frame
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
    }

    enum NodeMenuActions
    {

        AddNode,
        DeleteNode,
        AddArc,
        DeleteArc,
        SetObservedNode,
        UnsetObservedNode,
        QueryNode;

        @Override
        public String toString()
        {
            return this == AddNode ? "Add Node" :
                   this == DeleteNode ? "Delete Node" :
                   this == AddArc ? "Add Arc" :
                   this == DeleteArc ? "Delete Arc" :
                   this == SetObservedNode ? "Set observed Node" :
                   this == UnsetObservedNode ? "Unset observed Node" :
                   this == QueryNode ? "Query Node" : "<Unknown>";
        }

        public boolean needsNode()
        {
            return this == AddNode ? false :
                   this == DeleteNode ? true :
                   this == AddArc ? true :
                   this == DeleteArc ? false :
                   this == SetObservedNode ? true :
                   this == UnsetObservedNode ? true :
                   this == QueryNode;
        }
    }

    class NodeMenu extends JPopupMenu
    {

        InferenceGraphNode node;

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

        NodeMenu(InferenceGraphNode node)
        {
            this.node = node;
            ActionListener action = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    NodeMenuActions command =
                                    ((AugmentedMenuItem) e.getSource()).
                                    getMenuAction();
                    switch (command)
                    {
                        case AddNode:
                            //  BayesPanel.this.upButtonActionPerformed(e);
                            break;
                        case DeleteNode:
                            //  BayesPanel.this.downButtonActionPerformed(e);
                            break;
                        case AddArc:
                            //  BayesPanel.this.deleteButtonActionPerformed(e);
                            break;
                        case DeleteArc:
                            //  BayesPanel.this.deleteButtonActionPerformed(e);
                            break;
                        case SetObservedNode:
                            BayesPanel.this.
                                    setObserved(NodeMenu.this.node, true);
                            break;
                        case UnsetObservedNode:
                            BayesPanel.this.setObserved(NodeMenu.this.node,
                                                        false);
                            break;
                        case QueryNode:
                            //  BayesPanel.this.deleteButtonActionPerformed(e);
                            break;
                    }
                }
            };

            for (NodeMenuActions nma : NodeMenuActions.values())
            {
                if (!nma.needsNode() || node != null)
                {
                    AugmentedMenuItem menuItem = new AugmentedMenuItem(nma);
                    menuItem.addActionListener(action);
                    add(menuItem);
                }
            }
        }
    }

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
        InferenceGraphNode node;
        for (Object e : inferenceGraph.elements())
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
     * Determine whether an arc was hit by a mouse click. Sets the start and end
     * points of the arc as side-effect.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return
     */
    boolean isArcHit(int x, int y)
    {
        InferenceGraphNode hnode, pnode;
        double sdpa;

        for (Object e : inferenceGraph.elements())
        {
            hnode = (InferenceGraphNode) (e);
            for (Iterator it = (hnode.getParents()).iterator(); it.hasNext();)
            {
                Object ee = it.next();
                pnode = (InferenceGraphNode) (ee);
                sdpa = squareDistancePointArc(hnode, pnode, x, y);
                if ((sdpa >= 0.0) && (sdpa <= DISTANCE_HIT_ARC))
                {
                    arcHeadNode = hnode;
                    arcBottomNode = pnode;
                }
            }
        }
        if ((arcHeadNode != null) && (arcBottomNode != null))
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
     * (hnode and pnode); if the point does not lie over or above the segment
     * return -1.0.
     *
     * @param hnode
     * @param pnode
     * @param x3    X-coordinate
     * @param y3    Y-coordinate
     * @return
     */
    double squareDistancePointArc(InferenceGraphNode hnode,
                                  InferenceGraphNode pnode,
                                  int x3,
                                  int y3)
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
     *
     * @param g
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

        InferenceGraphNode node, parent;
        ExplanationType explanationStatus = frame.getCalculationType();

        if (inferenceGraph == null)
        {
            return;
        }

        // Draw a new arc upto current mouse position.
        g.setColor(arcColor);

        if (newArc)
        {
            g.drawLine(arcBottomNode.getPosX(),
                       arcBottomNode.getPosY(),
                       newArcHead.x,
                       newArcHead.y);
        }

        // Draw all arcs.
        for (Object e : inferenceGraph.elements())
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

        for (Object e : inferenceGraph.elements())
        {
            node = (InferenceGraphNode) e;

            g.setColor(nodeBorderColor);
            if ((node.getPosX()) >= 0)
            {
                g.fillOval((node.getPosX()) - NODE_RADIUS - 1,
                           (node.getPosY()) - NODE_RADIUS - 1,
                           NODE_SIZE + 2,
                           NODE_SIZE + 2);
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

            if ((node.getPosX()) >= 0)
            {
                g.fillOval((node.getPosX()) - NODE_RADIUS,
                           (node.getPosY()) - NODE_RADIUS,
                           NODE_SIZE,
                           NODE_SIZE);
            }

            g.setColor(nodenameColor);
            g.drawString(node.getName(),
                         (node.getPosX()) - SPACE_DRAW_NODE_NAME,
                         (node.getPosY()) + SPACE_DRAW_NODE_NAME);
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
     * @param g
     * @param node
     * @param parent
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
        nodeX = node.getPosX();
        nodeY = node.getPosY();
        parentX = parent.getPosX();
        parentY = parent.getPosY();

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
     * @param mode
     */
    void setMode(BayesGUI.EditMode mode)
    {
        this.mode = mode;
    }

    /**
     * Return the QuasiBayesNet object displayed int the NetworkPanel.
     *
     * @return
     */
    InferenceGraph getInferenceGraph()
    {
        return (inferenceGraph);
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
        InferenceGraphNode node;

        if (!insideGroup(movenode))
        {
            movingNodes = null;
        }
        else
        {
            movingNodes = new ArrayList();
            for (Object e : inferenceGraph.elements())
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
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    void moveNode(int x, int y)
    {
        InferenceGraphNode node;
        int deltaX = movenode.getPosX() - x;
        int deltaY = movenode.getPosY() - y;

        // Check whether the movenode is in the group.
        if (movingNodes == null)
        {
            inferenceGraph.setPos(movenode, new Point(x, y)); // Move only the movenode.
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
                inferenceGraph.setPos(node, // Move all nodes in the group.
                                      new Point(node.getPosX() - deltaX,
                                                node.getPosY() - deltaY));
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
        InferenceGraphNode dnode;
        ArrayList nodesToDelete;

        // Check whether the node is in the group.
        if (!insideGroup(node))
        {
            inferenceGraph.deleteNode(node); // Delete only the movenode.
        }
        else
        {
            nodesToDelete = new ArrayList();
            for (Object e : inferenceGraph.elements())
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
                inferenceGraph.deleteNode(dnode); // Delete node.
            }
        }
        inferenceGraph.resetMarginal();
    }

    /**
     * Determine whether a given InferenceGraphNode is inside the group.
     *
     * @param node
     * @return true if it is, false otherwise
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
     * @param node
     */
    void editVariable(InferenceGraphNode node)
    {
        inferenceGraph.resetMarginal();
        //       Dialog d = new EditVariableDialog(this, frame, ig, node);
        //       d.setVisible(true);
    }

    /**
     * Process a query.
     *
     * @param inferenceGraph
     * @param queriedVariable
     */
    public void processQuery(InferenceGraph inferenceGraph,
                             String queriedVariable)
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
        ExplanationType modeMenuChoice = frame.getCalculationType();
        // Print the Bayes net.
        if (frame.isBayesNetDisplay())
        {
            inferenceGraph.printBayesNet(pstream);
        }

        // Perform inference
        if (modeMenuChoice.isMarginalPosterior())
        {
            inferenceGraph.printMarginal(pstream,
                                         queriedVariable,
                                         frame.isComputingClusters(),
                                         frame.isBucketTree());
        }
        else if (modeMenuChoice.isExpectation())
        {
            inferenceGraph.printExpectation(pstream,
                                            queriedVariable,
                                            frame.isComputingClusters(),
                                            frame.isBucketTree());
        }
        else if (modeMenuChoice.isSubset())
        {
            inferenceGraph.printExplanation(pstream, frame.isBucketTree());
        }
        else if (modeMenuChoice.isFull())
        {
            inferenceGraph.printFullExplanation(pstream, frame.isBucketTree());
        }
        else if (modeMenuChoice.isSensitivityAnalysis())
        {
            inferenceGraph.printSensitivityAnalysis(pstream);
        }

        // Print results to test window
        frame.resultOutput(bstream.toString());

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
     * Edit the function in a node.
     *
     * @param node
     */
    void editFunction(InferenceGraphNode node)
    {
//        ig.resetMarginal();
//        Dialog d = new EditFunctionDialog(frame, ig, node);
//        d.setVisible(true);
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

        int x = evt.getX();
        int y = evt.getY();

        InferenceGraphNode node = getHitNode(x, y);
        if (mouseButton == MouseEvent.BUTTON1 && node != null)
        {
            setComponentPopupMenu(new NodeMenu(node));
        }

//        if (node == null)
//        { // If no node was clicked on.
//            if ((mode == BayesGUI.EditMode.DELETE) && (isArcHit(x, y)))
//            { // Delete arc
//                deleteArc();
//                arcHeadNode = null;
//                arcBottomNode = null;
//            }
//            else if (mode == BayesGUI.EditMode.CREATE)
//            { // Create a node
//                createNode(x, y);
//            }
//            else
//            {
//                // Start the creation of a group.
//                groupStart.setLocation(x, y);
//                groupEnd.setLocation(x, y);
//                modifyGroup = true;
//            }
//        }
//        else
//        { // If a node was clicked on.
//            if (mode == BayesGUI.EditMode.OBSERVE)
//            { // Observe node
//                // setObserved(node);
//            }
//            else if (mode == BayesGUI.EditMode.QUERY)
//            { // Query node
//                processQuery(inferenceGraph, node.getName());
//            }
//            else if (mode == BayesGUI.EditMode.MOVE)
//            { // Move node
//                movenode = node;
//                generateMovingNodes();
//            }
//            else if (mode == BayesGUI.EditMode.DELETE)
//            { // Delete node
//                deleteNode(node);
//            }
//            else if (mode == BayesGUI.EditMode.CREATE)
//            { // Create arc
//                newArc = true;
//                arcBottomNode = node;
//                newArcHead = new Point(x, y);
//            }
//            else if (mode == BayesGUI.EditMode.EDIT_VARIABLE)
//            { // Edit variable node
//                editVariable(node);
//            }
//            else if (mode == BayesGUI.EditMode.EDIT_FUNCTION)
//            { // Edit function node
//                editFunction(node);
//            }
//        }
        repaint();

    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseReleased
    {//GEN-HEADEREND:event_formMouseReleased
        int x = evt.getX();
        int y = evt.getY();

        if (movenode != null)
        {
            inferenceGraph.setPos(movenode, new Point(x, y));
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
