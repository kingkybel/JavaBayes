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
import java.util.ArrayList;
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
 * questions like: P(x,z|y) When we condition on y, are x and z independent?
 *
 * A structured belief network B = (N;A;F) consists of nodes N and directed arcs
 * A which together form a directed acyclic graph G(N;A), and a subset F of the
 * nodes that are deterministically (functionally) related to their parents
 * (that means: if I know the parents, then I can functionally determine the
 * value).
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

    static class ScheduleItem
    {

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 13 * hash + Objects.hashCode(this.var_);
            hash = 13 * hash + (this.fromParent ? 1 : 0);
            hash = 13 * hash + (this.fromChild ? 1 : 0);
            hash = 13 * hash + (this.visited_ ? 1 : 0);
            hash = 13 * hash + (this.top_ ? 1 : 0);
            hash = 13 * hash + (this.bottom_ ? 1 : 0);
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
            if (!Objects.equals(this.var_, other.var_))
            {
                return false;
            }
            if (this.fromParent != other.fromParent)
            {
                return false;
            }
            if (this.fromChild != other.fromChild)
            {
                return false;
            }
            if (this.visited_ != other.visited_)
            {
                return false;
            }
            if (this.top_ != other.top_)
            {
                return false;
            }
            return this.bottom_ == other.bottom_;
        }

        ScheduleItem(ProbabilityVariable var,
                     boolean byChild,
                     boolean byParent)
        {
            var_ = var;
            fromChild = byChild;
            fromParent = byParent;
            visited_ = false;
            top_ = false;
            bottom_ = false;
        }

        boolean fromChild()
        {
            return fromChild;
        }

        boolean fromParent()
        {
            return fromParent;
        }
        ProbabilityVariable var_;
        boolean fromParent = false;
        boolean fromChild = false;
        boolean visited_ = false;
        boolean top_ = false;
        boolean bottom_ = false;
    };

    class ResultType
    {

        ArrayList<ProbabilityVariable> irrelevantNodes = new ArrayList<>();
        ArrayList<ProbabilityVariable> requisiteProbability = new ArrayList<>();
        ArrayList<ProbabilityVariable> requisiteObservation = new ArrayList<>();
    }

    /**
     * Execute the algorithm. The algorithm explores a structured belief network
     * B = (N; A; F) with respect to the expression P(X_J |X_K) and constructs
     * the sets of relevant and requisite nodes.
     *
     * <ol>
     * <li>
     * Initialise all nodes as neither visited, nor marked on the top, nor
     * marked on the bottom.
     * </li>
     * <li>
     * Create a schedule of nodes to be visited, initialised with each node in J
     * to be visited as if from one of its children. (all nodes in J).
     * </li>
     * <li> While there are still nodes scheduled to be visited:
     * <ol>
     * <li> Pick any node j scheduled to be visited and remove it from the
     * schedule. Either j was scheduled for a visit from a parent, a visit from
     * a child, or both.
     * </li>
     * <li> Mark j as visited.
     * </li>
     * <li> If j not in K and the visit to j is from a child:
     * <ol>
     * <li> if the top of j is not marked, then mark its top and schedule each
     * of its parents to be visited
     * </li>
     * <li> if j not in F and the bottom of j is not marked, then mark its
     * bottom and schedule each of its children to be visited.
     * </li>
     * </ol>
     * </li>
     * <li> If the visit to j is from a parent:
     * <ol>
     * <li>
     * If j in K and the top of j is not marked, then mark its top and schedule
     * each of its parents to be visited;
     * </li>
     * <li>
     * if j not in K and the bottom of j is not marked, then mark its bottom and
     * schedule each of its children to be visited.
     * </li>
     * </ol>
     * </li>
     * </ol>
     * </li>
     * <li>
     * The irrelevant nodes, Ni(J|K), are those nodes not marked on the bottom.
     * </li>
     * <li>
     * The requisite probability nodes, Np(J|K), are those nodes marked on top.
     * </li>
     * <li>
     * The requisite observation nodes, Ne(J|K), are those nodes in K marked as
     * visited.
     * </li>
     * </ol>
     *
     * @param eventVars the set of variables representing the X_J in the
     *                  expression (P(X_J | X_K)
     * @param condVars  the set of variables representing the X_K in the
     *                  expression (P(X_J | X_K)
     * @return the result as irrelevant, requisite observational and requisite
     *         probability variables/nodes
     */
    ResultType run(TreeSet<ProbabilityVariable> eventVars, // X_J
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
            j_item.visited_ = true;

            // c. If j not in K  and the visit to j is from a child:
            if (!condVars.contains(j_item.var_) && j_item.fromChild)
            {
                // i. if the top of j is not marked, then mark its top and
                //    schedule each of its parents to be visited
                if (!j_item.top_)
                {
                    j_item.top_ = true;
                    TreeSet<ProbabilityVariable> parents = j_item.var_.
                                                 getParents();
                    for (ProbabilityVariable parent : parents)
                    {
                        if (!itemMap.containsKey(parent.getName()))
                        {
                            ScheduleItem parentItem = new ScheduleItem(parent,
                                                                       true,
                                                                       false);
                            theQueue.add(parentItem);
                            itemMap.put(parentItem.var_.getName(), parentItem);
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
                if (!j_item.bottom_)
                {
                    j_item.bottom_ = true;
                    TreeSet<ProbabilityVariable> children = j_item.var_.
                                                 getChildren();
                    for (ProbabilityVariable child : children)
                    {
                        if (!itemMap.containsKey(child.getName()))
                        {
                            ScheduleItem childItem = new ScheduleItem(child,
                                                                      true,
                                                                      false);
                            theQueue.add(childItem);
                            itemMap.put(childItem.var_.getName(), childItem);
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
                if (condVars.contains(j_item.var_) && !j_item.top_)
                {
                    j_item.top_ = true;
                    TreeSet<ProbabilityVariable> parents = j_item.var_.
                                                 getParents();
                    for (ProbabilityVariable parent : parents)
                    {
                        if (!itemMap.containsKey(parent.getName()))
                        {
                            ScheduleItem parentItem = new ScheduleItem(parent,
                                                                       true,
                                                                       false);
                            theQueue.add(parentItem);
                            itemMap.put(parentItem.var_.getName(), parentItem);
                        }
                        else
                        {
                            itemMap.get(parent.getName()).fromChild = true;
                        }
                    }
                }
                // ii. if j not in K and the bottom of j is not marked, then mark
                //     its bottom and schedule each of its children to be visited.
                if (!condVars.contains(j_item.var_) && !j_item.bottom_)
                {
                    j_item.bottom_ = true;
                    j_item.bottom_ = true;
                    TreeSet<ProbabilityVariable> children = j_item.var_.
                                                 getChildren();
                    for (ProbabilityVariable child : children)
                    {
                        if (!itemMap.containsKey(child.getName()))
                        {
                            ScheduleItem childItem = new ScheduleItem(child,
                                                                      true,
                                                                      false);
                            theQueue.add(childItem);
                            itemMap.put(childItem.var_.getName(), childItem);
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
            if (processedItem.bottom_)
            {
                reval.irrelevantNodes.add(processedItem.var_);
            }

            // 5. The requisite probability nodes, Np(J|K), are those nodes
            //    marked on top.
            else if (processedItem.top_)
            {
                reval.requisiteProbability.add(processedItem.var_);
            }

            // 6. The requisite observation nodes, Ne(J|K), are those nodes in
            //    K marked as visited.
            else if (processedItem.visited_)
            {
                reval.requisiteObservation.add(processedItem.var_);
            }
        }

        return reval;
    }

}
