/*
 * QBExpectation.java
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
package QuasiBayesianInferences;

import BayesianInferences.Inference;
import BayesianNetworks.BayesNet;
import BayesianNetworks.DiscreteFunction;
import BayesianNetworks.ProbabilityFunction;
import BayesianNetworks.ProbabilityVariable;
import CredalSets.ConstantDensityBoundedSet;
import CredalSets.ConstantDensityRatioSet;
import CredalSets.EpsilonContaminatedSet;
import CredalSets.QBProbabilityFunction;
import CredalSets.TotalVariationSet;
import CredalSets.VertexSet;
import QuasiBayesianNetworks.GlobalNeighbourhood;
import QuasiBayesianNetworks.QuasiBayesNet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * There are three cases to be considered: Either the models are single
 * distributions, local credal sets or global credal sets: NONE, LOCAL and
 * GLOBAL. Note that when explanations are requested and there are no
 * explanatory variables, the final result is the posterior marginal but the
 * possibly present local credal sets are ignored. This is a method to turn
 * local credal sets off without necessarily deleting them.
 *
 * Rule: LOCAL dominates NONE; GLOBAL dominates LOCAL.
 * <ul>
 * <li> -> When there is a local credal set, compute with the local credal
 * set.</li>
 * <li> -> When there is a global credal set specification, compute with that
 * (ignore local credal sets).</li>
 * </ul>
 *
 */
public class QBInference extends Inference
{

    private static final String CLASS_NAME = QBInference.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    BayesNet originalBn;
    ProbabilityFunction listOfLocalNeighborhoodResults;

    boolean isInferenceWithoutLocalNeighborhoods;

    /**
     * Constructor for a QBInference.
     *
     * @param bayesNet            the underlying Bayesian network
     * @param isProducingClusters
     */
    public QBInference(BayesNet bayesNet, boolean isProducingClusters)
    {
        super(bayesNet, isProducingClusters);
        originalBn = bayesNet;
        transformNetwork();
    }

    /**
     * Code for basic transformation. Create all the values and transparent
     * variables for the credal sets, but do not include the transparent
     * variables in the probabilityVariables array. Local neighborhoods are only
     * used if there are local credal sets and no global credal set.
     */
    private void transformNetwork()
    {
        // Decide whether transformation is necessary
        if (bayesNet instanceof QuasiBayesNet)
        {
            isInferenceWithoutLocalNeighborhoods =
            (((QuasiBayesNet) bayesNet).getGlobalNeighborhoodType() !=
             GlobalNeighbourhood.NO_CREDAL_SET) ||
            (!(((QuasiBayesNet) bayesNet).areLocalCredalSetsPresent()));
        }
        else
        {
            isInferenceWithoutLocalNeighborhoods = true;
        }

        // Generate the transformedBn.
        bayesNet = new QuasiBayesNet(bayesNet);

        // If no transformation, then return.
        if (isInferenceWithoutLocalNeighborhoods)
        {
            return;
        }

        // Else, copy all relevant content from bayesNet to transformedBn
        bayesNet.setName("Transformed-Network");
        ArrayList auxiliaryVariables = transformProbabilityFunctionsArray();
        transformProbabilityVariablesArray(auxiliaryVariables);
    }

    /**
     * Create all the values and transparent variables for the credal sets.
     */
    private ArrayList transformProbabilityFunctionsArray()
    {
        VertexSet qbpf, newQbpf;
        ProbabilityFunction probFunc, newProbabilityFunction;
        ArrayList auxiliaryVariables = new ArrayList();

        // Process every ProbabilityFunction
        for (int i = 0; i < bayesNet.numberProbabilityFunctions(); i++)
        {
            probFunc = bayesNet.getProbabilityFunction(i);
            if (probFunc instanceof VertexSet)
            {
                qbpf = (VertexSet) probFunc;
                newQbpf = qbpf.prepareAuxiliaryVariable(bayesNet);
                auxiliaryVariables.add(newQbpf.getAuxiliaryVariable());
                bayesNet.setProbabilityFunction(i, newQbpf);
            }
            else
            {
                newProbabilityFunction =
                new ProbabilityFunction(bayesNet, probFunc.getVariables(),
                                        probFunc.getValues(), (ArrayList) null);
                bayesNet.setProbabilityFunction(i, newProbabilityFunction);
            }
        }
        return (auxiliaryVariables);
    }

    /**
     * Copy all the regular and auxiliary variables into a new
     * probabilityVariables array, making the auxiliary variables available for
     * calculation of marginals.
     */
    private void transformProbabilityVariablesArray(ArrayList auxs)
    {
        ProbabilityVariable newProbabilityVariable;
        ProbabilityVariable newProbabilityVariables[];
        int i, j, newArraySize;

        // Create the new probabilityVariables array
        newArraySize = bayesNet.numberVariables() + auxs.size();
        newProbabilityVariables = new ProbabilityVariable[newArraySize];
        // Insert regular variables into new array
        for (i = 0; i < bayesNet.numberVariables(); i++)
        {
            newProbabilityVariable =
            new ProbabilityVariable(bayesNet, bayesNet.getProbabilityVariable(i));
            newProbabilityVariables[i] = newProbabilityVariable;
        }
        j = i;
        // Insert auxiliary variables into new array
        for (Iterator e = auxs.iterator();
             j < newProbabilityVariables.length;
             j++)
        {
            // Insert auxiliary variable
            newProbabilityVariables[j] =
            (ProbabilityVariable) (e.next());
            // Update the index of auxiliary variable
            newProbabilityVariables[j].setIndex(j);
        }

        // Replace probabilityVariables
        bayesNet.setProbabilityVariables(newProbabilityVariables);
    }

