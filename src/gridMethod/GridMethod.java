package gridMethod;
import grid.SpatialGrid;

/**
 * @author cleggrj
 * Interface detailing what should be done at a boundary. Typical examples
 * include Dirichlet and Neumann boundary conditions. 
 * 
 * TODO Bas: this way, xmin.setGridMethod("test", Boundary.cyclic());
 * becomes xmin.setGridMethod("test", Cyclic()); Yet you can still also
 * implement anonymous method: xmin.setGridMethod("test", new GridMethod() {
 * public double getBoundaryFlux(SpatialGrid grid) { etc. });
 * 
 * The advantage of not using anonymous classes is that you can use the class
 * name/type in your code eg: instanceof, myGridMethod.getClass(), or maybe even
 * more important create new instances from strings (out of our xml file).
 * Class gridMethodClass = Class.forName(stringFromXml);
 * xmin.setGridMethod("test", gridMethodClass.newInstance());
 */
public interface GridMethod
{
	double getBoundaryFlux(SpatialGrid grid);
}
