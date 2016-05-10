package BayesianInferences;

import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteVariable;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author kybelksd
 */
public class Ordering
{

    /**
     *
     */
    public static final int USER_DEFINED = 0;

    /**
     *
     */
    public static final int USER_ORDER = 1;

    /**
     *
     */
    public static final int MINIMUM_WEIGHT = 2;
    private static final String CLASS_NAME = Ordering.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    BayesNet bn;
    String order[];
    int explanationStatus = Inference.IGNORE_EXPLANATION;
    int orderingType = MINIMUM_WEIGHT;

    /**
     * Basic constructor for Ordering.
     *
     * @param bN
     * @param ot
     * @param objective
     */
    public Ordering(BayesNet bN, String objective, int ot)
    {
        bn = bN;
        explanationStatus = obtainExplanationStatus(bN);
        orderingType = ot;
        order = ordering(objective);
    }

    /**
     * Basic constructor for Ordering.
     *
     * @param bN
     * @param or
     */
    public Ordering(BayesNet bN, String or[])
    {
        bn = bN;
        order = or;
        explanationStatus = obtainExplanationStatus(bN);
    }

    /**
     * Basic constructor for Ordering.
     *
     * @param bN
     * @param ot
     * @param objective
     * @param ds
     */
    public Ordering(BayesNet bN, String objective, int ds, int ot)
    {
        bn = bN;
        explanationStatus = ds;
        orderingType = ot;
        order = ordering(objective);
    }

    /**
     * Basic constructor for Ordering.
     *
     * @param bN
     * @param ds
     * @param or
     */
    public Ordering(BayesNet bN, String or[], int ds)
    {
        bn = bN;
        order = or;
        explanationStatus = ds;
    }

    /*
     * Obtain explanationStatus: unless there are explanations
     * the status is IGNORE_EXPLANATION.
     */
    private int obtainExplanationStatus(BayesNet bN)
    {
        int explanationStatusFlag = Inference.IGNORE_EXPLANATION;
        for (int i = 0; i < bN.numberVariables(); i++)
        {
            if ((!(bN.getProbabilityVariable(i).isObserved())) &&
                (bN.getProbabilityVariable(i).isExplanation()))
            {
                explanationStatusFlag = Inference.EXPLANATION;
                break;
            }
        }
        return (explanationStatusFlag);
    }

    /*
     * Call the appropriate ordering depending on the type of
     * ordering.
     */
    private String[] ordering(String objective)
    {
        int i;
        ArrayList variablesToOrder = new ArrayList();

        int objectiveIndex = bn.indexOfVariable(objective);
        if (objectiveIndex == BayesNet.INVALID_INDEX)
        {
            objectiveIndex = 0;
        }

        if (bn.getProbabilityVariable(objectiveIndex).isObserved())
        {
            String oneOrder[] =
            {
                bn.getProbabilityVariable(objectiveIndex).getName()
            };
            return (oneOrder);
        }

        if (orderingType == USER_ORDER)
        {
            // For user order, just collect all variables.
            for (i = 0; i < bn.numberVariables(); i++)
            {
                variablesToOrder.add(bn.getProbabilityVariable(i));
            }
            return (userOrder(variablesToOrder, objectiveIndex));
        }
        else
        {
            // For explanations, just collect all variables.
            if (explanationStatus != Inference.IGNORE_EXPLANATION)
            {
                for (i = 0; i < bn.numberVariables(); i++)
                {
                    variablesToOrder.
                            add(bn.getProbabilityVariable(i));
                }
            }
            else
            { // For inference, get only the affecting variables.
                DSeparation dsep = new DSeparation(bn);
                variablesToOrder = dsep.allAffecting(objectiveIndex);
            }
            return (heuristicOrder(variablesToOrder, objectiveIndex,
                                   orderingType));
        }
    }

