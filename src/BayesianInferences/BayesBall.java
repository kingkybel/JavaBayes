/*
 * Copyright (C) 2015 Dieter J Kybelksties
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * @author  Dieter J Kybelksties
 * @date Feb 7, 2017
 *
 */
package BayesianInferences;

import BayesianNetworks.ProbabilityVariable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * An implementation of the Bayes-Ball Algorithm to find the d-separation of a
 * conditional expression.
 *
 * The Bayes Ball Algorithm determines the relevant nodes that need to be
 * considered given a conditional probability expression It returns conditional
 * independent nodes, irrelevant nodes and requisite probability nodes Answers
 * questions like: <b>P(x,z|y)</b> When we condition on y, are x and z
 * independent?
 *
 * A structured belief network <b>B = (N;A;F)</b> consists of nodes N and
 * directed arcs A which together form a directed acyclic graph <b>G(N;A)</b>,
 * and a subset <b>F</b> of the nodes that are deterministically (functionally)
 * related to their parents (that means: if I know the parents, then I can
 * functionally determine the value).
 *
 * @see <a href="http://mlg.eng.cam.ac.uk/zoubin/course03/BayesBall.pdf">
 * Bayes-Ball: The Rational Pastime</a>
 * <a href="mailto:shachter@stanford.edu">Ross D. Shachter</a>
 * <code>
 * <br/>
 * Engineering-Economic Systems and Operations Research Dept.<br/>
 * Stanford University<br/>
 * Stanford, CA 94305-4023<br/>
 * </code>
 * @author Dieter J Kybelksties
 */
public class BayesBall
{

    private static final Class CLAZZ = BayesBall.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private BayesBall()
    {
    }

    static private class ScheduleItem
    {

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 13 * hash + Objects.hashCode(this.probVar);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final ScheduleItem other = (ScheduleItem) obj;
            return Objects.equals(this.probVar, other.probVar);
        }

        ScheduleItem(ProbabilityVariable probVar,
                     boolean fromChild,
                     boolean fromParent)
        {
            this.probVar = probVar;
            this.fromChild = fromChild;
            this.fromParent = fromParent;
            this.visited = false;
            this.top = false;
            this.bottom = false;
        }

        boolean fromChild()
        {
            return fromChild;
        }

