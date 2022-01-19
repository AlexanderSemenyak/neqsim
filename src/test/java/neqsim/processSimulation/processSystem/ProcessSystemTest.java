package neqsim.processSimulation.processSystem;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import neqsim.processSimulation.processEquipment.ProcessEquipmentInterface;
import neqsim.processSimulation.processEquipment.separator.Separator;

public class ProcessSystemTest {
    ProcessSystem p;

    @BeforeEach
    public void setUp() {
        p = new ProcessSystem();
    }

    @Test
    public void testGetName() {
        String name = "TestProsess";
        p.setName(name);
        Assertions.assertEquals(p.getName(), name);
    }

    @Test
    public void testSetTimeStep() {
        double timeStep = 10;
        p.setTimeStep(timeStep);
        Assertions.assertEquals(p.getTimeStep(), timeStep);
    }

    @Test
    public void testHasUnitName() {
        String sepName = "TestSep";
        Assertions.assertFalse(p.hasUnitName(sepName));
        p.add(new Separator(sepName));
        Assertions.assertTrue(p.hasUnitName(sepName));
    }

    @Test
    void testAdd() {
        String sepName = "TestSep";
        Separator sep = new Separator();
        sep.setName(sepName);
        p.add(sep);

        ArrayList<ProcessEquipmentInterface> list = p.getUnitOperations();

        Assertions.assertTrue(sep == p.getUnit(sepName));

        Assertions.assertEquals(list.size(), 1);
        Assertions.assertEquals(p.size(), 1);

        Assertions.assertTrue((Separator) list.get(0) == sep);

        p.removeUnit(sepName);
        Assertions.assertNull(p.getUnit(sepName));
        Assertions.assertEquals(p.size(), 0);

        list = p.getUnitOperations();

        Assertions.assertEquals(list.size(), 0);

        p.add(sep);
        Assertions.assertEquals(p.size(), 1);

        p.clear();
        Assertions.assertEquals(p.size(), 0);

        p.add(sep);
        Assertions.assertEquals(p.size(), 1);

        p.clearAll();
        Assertions.assertEquals(p.size(), 0);
    }

    @Test
    public void testAddUnitTwice() {
        Separator sep = new Separator();
        p.add(sep);
        p.add(sep); // Won't add the copy
        Assertions.assertEquals(p.size(), 1);
    }

    @Test
    public void testRemoveUnit() {
        Separator sep = new Separator();
        p.add(sep);
        Assertions.assertEquals(p.size(), 1);
        p.removeUnit("");
        Assertions.assertEquals(p.size(), 0);
    }


    @Test
    public void testAddUnitsWithNoName() {
        Separator sep = new Separator();
        p.add(sep);
        sep = new Separator();
        p.add(sep);
        Assertions.assertEquals(p.size(), 2);
        p.removeUnit("Separator2");
        Assertions.assertEquals(p.size(), 1);
        p.removeUnit("");
        Assertions.assertEquals(p.size(), 0);
    }

    @Test
    public void testGetUnitNumber() {
        Separator sep = new Separator();
        p.add(sep);
        Separator sep2 = new Separator();
        p.add(sep2);

        Assertions.assertEquals(p.getUnitNumber(""), 0);
        Assertions.assertEquals(p.getUnitNumber("Separator2"), 1);

        p.removeUnit("");
        p.add(sep);

        Assertions.assertEquals(p.getUnitNumber("Separator2"), 0);
        Assertions.assertEquals(p.getUnitNumber(""), 1);
    }

    @Test
    public void testSetSurroundingTemperature() {
        double temp = 200;
        p.setSurroundingTemperature(temp);
        Assertions.assertEquals(p.getSurroundingTemperature(), temp);
    }
}
