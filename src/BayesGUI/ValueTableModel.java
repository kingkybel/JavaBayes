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

import BayesianNetworks.ProbabilityFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Dieter J Kybelksties
 */
public class ValueTableModel
        extends AbstractTableModel
        implements ListModel<Object>
{

    private static final Class<ValueTableModel> CLAZZ = ValueTableModel.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    ProbabilityFunction function;
    ArrayList<String> valueList = new ArrayList<>();
    ArrayList<String> commentList = new ArrayList<>();
    DefaultListModel listModel = new DefaultListModel();
    static final String[] columnNames = new String[]
                  {
                      "Value",
                      "Comment"
    };

    /**
     * Construct the ValueTableModel.
     *
     * @param function a probability function
     */
    public ValueTableModel(ProbabilityFunction function)
    {
        this.function = function;
        valueList.addAll(Arrays.asList(function.getVariable(0).getValues()));
        for (String value : valueList)
        {
            listModel.addElement(value);
            commentList.add("");
        }
    }

    /**
     * Add a new value to the list.
     *
     * @param newValue the new value
     * @param comments optional comments
     */
    public void add(String newValue, String... comments)
    {
        if (newValue != null)
        {
            valueList.add(newValue);
            if (comments != null && comments.length > 0)
            {
                commentList.addAll(Arrays.asList(comments));
            }
            else
            {
                commentList.add("");
            }
            listModel.addElement(newValue);
            fireTableDataChanged();
        }
    }

    /**
     * Remove a row from the model.
     *
     * @param row row-index
     */
    public void remove(int row)
    {
        if (row > -1 && row < valueList.size())
        {
            valueList.remove(row);
            commentList.remove(row);
            listModel.remove(row);
            fireTableDataChanged();
        }
    }

    /**
     * Remove some rows from the model.
     *
     * @param rows row-indices
     */
    public void remove(int[] rows)
    {
        for (int row : rows)
        {
            remove(row);
        }
    }

    /**
     * Remove a value from the list.
     *
     * @param value the value to remove
     */
    public void remove(String value)
    {
        if (value == null)
        {
            return;
        }
        int row = valueList.indexOf(value);
        if (row == -1)
        {
            return;
        }
        remove(row);
    }

    @Override // AbstractTableModel
    public String getColumnName(int column)
    {
        return column > -1 && column < columnNames.length ?
               columnNames[column] :
               "";
    }

    @Override // AbstractTableModel
    public int getRowCount()
    {
        return valueList != null ? valueList.size() : 0;
    }

    @Override // AbstractTableModel
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override // AbstractTableModel
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            valueList.set(rowIndex, (String) aValue);
        }
        if (columnIndex == 1)
        {
            commentList.set(rowIndex, (String) aValue);
        }
        fireTableRowsUpdated(rowIndex, rowIndex);

    }

    @Override // AbstractTableModel
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return true;
    }

    @Override // AbstractTableModel
    public int findColumn(String columnName)
    {
        return super.findColumn(columnName);
    }

    @Override // AbstractTableModel
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 ||
            columnIndex < 0 ||
            rowIndex > valueList.size() ||
            columnIndex > columnNames.length)
        {
            return null;
        }
        return columnIndex == 0 ? valueList.get(rowIndex) :
               columnIndex == 1 ? commentList.get(rowIndex) : "";
    }

    @Override // ListModel
    public int getSize()
    {
        return listModel.size();
    }

    @Override // ListModel
    public Object getElementAt(int index)
    {
        return listModel.get(index);
    }

    @Override // ListModel
    public void addListDataListener(ListDataListener l)
    {
        listModel.addListDataListener(l);
    }

    @Override // ListModel
    public void removeListDataListener(ListDataListener l)
    {
        listModel.removeListDataListener(l);
    }
}
