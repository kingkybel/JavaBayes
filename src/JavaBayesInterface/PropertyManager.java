/*
 * PropertyManager.java
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
package JavaBayesInterface;

import java.awt.TextField;
import java.util.ArrayList;
import java.util.logging.Logger;

class PropertyManager
{

    private static final String CLASS_NAME = PropertyManager.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // The original properties.
    private final ArrayList originalProperties;
    // The vector holding a copy of the relevant properties.
    private final ArrayList modifiedProperties;
    // A flag indicating whether the original properties are valid.
    private boolean areOriginalPropertiesStillValid;
    // The index of the displayed property;
    private int displayedPropertyIndex;
    // The text field that displays the property;
    private final TextField textField;

    /**
     * Default constructor for PropertyManager.
     */
    PropertyManager(ArrayList op, TextField tf)
    {
        String property;

        // Copy the inputs into internal variables.
        originalProperties = op;
        textField = tf;
        // Make the copy of the properties.
        modifiedProperties = new ArrayList();
        if (op != null)
        {
            for (Object e : op)
            {
                modifiedProperties.add(e);
            }
        }
        areOriginalPropertiesStillValid = true;
        // Now display if possible.
        if (modifiedProperties.size() > 0)
        {
            property = (String) (modifiedProperties.get(0));
            textField.setText(property);
            displayedPropertyIndex = 0;
        }
        else
        {
            displayedPropertyIndex = -1;
        }
    }

    /**
     * Create a new property.
     */
    void newProperty()
    {
        String property;

        if (displayedPropertyIndex != -1)
        {
            property = (String) modifiedProperties.
            get(displayedPropertyIndex);
        }
        else
        {
            property = "";
        }
        if (!(property.equals(textField.getText())))
        {
            updateProperty();
        }

        displayedPropertyIndex = -1;
        textField.setText("");
    }

    /**
     * Go to the next property.
     */
    void nextProperty()
    {
        String property;

        if (displayedPropertyIndex != -1)
        {
            property =
            (String) (modifiedProperties.get(displayedPropertyIndex));
        }
        else
        {
            property = "";
        }
        if (!(property.equals(textField.getText())))
        {
            updateProperty();
        }

        if (modifiedProperties.size() > 0)
        {
            displayedPropertyIndex++;
            if (displayedPropertyIndex >= modifiedProperties.size())
            {
                displayedPropertyIndex = 0;
            }
            property =
            (String) (modifiedProperties.get(displayedPropertyIndex));
            textField.setText(property);
        }
    }

    /**
     * Modify a property when changes were detected.
     */
    void updateProperty()
    {
        if (displayedPropertyIndex != -1)
        {
            modifiedProperties.remove(displayedPropertyIndex);
        }
        if (textField.getText().isEmpty())
        { // Property was deleted.
            displayedPropertyIndex = -1;
        }
        else
        { // Property is new or modified.
            modifiedProperties.add(textField.getText());
            displayedPropertyIndex =
            modifiedProperties.indexOf(textField.getText());
        }
        areOriginalPropertiesStillValid = false;
    }

    /**
     * Modify a property when the dialog exits.
     */
    ArrayList updatePropertyOnExit()
    {
        String property;

        if (displayedPropertyIndex != -1)
        {
            property =
            (String) (modifiedProperties.get(displayedPropertyIndex));
        }
        else
        {
            property = "";
        }
        if (!(property.equals(textField.getText())))
        {
            updateProperty();
        }
        if (areOriginalPropertiesStillValid == false)
        {
            return (modifiedProperties);
        }
        else
        {
            return (null);
        }
    }
}
