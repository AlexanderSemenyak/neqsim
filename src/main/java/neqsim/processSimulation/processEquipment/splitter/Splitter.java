package neqsim.processSimulation.processEquipment.splitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import neqsim.processSimulation.processEquipment.ProcessEquipmentBaseClass;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 * <p>
 * Splitter class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class Splitter extends ProcessEquipmentBaseClass implements SplitterInterface {
  private static final long serialVersionUID = 1000;
  static Logger logger = LogManager.getLogger(Splitter.class);
  SystemInterface thermoSystem, gasSystem, waterSystem, liquidSystem, thermoSystemCloned;
  StreamInterface inletStream;
  StreamInterface[] splitStream;
  protected int splitNumber = 1;
  double[] splitFactor = new double[1];

  /**
   * <p>
   * Constructor for Splitter.
   * </p>
   */
  public Splitter() {
    super("Splitter");
  }

  /**
   * <p>
   * Constructor for Splitter.
   * </p>
   *
   * @param inletStream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface}
   *        object
   */
  @Deprecated
  public Splitter(StreamInterface inletStream) {
    this();
    this.setInletStream(inletStream);
  }

  /**
   * Constructor for Splitter.
   * 
   * @param name name of splitter
   */
  public Splitter(String name) {
    super(name);
  }

  /**
   * Constructor for Splitter.
   * 
   * @param name name of splitter
   * @param inStream input stream
   */
  public Splitter(String name, StreamInterface inStream) {
    this(name);
    this.setInletStream(inStream);
  }

  /**
   * <p>
   * Constructor for Splitter.
   * </p>
   *
   * @param name a {@link java.lang.String} object
   * @param inletStream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface}
   *        object
   * @param i a int
   */
  public Splitter(String name, StreamInterface inletStream, int i) {
    this(name);
    setSplitNumber(i);
    this.setInletStream(inletStream);
  }

  /** {@inheritDoc} */
  @Override
  public void setSplitNumber(int i) {
    splitNumber = i;
    splitFactor = new double[splitNumber];
    splitFactor[0] = 1.0;
  }

  /**
   * <p>
   * setSplitFactors.
   * </p>
   *
   * @param splitFact an array of {@link double} objects
   */
  public void setSplitFactors(double[] splitFact) {
    double sum = 0.0;
    for (int i = 0; i < splitFact.length; i++) {
      if (splitFact[i] < 0.0) {
        splitFact[i] = 0.0;
      }
      sum += splitFact[i];
    }
    splitFactor = new double[splitFact.length];
    for (int i = 0; i < splitFact.length; i++) {
      splitFactor[i] = splitFact[i] / sum;
    }
    splitNumber = splitFact.length;
    setInletStream(inletStream);
  }

  /** {@inheritDoc} */
  @Override
  public void setInletStream(StreamInterface inletStream) {
    this.inletStream = inletStream;
    splitStream = new Stream[splitNumber];
    try {
      for (int i = 0; i < splitNumber; i++) {
        // System.out.println("splitting...." + i);
        splitStream[i] = new Stream("Split Stream", inletStream.getThermoSystem().clone());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** {@inheritDoc} */
  @Override
  public StreamInterface getSplitStream(int i) {
    return splitStream[i];
  }

  /** {@inheritDoc} */
  @Override
  public void run() {
    double totSplit = 0.0;
    for (int i = 0; i < splitNumber; i++) {
      if(splitFactor[i]<0) {
        logger.debug("split factor negative = " + splitFactor[i]);
        splitFactor[i] = 0.0;
      }
      totSplit += splitFactor[i];
    }
    if(Math.abs(totSplit-1.0)>1e-10) {
      logger.debug("total split factor different from 0 in splitter - totsplit = " + totSplit);
      logger.debug("setting first split to = " + (1.0 - totSplit));
      splitFactor[0] = 1.0 - totSplit;
    }
    
    for (int i = 0; i < splitNumber; i++) {
      thermoSystem = inletStream.getThermoSystem().clone();
      thermoSystem.init(0);
      splitStream[i].setThermoSystem(thermoSystem);
      for (int j = 0; j < inletStream.getThermoSystem().getPhase(0).getNumberOfComponents(); j++) {
        int index = inletStream.getThermoSystem().getPhase(0).getComponent(j).getComponentNumber();
        double moles = inletStream.getThermoSystem().getPhase(0).getComponent(j).getNumberOfmoles();
        splitStream[i].getThermoSystem().addComponent(index, moles * splitFactor[i] - moles);
      }
      ThermodynamicOperations thermoOps =
          new ThermodynamicOperations(splitStream[i].getThermoSystem());
      thermoOps.TPflash();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void displayResult() {}
}
