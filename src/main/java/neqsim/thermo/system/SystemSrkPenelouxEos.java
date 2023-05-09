package neqsim.thermo.system;

import neqsim.thermo.phase.PhaseHydrate;
import neqsim.thermo.phase.PhasePureComponentSolid;
import neqsim.thermo.phase.PhaseSrkPenelouxEos;

/**
 * This class defines a thermodynamic system using the SRK Peneloux equation of state.
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class SystemSrkPenelouxEos extends SystemSrkEos {
  private static final long serialVersionUID = 1000;

  /**
   * Constructor of a fluid object using the SRK-EoS.
   */
  public SystemSrkPenelouxEos() {
    super();
    modelName = "SRK-Peneloux-EOS";
    getCharacterization().setTBPModel("PedersenSRK"); // (RiaziDaubert PedersenPR PedersenSRK
    attractiveTermNumber = 0;

    for (int i = 0; i < numberOfPhases; i++) {
      phaseArray[i] = new PhaseSrkPenelouxEos();
      phaseArray[i].setTemperature(298.15);
      phaseArray[i].setPressure(1.0);
    }
  }

  /**
   * Constructor of a fluid object using the SRK-EoS.
   *
   * @param T The temperature in unit Kelvin
   * @param P The pressure in unit bara (absolute pressure)
   */
  public SystemSrkPenelouxEos(double T, double P) {
    super(T, P);
    modelName = "SRK-Peneloux-EOS";
    getCharacterization().setTBPModel("PedersenSRK");
    attractiveTermNumber = 0;
    for (int i = 0; i < numberOfPhases; i++) {
      phaseArray[i] = new PhaseSrkPenelouxEos();
      phaseArray[i].setTemperature(T);
      phaseArray[i].setPressure(P);
    }
  }

  /**
   * Constructor of a fluid object using the SRK-EoS.
   *
   * @param T The temperature in unit Kelvin
   * @param P The pressure in unit bara (absolute pressure)
   * @param checkForSolids Set true to do solid phase check and calculations
   */
  public SystemSrkPenelouxEos(double T, double P, boolean checkForSolids) {
    this(T, P);
    modelName = "SRK-Peneloux-EOS";
    attractiveTermNumber = 0;
    setNumberOfPhases(5);
    solidPhaseCheck = checkForSolids;
    for (int i = 0; i < numberOfPhases; i++) {
      phaseArray[i] = new PhaseSrkPenelouxEos();
      phaseArray[i].setTemperature(T);
      phaseArray[i].setPressure(P);
    }

    if (solidPhaseCheck) {
      // System.out.println("here first");
      phaseArray[numberOfPhases - 1] = new PhasePureComponentSolid();
      phaseArray[numberOfPhases - 1].setTemperature(T);
      phaseArray[numberOfPhases - 1].setPressure(P);
      phaseArray[numberOfPhases - 1].setRefPhase(phaseArray[1].getRefPhase());
    }

    if (hydrateCheck) {
      // System.out.println("here first");
      phaseArray[numberOfPhases - 1] = new PhaseHydrate();
      phaseArray[numberOfPhases - 1].setTemperature(T);
      phaseArray[numberOfPhases - 1].setPressure(P);
      phaseArray[numberOfPhases - 1].setRefPhase(phaseArray[1].getRefPhase());
    }
  }

  /** {@inheritDoc} */
  @Override
  public SystemSrkPenelouxEos clone() {
    SystemSrkPenelouxEos clonedSystem = null;
    try {
      clonedSystem = (SystemSrkPenelouxEos) super.clone();
    } catch (Exception ex) {
      logger.error("Cloning failed.", ex);
    }

    // clonedSystem.phaseArray = (PhaseInterface[]) phaseArray.clone();
    // for(int i = 0; i < numberOfPhases; i++) {
    // clonedSystem.phaseArray[i] = (PhaseInterface) phaseArray[i].clone();
    // }
    return clonedSystem;
  }
}
