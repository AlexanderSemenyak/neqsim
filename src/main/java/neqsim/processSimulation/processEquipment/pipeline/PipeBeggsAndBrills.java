package neqsim.processSimulation.processEquipment.pipeline;

import java.util.UUID;
import neqsim.fluidMechanics.flowSystem.FlowSystemInterface;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 * <p>
 * AdiabaticTwoPhasePipe class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class PipeBeggsAndBrills extends Pipeline {
  private static final long serialVersionUID = 1000;

  double inletPressure = 0;
  boolean setTemperature = false;

  boolean setPressureOut = false;

  protected double temperatureOut = 270;

  protected double pressureOut = 0.0;

  private double pressureOutLimit = 0.0;
  double length = 100.0;

  double flowLimit = 1e20;

  String maxflowunit = "kg/hr";
  double insideDiameter = 0.1;
  double velocity = 1.0;
  double pipeWallRoughness = 1e-5;
  private double inletElevation = 0;
  private double outletElevation = 0;
  double dH = 0.0;
  String flowPattern = "unknown";
  String pipeSpecification = "AP02";

  /**
   * <p>
   * Constructor for AdiabaticTwoPhasePipe.
   * </p>
   */
  @Deprecated
  public PipeBeggsAndBrills() {}

  /**
   * <p>
   * Constructor for AdiabaticTwoPhasePipe.
   * </p>
   *
   * @param inStream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface}
   *        object
   */
  @Deprecated
  public PipeBeggsAndBrills(StreamInterface inStream) {
    this("PipeBeggsAndBrills", inStream);
  }

  /**
   * Constructor for AdiabaticTwoPhasePipe.
   * 
   * @param name name of pipe
   */
  public PipeBeggsAndBrills(String name) {
    super(name);
  }

  /**
   * Constructor for AdiabaticTwoPhasePipe.
   * 
   * @param name name of pipe
   * @param inStream input stream
   */
  public PipeBeggsAndBrills(String name, StreamInterface inStream) {
    super(name, inStream);
  }

  /**
   * <p>
   * Setter for the field <code>pipeSpecification</code>.
   * </p>
   *
   * @param nominalDiameter a double
   * @param pipeSec a {@link java.lang.String} object
   */
  public void setPipeSpecification(double nominalDiameter, String pipeSec) {
    pipeSpecification = pipeSec;
    insideDiameter = nominalDiameter / 1000.0;
  }

  /** {@inheritDoc} */
  @Override
  public SystemInterface getThermoSystem() {
    return outStream.getThermoSystem();
  }

  /**
   * <p>
   * setOutTemperature.
   * </p>
   *
   * @param temperature a double
   */
  public void setOutTemperature(double temperature) {
    setTemperature = true;
    this.temperatureOut = temperature;
  }

  /**
   * <p>
   * setOutPressure.
   * </p>
   *
   * @param pressure a double
   */
  public void setOutPressure(double pressure) {
    setPressureOut = true;
    this.pressureOut = pressure;
  }

  /**
   * <p>
   * calcWallFrictionFactor.
   * </p>
   *
   * @param reynoldsNumber a double
   * @return a double
   */
  public double calcWallFrictionFactor(double reynoldsNumber) {
    double relativeRoughnes = getPipeWallRoughness() / insideDiameter;
    if (Math.abs(reynoldsNumber) < 2000) {
      flowPattern = "laminar";
      return 64.0 / reynoldsNumber;
    } else {
      flowPattern = "turbulent";
      return Math.pow((1.0
          / (-1.8 * Math.log10(6.9 / reynoldsNumber + Math.pow(relativeRoughnes / 3.7, 1.11)))),
          2.0);
    }
  }

  /**
   * <p>
   * calcPressureOut.
   * </p>
   *
   * @return a double
   */
  public double calcPressureOut() {
    double area = Math.PI / 4.0 * Math.pow(insideDiameter, 2.0);
    velocity = system.getFlowRate("m3/sec") / area;
    double supGasVel = system.getPhase(0).getFlowRate("m3/sec") / area;
    double supoilVel = system.getPhase(0).getFlowRate("m3/sec") / area;

    double reynoldsNumber = velocity * insideDiameter / system.getKinematicViscosity("m2/sec");
    double frictionFactor = calcWallFrictionFactor(reynoldsNumber);
    // double dp = 2.0 * frictionFactor * length * Math.pow(system.getFlowRate("kg/sec"), 2.0)
    // / insideDiameter / system.getDensity("kg/m3"); // * () *
    // neqsim.thermo.ThermodynamicConstantsInterface.R
    double dp = 6253000 * Math.pow(system.getFlowRate("kg/hr"), 2.0) * frictionFactor
        / Math.pow(insideDiameter * 1000, 5.0) / system.getDensity("kg/m3") / 100.0 * 1000.0
        * length;
    double dp_gravity =
        system.getDensity("kg/m3") * neqsim.thermo.ThermodynamicConstantsInterface.gravity
            * (inletElevation - outletElevation);
    return (inletPressure * 1e5 - dp) / 1.0e5 + dp_gravity / 1.0e5;
  }


  /** {@inheritDoc} */
  @Override
  public void run(UUID id) {
    system = inStream.getThermoSystem().clone();
    inletPressure = system.getPressure();
    // system.setMultiPhaseCheck(true);
    if (setTemperature) {
      system.setTemperature(this.temperatureOut);
    }

    ThermodynamicOperations testOps = new ThermodynamicOperations(system);
    testOps.TPflash();

    double oldPressure = 0.0;
    int iter = 0;

    system.initProperties();
    double outP = calcPressureOut();
    if (outP < 1e-10 || Double.isNaN(outP)) {
      system.setPressure(0.001);
      logger.debug("pressure too low in pipe....");
    }
    system.setPressure(outP);
    testOps = new ThermodynamicOperations(system);
    testOps.TPflash();

    if (system.getPressure() < pressureOutLimit) {
      iter = 0;
      outP = system.getPressure();
      oldPressure = system.getNumberOfMoles();
      system.setTotalNumberOfMoles(system.getNumberOfMoles() * outP / pressureOutLimit);

      // System.out.println("new moles " +
      // system.getNumberOfMoles() + " outP "+ outP);
      // outP = calcPressureOut();
      // System.out.println("out P " + outP + " oldP " + oldPressure);
      testOps = new ThermodynamicOperations(system);
      testOps.TPflash();
      system.initProperties();

      outP = calcPressureOut();
      system.setPressure(outP);
    }

    inStream.getThermoSystem().setTotalFlowRate(system.getFlowRate(maxflowunit), maxflowunit);
    inStream.run(id);

    testOps = new ThermodynamicOperations(system);
    testOps.TPflash();
    System.out.println("flow rate " + system.getFlowRate(maxflowunit));
    // system.setMultiPhaseCheck(false);
    outStream.setThermoSystem(system);
    outStream.setCalculationIdentifier(id);
    setCalculationIdentifier(id);
  }

  /** {@inheritDoc} */
  @Override
  public void displayResult() {
    system.display();
  }

  public double getSuperficialVelocity() {
    return getInletStream().getThermoSystem().getFlowRate("kg/sec")
        / getInletStream().getThermoSystem().getDensity("kg/m3")
        / (Math.PI / 4.0 * Math.pow(insideDiameter, 2.0));
  }

  /** {@inheritDoc} */
  @Override
  public FlowSystemInterface getPipe() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void setInitialFlowPattern(String flowPattern) {}

  /**
   * <p>
   * Getter for the field <code>length</code>.
   * </p>
   *
   * @return the length
   */
  public double getLength() {
    return length;
  }

  /**
   * <p>
   * Setter for the field <code>length</code>.
   * </p>
   *
   * @param length the length to set
   */
  public void setLength(double length) {
    this.length = length;
  }

  /**
   * <p>
   * getDiameter.
   * </p>
   *
   * @return the diameter
   */
  public double getDiameter() {
    return insideDiameter;
  }

  /**
   * <p>
   * setDiameter.
   * </p>
   *
   * @param diameter the diameter to set
   */
  public void setDiameter(double diameter) {
    insideDiameter = diameter;
  }

  /**
   * <p>
   * Getter for the field <code>pipeWallRoughness</code>.
   * </p>
   *
   * @return the pipeWallRoughness
   */
  public double getPipeWallRoughness() {
    return pipeWallRoughness;
  }

  /**
   * <p>
   * Setter for the field <code>pipeWallRoughness</code>.
   * </p>
   *
   * @param pipeWallRoughness the pipeWallRoughness to set
   */
  public void setPipeWallRoughness(double pipeWallRoughness) {
    this.pipeWallRoughness = pipeWallRoughness;
  }

  /**
   * <p>
   * Getter for the field <code>inletElevation</code>.
   * </p>
   *
   * @return the inletElevation
   */
  public double getInletElevation() {
    return inletElevation;
  }

  /**
   * <p>
   * Setter for the field <code>inletElevation</code>.
   * </p>
   *
   * @param inletElevation the inletElevation to set
   */
  public void setInletElevation(double inletElevation) {
    this.inletElevation = inletElevation;
  }

  /**
   * <p>
   * Getter for the field <code>outletElevation</code>.
   * </p>
   *
   * @return the outletElevation
   */
  public double getOutletElevation() {
    return outletElevation;
  }

  /**
   * <p>
   * Setter for the field <code>outletElevation</code>.
   * </p>
   *
   * @param outletElevation the outletElevation to set
   */
  public void setOutletElevation(double outletElevation) {
    this.outletElevation = outletElevation;
  }

  /**
   * <p>
   * main.
   * </p>
   *
   * @param name an array of {@link java.lang.String} objects
   */
  public static void main(String[] name) {
    neqsim.thermo.system.SystemInterface testSystem =
        new neqsim.thermo.system.SystemSrkEos((273.15 + 5.0), 200.00);
    testSystem.addComponent("methane", 75, "MSm^3/day");
    testSystem.addComponent("n-heptane", 0.0000001, "MSm^3/day");
    testSystem.createDatabase(true);
    testSystem.setMixingRule(2);
    testSystem.init(0);

    Stream stream_1 = new Stream("Stream1", testSystem);

    PipeBeggsAndBrills pipe = new PipeBeggsAndBrills(stream_1);
    pipe.setLength(400.0 * 1e3);
    pipe.setDiameter(1.017112);
    pipe.setPipeWallRoughness(5e-6);

    PipeBeggsAndBrills pipe2 = new PipeBeggsAndBrills(pipe.getOutletStream());
    pipe2.setLength(100.0);
    pipe2.setDiameter(0.3017112);
    pipe2.setPipeWallRoughness(5e-6);
    // pipe.setOutPressure(112.0);

    neqsim.processSimulation.processSystem.ProcessSystem operations =
        new neqsim.processSimulation.processSystem.ProcessSystem();
    operations.add(stream_1);
    operations.add(pipe);
    operations.add(pipe2);
    operations.run();
    // pipe.displayResult();
    System.out.println("flow " + pipe2.getOutletStream().getFluid().getFlowRate("MSm3/day"));
    System.out.println("out pressure " + pipe.getOutletStream().getPressure("bara"));
    System.out.println("velocity " + pipe.getSuperficialVelocity());
    System.out.println("out pressure " + pipe2.getOutletStream().getPressure("bara"));
    System.out.println("velocity " + pipe2.getSuperficialVelocity());
  }

  /**
   * <p>
   * Getter for the field <code>pressureOutLimit</code>.
   * </p>
   *
   * @return a double
   */
  public double getPressureOutLimit() {
    return pressureOutLimit;
  }

  /**
   * <p>
   * Setter for the field <code>pressureOutLimit</code>.
   * </p>
   *
   * @param pressureOutLimit a double
   */
  public void setPressureOutLimit(double pressureOutLimit) {
    this.pressureOutLimit = pressureOutLimit;
  }

  /**
   * <p>
   * Setter for the field <code>flowLimit</code>.
   * </p>
   *
   * @param flowLimit a double
   * @param unit a {@link java.lang.String} object
   */
  public void setFlowLimit(double flowLimit, String unit) {
    this.flowLimit = flowLimit;
    maxflowunit = unit;
  }
}
