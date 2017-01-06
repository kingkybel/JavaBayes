/*
 * Ordering.java
 *
 * @author Fabio G. Cozman
 * Copyright 1996 - 1999, Fabio G. Cozman, Carnergie
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
package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Fabio G. Cozman
 */
public class Ordering
{

    private static final Class CLAZZ = Ordering.class;
    private static final String CLASS_NAME = CLAZZ.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public enum Type
    {

        USER_DEFINED, USER_ORDER, MINIMUM_WEIGHT;
    }
    BayesNet bayesNet;
    String order[];
    ExplanationType explanationStatus = ExplanationType.IGNORE;
    Type orderingType = Type.MINIMUM_WEIGHT;

    /**
     * Basic constructor for Ordering.
     *
     * @param bayesNet     the underlying Bayesian network
     * @param orderingType
     * @param objective
     */
    public Ordering(BayesNet bayesNet, String objective, Type orderingType)
    {
        this.bayesNet = bayesNet;
        explanationStatus = obtainExplanationStatus(bayesNet);
        this.orderingType = orderingType;
        order = ordering(objective);
    }

    /**
     * Basic constructor for Ordering.
     *
     * @param bayesNet the underlying Bayesian network
     * @param order
     */
    public Ordering(BayesNet bayesNet, String order[])
    {
        this.bayesNet = bayesNet;
        this.order = order;
        this.explanationStatus = obtainExplanationStatus(bayesNet);
    }

    /**
     * Basic constructor for Ordering.
     *
     * @param bayesNet          the underlying Bayesian network
     * @param objective
     * @param explanationStatus
     * @param orderingType
     */
    public Ordering(BayesNet bayesNet,
                    String objective,
                    ExplanationType explanationStatus,
                    Type orderingType)
    {
        this.bayesNet = bayesNet;
        this.explanationStatus = explanationStatus;
        this.orderingType = orderingType;
        order = ordering(objective);
    }

    /**
     * Basic constructor for Ordering.
     *
     * @param bayesNet          the underlying Bayesian network
     * @param order
     * @param explanationStatus
     */
    public Ordering(BayesNet bayesNet,
                    String order[],
                    ExplanationType explanationStatus)
    {
        this.bayesNet = bayesNet;
        this.order = order;
        this.explanationStatus = explanationStatus;
    }

    /**
     * Obtain explanationStatus: unless there are explanations the status is
     * IGNORE.
     *
     * @param bayesNet the underlying Bayesian network
     * @return
     */
    private ExplanationType obtainExplanationStatus(BayesNet bayesNet)
    {
        ExplanationType explanationStatusFlag = ExplanationType.IGNORE;
        for (int i = 0; i < bayesNet.numberVariables(); i++)
        {
            if ((!(bayesNet.getProbabilityVariable(i).isObserved())) &&
                (bayesNet.getProbabilityVariable(i).isExplanation()))
            {
                explanationStatusFlag = ExplanationType.SUBSET;
                break;
            }
        }
        return explanationStatusFlag;
    }

    /**
     * Call the appropriate ordering depending on the type of ordering.
     *
     * @param objective
     * @return
     */
    private String[] ordering(String objective)
    {
        int i;
        ArrayList<DiscreteVariable> variablesToOrder = new ArrayList<>();

        int objectiveIndex = bayesNet.indexOfVariable(objective);
        if (objectiveIndex == BayesNet.INVALID_INDEX)
        {
            objectiveIndex = 0;
        }

        if (bayesNet.getProbabilityVariable(objectiveIndex).isObserved())
        {
            String oneOrder[] =
            {
                bayesNet.getProbabilityVariable(objectiveIndex).getName()
            };
            return oneOrder;
        }

        if (orderingType == Type.USER_ORDER)
        {
            // For user order, just collect all variables.
            for (i = 0; i < bayesNet.numberVariables(); i++)
            {
                variablesToOrder.add(bayesNet.getProbabilityVariable(i));
            }
            return userOrder(variablesToOrder, objectiveIndex);
        }
        else
        {
            // For explanations, just collect all variables.
            if (!explanationStatus.isIgnore())
            {
                for (i = 0; i < bayesNet.numberVariables(); i++)
                {
                    variablesToOrder.add(
                            bayesNet.getProbabilityVariable(i));
                }
            }
            else
            { // For inference, get only the affecting variables.
                DSeparation dsep = new DSeparation(bayesNet);
                variablesToOrder = dsep.getAllAffectingVariables(objectiveIndex);
            }
            return heuristicOrder(variablesToOrder,
                                  objectiveIndex,
                                  orderingType);
        }
    }

