/*
 * @author  Dieter J Kybelksties
 * @date Jun 29, 2016
 *
 */
package QuasiBayesianNetworks;

/**
 *
 * @author Dieter J Kybelksties
 */
public enum GlobalNeighbourhood
{

    NO_CREDAL_SET,
    CREDAL_SET,
    CONSTANT_DENSITY_RATIO,
    EPSILON_CONTAMINATED,
    CONSTANT_DENSITY_BOUNDED,
    TOTAL_VARIATION;

    @Override
    public String toString()
    {
        return this == NO_CREDAL_SET ? "none" :
               this == CREDAL_SET ? "credal-set" :
               this == CONSTANT_DENSITY_RATIO ? "constant-density-ratio" :
               this == EPSILON_CONTAMINATED ? "epsilon-contaminated" :
               this == CONSTANT_DENSITY_BOUNDED ? "constant-density-bounded" :
               this == TOTAL_VARIATION ? "total-variation" : "none";
    }

    static public GlobalNeighbourhood fromString(String str)
    {
        String localStr = str.trim().toLowerCase();
        return "none".equals(localStr) ?
               NO_CREDAL_SET :
               "credal-set".equals(localStr) ?
               CREDAL_SET :
               "constant-density-ratio".equals(localStr) ?
               CONSTANT_DENSITY_RATIO :
               "epsilon-contaminated".equals(localStr) ?
               EPSILON_CONTAMINATED :
               "constant-density-bounded".equals(localStr) ?
               CONSTANT_DENSITY_BOUNDED :
               "total-variation".equals(localStr) ?
               TOTAL_VARIATION :
               // none - default
               NO_CREDAL_SET;
    }

}
