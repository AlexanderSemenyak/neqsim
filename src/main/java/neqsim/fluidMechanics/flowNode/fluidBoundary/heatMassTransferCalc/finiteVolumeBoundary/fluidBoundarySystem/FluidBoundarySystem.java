package neqsim.fluidMechanics.flowNode.fluidBoundary.heatMassTransferCalc.finiteVolumeBoundary.fluidBoundarySystem;

import neqsim.fluidMechanics.flowNode.fluidBoundary.heatMassTransferCalc.FluidBoundaryInterface;
import neqsim.fluidMechanics.flowNode.fluidBoundary.heatMassTransferCalc.finiteVolumeBoundary.fluidBoundaryNode.FluidBoundaryNodeInterface;
import neqsim.fluidMechanics.flowNode.fluidBoundary.heatMassTransferCalc.finiteVolumeBoundary.fluidBoundarySolver.FluidBoundarySolver;
import neqsim.fluidMechanics.flowNode.fluidBoundary.heatMassTransferCalc.finiteVolumeBoundary.fluidBoundarySolver.FluidBoundarySolverInterface;

/**
 * <p>
 * FluidBoundarySystem class.
 * </p>
 *
 * @author esol
 * @version $Id: $Id
 */
public class FluidBoundarySystem implements FluidBoundarySystemInterface {
    protected FluidBoundaryInterface boundary;
    protected int numberOfNodes = 10;
    protected double filmThickness = 0.01;
    protected FluidBoundaryNodeInterface[] nodes;
    protected boolean reactive = false;
    protected FluidBoundarySolverInterface solver;

    /**
     * <p>
     * Constructor for FluidBoundarySystem.
     * </p>
     */
    public FluidBoundarySystem() {}

    /**
     * <p>
     * Constructor for FluidBoundarySystem.
     * </p>
     *
     * @param boundary a
     *        {@link neqsim.fluidMechanics.flowNode.fluidBoundary.heatMassTransferCalc.FluidBoundaryInterface}
     *        object
     */
    public FluidBoundarySystem(FluidBoundaryInterface boundary) {
        this.boundary = boundary;
        reactive = false;
    }

    /** {@inheritDoc} */
    @Override
    public void addBoundary(FluidBoundaryInterface boundary) {
        this.boundary = boundary;
    }

    /** {@inheritDoc} */
    @Override
    public void setNumberOfNodes(int nodes) {
        this.numberOfNodes = nodes;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    /** {@inheritDoc} */
    @Override
    public FluidBoundaryNodeInterface getNode(int i) {
        return nodes[i];
    }

    /** {@inheritDoc} */
    @Override
    public void setFilmThickness(double filmThickness) {
        this.filmThickness = filmThickness;
    }

    /** {@inheritDoc} */
    @Override
    public double getNodeLength() {
        return this.filmThickness / this.numberOfNodes;
    }

    /** {@inheritDoc} */
    @Override
    public double getFilmThickness() {
        return filmThickness;
    }

    /** {@inheritDoc} */
    @Override
    public FluidBoundaryInterface getFluidBoundary() {
        return boundary;
    }

    /** {@inheritDoc} */
    @Override
    public void createSystem() {}

    /** {@inheritDoc} */
    @Override
    public void solve() {
        solver = new FluidBoundarySolver(this, reactive);
        solver.solve();
    }
}
