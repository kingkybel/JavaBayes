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

    private static final Class CLAZZ = InterchangeFormat.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Size of the buffer for reading and resetting streams
    private static final int MARK_READ_LIMIT = 10000;
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
     * @param rhs
     */
    public InterchangeFormat(InterchangeFormat rhs)
    {
        xmlBif03 = rhs.xmlBif03;
        xmlBif02 = rhs.xmlBif02;
        bif015 = rhs.bif015;
        bif01 = rhs.bif01;
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

        try
        {
            xmlBif03 = new Parsers.XMLBIFv03.XMLBIFv03(istream);
            xmlBif03.CompilationUnit();
            xmlBif03.invertProbabilityTables();
            return;
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
            xmlBif03 = null;
        }

        try
        {
            xmlBif02 = new Parsers.XMLBIFv02.XMLBIFv02(istream);
            xmlBif02.CompilationUnit();
            return;
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
            xmlBif02 = null;
        }

        try
        {
            bif015 = new Parsers.BIFv015.BIFv015(istream);
            bif015.CompilationUnit();
            return;
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
            bif015 = null;
        }
        // Note that the following lines are within an enclosing catch block.
        try
        {
            bif01 = new Parsers.BIFv01.BIFv01(istream);
            bif01.CompilationUnit();
        }
        catch (Throwable e1)
        { // Catch anything!
            errorMessages.append(e1);
            bif01 = null;
            throw new IFException(new String(errorMessages));
        }
    }

    /**
     *
     * @return
     */
    public IFBayesNet getBayesNetFromInterchangeFmt()
    {
        if (xmlBif03 != null && xmlBif03.getBayesNetFromInterchangeFmt() != null)
        {
            return xmlBif03.getBayesNetFromInterchangeFmt();
        }
        if (xmlBif02 != null && xmlBif02.getBayesNetFromInterchangeFmt() != null)
        {
            return xmlBif02.getBayesNetFromInterchangeFmt();
        }
        if (bif015 != null && bif015.getBayesNetFromInterchangeFmt() != null)
        {
            return bif015.getBayesNetFromInterchangeFmt();
        }
        if (bif01 != null && bif015.getBayesNetFromInterchangeFmt() != null)
        {
            return bif01.getBayesNetFromInterchangeFmt();
        }
        return (null);
    }
}
