package neqsim.processSimulation.processEquipment.util;

import java.util.ArrayList;
import neqsim.processSimulation.processEquipment.ProcessEquipmentBaseClass;
import neqsim.processSimulation.processEquipment.ProcessEquipmentInterface;
import neqsim.processSimulation.processEquipment.stream.Stream;

/**
 * <p>Calculator class.</p>
 *
 * @author asmund
 * @version $Id: $Id
 */
public class Calculator extends ProcessEquipmentBaseClass {
    ArrayList<ProcessEquipmentInterface> inputVariable = new ArrayList<ProcessEquipmentInterface>();
    private ProcessEquipmentInterface outputVariable;
    String type = "sumTEG";

    /**
     * <p>Constructor for Calculator.</p>
     *
     * @param name a {@link java.lang.String} object
     */
    public Calculator(String name) {
        super(name);
    }

    /**
     * <p>addInputVariable.</p>
     *
     * @param unit a {@link neqsim.processSimulation.processEquipment.ProcessEquipmentInterface} object
     */
    public void addInputVariable(ProcessEquipmentInterface unit) {
        inputVariable.add(unit);
    }

    /**
     * <p>Getter for the field <code>outputVariable</code>.</p>
     *
     * @return a {@link neqsim.processSimulation.processEquipment.ProcessEquipmentInterface} object
     */
    public ProcessEquipmentInterface getOutputVariable() {
        return outputVariable;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        double sum = 0.0;

        if (name.equals("MEG makeup calculator")) {
            for (int i = 0; i < inputVariable.size(); i++) {
                sum += inputVariable.get(i).getFluid().getPhase(0).getComponent("MEG")
                        .getFlowRate("kg/hr");
            }
        } else {
            for (int i = 0; i < inputVariable.size(); i++) {
                sum += inputVariable.get(i).getFluid().getPhase(0).getComponent("TEG")
                        .getFlowRate("kg/hr");
            }
        }

        // System.out.println("make up MEG " + sum);
        outputVariable.getFluid().setTotalFlowRate(sum, "kg/hr");
        try {
            ((Stream) outputVariable).setFlowRate(sum, "kg/hr");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Setter for the field <code>outputVariable</code>.</p>
     *
     * @param outputVariable a {@link neqsim.processSimulation.processEquipment.ProcessEquipmentInterface} object
     */
    public void setOutputVariable(ProcessEquipmentInterface outputVariable) {
        this.outputVariable = outputVariable;
    }
}
