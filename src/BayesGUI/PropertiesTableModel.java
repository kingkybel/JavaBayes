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
 * Table model class for properties as Key-value-pairs.
 *
 * @author Dieter J Kybelksties
 */
public class PropertiesTableModel
        extends AbstractTableModel
{

    private static final Class CLAZZ = PropertiesTableModel.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    static final String[] columnNames = new String[]
                  {
                      "Property",
                      "Value"
    };
    ArrayList<KV> propertyList = new ArrayList<>();

    /**
     * Construct the Model from a list of properties.
     *
     * @param properties list of properties
     */
    public PropertiesTableModel(ArrayList<String> properties)
    {
        for (String prop : properties)
        {
            int splitIndex = prop.indexOf('=');
            String key = splitIndex > -1 ?
                         prop.substring(0, prop.indexOf('=')) :
                         "";
            String val = splitIndex > -1 ?
                         prop.substring(prop.indexOf('=') + 1) :
                         "";
            KV kv = new KV(key, val);
            propertyList.add(kv);
        }
    }

    /**
     * Retrieve properties as list of strings of the form [key]=[value].
     *
     * @return the property list
     */
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
     * Add a new key-value-pair.
     *
     * @param newKey   given key
     * @param newValue new value
     * @throws java.lang.Exception
     */
    public void add(String newKey, String newValue) throws Exception
    {
        if (newKey == null || newKey.isEmpty())
        {
            throw new Exception("Cannot add empty property.");
        }
        KV kv = new KV(newKey, newValue != null ? newValue : "");
        propertyList.add(kv);
        fireTableDataChanged();
    }

    /**
     * Add a new key-value-pair with empty value.
     *
     * @param newKey given key
     * @throws java.lang.Exception
     */
    public void add(String newKey) throws Exception
    {
        add(newKey, null);
    }

    /**
     * Remove row by index.
     *
     * @param row row-index
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
     * Remove rows by index.
     *
     * @param rows array of row-indices
     */
    public void remove(int[] rows)
    {
        for (int row : rows)
        {
            remove(row);
        }
    }

    /**
     * Remove rows by key.
     *
     * @param key key to remove from the list
     */
    public void remove(String key)
    {
        for (int i = 0; i < propertyList.size(); i++)
        {
            if (propertyList.get(i).key.equals(key))
            {
                remove(i);
            }
        }
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

    /**
     * Key/Value convenience class.
     */
    class KV
    {

        String key;
        String value;

        /**
         * Construct from given key and value.
         *
         * @param key   given key
         * @param value new value
         */
        KV(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
    }

}
