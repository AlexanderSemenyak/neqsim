package neqsim.thermodynamicOperations.util.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkEos;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/*
 *
 * @author esol
 * 
 * @version
 */
public class TVflash {

    private static final long serialVersionUID = 1000;
    static Logger logger = LogManager.getLogger(TVflash.class);

    /** Creates new TPflash */
    public TVflash() {}

    public static void main(String args[]) {
        // SystemInterface testSystem2 = (SystemInterface)
        // util.serialization.SerializationManager.open("c:/test.fluid");
        // testSystem2.display();
        SystemInterface testSystem = new SystemSrkEos(273.15 + 5, 1.0);

        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        testSystem.addComponent("nitrogen", 90.0);
        // testSystem.addComponent("ethane", 4.0);
        testSystem.addComponent("oxygen", 10.2);
        testSystem.addComponent("water", 1.5);

        // 1500 m3 vann

        // testSystem.addComponent("water", 1.0);
        testSystem.createDatabase(true);
        // 1- orginal no interaction 2- classic w interaction
        // 3- Huron-Vidal 4- Wong-Sandler
        testSystem.setMixingRule(2);
        // testSystem.setMultiPhaseCheck(true);
        testSystem.init(0);
        try {
            testOps.TPflash();
            testSystem.display();
            testSystem.setTemperature(273.15 + 55.1);
            testOps.TVflash(testSystem.getVolume());
            testSystem.display();
            // testOps.PVrefluxFlash(0.05, 1);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