    /**
     * Simple ordering for the variables:
     * <ol>
     * <li> Transparent variables are not included; note that transparent
     * variables are only present in cases where explanation variables are not
     * to be considered.</li>
     * <li> Explanation variables come last in the order they were input</li>
     * <li> Non-explanation variables come in the order they were input. </li>
     * </ol>
     * When there are no explanation variables (or explanation variables are to
     * be ignored), the objective variable comes last. Note that this violates
     * the inserted by the user, but the bucket elimination algorithm requires
     * the ordering to have this property (objective variable last for
     * inference).
     *
     * @param variablesToOrder
     * @param objectiveIndex
     * @return
     */
    private String[] userOrder(ArrayList<DiscreteVariable> variablesToOrder,
                               int objectiveIndex)
    {
        int i, j;
        boolean isVariableExplanationFlag = false;
        ArrayList<String> nonExplanationVariables = new ArrayList<>();
        ArrayList<String> explanationVariables = new ArrayList<>();
        String ord[];

        // Collect variables into related vectors
        for (DiscreteVariable discrVar : variablesToOrder)
        {
            ProbabilityVariable probVar = (ProbabilityVariable) discrVar;
            // Skip transparent variables
            if (probVar.getType() == ProbabilityVariable.TRANSPARENT)
            {
                continue;
            }
            // Check the status of the variable as a explanatory variable
            switch (explanationStatus)
            {
                case IGNORE:
                    isVariableExplanationFlag = false;
                    break;
                case SUBSET:
                    isVariableExplanationFlag = probVar.isExplanation();
                    break;
                case FULL:
                    isVariableExplanationFlag = true;
                    break;
            }

            // Observed variables are not explanation variables
            // (evidence has precedence over explanations).
            if (probVar.isObserved())
            {
                isVariableExplanationFlag = false;
            }

            // Put the variable in the correct vector
            if (isVariableExplanationFlag)
            {
                explanationVariables.add(probVar.getName());
            }
            else
            {
                nonExplanationVariables.add(probVar.getName());
            }
        }

        ord = new String[nonExplanationVariables.size() +
                         explanationVariables.size()];

        if (explanationVariables.isEmpty())
        {
            i = 0;
            for (String varName : nonExplanationVariables)
            {
                ord[i] = varName;
                if (ord[i].equals(bayesNet.
                        getProbabilityVariable(objectiveIndex).
                        getName()))
                {
                    i--;
                }
                i++;
            }
            ord[i] = bayesNet.getProbabilityVariable(objectiveIndex).getName();
        }
        else
        {
            i = 0;
            for (String varName : nonExplanationVariables)
            {
                ord[i] = varName;
                i++;
            }
            j = i;
            for (String varName : explanationVariables)
            {
                ord[j] = varName;
            }

        }

        return ord;
    }

