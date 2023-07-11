package neqsim.fluidMechanics.flowNode.twoPhaseNode.twoPhasePipeFlowNode;

import org.junit.jupiter.api.Test;
import neqsim.fluidMechanics.geometryDefinitions.pipe.PipeData;
import neqsim.thermo.phase.PhaseType;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkEos;

public class StratifiedFlowNodeTest {
  @Test
  void testInit() {
    SystemInterface testSystem = new SystemSrkEos(313.3, 100.01325);
    testSystem.addComponent("methane", 1100, "kg/hr", 0);
    testSystem.addComponent("nC10", 11.1, "kg/hr", 1);
    testSystem.setMixingRule(2);

    PipeData pipe1 = new PipeData(0.1, 0.00025);

    StratifiedFlowNode test = new StratifiedFlowNode(testSystem, pipe1);
    test.setInterphaseModelType(1);
    test.setLengthOfNode(0.1);
    test.getFluidBoundary().setHeatTransferCalc(false);
    test.getFluidBoundary().setMassTransferCalc(true);

    for (int i = 0; i < 10; i++) {
      test.initFlowCalc();
      test.calcFluxes();
      test.update();
      /*
       * System.out.println( "flux methane " + test.getFluidBoundary().getInterphaseMolarFlux(0) +
       * " [mol/m2*sec]"); System.out.println( "flux nC10 " +
       * test.getFluidBoundary().getInterphaseMolarFlux(1) + " [mol/m2*sec]");
       * System.out.println("gas velocity " + test.getSuperficialVelocity(0) + " m/sec");
       * System.out.println("liquid velocity " + test.getVelocity(1) + " m/sec");
       * System.out.println("liquid holdup " + test.getPhaseFraction(1) + "-");
       * System.out.println("interface contact area " + test.getInterphaseContactLength(0) +
       * "m2/m"); System.out.println("gas flow rate " + test.getMassFlowRate(0) * 60 * 60 +
       * " kg/hr"); System.out.println("liquid flow rate " + test.getMassFlowRate(1) * 60 * 60 +
       * " kg/hr");
       */
    }
  }

  @Test
  void testInit2() {
    SystemInterface testSystem = new SystemSrkEos(313.3, 100.01325);
    testSystem.addComponent("methane", 1100, "kg/hr", 0);
    testSystem.addComponent("nC10", 11.1, "kg/hr", 1);
    testSystem.setMixingRule(2);
    testSystem.initProperties();
    // testSystem.prettyPrint();
    PipeData pipe1 = new PipeData(0.1, 0.00025);

    StratifiedFlowNode test = new StratifiedFlowNode(testSystem, pipe1);
    test.setInterphaseModelType(1);
    test.setLengthOfNode(0.001);
    test.getFluidBoundary().setHeatTransferCalc(false);
    test.getFluidBoundary().setMassTransferCalc(true);

    test.initFlowCalc();
    test.calcFluxes();

    System.out.println(
        "flux methane " + test.getFluidBoundary().getInterphaseMolarFlux(0) + " [mol/m2*sec]");
    System.out.println(
        "flux nC10 " + test.getFluidBoundary().getInterphaseMolarFlux(1) + " [mol/m2*sec]");

    SystemInterface gasPhase = testSystem.phaseToSystem("gas");
    SystemInterface oilPhase = testSystem.phaseToSystem("oil");

    oilPhase.addComponent("methane",
        test.getFluidBoundary().getInterphaseMolarFlux(0) * 1.0 * test.getInterphaseContactArea());
    oilPhase.addComponent(1,
        test.getFluidBoundary().getInterphaseMolarFlux(1) * 1.0 * test.getInterphaseContactArea());
    oilPhase.initBeta();
    oilPhase.init_x_y();
    oilPhase.initProperties();
    oilPhase.prettyPrint();

    SystemInterface newFluid = new SystemSrkEos(313.3, 100.01325);
    newFluid.addFluid(gasPhase, 0);
    newFluid.addFluid(oilPhase, 1);
    newFluid.setMixingRule(2);
    newFluid.initProperties();
    newFluid.prettyPrint();

    StratifiedFlowNode test2 = new StratifiedFlowNode(newFluid, pipe1);
    test2.setInterphaseModelType(1);
    test2.setLengthOfNode(0.001);
    test2.getFluidBoundary().setHeatTransferCalc(false);
    test2.getFluidBoundary().setMassTransferCalc(true);
    test2.initFlowCalc();
    test2.calcFluxes();

    System.out.println(
        "flux methane " + test2.getFluidBoundary().getInterphaseMolarFlux(0) + " [mol/m2*sec]");
    System.out.println(
        "flux nC10 " + test2.getFluidBoundary().getInterphaseMolarFlux(1) + " [mol/m2*sec]");

    oilPhase.addComponent(0, test2.getFluidBoundary().getInterphaseMolarFlux(0) * 100.0
        * test.getInterphaseContactArea());
    oilPhase.addComponent(1, test2.getFluidBoundary().getInterphaseMolarFlux(1) * 100.0
        * test.getInterphaseContactArea());
    oilPhase.initBeta();
    oilPhase.init_x_y();
    oilPhase.initProperties();

    gasPhase = test2.getBulkSystem().phaseToSystem("gas");

    SystemInterface newFluid2 = new SystemSrkEos(313.3, 100.01325);
    newFluid2.addFluid(gasPhase, 0);
    newFluid2.addFluid(oilPhase, 1);
    newFluid2.setMixingRule(2);
    newFluid2.initProperties();
    newFluid2.prettyPrint();

    StratifiedFlowNode test3 = new StratifiedFlowNode(newFluid2, pipe1);
    test3.setInterphaseModelType(1);
    test3.setLengthOfNode(0.001);
    test3.getFluidBoundary().setHeatTransferCalc(false);
    test3.getFluidBoundary().setMassTransferCalc(true);
    test3.initFlowCalc();
    test3.calcFluxes();

    System.out.println(
        "flux methane " + test3.getFluidBoundary().getInterphaseMolarFlux(0) + " [mol/m2*sec]");
    System.out.println(
        "flux nC10 " + test3.getFluidBoundary().getInterphaseMolarFlux(1) + " [mol/m2*sec]");


  }



