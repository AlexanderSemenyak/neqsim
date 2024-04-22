package neqsim.thermodynamicOperations.flashOps.saturationOps;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


import neqsim.thermo.ThermodynamicConstantsInterface;
import neqsim.thermo.phase.PhaseType;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkSchwartzentruberEos;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 * <p>
 * FreezeOut class.
 * </p>
 *
 * @author esol
 * @version $Id: $Id
 */
public class FreezeOut extends constantDutyTemperatureFlash
    implements ThermodynamicConstantsInterface {
  private static final long serialVersionUID = 1000;
  

  public double[] FCompTemp = new double[10];
  public String[] FCompNames = new String[10];
  public boolean noFreezeFlash = true;

  /**
   * <p>
   * Constructor for FreezeOut.
   * </p>
   *
   * @param system a {@link neqsim.thermo.system.SystemInterface} object
   */
  public FreezeOut(SystemInterface system) {
    super(system);
  }

  /** {@inheritDoc} */
  @Override
  public void run() {
    SystemInterface testSystem = system;
    ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);

    // double[][] Fug = new double[12][35];
    // double[][] Fugrel = new double[2][40];
    int iterations = 0;
    double newTemp = 0;
    double OldTemp = 0;
    double FugRatio;
    double T2low = 0;
    double T2high = 0;
    boolean Left = true;
    boolean half = false;
    double SolidFug = 0.0;
    double FluidFug = 0.0;
    double temp = 0.0;
    double pres = 0.0;
    double Pvapsolid = 0.0;
    double solvol = 0.0;
    double soldens = 0.0;
    double trpTemp = 0.0;
    boolean CCequation = true;
    boolean noFreezeliq = true;
    boolean SolidForms = true;
    double maximum = 0;

    for (int k = 0; k < testSystem.getPhase(0).getNumberOfComponents(); k++) {
      FCompNames[k] = testSystem.getPhase(0).getComponent(k).getComponentName();
      if (testSystem.getPhase(0).getComponent(k).doSolidCheck()) {
        trpTemp = testSystem.getPhases()[0].getComponents()[k].getTriplePointTemperature();
        if (noFreezeFlash) {
          testSystem.setTemperature(trpTemp);
          
        } else {
          testSystem.setTemperature(FCompTemp[k]);
          
        }

        SystemInterface testSystem2 = new SystemSrkSchwartzentruberEos(216, 1);
        ThermodynamicOperations testOps2 = new ThermodynamicOperations(testSystem2);
        testSystem2.addComponent(testSystem.getPhase(0).getComponent(k).getComponentName(), 1);
        testSystem2.setPhaseType(0, PhaseType.byValue(1));
        noFreezeliq = true;
        SolidFug = 0.0;
        FluidFug = 0.0;
        SolidForms = true;
        temp = 0.0;
        Pvapsolid = 0.0;
        iterations = 0;
        half = false;
        T2high = trpTemp + 0.1;
        if (Math.abs(testSystem.getPhases()[0].getComponents()[k].getHsub()) < 1) {
          CCequation = false;
        }
        do {
          iterations++;
          
          temp = testSystem.getTemperature();
          
          if (temp > trpTemp + 0.01) {
            temp = trpTemp;
          }
          if (CCequation) {
            Pvapsolid = testSystem.getPhase(0).getComponent(k).getCCsolidVaporPressure(temp);
          } else {
            Pvapsolid = testSystem.getPhase(0).getComponent(k).getSolidVaporPressure(temp);
          }
          soldens =
              testSystem.getPhase(0).getComponent(k).getPureComponentSolidDensity(temp) * 1000;
          if (soldens > 2000) {
            soldens = 1000;
          }
          solvol = 1.0 / soldens * testSystem.getPhase(0).getComponent(k).getMolarMass();

          
          testSystem.setTemperature(temp);
          testSystem2.setTemperature(temp);
          testSystem2.setPressure(Pvapsolid);
          testOps.TPflash();
          testOps2.TPflash();

          

          SolidFug = Pvapsolid * testSystem2.getPhase(0).getComponent(0).getFugacityCoefficient()
              * Math.exp(solvol / (R * temp) * (pres - Pvapsolid));
          FluidFug = testSystem.getPhase(0).getFugacity(k);

          FugRatio = SolidFug / FluidFug;

          OldTemp = testSystem.getTemperature();
          
          

          if (1 < (FugRatio)) {
            if (OldTemp < trpTemp / 3) {
              SolidForms = false;
            }
            T2high = OldTemp;

            if (half) {
              newTemp = 0.5 * (T2low + T2high);
            } else if (1.5 > FugRatio) {
              newTemp = OldTemp - trpTemp * 0.1;
            } else if (1.5 < FugRatio) {
              newTemp = OldTemp - trpTemp * 0.15;
            } else {
              newTemp = OldTemp - trpTemp * 0.15;
            }
            Left = false;
          } else if (1 > (FugRatio)) {
            if (Left && ((OldTemp - trpTemp) > 0)) {
              noFreezeliq = false;
            }

            T2low = OldTemp;
            Left = true;
            half = true;
            newTemp = 0.5 * (T2low + T2high);
          }

          testSystem.setTemperature(newTemp);
        } while (((Math.abs(FugRatio - 1) >= 0.00001 && iterations < 100)) && noFreezeliq
            && SolidForms);
        

        if (noFreezeliq && SolidForms) {
          testSystem.setTemperature(OldTemp);
          FCompTemp[k] = OldTemp;
        } else if (!noFreezeliq) {
          testSystem.setTemperature(OldTemp);
          FCompTemp[k] = OldTemp;
          
        } else {
          testSystem.setTemperature(1000);
          FCompTemp[k] = OldTemp;
        }

        
      } // end Iflokke
    } // end for
    maximum = FCompTemp[0]; // start with the first value
    for (int i = 1; i < FCompTemp.length; i++) {
      if (FCompTemp[i] > maximum) {
        maximum = FCompTemp[i]; // new maximum
      }
    }

    testSystem.setTemperature(maximum);
    // this.printToFile("FrzOut");
  } // end Main

  /** {@inheritDoc} */
  @Override
  public void printToFile(String name) {}
}
