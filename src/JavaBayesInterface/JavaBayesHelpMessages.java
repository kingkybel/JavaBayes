/*
 * JavaBayesHelpMessages.java
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

import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class JavaBayesHelpMessages
{

    // Static JavaBayes object; there must be a single JavaBayesHelpMessages
    static JavaBayes jb;

    // Help messages
    static final String versionNumberMessage = "Version 0.346";

    /**
     *
     */
    public static final String aboutMessage = "JavaBayes " +
                                              versionNumberMessage + "\n" +
                                              "Copyright 1996 - 1997 Carnegie Mellon University \n" +
                                              "Copyright 1998 - 2000 Fabio Gagliardi Cozman \n" +
                                              "<fgcozman@usp.br>\n" +
                                              "<http://www.cs.cmu.edu/~fgcozman/home.html>\n" +
                                              "JavaBayes is a system for inferences with Bayesian \n" +
                                              "networks entirely written in Java.\n" +
                                              "More documentation at\n" +
                                              "<http://www.cs.cmu.edu/~javabayes/>\n\n";

    /**
     *
     */
    public static final String startMessage =
                               "JavaBayes starts in Move mode.\n" +
                               "To start editing networks, press the Create button and\n" +
                               "click on the JavaBayes editor, or load a network using\n" +
                               "the Network->Open menu.\n\n";

    static final String createMessage =
                        "To create a new node, click the mouse button once\n" +
                        "on the area above.\n" +
                        "To connect two nodes, click on the parent node\n" +
                        "drag to the child node, and then release.\n" +
                        "To edit node attributes, click on Edit button.\n" +
                        "To move or delete nodes, click on appropriate button.\n\n";

    static final String moveMessage =
                        "To move a node, click on it and drag it to the new position.\n\n";

    static final String deleteMessage = "To delete a node, click on it.\n" +
                                        "To delete an arrow, click on the arrow's head.\n\n";

    static final String editMessage =
                        "To edit attributes of a node, click on it.\n\n";

    static final String observeMessage = "To observe a node, click on it.\n\n";

    static final String queryMessage =
                        "To query on a particular node, click on it.\n\n";

    // Error and exception messages
    static final String unexpectedEndOfInput =
                        "Unable to complete load: Unexpected end of input!\n\n";

    static final String incorrectFileFormat =
                        "Unable to complete load: Incorrect file format.\n\n";

    static final String unableReadFile = "Unable to read file!\n\n";

    static final String unableGenerateParentsDialog =
                        "Unable to generate parent values dialog!\n\n";

    static final String duplicateValues = "Duplicate value!\n\n";

    static final String nodeNameChangeFailed = "Node name change failed.\n\n";

    static final String noValueToReplace = "No new value to replace!\n\n";

    static final String noValueSelectedToReplace =
                        "No value selected for replace!\n\n";

    static final String noValueToAdd = "No value to add!\n\n";

    static final String noValueSelectedToDelete =
                        "No value selected for delete!\n\n";

    static final String observeError = "No value selected for Observe!\n\n";

    static final String notnode = "Please click on a node.\n\n";

    static final String maxnodes =
                        "Reached limit on maximum number of nodes.\n\n";

    static final String selfarc = "Can not create arc to self.\n\n";

    static final String circular = "Circular parent relations not allowed.\n\n";
    private static final Logger LOGGER =
                                Logger.getLogger(JavaBayesHelpMessages.class.
                                        getName());

    /**
     * Constructor
     *
     * @param javaBayes
     */
    public static void insert(JavaBayes javaBayes)
    {
        jb = javaBayes;
    }

    /**
     * Basic method to display messages
     *
     * @param message
     */
    public static void show(String message)
    {
        jb.appendText(message);
    }
}