    /*
     * Simple ordering for the variables:
     * 1) Transparent variables are not included; note that
     *    transparent variables are only present in cases where
     *    explanation variables are not to be considered.
     * 2) Explanation variables come last in the order they
     *    were input
     * 3) Non-explanation variables come in the order they were
     *    input. When there are no explanation variables (or
     *    explanation variables are to be ignored), the objective
     *    variable comes last. Note that this violates the
     *    inserted by the user, but the bucket elimination
     *    algorithm requires the ordering to have this property
     *    (objective variable last for inference).
     */
    private String[] userOrder(ArrayList variablesToOrder,
                               int objectiveIndex)
    {
        int i, j;
        boolean isVariableExplanationFlag = false;
        ProbabilityVariable pv;
        ArrayList nonExplanationVariables = new ArrayList();
        ArrayList explanationVariables = new ArrayList();
        String ord[];

        // Collect variables into related vectors
        for (Object e : variablesToOrder)
        {
            pv = (ProbabilityVariable) (e);
            // Skip transparent variables
            if (pv.getType() == ProbabilityVariable.TRANSPARENT)
            {
                continue;
            }
            // Check the status of the variable as a explanatory variable
            switch (explanationStatus)
            {
                case Inference.IGNORE_EXPLANATION:
                    isVariableExplanationFlag = false;
                    break;
                case Inference.EXPLANATION:
                    isVariableExplanationFlag = pv.isExplanation();
                    break;
                case Inference.FULL_EXPLANATION:
                    isVariableExplanationFlag = true;
                    break;
            }

            // Observed variables are not explanation variables
            // (evidence has precedence over explanations).
            if (pv.isObserved())
            {
                isVariableExplanationFlag = false;
            }

            // Put the variable in the correct vector
            if (isVariableExplanationFlag)
            {
                explanationVariables.add(pv.getName());
            }
            else
            {
                nonExplanationVariables.add(pv.getName());
            }
        }

        ord = new String[nonExplanationVariables.size() +
                         explanationVariables.size()];

        if (explanationVariables.isEmpty())
        {
            i = 0;
            for (Object e : nonExplanationVariables)
            {
                ord[i] = (String) (e);
                if (ord[i].equals(bn.getProbabilityVariable(objectiveIndex).
                        getName()))
                {
                    i--;
                }
                i++;
            }
            ord[i] = bn.getProbabilityVariable(objectiveIndex).getName();
        }
        else
        {
            i = 0;
            for (Object e : nonExplanationVariables)
            {
                ord[i] = (String) (e);
                i++;
            }
            j = i;
            for (Object e : explanationVariables)
            {
                ord[j] = (String) (e);
            }

        }

        return (ord);
    }

