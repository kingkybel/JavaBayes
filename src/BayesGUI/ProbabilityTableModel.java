
/*
 * @author  Dieter J Kybelksties
 * @date Jul 1, 2016
 *
 */
package BayesGUI;

import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Dieter J Kybelksties
 */
public class ProbabilityTableModel extends AbstractTableModel
{

    private static final String CLASS_NAME =
                                ProbabilityTableModel.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    ProbabilityFunction function;
    ArrayList<String> variableNames = new ArrayList<>();

    class Row implements Comparable<Row>
    {

        private final int rowNum;
        private Comparable variableValue;
        private final ArrayList<Comparable> conditionalsValues =
                                            new ArrayList<>();
        private double probability;
        private int totalColumnCount = 0;

        Row(int rowNum, ArrayList<Comparable> variables)
        {
            this.rowNum = rowNum;
            if (variables != null && variables.size() > 1)
            {
                variableValue = variables.get(0);
                for (int i = 1; i < variables.size() - 1; i++)
                {
                    conditionalsValues.add(variables.get(i));
                }
                probability = (double) variables.get(variables.size() - 1);
                totalColumnCount = variables.size();
            }

        }

        int probabiltyIndex()
        {
            return totalColumnCount - 1;
        }

        void setProbability(Double probability)
        {
            this.probability = probability;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || obj.getClass() != this.getClass())
            {
                return false;
            }
            Row other = (Row) obj;
            if (rowNum == other.rowNum &&
                (variableValue == null ?
                 other.variableValue == null :
                 variableValue.equals(other.variableValue)))
            {
                if (conditionalsValues == null &&
                    other.conditionalsValues == null)
                {
                    return true;
                }
                else if (conditionalsValues == null ||
                         other.conditionalsValues == null)
                {
                    return false;
                }
                else if (conditionalsValues.size() !=
                         other.conditionalsValues.size())
                {
                    return false;
                }
                else
                {
                    for (int i = 0; i < conditionalsValues.size(); i++)
                    {
                        if (conditionalsValues.get(i) !=
                            other.conditionalsValues.get(i))
                        {
                            return false;
                        }
                    }
                }
                return true;
            }

            return false;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 17 * hash + this.rowNum;
            hash = 17 * hash + Objects.hashCode(this.variableValue);
            hash = 17 * hash + Objects.hashCode(this.conditionalsValues);
            return hash;
        }

        @Override
        public int compareTo(Row o)
        {
            if (o == null)
            {
                return 1;
            }
            int thisSize = conditionalsValues.size();
            int thatSize = o.conditionalsValues.size();
            int minLen = min(thisSize, thatSize);
            for (int i = 0; i < minLen; i++)
            {
                int compareResult = conditionalsValues.get(i).compareTo(
                    o.conditionalsValues.get(i));
                if (compareResult != 0)
                {
                    return compareResult;
                }
            }
            if (thisSize < thatSize)
            {
                return -1;
            }
            if (thisSize > thatSize)
            {
                return 1;
            }
            return variableValue.compareTo(o.variableValue);
        }

    }

    ArrayList<Row> dataRows = new ArrayList<>();

    ProbabilityTableModel(ProbabilityFunction function)
    {
        this.function = function;
        slurpData();
    }

    private ArrayList<Integer> incrementValueIndex(
            ArrayList<Integer> currentValueIndex,
            ArrayList<String[]> valueList)
    {
        int index = currentValueIndex.size() - 1;
        boolean hasOverflow = true; // first one treated as having overflow
        while (index > -1 && hasOverflow)
        {
            int module = valueList.get(index).length;
            currentValueIndex.set(index,
                                  (currentValueIndex.get(index) + 1) % module);
            hasOverflow = currentValueIndex.get(index) == 0;
            index--;

        }
        return currentValueIndex;
    }

    private void slurpData()
    {
        ArrayList<ArrayList<Comparable>> valuesTable = new ArrayList<>();
        ArrayList<String[]> valueList = new ArrayList<>();
        ArrayList<Integer> currentValueIndex = new ArrayList<>();
        TreeSet<Row> sorted = new TreeSet<>();
        int numRows = 1;
        variableNames = new ArrayList<>();
        for (DiscreteVariable var : function.getVariables())
        {
            String varName = var.getName();
            variableNames.add(varName);
            valueList.add(var.getValues());
            currentValueIndex.add(0);
            numRows *= var.getValues().length;
        }
        variableNames.add("Probability");
        for (int row = 0; row < numRows; row++)
        {
            valuesTable.add(new ArrayList<Comparable>());
            Comparable o[] = new Comparable[valueList.size() + 1];
            valuesTable.get(row).addAll(Arrays.asList(o));
        }
        double probs[] = function.getValues();
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < valueList.size(); col++)
            {
                Comparable value =
                           valueList.get(col)[currentValueIndex.get(col)];
                valuesTable.get(row).set(col, value);
            }
            // the last index in the row holds the probability
            valuesTable.get(row).set(getColumnCount() - 1, probs[row]);
            sorted.add(new Row(row, valuesTable.get(row)));
            currentValueIndex =
            incrementValueIndex(currentValueIndex, valueList);
        }
        for (Row r : sorted)
        {
            dataRows.add(r); // can "un-sort" with the rowNum - field
        }
    }

    @Override
    public String getColumnName(int column)
    {
        return (variableNames != null && column < getColumnCount()) ?
               variableNames.get(column) : "";

    }

    @Override
    public int getRowCount()
    {
        return dataRows != null ? dataRows.size() : 0;
    }

    @Override
    public int getColumnCount()
    {
        return variableNames != null ? variableNames.size() : 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == dataRows.get(rowIndex).probabiltyIndex())
        {
            dataRows.get(rowIndex).setProbability(
                    Double.parseDouble((String) aValue));
            fireTableRowsUpdated(rowIndex, rowIndex);

        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return columnIndex == function.getVariables().length;
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
        return columnIndex == 0 ?
               dataRows.get(rowIndex).variableValue :
               columnIndex == dataRows.get(rowIndex).probabiltyIndex() ?
               dataRows.get(rowIndex).probability :
               dataRows.get(rowIndex).conditionalsValues.get(columnIndex - 1);
    }

}
