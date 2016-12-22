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

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Dieter J Kybelksties
 */
public class PropertiesTableModel
        extends AbstractTableModel
{

    private static final Class CLAZZ = PropertiesTableModel.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    class KV
    {

        KV(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
        String key;
        String value;
    }
    ArrayList<KV> propertyList = new ArrayList<>();
    static final String[] columnNames = new String[]
                  {
                      "Property",
                      "Value"
    };

    /**
     * Construct the Model from a list of properties.
     *
     * @param properties list of properties
     */
    public PropertiesTableModel(ArrayList<String> properties)
    {
        for (String prop : properties)
        {
            int splitIndex = prop.indexOf("=");
            String key = splitIndex > -1 ?
                         prop.substring(0, prop.indexOf("=")) :
                         "";
            String val = splitIndex > -1 ?
                         prop.substring(prop.indexOf("=") + 1) :
                         "";
            KV kv = new KV(key, val);
            propertyList.add(kv);
        }
    }

    ArrayList<String> getProperties()
    {
        ArrayList<String> reval = new ArrayList<>();
        for (KV prop : propertyList)
        {
            reval.add(prop.key + "=" + prop.value);
        }
        return reval;
    }

    /**
     *
     * @param newKey
     * @param newValue
     */
    public void add(String newKey, String... newValue)
    {
        if (newKey != null)
        {
            KV kv = new KV(newKey, newValue != null ? newValue[1] : "");
            propertyList.add(kv);
            fireTableDataChanged();
        }
    }

    /**
     *
     * @param row
     */
    public void remove(int row)
    {
        if (row > -1 && row < propertyList.size())
        {
            propertyList.remove(row);
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
        int row = propertyList.indexOf(value);
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
        return propertyList != null ? propertyList.size() : 0;
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
            propertyList.get(rowIndex).key = (String) aValue;
        }
        if (columnIndex == 1)
        {
            propertyList.get(rowIndex).value = (String) aValue;
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
            rowIndex > propertyList.size() ||
            columnIndex > columnNames.length)
        {
            return null;
        }
        return columnIndex == 0 ? propertyList.get(rowIndex).key :
               columnIndex == 1 ? propertyList.get(rowIndex).value : "";
    }

}
