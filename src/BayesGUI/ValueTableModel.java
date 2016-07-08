
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
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Dieter J Kybelksties
 */
public class ValueTableModel extends AbstractTableModel
{

    private static final String CLASS_NAME = ValueTableModel.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    ProbabilityFunction function;
    ArrayList<String> valueList = new ArrayList<>();

    ValueTableModel(ProbabilityFunction function)
    {
        this.function = function;
        valueList.addAll(Arrays.asList(function.getVariable(0).getValues()));
    }

    @Override
    public String getColumnName(int column)
    {
        return (column == 0) ? "Value" : (column == 1) ? "Comment" : "";

    }

    @Override
    public int getRowCount()
    {
        return valueList != null ? valueList.size() : 0;
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            valueList.set(rowIndex, (String) aValue);
        }
        fireTableRowsUpdated(rowIndex, rowIndex);

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return true;
    }

    @Override
    public int findColumn(String columnName)
    {
        return super.findColumn(columnName);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 || columnIndex < 0)
        {
            return null;
        }
        return valueList.get(rowIndex);
    }
}