    /*
     * Heuristic ordering for the variables:
     * 1) Transparent variables are not included
     * 2) Decision variables come last in the order they
     *                were input
     * 3) Non-explanation variables come in the order they were
     *                input, except objective variable which
     *                is the last of all non-explanation variables
     * Produce an ordering for the variables in variablesToOrder,
     * assuming that all variables are in the BayesNet bn object.
     * The orderingType indicates which heuristic to use in the
     * elimination procedure.
     */
    private String[] heuristicOrder(ArrayList vo,
                                    int objectiveIndex,
                                    int orderingType)
    {
        int i, j;
        int PHASE_ONE = 1;
        int PHASE_TWO = 2;
        int phase;
        long value, minValue;
        int minIndex;
        int numberVariablesInPhase;
        int numberVariablesInPhaseTwo = 0;

        ProbabilityVariable pv;
        ProbabilityVariable neighbors[];
        ProbabilityFunction pf;

        // The vector with the filtered variables to order.
        ArrayList variablesToOrder = new ArrayList();

        // The vector that will contain the final ordering.
        ArrayList eliminationOrdering = new ArrayList();

        // Phase markers: indicates in which phase of the
        // algorithm a variable will be eliminated.
        int phaseMarkers[] = new int[bn.numberVariables()];
        for (i = 0; i < phaseMarkers.length; i++)
        {
            phaseMarkers[i] = PHASE_ONE;
        }

        // Filter the incoming variables
        for (Object e : vo)
        {
            pv = (ProbabilityVariable) (e);
            if (pv.isObserved())
            { // Put observed variables at the beginning
                eliminationOrdering.add(pv);
            }
            else
            { // Skip transparent variables
                if (pv.getType() != ProbabilityVariable.TRANSPARENT)
                {
                    // Order all other variables
                    variablesToOrder.add(pv);
                    // Check the status of the variable as an explanatory variable
                    if ((explanationStatus == Inference.FULL_EXPLANATION) ||
                        ((explanationStatus == Inference.EXPLANATION) &&
                         (pv.isExplanation())))
                    {
                        phaseMarkers[pv.getIndex()] = PHASE_TWO;
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
        ArrayList vectors[] = new ArrayList[bn.numberVariables()];
        // Initialize the vectors only for the variables that are to be ordered.
        for (Object e : variablesToOrder)
        {
            pv = (ProbabilityVariable) (e);
            vectors[pv.getIndex()] = new ArrayList();
        }

        // Moralize the network: build an undirected graph where each variable
        // is linked to its parents, children, and parents of its children.
        // The idea is to go through the variables and, for each variable,
        // interconnect the variable and all its parents. That connects
        // all variables to its parents and "moralizes" the graph simultaneously;
        // since all variables are analyzed, every variable ends up connected
        // to its children.
        for (Object e : variablesToOrder)
        {
            pv = (ProbabilityVariable) (e);
            pf = bn.getFunction(pv);
            vectors[pv.getIndex()].add(pv);
            interconnect(bn, vectors, pf.getVariables());
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
                    value = obtainValue(vectors[j], orderingType); // Get the value for the heuristic.
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
            pv = bn.getProbabilityVariable(minIndex);
            eliminationOrdering.add(pv);

            // Now remove the variable:
            //   Remove it from every other list of variables
            for (j = 0; j < vectors.length; j++)
            { // Go through all lists of variables.
                if (vectors[j] != null)
                { // Only proceed is list is non-null.
                    vectors[j].remove(pv); // Now remove the variable from the vector
                }
            }
            //   Interconnect all its neighbors
            neighbors = new ProbabilityVariable[vectors[minIndex].size()];
            j = 0;
            for (Object e : vectors[minIndex])
            {
                pv = (ProbabilityVariable) (e);
                neighbors[j] = pv;
                j++;
            }
            interconnect(bn, vectors, neighbors);
            //   Erase its list of neighbors.
            vectors[minIndex] = null;
        }

        // Return the ordering
        String returnOrdering[] = new String[eliminationOrdering.size()];
        i = 0;
        for (Object e : eliminationOrdering)
        {
            pv = (ProbabilityVariable) (e);
            returnOrdering[i] = pv.getName();
            i++;
        }
        return (returnOrdering);
    }

    /*
     * Obtain the heuristic value of eliminating a variable,
     * represented by the list of variables linked to it.
     */
    private long obtainValue(ArrayList v, int orderingType)
    {
        ProbabilityVariable pv;
        long value = 0;

        if (orderingType == Ordering.MINIMUM_WEIGHT)
        {
            long weight = 1;
            for (Object e : v)
            {
                pv = (ProbabilityVariable) (e);
                weight *= pv.numberValues();
            }
            value = weight;
        }

        return (value);
    }

    /*
     * Interconnect a group of variables; each variable
     * connected to all the others.
     */
    private void interconnect(BayesNet bn, ArrayList vectors[],
                              DiscreteVariable variablesToBeInterconnected[])
    {
        int i, j;
        for (i = 0; i < (variablesToBeInterconnected.length - 1); i++)
        {
            for (j = (i + 1); j < variablesToBeInterconnected.length; j++)
            {
                interconnect(bn, vectors,
                             variablesToBeInterconnected[i],
                             variablesToBeInterconnected[j]);
            }
        }
    }

    /*
     * Connect two variables.
     */
    private void interconnect(BayesNet bn, ArrayList vectors[],
                              DiscreteVariable pvi, DiscreteVariable pvj)
    {
        ArrayList iv = vectors[pvi.getIndex()];
        ArrayList jv = vectors[pvj.getIndex()];

        // Avoid problems if parent is observed or transparent.
        if ((iv == null) || (jv == null))
        {
            return;
        }

        // Now interconnect.
        if (!iv.contains(pvj))
        {
            iv.add(pvj);
        }
        if (!jv.contains(pvi))
        {
            jv.add(pvi);
        }
    }
}