        boolean fromParent()
        {
            return fromParent;
        }
        ProbabilityVariable probVar;
        boolean fromParent = false;
        boolean fromChild = false;
        boolean visited = false;
        boolean top = false;
        boolean bottom = false;
    };

    static public class ResultType
    {

        HashSet<ProbabilityVariable> irrelevantNodes = new HashSet<>();
        HashSet<ProbabilityVariable> requisiteProbability = new HashSet<>();
        HashSet<ProbabilityVariable> requisiteObservation = new HashSet<>();

        public boolean isIrrelevant(ProbabilityVariable node)
        {
            return irrelevantNodes.contains(node);
        }

        public boolean isRequisiteProbability(ProbabilityVariable node)
        {
            return requisiteProbability.contains(node);
        }

        public boolean isRequisiteObservation(ProbabilityVariable node)
        {
            return requisiteObservation.contains(node);
        }
    }

    /**
     * Run the algorithm. The algorithm explores a structured belief network
     * <b>B = (N; A; F)</b> with respect to the expression
     * <b>P(X<sub>J</sub>|X<sub>K</sub>)</b> and constructs the sets of relevant
     * and requisite nodes.
     *
     * <ol style="list-style-type:decimal">
     * <li>
     * Initialise all nodes as neither visited, nor marked on the top, nor
     * marked on the bottom.
     * </li>
     * <li>
     * Create a schedule of nodes to be visited, initialised with each node in
     * <b>J</b>
     * to be visited as if from one of its children. (all nodes in <b>J</b>).
     * </li>
     * <li> While there are still nodes scheduled to be visited:
     * <ol style="list-style-type:lower-alpha">
     * <li> Pick any node j scheduled to be visited and remove it from the
     * schedule. Either j was scheduled for a visit from a parent, a visit from
     * a child, or both.
     * </li>
     * <li> Mark j as visited.
     * </li>
     * <li> If j not in <b>K</b> and the visit to j is from a child:
     * <ol style="list-style-type:lower-roman">
     * <li> if the top of j is not marked, then mark its top and schedule each
     * of its parents to be visited
     * </li>
     * <li> if j not in <b>F</b> and the bottom of j is not marked, then mark
     * its bottom and schedule each of its children to be visited.
     * </li>
     * </ol>
     * </li>
     * <li> If the visit to j is from a parent:
     * <ol style="list-style-type:lower-roman">
     * <li>
     * If j in <b>K</b> and the top of j is not marked, then mark its top and
     * schedule each of its parents to be visited;
     * </li>
     * <li>
     * if j not in <b>K</b> and the bottom of j is not marked, then mark its
     * bottom and schedule each of its children to be visited.
     * </li>
     * </ol>
     * </li>
     * </ol>
     * </li>
     * <li>
     * The irrelevant nodes, <b>N<sub>i</sub>(J|K)</b>, are those nodes not
     * marked on the bottom.
     * </li>
     * <li>
     * The requisite probability nodes, <b>N<sub>p</sub>(J|K)</b>, are those
     * nodes marked on top.
     * </li>
     * <li>
     * The requisite observation nodes, <b>N<sub>e</sub>(J|K)</b>, are those
     * nodes in <b>K</b>
     * marked as visited.
     * </li>
     * </ol>
     *
     * @param eventVars the set of variables representing the
     * <b>X<sub>J</sub></b> in the expression
     * <b>P(X<sub>J</sub> | X<sub>K</sub>)</b>
     * @param condVars  the set of variables representing the
     * <b>X<sub>K</sub></b> in the expression
     * <b>P(X<sub>J</sub> | X<sub>K</sub>)</b>
     * @return the result as irrelevant, requisite observational and requisite
     *         probability variables/nodes
     */
    static public ResultType run(TreeSet<ProbabilityVariable> eventVars, // X_J
                                 TreeSet<ProbabilityVariable> condVars) // X_K
    {

        TreeMap<String, ScheduleItem> itemMap = new TreeMap<>();

        // 1. Initialise all nodes as neither visited, nor marked on the top, nor
        //    marked on the bottom.
        // 2. Create a schedule of nodes to be visited, initialised with each node
        //    in J to be visited as if from one of its children. (all nodes in J)
        Queue<ScheduleItem> theQueue = new LinkedList<>();
        for (ProbabilityVariable evtVar : eventVars)
        {
            ScheduleItem item = new ScheduleItem(evtVar, true, false);
            itemMap.put(evtVar.getName(), item);
            theQueue.add(item);
        }

        // 3. While there are still nodes scheduled to be visited:
        while (theQueue.size() > 0)
        {
            // a. Pick any node j scheduled to be visited and remove it from the
            //    schedule. Either j was scheduled for a visit from a parent, a
            //    visit from a child, or both.
            ScheduleItem j_item = theQueue.remove();

            // b. Mark j as visited.
            j_item.visited = true;

            // c. If j not in K  and the visit to j is from a child:
            if (!condVars.contains(j_item.probVar) && j_item.fromChild)
            {
                // i. if the top of j is not marked, then mark its top and
                //    schedule each of its parents to be visited
                if (!j_item.top)
                {
                    j_item.top = true;
                    TreeSet<ProbabilityVariable> parents = j_item.probVar.
                                                 getParents();
                    for (ProbabilityVariable parent : parents)
                    {
                        if (!itemMap.containsKey(parent.getName()))
                        {
                            ScheduleItem parentItem = new ScheduleItem(parent,
                                                                       true,
                                                                       false);
                            theQueue.add(parentItem);
                            itemMap.put(parentItem.probVar.getName(),
                                        parentItem);
                        }
                        else
                        {
                            itemMap.get(parent.getName()).fromChild = true;
                        }
                    }
                }
                // ii. if j not in F and the bottom of j is not marked, then mark
                //     its bottom and schedule each of its children to be visited.
                //     (we do not have F here!)
                if (!j_item.bottom)
                {
                    j_item.bottom = true;
                    TreeSet<ProbabilityVariable> children = j_item.probVar.
                                                 getChildren();
                    for (ProbabilityVariable child : children)
                    {
                        if (!itemMap.containsKey(child.getName()))
                        {
                            ScheduleItem childItem = new ScheduleItem(child,
                                                                      true,
                                                                      false);
                            theQueue.add(childItem);
                            itemMap.put(childItem.probVar.getName(), childItem);
                        }
                        else
                        {
                            itemMap.get(child.getName()).fromParent = true;
                        }
                    }
                }
            }
            // d. If the visit to j is from a parent:
            if (j_item.fromParent)
            {
                // i. If j in K and the top of j is not marked, then mark its top
                //    and schedule each of its parents to be visited;
                if (condVars.contains(j_item.probVar) && !j_item.top)
                {
                    j_item.top = true;
                    TreeSet<ProbabilityVariable> parents = j_item.probVar.
                                                 getParents();
                    for (ProbabilityVariable parent : parents)
                    {
                        if (!itemMap.containsKey(parent.getName()))
                        {
                            ScheduleItem parentItem = new ScheduleItem(parent,
                                                                       true,
                                                                       false);
                            theQueue.add(parentItem);
                            itemMap.
                                    put(parentItem.probVar.getName(), parentItem);
                        }
                        else
                        {
                            itemMap.get(parent.getName()).fromChild = true;
                        }
                    }
                }
                // ii. if j not in K and the bottom of j is not marked, then mark
                //     its bottom and schedule each of its children to be visited.
                if (!condVars.contains(j_item.probVar) && !j_item.bottom)
                {
                    j_item.bottom = true;
                    j_item.bottom = true;
                    TreeSet<ProbabilityVariable> children = j_item.probVar.
                                                 getChildren();
                    for (ProbabilityVariable child : children)
                    {
                        if (!itemMap.containsKey(child.getName()))
                        {
                            ScheduleItem childItem = new ScheduleItem(child,
                                                                      true,
                                                                      false);
                            theQueue.add(childItem);
                            itemMap.put(childItem.probVar.getName(), childItem);
                        }
                        else
                        {
                            itemMap.get(child.getName()).fromParent = true;
                        }
                    }
                }
            }
        }

        ResultType reval = new ResultType();

        for (ScheduleItem processedItem : itemMap.values())
        {
            // 4. The irrelevant nodes, Ni(J|K), are those nodes not marked on
            //    the bottom.
            if (processedItem.bottom)
            {
                reval.irrelevantNodes.add(processedItem.probVar);
            }

            // 5. The requisite probability nodes, Np(J|K), are those nodes
            //    marked on top.
            else if (processedItem.top)
            {
                reval.requisiteProbability.add(processedItem.probVar);
            }

            // 6. The requisite observation nodes, Ne(J|K), are those nodes in
            //    K marked as visited.
            else if (processedItem.visited)
            {
                reval.requisiteObservation.add(processedItem.probVar);
            }
        }

        return reval;
    }

}
