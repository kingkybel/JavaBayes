/*
 * Bracketing.java
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
package CredalSets;

import java.util.logging.Logger;

class Bracketing
{

    private final static int MAXIMUM_ITERATIONS = 40;

    private final static int ERROR = -1;
    private final static int TOO_MANY_BISECTIONS = 0;
    private final static int EXACT_ROOT_FOUND = 1;
    private final static int APPROXIMATE_ROOT_FOUND = 2;
    private static final String CLASS_NAME = Bracketing.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    int status;

    /**
     * Perform bisection.
     */
    double perform(MappingDouble function,
                   double x1, double x2, double xAccuracy)
    {
        return (perform(function, 0, x1, x2, xAccuracy));
    }

    /**
     * Perform bisection.
     */
    double
            perform(MappingDouble function, int functionType,
                    double x1, double x2, double xAccuracy)
    {
        int j;
        double f1, f2;
        double dx, xMiddle, currentSolutionPoint;

        // Initialize variables with the function values at endpoints
        f1 = function.map(functionType, x1);
        f2 = function.map(functionType, x2);

        // Check whether endpoints are solution
        if (f1 == 0.0)
        {
            status = EXACT_ROOT_FOUND;
            return (x1);
        }

        if (f2 == 0.0)
        {
            status = EXACT_ROOT_FOUND;
            return (x2);
        }

        // Error: both endpoints have same sign
        if ((f1 * f2) > 0.0)
        {
            status = ERROR;
            return (0.0);
        }

        // Bisection goes from x (where f(x)<=0) to x + dx
        if (f1 < 0.0)
        {
            dx = x2 - x1;
            currentSolutionPoint = x1;
        }
        else
        {
            dx = x1 - x2;
            currentSolutionPoint = x2;
        }

        // Bisection loop
        for (j = 1; j <= MAXIMUM_ITERATIONS; j++)
        {
            dx *= 0.5;
            xMiddle = currentSolutionPoint + dx;
            f2 = function.map(functionType, xMiddle);
            if (f2 <= 0.0)
            {
                currentSolutionPoint = xMiddle;
            }
            // Check whether stop conditions are met
            if (f2 == 0.0)
            {
                status = EXACT_ROOT_FOUND;
                return (currentSolutionPoint);
            }
            if (Math.abs(dx) < xAccuracy)
            {
                status = APPROXIMATE_ROOT_FOUND;
                return (currentSolutionPoint);
            }
        }

        status = TOO_MANY_BISECTIONS;
        return (0.0);
    }
}
