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

import BayesianInferences.InferenceGraph;
import BayesianInferences.InferenceGraphNode;
import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Dieter J Kybelksties
 */
public class EditProbabilitiesDialog extends javax.swing.JDialog
{

    private static final Class CLAZZ = EditProbabilitiesDialog.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    int numValues = 0;

    class MyTableCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            Component c = super.getTableCellRendererComponent(table,
                                                              value,
                                                              isSelected,
                                                              hasFocus,
                                                              row,
                                                              column);
            if ((row / numValues) % 2 == 0)
            {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
                if (column == 0 || column == table.getColumnCount() - 1)
                {
                    c.setForeground(Color.BLUE);
                }
                if (column == table.getColumnCount() - 1)
                {
                    c.setBackground(Color.GREEN);
                }
            }
            else
            {
                c.setBackground(Color.GRAY);
                c.setForeground(Color.WHITE);
                if (column == 0 || column == table.getColumnCount() - 1)
                {
                    c.setForeground(Color.GREEN);
                }
                if (column == table.getColumnCount() - 1)
                {
                    c.setBackground(Color.BLUE);
                }
            }
            if (column == table.getColumnCount() - 1)
            {
                // set the probabilities that do not add up to 1.0 to red
                int set = row / numValues;
                double sum = 0.0;
                for (int setRow = 0; setRow < numValues; setRow++)
                {
                    sum += (Double) table.getValueAt(numValues * set + setRow,
                                                     table.getColumnCount() - 1);
                }
                if (sum != 1.0)
                {
                    for (int setRow = 0; setRow < numValues; setRow++)
                    {
                        Component c1 =
                                  super.getTableCellRendererComponent(
                                          table,
                                          value,
                                          isSelected,
                                          hasFocus,
                                          numValues *
                                          set +
                                          setRow,
                                          table.
                                          getColumnCount() - 1);
                        c1.setForeground(Color.RED);
                    }
                }
            }
            return c;
        }
    }

    /**
     * Creates new form EditProbabilitiesDialog
     *
     * @param parent
     * @param graph
     * @param node
     */
    public EditProbabilitiesDialog(java.awt.Frame parent,
                                   InferenceGraph graph,
                                   InferenceGraphNode node)
    {
        super(parent, true);
        try
        {
            initComponents();
            final String nodeName = node.getName();
            ProbabilityTableModel model = new ProbabilityTableModel(
                                  graph.getBayesNet().getFunction(nodeName));
            probabilityTable.setModel(model);
            probabilityTable.setDefaultRenderer(Object.class,
                                                new MyTableCellRenderer());
            numValues = model.getNumberOfValues();
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
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

        probabilityScrollPane = new javax.swing.JScrollPane();
        probabilityTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        probabilityTable.setModel(new javax.swing.table.DefaultTableModel(
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
        probabilityScrollPane.setViewportView(probabilityTable);

        getContentPane().add(probabilityScrollPane);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane probabilityScrollPane;
    private javax.swing.JTable probabilityTable;
    // End of variables declaration//GEN-END:variables
}
