package neqsim.thermo.phase;

import neqsim.thermo.component.ComponentTST;

/**
 * <p>
 * PhaseTSTEos class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class PhaseTSTEos extends PhaseEos {
    private static final long serialVersionUID = 1000;

    /**
     * <p>
     * Constructor for PhaseTSTEos.
     * </p>
     */
    public PhaseTSTEos() {
        super();
        uEOS = 2.5;
        wEOS = -1.5;
        delta1 = 1.0 + Math.sqrt(2.0);
        delta2 = 1.0 - Math.sqrt(2.0);
    }

    /** {@inheritDoc} */
    @Override
    public PhaseTSTEos clone() {
        PhaseTSTEos clonedPhase = null;
        try {
            clonedPhase = (PhaseTSTEos) super.clone();
        } catch (Exception e) {
            logger.error("Cloning failed.", e);
        }

        return clonedPhase;
    }

    /** {@inheritDoc} */
    @Override
    public void addcomponent(String componentName, double moles, double molesInPhase,
            int compNumber) {
        super.addcomponent(molesInPhase);
        componentArray[compNumber] =
                new ComponentTST(componentName, moles, molesInPhase, compNumber);
    }
}
