package neqsim.processSimulation.measurementDevice;

import neqsim.processSimulation.processEquipment.stream.StreamInterface;

/**
 * <p>
 * PressureTransmitter class.
 * </p>
 *
 * @author ESOL
 * @version $Id: $Id
 */
public class PressureTransmitter extends MeasurementDeviceBaseClass {
  private static final long serialVersionUID = 1000;
  protected StreamInterface stream = null;

  /**
   * <p>
   * Constructor for PressureTransmitter.
   * </p>
   */
  public PressureTransmitter() {
    name = "Pressure Transmitter";
    unit = "bar";
  }

  /**
   * <p>
   * Constructor for PressureTransmitter.
   * </p>
   *
   * @param stream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface} object
   */
  public PressureTransmitter(StreamInterface stream) {
    this();
    this.stream = stream;
  }

  /** {@inheritDoc} */
  @Override
  public void displayResult() {
    System.out.println("measured temperature " + stream.getPressure());
  }

  /** {@inheritDoc} */
  @Override
  public double getMeasuredValue() {
    return stream.getThermoSystem().getPressure();
  }
}