    /**
     * Heuristic ordering for the variables:
     * <ol>
     * <li> Transparent variables are not included</li>
     * <li> Decision variables come last in the order they were input</li>
     * <li> Non-explanation variables come in the order they were input, except
     * objective variable which is the last of all non-explanation
     * variables</li>
     * </ol>
     * Produce an ordering for the variables in variablesToOrder, assuming that
     * all variables are in the BayesNet bayesNet object. The orderingType
     * indicates which heuristic to use in the elimination procedure.
     *
     * @param origVars
     * @param objectiveIndex
     * @param orderingType
     * @return
     */
    private String[] heuristicOrder(ArrayList<DiscreteVariable> origVars,
                                    int objectiveIndex,
                                    Type orderingType)
    {
        int i, j;
        int PHASE_ONE = 1;
        int PHASE_TWO = 2;
        int phase;
        long value, minValue;
        int minIndex;
        int numberVariablesInPhase;
        int numberVariablesInPhaseTwo = 0;

        ProbabilityVariable probVar;
        ProbabilityVariable neighbors[];
        ProbabilityFunction probFunc;

        // The vector with the filtered variables to order.
        ArrayList<DiscreteVariable> variablesToOrder = new ArrayList<>();

        // The vector that will contain the final ordering.
        ArrayList<DiscreteVariable> eliminationOrdering = new ArrayList<>();

        // Phase markers: indicates in which phase of the
        // algorithm a variable will be eliminated.
        int phaseMarkers[] = new int[bayesNet.numberVariables()];
        for (i = 0; i < phaseMarkers.length; i++)
        {
            phaseMarkers[i] = PHASE_ONE;
        }

        // Filter the incoming variables
        for (DiscreteVariable discrVar : origVars)
        {
            probVar = (ProbabilityVariable) (discrVar);
            if (probVar.isObserved())
            { // Put observed variables at the beginning
                eliminationOrdering.add(probVar);
            }
            else
            { // Skip transparent variables
                if (probVar.getType() != ProbabilityVariable.TRANSPARENT)
                {
                    // Order all other variables
                    variablesToOrder.add(probVar);
                    // Check the status of the variable as an explanatory variable
                    if ((explanationStatus.isFull()) ||
                        ((explanationStatus.isSubset()) &&
                         (probVar.isExplanation())))
                    {
                        phaseMarkers[probVar.getIndex()] = PHASE_TWO;
                        numberVariablesInPhaseTwo++;
                    }
                }
            }
        }

        // Define whether the objective variable will be
        // processed in the second phase.
        if (numberVariablesInPhaseTwo == 0)
        {
            phaseMarkers[objectiveIndex] = PHASE_TWO;
            numberVariablesInPhaseTwo = 1;
        }

        // Each variable is associated to a vector (the vector contains
        // all variables that are linked to the variable).
        ArrayList<DiscreteVariable> vectors[] =
                                      new ArrayList[bayesNet.numberVariables()];
        // Initialize the vectors only for the variables that are to be ordered.
        for (DiscreteVariable discrVar : variablesToOrder)
        {
            probVar = (ProbabilityVariable) discrVar;
            vectors[probVar.getIndex()] = new ArrayList();
        }

        // Moralize the network: build an undirected graph where each variable
        // is linked to its parents, children, and parents of its children.
        // The idea is to go through the variables and, for each variable,
        // interconnect the variable and all its parents. That connects
        // all variables to its parents and "moralizes" the graph simultaneously;
        // since all variables are analyzed, every variable ends up connected
        // to its children.
        for (DiscreteVariable discrVar : variablesToOrder)
        {
            probVar = (ProbabilityVariable) (discrVar);
            probFunc = bayesNet.getFunction(probVar);
            vectors[probVar.getIndex()].add(probVar);
            interconnect(bayesNet, vectors, probFunc.getVariables());
        }

        // Decide which phase to start;
        if (numberVariablesInPhaseTwo == variablesToOrder.size())
        {
            phase = PHASE_TWO;
        }
        else
        {
            phase = PHASE_ONE;
        }

        // Eliminate the variable that has the smallest value for
        // the heuristic of interest, until all variables are eliminated.
        // As a variable is eliminated, it is removed from all other
        // links and all its neighbors are interconnected.
        for (i = 0; i < variablesToOrder.size(); i++)
        {
            // Get the variable with minimum heuristic value.
            minValue = -1;
            minIndex = -1;

            numberVariablesInPhase = 0;
            for (j = 0; j < vectors.length; j++)
            { // Go through all the variables
                // Only proceed if variable is to be ordered in this phase.
                if ((vectors[j] != null) && (phaseMarkers[j] == phase))
                {
                    numberVariablesInPhase++;

                    // Get the value for the heuristic.
                    value = obtainValue(vectors[j], orderingType);
                    if ((value < minValue) || (minIndex == -1))
                    { // Minimize the heuristic.
                        minIndex = j;
                        minValue = value;
                    }
                }
            }
            if ((phase == PHASE_ONE) && (numberVariablesInPhase == 1))
            {
                phase = PHASE_TWO;
            }

            // Add the variable with minimum value for the heuristic
            // to the ordering.
            probVar = bayesNet.getProbabilityVariable(minIndex);
            eliminationOrdering.add(probVar);

            // Now remove the variable:
            //   Remove it from every other list of variables
            for (j = 0; j < vectors.length; j++)
            { // Go through all lists of variables.
                if (vectors[j] != null)
                { // Only proceed is list is non-null.
                    vectors[j].remove(probVar); // Now remove the variable from the vector
                }
            }
            //   Interconnect all its neighbors
            neighbors = new ProbabilityVariable[vectors[minIndex].size()];
            j = 0;
            for (DiscreteVariable discrVar : vectors[minIndex])
            {
                probVar = (ProbabilityVariable) discrVar;
                neighbors[j] = probVar;
                j++;
            }
            interconnect(bayesNet, vectors, neighbors);
            //   Erase its list of neighbors.
            vectors[minIndex] = null;
        }

        // Return the ordering
        String returnOrdering[] = new String[eliminationOrdering.size()];
        i = 0;
        for (DiscreteVariable discrVar : eliminationOrdering)
        {
            probVar = (ProbabilityVariable) discrVar;
            returnOrdering[i] = probVar.getName();
            i++;
        }
        return returnOrdering;
    }

