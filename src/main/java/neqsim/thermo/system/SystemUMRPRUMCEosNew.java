/*
 * System_SRK_EOS.java
 *
 * Created on 8. april 2000, 23:05
 */

package neqsim.thermo.system;
/**
 * This class defines a thermodynamic system using the UMR-PRU with MC paramters equation of state
 * 
 * @author Even Solbraa
 * @version
 */

import neqsim.thermo.phase.PhasePrEosvolcor;

/**
 * This class defines a thermodynamic system using the UMR-PRU with MC paramters equation of state
 */
public class SystemUMRPRUMCEosNew extends SystemUMRPRUMCEos {


  public SystemUMRPRUMCEosNew() {
    super();
    modelName = "UMR-PRU-MC-EoS-New";
    attractiveTermNumber = 19;
    useVolumeCorrection(false);
    for (int i = 0; i < numberOfPhases; i++) {
      phaseArray[i] = new PhasePrEosvolcor();
      phaseArray[i].setTemperature(298.15);
      phaseArray[i].setPressure(1.0);
      phaseArray[i].useVolumeCorrection(false);
    }
  }

  public SystemUMRPRUMCEosNew(double T, double P) {
    super(T, P);
    modelName = "UMR-PRU-MC-EoS-New";
    attractiveTermNumber = 19;
    useVolumeCorrection(false);
    for (int i = 0; i < numberOfPhases; i++) {
      phaseArray[i] = new PhasePrEosvolcor();
      phaseArray[i].setTemperature(T);
      phaseArray[i].setPressure(P);
      phaseArray[i].useVolumeCorrection(false);
    }
  }

}