    /**
     * Calculation of Inference.
     */
    @Override
    public void inference(String queriedVariableName)
    {
        super.inference(queriedVariableName);
        doQuasiBayesianInference();
    }

    /**
     * Calculation of marginal posterior envelope using a given ordering.
     *
     * @param order
     */
    @Override
    public void inference(String order[])
    {
        super.inference(order);
        doQuasiBayesianInference();
    }

    /**
     * Do the Inference.
     *
     */
    protected void doQuasiBayesianInference()
    {
        // Process result
        if (isInferenceWithoutLocalNeighborhoods)
        {
            inferenceWithoutLocalNeighborhoods();
        }
        else
        {
            inferenceWithLocalNeighborhoods();
        }
    }

    /**
     * Perform calculation of marginal posterior distributions when local
     * neighborhoods are present. Note that the distributions for the queried
     * variable, for all transparent variables, is stored at results.
     */
    private void inferenceWithLocalNeighborhoods()
    {
        int i, j, jump = 1;
        double v, min[], max[];
        DiscreteFunction unnormalizedResults;
        ProbabilityFunction normalizedResults;

        // Normalize with respect to transparent variables
        unnormalizedResults = bucketTree.getUnnormalizedResult();
        normalizedResults =
        new ProbabilityFunction(bayesNet, unnormalizedResults.getVariables(),
                                unnormalizedResults.getValues(),
                                (ArrayList) null);
        normalizedResults.normalizeFirst();

        // Get the bounds on probability
        for (i = 1; i < normalizedResults.numberVariables(); i++)
        {
            jump *= normalizedResults.getVariable(i).numberValues();
        }
        min = new double[normalizedResults.getVariable(0).numberValues()];
        max = new double[normalizedResults.getVariable(0).numberValues()];
        for (i = 0; i < normalizedResults.getVariable(0).numberValues(); i++)
        {
            min[i] = 1.0;
            max[i] = 0.0;
            for (j = 0; j < jump; j++)
            {
                v = normalizedResults.getValue(j + i * jump);
                if (v < min[i])
                {
                    min[i] = v;
                }
                if (v > max[i])
                {
                    max[i] = v;
                }
            }

        }
        // Construct results
        result = new QBProbabilityFunction(normalizedResults,
                                           (double[]) null, min, max);
        listOfLocalNeighborhoodResults = normalizedResults;
    }

    /**
     * Perform calculation of marginal posterior distributions when local
     * neighborhoods are absent; handles global neighborhoods if necessary.
     */
    private void inferenceWithoutLocalNeighborhoods()
    {
        DiscreteFunction unnormalized = bucketTree.getUnnormalizedResult();

        switch (((QuasiBayesNet) bayesNet).getGlobalNeighborhoodType())
        {
            case NO_CREDAL_SET:
                result = new ProbabilityFunction(unnormalized, bayesNet);
                result.normalize();
                break;
            case CONSTANT_DENSITY_RATIO:
                ProbabilityFunction cdrRes =
                                    new ProbabilityFunction(unnormalized,
                                                            bayesNet);
                ConstantDensityRatioSet cdr =
                                        new ConstantDensityRatioSet(cdrRes,
                                                                    ((QuasiBayesNet) bayesNet).
                                                                    getGlobalNeighborhoodParameter());
                result = cdr.posteriorMarginal();
                break;
            case EPSILON_CONTAMINATED:
                ProbabilityFunction epsRes =
                                    new ProbabilityFunction(unnormalized,
                                                            bayesNet);
                EpsilonContaminatedSet eps =
                                       new EpsilonContaminatedSet(epsRes,
                                                                  ((QuasiBayesNet) bayesNet).
                                                                  getGlobalNeighborhoodParameter());
                result = eps.posteriorMarginal();
                break;
            case CONSTANT_DENSITY_BOUNDED:
                ProbabilityFunction cdbRes =
                                    new ProbabilityFunction(unnormalized,
                                                            bayesNet);
                ConstantDensityBoundedSet cdb =
                                          new ConstantDensityBoundedSet(cdbRes,
                                                                        ((QuasiBayesNet) bayesNet).
                                                                        getGlobalNeighborhoodParameter());
                result = cdb.posteriorMarginal();
                break;
            case TOTAL_VARIATION:
                ProbabilityFunction tvRes =
                                    new ProbabilityFunction(unnormalized,
                                                            bayesNet);
                TotalVariationSet tv =
                                  new TotalVariationSet(tvRes,
                                                        ((QuasiBayesNet) bayesNet).
                                                        getGlobalNeighborhoodParameter());
                result = tv.posteriorMarginal();
                break;
        }
    }
}