    /**
     * Obtain the heuristic value of eliminating a variable, represented by the
     * list of variables linked to it.
     *
     * @param linkedVars
     * @param orderingType
     * @return
     */
    private long obtainValue(ArrayList<DiscreteVariable> linkedVars,
                             Type orderingType)
    {
        ProbabilityVariable probVar;
        long value = 0;

        if (orderingType == Type.MINIMUM_WEIGHT)
        {
            long weight = 1;
            for (DiscreteVariable discrVar : linkedVars)
            {
                probVar = (ProbabilityVariable) discrVar;
                weight *= probVar.numberValues();
            }
            value = weight;
        }

        return value;
    }

    /**
     * Interconnect a group of variables; each variable connected to all the
     * others.
     *
     * @param bayesNet                    the underlying Bayesian network
     * @param vectors
     * @param variablesToBeInterconnected
     */
    private void interconnect(BayesNet bayesNet,
                              ArrayList<DiscreteVariable> vectors[],
                              DiscreteVariable variablesToBeInterconnected[])
    {
        int i, j;
        for (i = 0; i < (variablesToBeInterconnected.length - 1); i++)
        {
            for (j = (i + 1); j < variablesToBeInterconnected.length; j++)
            {
                interconnect(bayesNet,
                             vectors,
                             variablesToBeInterconnected[i],
                             variablesToBeInterconnected[j]);
            }
        }
    }

    /**
     * Connect two variables.
     *
     * @param bayesNet the underlying Bayesian network
     * @param vectors
     * @param probVar  a probability variable_i
     * @param probVar  a probability variable_j
     */
    private void interconnect(BayesNet bayesNet,
                              ArrayList<DiscreteVariable> vectors[],
                              DiscreteVariable probVar_i,
                              DiscreteVariable probVar_j)
    {
        ArrayList<DiscreteVariable> iv = vectors[probVar_i.getIndex()];
        ArrayList<DiscreteVariable> jv = vectors[probVar_j.getIndex()];

        // Avoid problems if parent is observed or transparent.
        if ((iv == null) || (jv == null))
        {
            return;
        }

        // Now interconnect.
        if (!iv.contains(probVar_j))
        {
            iv.add(probVar_j);
        }
        if (!jv.contains(probVar_i))
        {
            jv.add(probVar_i);
        }
    }
}
