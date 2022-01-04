package neqsim.thermo.util.parameterFitting.binaryInteractionParameterFitting.HuronVidalParameterFitting;

/**
 * <p>
 * BinaryHVparameterFitToActivityCoefficientFunction class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class BinaryHVparameterFitToActivityCoefficientFunction extends HuronVidalFunction {
    private static final long serialVersionUID = 1000;

    /**
     * <p>
     * Constructor for BinaryHVparameterFitToActivityCoefficientFunction.
     * </p>
     */
    public BinaryHVparameterFitToActivityCoefficientFunction() {}

    /** {@inheritDoc} */
    @Override
    public double calcValue(double[] dependentValues) {
        system.init(0);
        system.init(1);

        // double fug = system.getPhases()[1].getComponents()[0].getFugasityCoeffisient();
        // double pureFug = system.getPhases()[1].getPureComponentFugacity(0);
        double val = system.getPhase(1).getActivityCoefficient(0);
        // System.out.println("activity: " + val);
        return val;
    }

    /** {@inheritDoc} */
    @Override
    public double calcTrueValue(double val) {
        return val;
    }
}
