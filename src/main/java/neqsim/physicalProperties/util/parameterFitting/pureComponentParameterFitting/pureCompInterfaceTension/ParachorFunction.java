package neqsim.physicalProperties.util.parameterFitting.pureComponentParameterFitting.pureCompInterfaceTension;



import neqsim.statistics.parameterFitting.nonLinearParameterFitting.LevenbergMarquardtFunction;

/**
 * <p>
 * ParachorFunction class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class ParachorFunction extends LevenbergMarquardtFunction {
  

  /**
   * <p>
   * Constructor for ParachorFunction.
   * </p>
   */
  public ParachorFunction() {
    params = new double[1];
  }

  /** {@inheritDoc} */
  @Override
  public double calcValue(double[] dependentValues) {
    system.init(3);
    try {
      thermoOps.bubblePointPressureFlash(false);
    } catch (Exception ex) {
      
    }
    system.initPhysicalProperties();
    return system.getInterphaseProperties().getSurfaceTension(0, 1) * 1e3;
  }

  /** {@inheritDoc} */
  @Override
  public void setFittingParams(int i, double value) {
    params[i] = value;
    system.getPhases()[0].getComponent(0).setParachorParameter(value);
    system.getPhases()[1].getComponent(0).setParachorParameter(value);
  }
}
