
/*
 * @author  Dieter J Kybelksties
 * @date Jul 1, 2016
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

    private static final String CLASS_NAME = ValueTableModel.class.getName();
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
     *
     * @param function
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
     *
     * @param newValue
     * @param comment
     */
    public void add(String newValue, String... comment)
    {
        if (newValue != null)
        {
            valueList.add(newValue);
            commentList.add((comment != null && comment.length > 0) ?
                            comment[0] :
                            "");
            listModel.addElement(newValue);
            fireTableDataChanged();
        }
    }

    /**
     *
     * @param row
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
     *
     * @param rows
     */
    public void remove(int[] rows)
    {
        for (int row : rows)
        {
            remove(row);
        }
    }

    /**
     *
     * @param value
     */
    public void remove(String value)
    {
        int row = valueList.indexOf(value);
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
