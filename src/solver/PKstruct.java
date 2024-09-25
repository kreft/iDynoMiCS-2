package solver;

public class PKstruct {

    /* name of solute */
    public String solute;

    /* local solute consentration */
    public double conc;

    /* pKas of solute */
    public double[] pKa;

    /* protonation state concentrations */
    public double[] pStates;

    /* charge of the fully protonated form */
    public double maxCharge;
}