  @Test
  void testInit3() {

    SystemInterface[] gasPhases = new SystemInterface[10];
    SystemInterface[] oilPhases = new SystemInterface[10];
    StratifiedFlowNode[] nodes = new StratifiedFlowNode[10];
    SystemInterface[] fluids = new SystemInterface[10];
    PipeData[] pipes = new PipeData[10];

    SystemInterface testSystem = new SystemSrkEos(313.3, 100.01325);
    testSystem.addComponent("methane", 1100, "kg/hr", 0);
    testSystem.addComponent("nC10", 11.1, "kg/hr", 1);
    testSystem.setMixingRule(2);
    testSystem.initProperties();

    gasPhases[0] = testSystem.phaseToSystem(0);
    gasPhases[0].setTotalFlowRate(1100.0, "kg/hr");
    gasPhases[0].setNumberOfPhases(1);
    gasPhases[0].setPhaseType(0, PhaseType.GAS);

    for (int i = 0; i < oilPhases.length; i++) {
      oilPhases[i] = testSystem.phaseToSystem(1);
      oilPhases[i].setTotalFlowRate(10.0, "kg/hr");
      oilPhases[i].setNumberOfPhases(1);
      oilPhases[i].setPhaseType(0, PhaseType.OIL);
    }

    for (int time = 0; time < 4000; time++) {
      for (int i = 0; i < 9; i++) {
        fluids[i] = new SystemSrkEos(313.3, 100.01325);
        fluids[i].addFluid(gasPhases[i], 0);
        fluids[i].addFluid(oilPhases[i], 1);
        fluids[i].setMixingRule(2);
        fluids[i].initProperties();

        pipes[i] = new PipeData(0.1, 0.00025);

        nodes[i] = new StratifiedFlowNode(fluids[i], pipes[i]);
        nodes[i].setInterphaseModelType(1);
        nodes[i].setLengthOfNode(0.01);
        nodes[i].getFluidBoundary().setHeatTransferCalc(false);
        nodes[i].getFluidBoundary().setMassTransferCalc(true);

        nodes[i].initFlowCalc();
        nodes[i].calcFluxes();
        nodes[i].update();

        if (i <= 9) {
          gasPhases[i + 1] = nodes[i].getBulkSystem().phaseToSystem(0);
        }
        oilPhases[i] = nodes[i].getBulkSystem().phaseToSystem(1);

        // gasPhases[i + 1].prettyPrint();
        // oilPhases[i].prettyPrint();
        System.out.println(
            "time " + time + " node " + i + "  mass oil " + oilPhases[i].getFlowRate("kg/hr"));
      }
    }

  }

}
