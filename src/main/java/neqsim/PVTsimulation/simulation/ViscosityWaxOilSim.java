package neqsim.PVTsimulation.simulation;

import java.util.ArrayList;
import neqsim.physicalProperties.util.parameterFitting.pureComponentParameterFitting.pureCompViscosity.linearLiquidModel.ViscosityFunction;
import neqsim.statistics.parameterFitting.SampleSet;
import neqsim.statistics.parameterFitting.SampleValue;
import neqsim.statistics.parameterFitting.nonLinearParameterFitting.LevenbergMarquardt;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkEos;

/**
 * <p>
 * ViscosityWaxOilSim class.
 * </p>
 *
 * @author esol
 * @version $Id: $Id
 */
public class ViscosityWaxOilSim extends BasePVTsimulation {
  double[] temperature = null;

  double[] pressure = null;
  private double[] waxFraction = null;
  private double[] gasViscosity;
  private double[] oilViscosity;

  private double[] oilwaxDispersionViscosity;

  private double[] aqueousViscosity;
  private double[] shareRate;

  /**
   * <p>
   * Constructor for ViscosityWaxOilSim.
   * </p>
   *
   * @param tempSystem a {@link neqsim.thermo.system.SystemInterface} object
   */
  public ViscosityWaxOilSim(SystemInterface tempSystem) {
    super(tempSystem);
    temperature = new double[1];
    pressure = new double[1];
    temperature[0] = tempSystem.getTemperature();
    pressure[0] = tempSystem.getPressure();
  }

  /**
   * <p>
   * setTemperaturesAndPressures.
   * </p>
   *
   * @param temperature an array of {@link double} objects
   * @param pressure an array of {@link double} objects
   */
  public void setTemperaturesAndPressures(double[] temperature, double[] pressure) {
    this.pressure = pressure;
    this.temperature = temperature;
    experimentalData = new double[temperature.length][1];
  }

  /**
   * <p>
   * runTuning.
   * </p>
   */
  public void runTuning() {
    ArrayList<SampleValue> sampleList = new ArrayList<SampleValue>();

    try {
      System.out.println("adding....");

      for (int i = 0; i < experimentalData[0].length; i++) {
        ViscosityFunction function = new ViscosityFunction();
        double[] guess = {1.0}; // getThermoSystem().getPhase(0).getComponent(0).getCriticalViscosity()};
        function.setInitialGuess(guess);

        SystemInterface tempSystem = getThermoSystem().clone();

        tempSystem.setTemperature(temperature[i]);
        tempSystem.setPressure(pressure[i]);
        thermoOps.TPflash();
        // tempSystem.display();
        double[] sample1 = {shareRate[i]};
        double viscosity = experimentalData[0][i];
        double[] standardDeviation1 = {1.5};
        SampleValue sample =
            new SampleValue(viscosity, viscosity / 50.0, sample1, standardDeviation1);
        sample.setFunction(function);
        sample.setThermodynamicSystem(tempSystem);
        sampleList.add(sample);
      }
    } catch (Exception e) {
      System.out.println("database error" + e);
    }

    SampleSet sampleSet = new SampleSet(sampleList);

    optimizer = new LevenbergMarquardt();
    optimizer.setMaxNumberOfIterations(20);

    optimizer.setSampleSet(sampleSet);
    optimizer.solve();
    runCalc();
    optimizer.displayCurveFit();
  }

