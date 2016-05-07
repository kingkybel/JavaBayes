package InferenceGraphs;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import CredalSets.QBProbabilityFunction;
import CredalSets.VertexSet;
import java.awt.Point;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public final class InferenceGraphNode
{
    private static final Logger LOG =
    Logger.getLogger(InferenceGraphNode.class.
            getName());

    InferenceGraph ig;

    ProbabilityVariable pv;
    ProbabilityFunction pf;

    ArrayList parents = new ArrayList();
    ArrayList children = new ArrayList();

    Point pos;

    private final String defaultInferenceGraphNodeValues[] =
    {
        "true", "false"
    };
    private final BayesNet defaultInferenceGraphNodeBayesNet = null;
    private final ArrayList defaultInferenceGraphNodeProperties = null;

    /*
     * Default constructor for an InferenceGraphNode.
     */
    InferenceGraphNode(InferenceGraph i_g, String name)
    {
        this(i_g, name, new Point(100, 100));
    }

    /*
     * Constructor for a InferenceGraphNode object. The created
     * node is in an incomplete state; the constructor assumes the
     * node is new and not necessarily attached to the current
     * network in the InferenceGraph; no parents nor
     * children are defined for such a node.
     */
    InferenceGraphNode(InferenceGraph i_g, String name, Point position)
    {
        ig = i_g;

        // Initialize the variable
        pv = new ProbabilityVariable(defaultInferenceGraphNodeBayesNet,
                                     name, BayesNet.INVALID_INDEX,
                                     defaultInferenceGraphNodeValues,
                                     defaultInferenceGraphNodeProperties);
        // Initialize the probability function
        init_dists();
        // Initialize the position of the node
        pos = position;
    }

    /*
     * Constructor for a InferenceGraphNode object.
     * Note that parents and children are not properly set here.
     */
    InferenceGraphNode(InferenceGraph i_g,
                       ProbabilityVariable p_v, ProbabilityFunction p_f)
    {
        ig = i_g;
        pv = p_v;
        pf = p_f;
        pos = parse_position(p_v);
    }

    /*
     * Constructor for a InferenceGraphNode object.
     * Note that parents and children are not properly set here.
     */
    InferenceGraphNode(InferenceGraph i_g,
                       ProbabilityVariable p_v, ProbabilityFunction p_f,
                       Point position)
    {
        ig = i_g;
        pv = p_v;
        pf = p_f;
        pos = position;
    }

    /*
     * Initialization for the probability function
     * in the InferenceGraphNode.
     */
    void init_dists()
    {
        int i, total_values;
        double new_value;
        InferenceGraphNode pnode;

        // Create the probability_variables
        ProbabilityVariable pvs[] =
                              new ProbabilityVariable[parents.size() + 1];
        pvs[0] = pv;

        total_values = pv.number_values();
        new_value = 1.0 / ((double) (total_values));

        i = 1;
        for (Object e : parents)
        {
            pnode = (InferenceGraphNode) (e);
            pvs[i] = pnode.pv;
            total_values *= pnode.pv.number_values();
            i++;
        }

        // Compute the default (uniformly distributed) values
        double dists[] = new double[total_values];
        for (i = 0; i < dists.length; i++)
        {
            dists[i] = new_value;
        }

        // Construct the ProbabilityFunction
        pf = new ProbabilityFunction(defaultInferenceGraphNodeBayesNet,
                                     pvs,
                                     dists,
                                     defaultInferenceGraphNodeProperties);
    }

    /*
     * Update the position property.
     */
    void update_position()
    {
        ArrayList properties = pv.get_properties();
        ArrayList properties_to_remove = new ArrayList();
        String final_property = null;
        String s, ss;

        if ((properties != null) && (properties.size() > 0))
        {
            for (Object e : properties)
            {
                ss = (String) e;
                s = ss.trim();

                // If property is not position, skip it
                if (!s.startsWith("position"))
                {
                    continue;
                }

                //Schedule the current position property for removal
                properties_to_remove.add(ss);
            }

            // Remove the old position properties
            for (Object e : properties_to_remove)
            {
                ss = (String) (e);
                pv.remove_property(ss);
            }
        }

        // Build the new position property
        final_property = "position = (" +
                         pos.x + ", " + pos.y + ")";
        // Insert the new position
        pv.add_property(final_property);
    }

    /*
     * Get the position of a InferenceGraphNode from the
     * properties in the variable.
     */
    private Point parse_position(ProbabilityVariable p_v)
    {
        ArrayList properties = p_v.get_properties();
        Point final_position = null;
        String s, ss;

        // get position values from the list of properties
        if (properties.isEmpty())
        {
            return (new Point(100, 100));
        }

        try
        {
            for (Object e : properties)
            {
                ss = (String) e;
                s = ss.trim();

                // If property is not position, skip it
                if (!s.startsWith("position"))
                {
                    continue;
                }

                // Parse the position property
                StreamTokenizer st =
                                new StreamTokenizer(new StringBufferInputStream(
                                                s));
                st.parseNumbers();
                int tok;
                int x = -1, y = 0;
                while ((tok = st.nextToken()) != StreamTokenizer.TT_EOF)
                {
                    if (tok != StreamTokenizer.TT_NUMBER)
                    {
                        continue;
                    }
                    if (x == -1)
                    {
                        x = (int) st.nval;
                    }
                    else
                    {
                        y = (int) st.nval;
                        final_position = new Point(x, y);
                    }
                }
                break;
            }
        }
        catch (IOException e)
        {
            final_position = new Point(100, 100);
        }
        if (final_position == null)
        {
            final_position = new Point(100, 100);
        }

        return (final_position);
    }

    /**
     * Get a single value of the probability function in the node given a list
     * of pairs (Variable Value). The list specifies which element of the
     * function is referred to.
     *
     * @param variable_value_pairs
     * @param index_extreme_point
     * @return
     */
    public double get_function_value(String variable_value_pairs[][],
                                     int index_extreme_point)
    {
        if (pf instanceof VertexSet)
        {
            return (((VertexSet) pf).evaluate(variable_value_pairs,
                                              index_extreme_point));
        }
        else
        {
            return (pf.evaluate(variable_value_pairs));
        }
    }

    /**
     * Get an array containing probability values.
     *
     * @return
     */
    public double[] get_function_values()
    {
        if (pf instanceof VertexSet)
        {
            double[][] ep = ((VertexSet) pf).get_extreme_points();
            return (ep[0]);
        }
        else
        {
            return (pf.get_values());
        }
    }

    /**
     * Get an array containing probability values; if credal set, return the
     * first extreme point.
     *
     * @param index
     * @return
     */
    public double[] get_function_values(int index)
    {
        if (pf instanceof VertexSet)
        {
            double[][] ep = ((VertexSet) pf).get_extreme_points();
            return (ep[index]);
        }
        else
        {
            return (pf.get_values());
        }
    }

    /**
     * Set an array containing probability values; if credal set, insert the
     * array in the first extreme point.
     *
     * @param fv
     */
    public void set_function_values(double[] fv)
    {
        if (pf instanceof VertexSet)
        {
            ((VertexSet) pf).set_extreme_point(0, fv);
        }
        else
        {
            pf.set_values(fv);
        }
    }

    /**
     * Set an array containing an extreme point of the credal set.
     *
     * @param iep
     * @param fv
     */
    public void set_function_values(int iep, double[] fv)
    {
        if (pf instanceof VertexSet)
        {
            ((VertexSet) pf).set_extreme_point(iep, fv);
        }
        else
        {
            if (iep == 0)
            {
                pf.set_values(fv);
            }
        }
    }

    /**
     * Get a single value of the probability function in the node given the
     * index of the value and the index of the extreme point.
     */
//    public double get_function_value(int index, int index_extreme_point) {
//        if (pf instanceof VertexQBProbabilityFunction)
//            return( ((VertexQBProbabilityFunction)pf).get_value(index, index_extreme_point) );
//        else
//            return(pf.get_value(index));
//    }
    /**
     * Get a single value of the probability function in the node given the
     * index of the value.
     */
//    public double get_function_value(int index) {
//        if (pf instanceof VertexQBProbabilityFunction)
//            return( ((VertexQBProbabilityFunction)pf).get_value(index, 0) );
//        else
//            return(pf.get_value(index));
//    }
    /**
     * Set a single value of the probability function in the node given a list
     * of pairs (Variable Value). The list specifies which element of the
     * function is referred to.
     *
     * @param variable_value_pairs
     * @param index_extreme_point
     * @param val
     */
    public void set_function_value(String variable_value_pairs[][], double val,
                                   int index_extreme_point)
    {
        if (pf instanceof VertexSet)
        {
            ((VertexSet) pf).set_value(variable_value_pairs, val,
                                       index_extreme_point);
        }
        else
        {
            pf.set_value(variable_value_pairs, val);
        }
    }

    /* ******************** Public methods ******************** */
    /**
     * Return the name of the variable in the node.
     *
     * @return
     */
    public String get_name()
    {
        return (pv.get_name());
    }

    /**
     * Set the name of the variable.
     *
     * @param n
     */
    public void set_name(String n)
    {
        pv.set_name(n);
    }

    /**
     * Get the name of all variables in the probability function.
     *
     * @return
     */
    public String[] get_all_names()
    {
        String[] ns = new String[pf.number_variables()];
        for (int i = 0; i < ns.length; i++)
        {
            ns[i] = pf.get_variable(i).get_name();
        }
        return (ns);
    }

    /**
     * Return the values of the variable in the node.
     *
     * @return
     */
    public String[] get_values()
    {
        return (pv.get_values());
    }

    /**
     * Get all values for variables in the function in the node.
     *
     * @return
     */
    public String[][] get_all_values()
    {
        int i, j;
        String all_values[][] = new String[pf.number_variables()][];
        DiscreteVariable dv;
        for (i = 0; i < pf.number_variables(); i++)
        {
            dv = pf.get_variable(i);
            all_values[i] = new String[dv.number_values()];
            for (j = 0; j < all_values[i].length; j++)
            {
                all_values[i][j] = dv.get_value(j);
            }
        }
        return (all_values);
    }

    /**
     * Return the number of values in the variable in the node.
     *
     * @return
     */
    public int get_number_values()
    {
        return (pv.number_values());
    }

    /**
     * Indicate whether the node has parents.
     *
     * @return
     */
    public boolean hasParent()
    {
        return (pf.number_variables() > 1);
    }

    /**
     * Return the parents of a node as an Iterator object.
     *
     * @return
     */
    public ArrayList get_parents()
    {
        return (parents);
    }

    /**
     * Return the children of a node as an Iterator object.
     *
     * @return
     */
    public ArrayList get_children()
    {
        return (children);
    }

    /**
     * Indicate whether the variable in the node is observed.
     *
     * @return
     */
    public boolean is_observed()
    {
        return (pv.is_observed());
    }

    /**
     * Indicate whether the variable in the node is an explanatory variable.
     *
     * @return
     */
    public boolean is_explanation()
    {
        return (pv.is_explanation());
    }

    /**
     * Return the observed value for the variable in the node.
     *
     * @return
     */
    public int get_observed_value()
    {
        return (pv.get_observed_index());
    }

    /**
     * Return the X position of the node.
     *
     * @return
     */
    public int get_pos_x()
    {
        return (pos.x);
    }

    /**
     * Return the Y position of the node.
     *
     * @return
     */
    public int get_pos_y()
    {
        return (pos.y);
    }

    /**
     * Return the variable properties
     *
     * @return
     */
    public ArrayList get_variable_properties()
    {
        return (pv.get_properties());
    }

    /**
     * Set the variable properties.
     *
     * @param prop
     */
    public void set_variable_properties(ArrayList prop)
    {
        pv.set_properties(prop);
    }

    /**
     * Return the function properties.
     *
     * @return
     */
    public ArrayList get_function_properties()
    {
        return (pf.get_properties());
    }

    /**
     * Set the function properties.
     *
     * @param prop
     */
    public void set_function_properties(ArrayList prop)
    {
        pf.set_properties(prop);
    }

    /**
     * Whether or not the node represents a convex set of distributions (credal
     * set).
     *
     * @return
     */
    public boolean is_credal_set()
    {
        if (pf instanceof QBProbabilityFunction)
        {
            return (true);
        }
        else
        {
            return (false);
        }
    }

    /**
     * Number of distributions that are represented by a node.
     *
     * @return
     */
    public int number_extreme_distributions()
    {
        if (pf instanceof VertexSet)
        {
            return (((VertexSet) pf).get_extreme_points().length);
        }
        else
        {
            return (1);
        }
    }

    /**
     * Make sure the node represents a single distribution.
     */
    public void set_no_local_credal_set()
    {
        if (pf instanceof QBProbabilityFunction)
        {
            if (pf instanceof VertexSet)
            {
                ((VertexSet) pf).compose_values();
            }
            pf = new ProbabilityFunction(pf, pf.get_values());
        }
    }

    /**
     * Make sure the node represents a VertexSet a given number of extreme
     * distributions.
     *
     * @param number_extreme_points
     */
    public void set_local_credal_set(int number_extreme_points)
    {
        if (!(pf instanceof VertexSet))
        {
            pf = new VertexSet(pf);
        }
        ((VertexSet) pf).set_local_credal_set(number_extreme_points);
    }

    /**
     * Make sure the node represents a VertexSet.
     */
    public void set_local_credal_set()
    {
        if (!(pf instanceof VertexSet))
        {
            pf = new VertexSet(pf);
        }
    }

    /**
     * Set the observation for the node.
     *
     * @param value
     */
    public void set_observation_value(String value)
    {
        pv.set_observed_value(value);
    }

    /**
     * Clear the observation for the node.
     */
    public void clear_observation()
    {
        pv.set_invalid_observed_index();
    }

    /**
     * Set the explanatory status of the node.
     *
     * @param flag
     */
    public void set_explanation(boolean flag)
    {
        if (flag == true)
        {
            pv.set_explanation_value(0);
        }
        else
        {
            pv.set_explanation_value(BayesNet.INVALID_INDEX);
        }
    }

    /**
     * Remove a property from a variable.
     *
     * @param index
     */
    public void remove_variable_property(int index)
    {
        pv.remove_property(index);
    }

    /**
     * Remove a property from a function.
     *
     * @param index
     */
    public void remove_function_property(int index)
    {
        pf.remove_property(index);
    }

    /**
     * Add a property to a variable.
     *
     * @param s
     */
    public void add_variable_property(String s)
    {
        pv.add_property(s);
        update_position_from_property(s);
    }

    /*
     * Update the position of a node given a property.
     */
    /**
     *
     * @param s
     */
    public void update_position_from_property(String s)
    {
        // If property is position:
        if (s.startsWith("position"))
        {
            Point final_position = null;
            // Parse the position property
            try
            {
                StreamTokenizer st =
                                new StreamTokenizer(new StringBufferInputStream(
                                                s));
                st.parseNumbers();
                int tok;
                int x = -1, y = 0;
                while ((tok = st.nextToken()) != StreamTokenizer.TT_EOF)
                {
                    if (tok != StreamTokenizer.TT_NUMBER)
                    {
                        continue;
                    }
                    if (x == -1)
                    {
                        x = (int) st.nval;
                    }
                    else
                    {
                        y = (int) st.nval;
                        final_position = new Point(x, y);
                    }
                }
            }
            catch (IOException e)
            {
                final_position = new Point(100, 100);
            }
            if (final_position == null)
            {
                final_position = new Point(100, 100);
            }
            // Update the position property.
            pos = final_position;
        }
    }

    /**
     * Add a property from to function.
     *
     * @param prop
     */
    public void add_function_property(String prop)
    {
        pf.add_property(prop);
    }

}
