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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class InterchangeFormat
{

    // Size of the buffer for reading and resetting streams
    private static final int MARK_READ_LIMIT = 10000;
    private static final String CLASS_NAME = InterchangeFormat.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    InputStream istream;
    Parsers.XMLBIFv03.XMLBIFv03 xmlBif03;
    Parsers.XMLBIFv02.XMLBIFv02 xmlBif02;
    Parsers.BIFv015.BIFv015 bif015;
    Parsers.BIFv01.BIFv01 bif01;

    /**
     *
     */
    public InterchangeFormat()
    {
    }

    /**
     *
     * @param is
     */
    public InterchangeFormat(InputStream is)
    {
        setStream(is);
    }

    /**
     *
     * @param is
     */
    public final void setStream(InputStream is)
    {
        istream = new BufferedInputStream(is);
    }

    /**
     *
     * @throws IFException
     * @throws Parsers.BIFv01.ParseException
     */
    public void CompilationUnit() throws Exception
    {
        StringBuffer errorMessages = new StringBuffer("Error messages\n");
        xmlBif03 = null;
        xmlBif02 = null;
        bif015 = null;
        bif01 = null;

        if (istream.markSupported())
        {
            istream.mark(MARK_READ_LIMIT);
        }
        else
        {
            errorMessages.append("\nNo support for reset operation.");
        }

        xmlBif03 = new Parsers.XMLBIFv03.XMLBIFv03(istream);
        try
        {
            xmlBif03.CompilationUnit();
            xmlBif03.invertProbabilityTables();
        }
        catch (Throwable e4)
        { // Catch anything!
            errorMessages.append(e4);
            try
            {
                istream.reset();
            }
            catch (Exception e)
            {
                errorMessages.append("\n\nReset not allowed!");
            }
            errorMessages.append("Input stream reset!\n");
            // Note that the following lines are within an enclosing catch block.
            xmlBif02 = new Parsers.XMLBIFv02.XMLBIFv02(istream);
            try
            {
                xmlBif02.CompilationUnit();
            }
            catch (Throwable e3)
            { // Catch anything!
                errorMessages.append(e3);
                try
                {
                    istream.reset();
                }
                catch (Exception e)
                {
                    errorMessages.append("\n\nReset not allowed!");
                }
                errorMessages.append("Input stream reset!\n");
                // Note that the following lines are within an enclosing catch block.
                bif015 = new Parsers.BIFv015.BIFv015(istream);
                try
                {
                    bif015.CompilationUnit();
                }
                catch (Throwable e2)
                { // Catch anything!
                    errorMessages.append(e2);
                    try
                    {
                        istream.reset();
                    }
                    catch (Exception e)
                    {
                        errorMessages.append("\n\nReset not allowed!");
                    }
                    errorMessages.append("Input stream reset!\n");
                    // Note that the following lines are within an enclosing catch block.
                    bif01 = new Parsers.BIFv01.BIFv01(istream);
                    try
                    {
                        bif01.CompilationUnit();
                    }
                    catch (Throwable e1)
                    { // Catch anything!
                        errorMessages.append(e1);
                        throw new IFException(new String(errorMessages));
                    } // End bif01
                } // End bif015
            } // End xmlBif02
        } // End xmlBif03
    }

    /**
     *
     * @return
     */
    public IFBayesNet getIfbn()
    {
        IFBayesNet ifbn = null;

        if (xmlBif03 != null)
        {
            ifbn = xmlBif03.getIfbn();
        }
        if (ifbn != null)
        {
            return (ifbn);
        }
        else
        {
            // Note that the following lines are inside an else.
            if (xmlBif02 != null)
            {
                ifbn = xmlBif02.getIfbn();
            }
            if (ifbn != null)
            {
                return (ifbn);
            }
            else
            {
                // Note that the following lines are inside an else.
                if (bif015 != null)
                {
                    ifbn = bif015.getIfbn();
                }
                if (ifbn != null)
                {
                    return (ifbn);
                }
                else
                {
                    // Note that the following lines are inside an else.
                    if (bif01 != null)
                    {
                        ifbn = bif01.getIfbn();
                    }
                    if (ifbn != null)
                    {
                        return (ifbn);
                    }
                } // End of bif01
            } // End of bif015
        } // End of xmlBif02
        return (ifbn);
    }
}
