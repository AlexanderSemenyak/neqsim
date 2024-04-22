package neqsim.physicalProperties.physicalPropertyMethods.gasPhysicalProperties.diffusivity;




/**
 * <p>
 * Diffusivity class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class Diffusivity extends
    neqsim.physicalProperties.physicalPropertyMethods.gasPhysicalProperties.GasPhysicalPropertyMethod
    implements
    neqsim.physicalProperties.physicalPropertyMethods.methodInterface.DiffusivityInterface {
  private static final long serialVersionUID = 1000;
  

  double[][] binaryDiffusionCoefficients;
  double[][] binaryLennardJonesOmega;
  double[] effectiveDiffusionCoefficient;

  /**
   * <p>
   * Constructor for Diffusivity.
   * </p>
   */
  public Diffusivity() {}

  /**
   * <p>
   * Constructor for Diffusivity.
   * </p>
   *
   * @param gasPhase a
   *        {@link neqsim.physicalProperties.physicalPropertySystem.PhysicalPropertiesInterface}
   *        object
   */
  public Diffusivity(
      neqsim.physicalProperties.physicalPropertySystem.PhysicalPropertiesInterface gasPhase) {
    super(gasPhase);
    binaryDiffusionCoefficients = new double[gasPhase.getPhase().getNumberOfComponents()][gasPhase
        .getPhase().getNumberOfComponents()];
    binaryLennardJonesOmega = new double[gasPhase.getPhase().getNumberOfComponents()][gasPhase
        .getPhase().getNumberOfComponents()];
    effectiveDiffusionCoefficient = new double[gasPhase.getPhase().getNumberOfComponents()];
  }

  /** {@inheritDoc} */
  @Override
  public Diffusivity clone() {
    Diffusivity properties = null;

    try {
      properties = (Diffusivity) super.clone();
    } catch (Exception ex) {
      
    }

    properties.binaryDiffusionCoefficients = this.binaryDiffusionCoefficients.clone();
    for (int i = 0; i < gasPhase.getPhase().getNumberOfComponents(); i++) {
      System.arraycopy(this.binaryDiffusionCoefficients[i], 0,
          properties.binaryDiffusionCoefficients[i], 0,
          gasPhase.getPhase().getNumberOfComponents());
    }
    return properties;
  }

  /** {@inheritDoc} */
  @Override
  public double calcBinaryDiffusionCoefficient(int i, int j, int method) {
    // method - estimation method
    // if(method==? then)
    double A2 = 1.06036;
    double B2 = 0.15610;
    double C2 = 0.19300;
    double D2 = 0.47635;
    double E2 = 1.03587;
    double F2 = 1.52996;
    double G2 = 1.76474;
    double H2 = 3.89411;
    double tempVar2 = gasPhase.getPhase().getTemperature() / binaryEnergyParameter[i][j];
    binaryLennardJonesOmega[i][j] = A2 / Math.pow(tempVar2, B2) + C2 / Math.exp(D2 * tempVar2)
        + E2 / Math.exp(F2 * tempVar2) + G2 / Math.exp(H2 * tempVar2);
    binaryDiffusionCoefficients[i][j] =
        0.00266 * Math.pow(gasPhase.getPhase().getTemperature(), 1.5)
            / (gasPhase.getPhase().getPressure() * Math.sqrt(binaryMolecularMass[i][j])
                * Math.pow(binaryMolecularDiameter[i][j], 2) * binaryLennardJonesOmega[i][j]);
    return binaryDiffusionCoefficients[i][j] *= 1e-4;
  }

  /** {@inheritDoc} */
  @Override
  public double[][] calcDiffusionCoefficients(int binaryDiffusionCoefficientMethod,
      int multicomponentDiffusionMethod) {
    for (int i = 0; i < gasPhase.getPhase().getNumberOfComponents(); i++) {
      for (int j = i; j < gasPhase.getPhase().getNumberOfComponents(); j++) {
        binaryDiffusionCoefficients[i][j] =
            calcBinaryDiffusionCoefficient(i, j, binaryDiffusionCoefficientMethod);
        binaryDiffusionCoefficients[j][i] = binaryDiffusionCoefficients[i][j];
      }
    }

    if (multicomponentDiffusionMethod == 0) {
      // ok use full matrix
    } else if (multicomponentDiffusionMethod == 0) {
      // calcEffectiveDiffusionCoefficients();
    }
    return binaryDiffusionCoefficients;
  }

  /** {@inheritDoc} */
  @Override
  public void calcEffectiveDiffusionCoefficients() {
    double sum = 0;

    for (int i = 0; i < gasPhase.getPhase().getNumberOfComponents(); i++) {
      sum = 0;
      for (int j = 0; j < gasPhase.getPhase().getNumberOfComponents(); j++) {
        if (i == j) {
        } else {
          sum += gasPhase.getPhase().getComponents()[j].getx() / binaryDiffusionCoefficients[i][j];
        }
      }
      effectiveDiffusionCoefficient[i] =
          (1.0 - gasPhase.getPhase().getComponents()[i].getx()) / sum;
    }
  }

  /** {@inheritDoc} */
  @Override
  public double getFickBinaryDiffusionCoefficient(int i, int j) {
    return binaryDiffusionCoefficients[i][j];
  }

  /** {@inheritDoc} */
  @Override
  public double getEffectiveDiffusionCoefficient(int i) {
    return effectiveDiffusionCoefficient[i];
  }

  /** {@inheritDoc} */
  @Override
  public double getMaxwellStefanBinaryDiffusionCoefficient(int i, int j) {
    /*
     * double temp = (i==j)? 1.0: 0.0; double nonIdealCorrection = temp +
     * gasPhase.getPhase().getComponents()[i].getx() *
     * gasPhase.getPhase().getComponents()[i].getdfugdn(j) *
     * gasPhase.getPhase().getNumberOfMolesInPhase(); if (Double.isNaN(nonIdealCorrection))
     * nonIdealCorrection=1.0; return binaryDiffusionCoefficients[i][j]/nonIdealCorrection; // shuld
     * be divided by non ideality factor
     */
    return binaryDiffusionCoefficients[i][j];
  }
}
