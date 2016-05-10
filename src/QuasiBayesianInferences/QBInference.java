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
     * @param bN
     * @param dpc
     */
    public QBInference(BayesNet bN, boolean dpc)
    {
        super(bN, dpc);
        originalBn = bN;
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
        if (bn instanceof QuasiBayesNet)
        {
            isInferenceWithoutLocalNeighborhoods =
            (((QuasiBayesNet) bn).getGlobalNeighborhoodType() !=
             QuasiBayesNet.NO_CREDAL_SET) ||
            (!(((QuasiBayesNet) bn).areLocalCredalSetsPresent()));
        }
        else
        {
            isInferenceWithoutLocalNeighborhoods = true;
        }

        // Generate the transformedBn.
        bn = new QuasiBayesNet(bn);

        // If no transformation, then return.
        if (isInferenceWithoutLocalNeighborhoods)
        {
            return;
        }

        // Else, copy all relevant content from bn to transformedBn
        bn.setName("Transformed-Network");
        ArrayList auxiliaryVariables = transformProbabilityFunctionsArray();
        transformProbabilityVariablesArray(auxiliaryVariables);
    }

    /**
     * Create all the values and transparent variables for the credal sets.
     */
    private ArrayList transformProbabilityFunctionsArray()
    {
        VertexSet qbpf, newQbpf;
        ProbabilityFunction pf, newProbabilityFunction;
        ArrayList auxiliaryVariables = new ArrayList();

        // Process every ProbabilityFunction
        for (int i = 0; i < bn.numberProbabilityFunctions(); i++)
        {
            pf = bn.getProbabilityFunction(i);
            if (pf instanceof VertexSet)
            {
                qbpf = (VertexSet) pf;
                newQbpf = qbpf.prepareAuxiliaryVariable(bn);
                auxiliaryVariables.add(newQbpf.getAuxiliaryVariable());
                bn.setProbabilityFunction(i, newQbpf);
            }
            else
            {
                newProbabilityFunction =
                new ProbabilityFunction(bn, pf.getVariables(),
                                        pf.getValues(), (ArrayList) null);
                bn.setProbabilityFunction(i, newProbabilityFunction);
            }
        }
        return (auxiliaryVariables);
    }

    /**
     * Copy all the regular and auxiliary variables into a new
     * probabilityVariables array, making the auxiliary variables available for
     * calculation of marginals
     */
    private void transformProbabilityVariablesArray(ArrayList auxs)
    {
        ProbabilityVariable newProbabilityVariable;
        ProbabilityVariable newProbabilityVariables[];
        int i, j, newArraySize;

        // Create the new probabilityVariables array
        newArraySize = bn.numberVariables() + auxs.size();
        newProbabilityVariables = new ProbabilityVariable[newArraySize];
        // Insert regular variables into new array
        for (i = 0; i < bn.numberVariables(); i++)
        {
            newProbabilityVariable =
            new ProbabilityVariable(bn, bn.getProbabilityVariable(i));
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
        bn.setProbabilityVariables(newProbabilityVariables);
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
        inference(order);
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
        new ProbabilityFunction(bn, unnormalizedResults.getVariables(),
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

        switch (((QuasiBayesNet) bn).getGlobalNeighborhoodType())
        {
            case QuasiBayesNet.NO_CREDAL_SET:
                result = new ProbabilityFunction(unnormalized, bn);
                result.normalize();
                break;
            case QuasiBayesNet.CONSTANT_DENSITY_RATIO:
                ProbabilityFunction cdrRes =
                                    new ProbabilityFunction(unnormalized, bn);
                ConstantDensityRatioSet cdr =
                                        new ConstantDensityRatioSet(cdrRes,
                                                                    ((QuasiBayesNet) bn).
                                                                    getGlobalNeighborhoodParameter());
                result = cdr.posteriorMarginal();
                break;
            case QuasiBayesNet.EPSILON_CONTAMINATED:
                ProbabilityFunction epsRes =
                                    new ProbabilityFunction(unnormalized, bn);
                EpsilonContaminatedSet eps =
                                       new EpsilonContaminatedSet(epsRes,
                                                                  ((QuasiBayesNet) bn).
                                                                  getGlobalNeighborhoodParameter());
                result = eps.posteriorMarginal();
                break;
            case QuasiBayesNet.CONSTANT_DENSITY_BOUNDED:
                ProbabilityFunction cdbRes =
                                    new ProbabilityFunction(unnormalized, bn);
                ConstantDensityBoundedSet cdb =
                                          new ConstantDensityBoundedSet(cdbRes,
                                                                        ((QuasiBayesNet) bn).
                                                                        getGlobalNeighborhoodParameter());
                result = cdb.posteriorMarginal();
                break;
            case QuasiBayesNet.TOTAL_VARIATION:
                ProbabilityFunction tvRes =
                                    new ProbabilityFunction(unnormalized, bn);
                TotalVariationSet tv =
                                  new TotalVariationSet(tvRes,
                                                        ((QuasiBayesNet) bn).
                                                        getGlobalNeighborhoodParameter());
                result = tv.posteriorMarginal();
                break;
        }
    }
}
