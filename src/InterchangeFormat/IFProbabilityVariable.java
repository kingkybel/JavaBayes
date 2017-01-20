/*
 * IFProbabilityVariable.java
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
package InterchangeFormat;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Auxiliar class that stores the temporary information about a
 * ProbabilityVariable.
 */
public class IFProbabilityVariable
{

    private static final Class CLAZZ = IFProbabilityVariable.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    String name;
    ArrayList<String> properties;
    String values[];

    /**
     * Set the network name.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Set the properties.
     *
     * @param properties new String list of properties
     */
    public void setProperties(ArrayList<String> properties)
    {
        this.properties = properties;
    }

    /**
     * Set the values.
     *
     * @param values values the variable can assume as string array
     */
    public void setValues(String values[])
    {
        this.values = values;
    }

    /**
     * Retrieve the network name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieve the properties of the network.
     *
     * @return the properties
     */
    public ArrayList getProperties()
    {
        return properties;
    }

    /**
     * Retrieve the values.
     *
     * @return the values as String-array
     */
    public String[] getValues()
    {
        return values;
    }
}
