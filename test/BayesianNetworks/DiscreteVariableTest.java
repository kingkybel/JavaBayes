/*
 * @author  Dieter J Kybelksties
 * @date Jan 12, 2017
 *
 */
package BayesianNetworks;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author kybelksd
 */
public class DiscreteVariableTest
{

    private static final Logger LOG =
                                Logger.getLogger(DiscreteVariableTest.class.
                                        getName());

    /**
     * Set up the class for testing.
     */
    @BeforeClass
    public static void setUpClass()
    {
    }

    /**
     * Tear down the class after testing.
     */
    @AfterClass
    public static void tearDownClass()
    {
    }

    /**
     * Default constructor.
     */
    public DiscreteVariableTest()
    {
    }

    /**
     * Set up a test.
     */
    @Before
    public void setUp()
    {
    }

    /**
     * Tear down a test.
     */
    @After
    public void tearDown()
    {
    }

    /**
     * Test of indexOfValue method, of class DiscreteVariable.
     */
    @Test
    public void testIndexOfValue()
    {
        System.out.println("indexOfValue");
        DiscreteVariable instance = new DiscreteVariable("indexTest",
                                                         0,
                                                         new String[]
                                                         {
                                                             "1.1", "2.2", "3.3"
                                                         });
        int result = instance.indexOfValue("1.1");
        assertEquals(0, result);
        result = instance.indexOfValue("2.2");
        assertEquals(1, result);
        result = instance.indexOfValue("3.3");
        assertEquals(2, result);
        result = instance.indexOfValue("0.0");
        assertEquals(BayesNet.INVALID_INDEX, result);
        result = instance.indexOfValue(null);
        assertEquals(BayesNet.INVALID_INDEX, result);
    }

    /**
     * Test of getNumericValues method, of class DiscreteVariable.
     */
    @Test
    public void testGetNumericValues()
    {
        System.out.println("getNumericValues");
        DiscreteVariable instance = new DiscreteVariable("numericValuesTest",
                                                         0,
                                                         new String[]
                                                         {
                                                             "1.1", "2.2", "3.3"
                                                         });
        DiscreteFunction result = instance.getNumericValues();
        double[] expectedResult = new double[]
         {
             1.1, 2.2, 3.3
        };
        assertArrayEquals(expectedResult, result.getValues(), 0.00000000000001);
        instance = new DiscreteVariable("numericValuesTest",
                                        0,
                                        new String[]
                                        {
                                            "USE DEFAULT",
                                            "1.1",
                                            "2.2",
                                            "3.3",
                                            "Another inconvertible"
                                        });
        result = instance.getNumericValues();
        expectedResult =
        new double[]
        {
            0.0, 1.1, 2.2, 3.3, 4.0
        };
        assertArrayEquals(expectedResult, result.getValues(), 0.00000000000001);

    }

    /**
     * Test of numberValues method, of class DiscreteVariable.
     */
    @Test
    public void testNumberValues()
    {
        System.out.println("numberValues");
        DiscreteVariable instance = new DiscreteVariable();
        int expResult = 0;
        int result = instance.numberValues();
        assertEquals(expResult, result);

        instance = new DiscreteVariable("numericValuesTest",
                                        0,
                                        new String[]
                                        {
                                            "1.1", "2.2", "3.3"
                                        });
        expResult = 3;
        result = instance.numberValues();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValues method, of class DiscreteVariable.
     */
    @Test
    public void testGetValues()
    {
        System.out.println("getValues");
        DiscreteVariable instance = new DiscreteVariable();
        String[] expResult = null;
        String[] result = instance.getValues();
        assertArrayEquals(expResult, result);
        instance = new DiscreteVariable("numericValuesTest",
                                        0,
                                        new String[]
                                        {
                                            "1.1", "2.2", "3.3"
                                        });
        expResult =
        new String[]
        {
            "1.1", "2.2", "3.3"
        };
        result = instance.getValues();
        assertArrayEquals(expResult, result);

        for (int i = 0; i < result.length; i++)
        {
            assertEquals(expResult[i], result[i]);
        }
    }

}
