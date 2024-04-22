package neqsim.processSimulation.measurementDevice;



import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 * <p>
 * CricondenbarAnalyser class.
 * </p>
 *
 * @author ESOL
 * @version $Id: $Id
 */
public class CricondenbarAnalyser extends StreamMeasurementDeviceBaseClass {
  private static final long serialVersionUID = 1000;
  

  /**
   * <p>
   * Constructor for CricondenbarAnalyser.
   * </p>
   *
   * @param stream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface} object
   */
  public CricondenbarAnalyser(StreamInterface stream) {
    this("CricondenbarAnalyser", stream);
  }

  /**
   * <p>
   * Constructor for CricondenbarAnalyser.
   * </p>
   *
   * @param name Name of CricondenbarAnalyser
   * @param stream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface} object
   */
  public CricondenbarAnalyser(String name, StreamInterface stream) {
    super(name, "K", stream);
    setConditionAnalysisMaxDeviation(1.0);
  }

  /** {@inheritDoc} */
  @Override
  public void displayResult() {}

  /** {@inheritDoc} */
  @Override
  public double getMeasuredValue(String unit) {
    SystemInterface tempFluid = stream.getThermoSystem().clone();
    tempFluid.removeComponent("water");
    ThermodynamicOperations thermoOps = new ThermodynamicOperations(tempFluid);
    try {
      thermoOps.setRunAsThread(true);
      thermoOps.calcPTphaseEnvelope(false, 1.);
      thermoOps.waitAndCheckForFinishedCalculation(15000);
    } catch (Exception ex) {
      
    }
    return thermoOps.get("cricondenbar")[1];
  }

  /**
   * <p>
   * getMeasuredValue2.
   * </p>
   *
   * @param unit a {@link java.lang.String} object
   * @param temp a double
   * @return a double
   */
  public double getMeasuredValue2(String unit, double temp) {
    SystemInterface tempFluid = stream.getThermoSystem().clone();
    tempFluid.setTemperature(temp, "C");
    tempFluid.setPressure(10.0, "bara");
    if (tempFluid.getPhase(0).hasComponent("water")) {
      tempFluid.removeComponent("water");
    }
    neqsim.PVTsimulation.simulation.SaturationPressure thermoOps =
        new neqsim.PVTsimulation.simulation.SaturationPressure(tempFluid);
    try {
      thermoOps.run();
    } catch (Exception ex) {
      
    }
    return thermoOps.getSaturationPressure();
  }
}