  /**
   * <p>
   * runCalc.
   * </p>
   */
  public void runCalc() {
    gasViscosity = new double[pressure.length];
    oilViscosity = new double[pressure.length];
    aqueousViscosity = new double[pressure.length];
    waxFraction = new double[pressure.length];
    oilwaxDispersionViscosity = new double[pressure.length];

    for (int i = 0; i < pressure.length; i++) {
      // thermoOps.setSystem(getThermoSystem());
      getThermoSystem().setPressure(pressure[i]);
      getThermoSystem().setTemperature(temperature[i]);
      thermoOps.TPflash();
      getThermoSystem().initPhysicalProperties();
      waxFraction[i] = 0.0;
      if (getThermoSystem().hasPhaseType("wax") && getThermoSystem().hasPhaseType("oil")) {
        waxFraction[i] =
            getThermoSystem().getWtFraction(getThermoSystem().getPhaseNumberOfPhase("wax"));
        oilwaxDispersionViscosity[i] = getThermoSystem().getPhase("oil").getPhysicalProperties()
            .getViscosityOfWaxyOil(waxFraction[i], getShareRate()[i]);
      }
      if (getThermoSystem().hasPhaseType("gas")) {
        gasViscosity[i] = getThermoSystem().getPhase("gas").getPhysicalProperties().getViscosity();
      }
      if (getThermoSystem().hasPhaseType("oil")) {
        oilViscosity[i] = getThermoSystem().getPhase("oil").getPhysicalProperties().getViscosity();
      }
      if (getThermoSystem().hasPhaseType("aqueous")) {
        aqueousViscosity[i] =
            getThermoSystem().getPhase("aqueous").getPhysicalProperties().getViscosity();
      }
    }
  }

  /**
   * <p>
   * main.
   * </p>
   *
   * @param args an array of {@link java.lang.String} objects
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    SystemInterface tempSystem = new SystemSrkEos(298.0, 10.0);
    // tempSystem.addComponent("n-heptane", 6.78);

    tempSystem.addTBPfraction("C19", 10.13, 270.0 / 1000.0, 0.814);
    tempSystem.addPlusFraction("C20", 10.62, 330.0 / 1000.0, 0.83);

    tempSystem.getCharacterization().characterisePlusFraction();
    tempSystem.getWaxModel().addTBPWax();
    tempSystem.createDatabase(true);
    tempSystem.setMixingRule(2);
    // tempSystem.addSolidComplexPhase("wax");
    // tempSystem.setMultiphaseWaxCheck(true);
    // tempSystem.setMultiPhaseCheck(true);
    tempSystem.init(0);
    tempSystem.init(1);

    ViscosityWaxOilSim sepSim = new ViscosityWaxOilSim(tempSystem);
    double[] temps = {300.15, 293.15, 283.15, 273.15, 264.15};
    double[] pres = {5, 5, 5, 5.0, 5.0};
    sepSim.setTemperaturesAndPressures(temps, pres);
    sepSim.setShareRate(new double[] {0, 0, 0, 100, 100});
    sepSim.runCalc();

    double[][] expData = {{2e-4, 3e-4, 4e-4, 5e-4, 6e-4},};
    sepSim.setExperimentalData(expData);
    sepSim.runTuning();
    // sepSim.runCalc();
    double a = sepSim.getGasViscosity()[0];
    double a2 = sepSim.getOilViscosity()[0];
    sepSim.getThermoSystem().display();
    // sepSim.tuneModel(exptemperatures, exppressures, expwaxFrations);
  }

  /**
   * <p>
   * Getter for the field <code>waxFraction</code>.
   * </p>
   *
   * @return the waxFraction
   */
  public double[] getWaxFraction() {
    return waxFraction;
  }

  /**
   * <p>
   * Getter for the field <code>gasViscosity</code>.
   * </p>
   *
   * @return the gasViscosity
   */
  public double[] getGasViscosity() {
    return gasViscosity;
  }

  /**
   * <p>
   * Getter for the field <code>oilViscosity</code>.
   * </p>
   *
   * @return the oilViscosity
   */
  public double[] getOilViscosity() {
    return oilViscosity;
  }

  /**
   * <p>
   * Getter for the field <code>aqueousViscosity</code>.
   * </p>
   *
   * @return the aqueousViscosity
   */
  public double[] getAqueousViscosity() {
    return aqueousViscosity;
  }

  /**
   * <p>
   * Getter for the field <code>shareRate</code>.
   * </p>
   *
   * @return the shareRate
   */
  public double[] getShareRate() {
    return shareRate;
  }

  /**
   * <p>
   * Setter for the field <code>shareRate</code>.
   * </p>
   *
   * @param shareRate the shareRate to set
   */
  public void setShareRate(double[] shareRate) {
    this.shareRate = shareRate;
  }

  /**
   * <p>
   * Getter for the field <code>oilwaxDispersionViscosity</code>.
   * </p>
   *
   * @return the oilwaxDispersionViscosity
   */
  public double[] getOilwaxDispersionViscosity() {
    return oilwaxDispersionViscosity;
  }
}
