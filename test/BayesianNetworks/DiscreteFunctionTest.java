/*
 * @author  Dieter J Kybelksties
 * @date Jan 12, 2017
 *
 */
package BayesianNetworks;

import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Dieter J Kybelksties
 */
public class DiscreteFunctionTest
{

    public DiscreteFunctionTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of isParameter method, of class DiscreteFunction.
     */
    @Test
    public void testIsParameter()
    {
        System.out.println("memberOf");
        DiscreteFunction instance = new DiscreteFunction(3, 5);
        assertEquals(true, instance.isParameter(0));
        assertEquals(true, instance.isParameter(1));
        assertEquals(true, instance.isParameter(2));
        assertEquals(false, instance.isParameter(3));
        assertEquals(false, instance.isParameter(-1));

        instance = new DiscreteFunction();
        assertEquals(true, instance.isParameter(0));
        assertEquals(true, instance.isParameter(1));
        assertEquals(true, instance.isParameter(2));
        assertEquals(false, instance.isParameter(3));
        assertEquals(false, instance.isParameter(-1));
    }

    /**
     * Test of sameVariables method, of class DiscreteFunction.
     */
//    @Test
    public void testSameVariables()
    {
        System.out.println("sameVariables");
        DiscreteFunction discrFunc = null;
        DiscreteFunction instance = new DiscreteFunction();
        boolean expResult = false;
        boolean result = instance.sameVariables(discrFunc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of evaluate method, of class DiscreteFunction.
     */
    //   @Test
    public void testEvaluate()
    {
        System.out.println("evaluate");
        DiscreteVariable[] variables = null;
        int[] valueIndexes = null;
        DiscreteFunction instance = new DiscreteFunction();
        double expResult = 0.0;
        double result = instance.evaluate(variables, valueIndexes);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPositionFromIndexes method, of class DiscreteFunction.
     */
//    @Test
    public void testGetPositionFromIndexes()
    {
        System.out.println("getPositionFromIndexes");
        DiscreteVariable[] probVar = null;
        int[] variableIndexes = null;
        DiscreteFunction instance = new DiscreteFunction();
        int expResult = 0;
        int result = instance.getPositionFromIndexes(probVar, variableIndexes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sumOut method, of class DiscreteFunction.
     */
//    @Test
    public void testSumOut()
    {
        System.out.println("sumOut");
        DiscreteVariable[] newVariables = null;
        boolean[] markers = null;
        DiscreteFunction instance = new DiscreteFunction();
        DiscreteFunction expResult = null;
        DiscreteFunction result = instance.sumOut(newVariables, markers);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multiply method, of class DiscreteFunction.
     */
//    @Test
    public void testMultiply()
    {
        System.out.println("multiply");
        DiscreteVariable[] variables = null;
        DiscreteFunction multDiscrFunc = null;
        DiscreteFunction instance = new DiscreteFunction();
        DiscreteFunction expResult = null;
        DiscreteFunction result = instance.multiply(variables, multDiscrFunc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of normalize method, of class DiscreteFunction.
     */
    //   @Test
    public void testNormalize()
    {
        System.out.println("normalize");
        DiscreteFunction instance = new DiscreteFunction();
        instance.normalize();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of normalizeFirst method, of class DiscreteFunction.
     */
//    @Test
    public void testNormalizeFirst()
    {
        System.out.println("normalizeFirst");
        DiscreteFunction instance = new DiscreteFunction();
        instance.normalizeFirst();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class DiscreteFunction.
     */
//    @Test
    public void testPrint_0args()
    {
        System.out.println("print");
        DiscreteFunction instance = new DiscreteFunction();
        instance.print();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class DiscreteFunction.
     */
    //   @Test
    public void testPrint_PrintStream()
    {
        System.out.println("print");
        PrintStream out = null;
        DiscreteFunction instance = new DiscreteFunction();
        instance.print(out);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numberVariables method, of class DiscreteFunction.
     */
//    @Test
    public void testNumberVariables()
    {
        System.out.println("numberVariables");
        DiscreteFunction instance = new DiscreteFunction();
        int expResult = 0;
        int result = instance.numberVariables();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numberValues method, of class DiscreteFunction.
     */
//    @Test
    public void testNumberValues()
    {
        System.out.println("numberValues");
        DiscreteFunction instance = new DiscreteFunction();
        int expResult = 0;
        int result = instance.numberValues();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVariables method, of class DiscreteFunction.
     */
//    @Test
    public void testGetVariables()
    {
        System.out.println("getVariables");
        DiscreteFunction instance = new DiscreteFunction();
        DiscreteVariable[] expResult = null;
        DiscreteVariable[] result = instance.getVariables();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVariable method, of class DiscreteFunction.
     */
//    @Test
    public void testGetVariable()
    {
        System.out.println("getVariable");
        int index = 0;
        DiscreteFunction instance = new DiscreteFunction();
        DiscreteVariable expResult = null;
        DiscreteVariable result = instance.getVariable(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIndexes method, of class DiscreteFunction.
     */
//    @Test
    public void testGetIndexes()
    {
        System.out.println("getIndexes");
        DiscreteFunction instance = new DiscreteFunction();
        int[] expResult = null;
        int[] result = instance.getIndexes();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIndex method, of class DiscreteFunction.
     */
    //   @Test
    public void testGetIndex()
    {
        System.out.println("getIndex");
        int varIndex = 0;
        DiscreteFunction instance = new DiscreteFunction();
        int expResult = 0;
        int result = instance.getIndex(varIndex);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues method, of class DiscreteFunction.
     */
    //   @Test
    public void testGetValues()
    {
        System.out.println("getValues");
        DiscreteFunction instance = new DiscreteFunction();
        double[] expResult = null;
        double[] result = instance.getValues();
        assertArrayEquals(expResult, result, 0.0000000001);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class DiscreteFunction.
     */
//    @Test
    public void testGetValue()
    {
        System.out.println("getValue");
        int index = 0;
        DiscreteFunction instance = new DiscreteFunction();
        double expResult = 0.0;
        double result = instance.getValue(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class DiscreteFunction.
     */
    //   @Test
    public void testSetValue()
    {
        System.out.println("setValue");
        int index = 0;
        double value = 0.0;
        DiscreteFunction instance = new DiscreteFunction();
        instance.setValue(index, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValues method, of class DiscreteFunction.
     */
//    @Test
    public void testSetValues()
    {
        System.out.println("setValues");
        double[] values = null;
        DiscreteFunction instance = new DiscreteFunction();
        instance.setValues(values);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setVariable method, of class DiscreteFunction.
     */
//    @Test
    public void testSetVariable()
    {
        System.out.println("setVariable");
        int index = 0;
        DiscreteVariable discrVar = null;
        DiscreteFunction instance = new DiscreteFunction();
        instance.setVariable(index, discrVar);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
