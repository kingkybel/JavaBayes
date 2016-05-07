/**
 * IFBayesNet.java
 *
 * @author Fabio G. Cozman Copyright 1996 - 1999, Fabio G. Cozman, Carnergie
 * Mellon University, Universidade de Sao Paulo fgcozman@usp.br,
 * http://www.cs.cmu.edu/~fgcozman/home.html
 *
 * The JavaBayes distribution is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (either version 2 of the License or, at your
 * option, any later version), provided that this notice and the name of the
 * author appear in all copies. Upon request to the author, some of the packages
 * in the JavaBayes distribution can be licensed under the GNU Lesser General
 * Public License as published by the Free Software Foundation (either version 2
 * of the License, or (at your option) any later version). If you're using the
 * software, please notify fgcozman@usp.br so that you can receive updates and
 * patches. JavaBayes is distributed "as is", in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with the JavaBayes distribution. If not, write
 * to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 */
package InterchangeFormat;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public class IFBayesNet
{
    private static final Logger LOG =
    Logger.getLogger(IFBayesNet.class.getName());

    String name;
    ArrayList properties;
    ArrayList pvs;
    ArrayList upfs;

    /**
     * Basic constructor.
     * @param n
     * @param p
     */
    public IFBayesNet(String n, ArrayList p)
    {
        name = n;
        properties = p;
        pvs = new ArrayList();
        upfs = new ArrayList();
    }

    /**
     *
     * @return
     */
    public String get_name()
    {
        return (name);
    }

    /**
     *
     * @return
     */
    public ArrayList get_properties()
    {
        return (properties);
    }

    /**
     *
     * @return
     */
    public ArrayList get_pvs()
    {
        return (pvs);
    }

    /**
     *
     * @return
     */
    public ArrayList get_upfs()
    {
        return (upfs);
    }

    /**
     * Method that adds a ProbabilityVariable object to the vector of variables.
     * @param pv
     */
    public void add(IFProbabilityVariable pv)
    {
        pvs.add(pv);
    }

    /**
     * Method that adds a IFProbabilityFunction object to the vector of
     * functions.
     * @param upf
     */
    public void add(IFProbabilityFunction upf)
    {
        upfs.add(upf);
    }

    /**
     * Method that inverts the tables in the ProbabilityFunction objects;
     * necessary for formats that put the GIVEN variables as the lowest running
     * indexes in the tables.
     */
    public void invert_probability_tables()
    {
        for (Object e : upfs)
        {
            ((IFProbabilityFunction) (e)).invert_tables(this);
        }
    }
}
