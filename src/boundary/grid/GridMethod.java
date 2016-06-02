/**
 * 
 */
package boundary.grid;

import generalInterfaces.XMLable;
import grid.SpatialGrid;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class GridMethod implements XMLable
{
	/**
	 * Interface detailing what should be done at a boundary. Typical examples
	 * include Dirichlet and Neumann boundary conditions. 
	 */
	public abstract double getBoundaryFlux(SpatialGrid grid);
	
	/*************************************************************************
	 * SUB-MODEL BUILDING
	 ************************************************************************/
	
	public String getName()
	{
		// TODO quick fix, do properly
		return "Grid Boundary";
	}
	
	//FIXME to be replaced by modelnode paradigm?
//	public List<InputSetter> getRequiredInputs()
//	{
//		// TODO quick fix, do properly
//		return new LinkedList<InputSetter>();
//	}
//	
//	public void acceptInput(String name, Object input)
//	{
//		// TODO quick fix, do properly
//	}
	
	/*************************************************************************
	 * XML-ABLE
	 ************************************************************************/
	
	public static GridMethod getNewInstance(String className)
	{
		return (GridMethod) XMLable.getNewInstance(className, 
									"boundary.grid.GridMethodLibrary$");
	}
		
	/*************************************************************************
	 * USEFUL SUBMETHODS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * TODO Rob [10Dec2015]: This method is doubtless wrong! 
	 * 
	 * @param bndryConcn
	 * @param gridConcn
	 * @param diffusivity
	 * @param surfaceArea
	 * @return
	 */
	public static double calcFlux(double bndryConcn, double gridConcn,
									double diffusivity, double surfaceArea)
	{
		return (bndryConcn - gridConcn) * diffusivity * surfaceArea;
	}
	
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
				GridMethodLibrary.class.getDeclaredClasses());
	}
	
	//FIXME to be replaced by modelnode paradigm?
//	public static class GridMethodMaker extends SubmodelMaker
//	{
//		private static final long serialVersionUID = -2794244870765785699L;
//		
//		// TODO give this a name?
//		
//		/**\brief TODO
//		 * 
//		 * @param name
//		 * @param req
//		 * @param target
//		 */
//		public GridMethodMaker(String name, Requirement req, IsSubmodel target)
//		{
//			super(name, req, target);
//		}
//		
//		@Override
//		protected void doAction(ActionEvent e)
//		{
//			String name;
//			if ( e == null )
//				name = "";
//			else
//				name = e.getActionCommand();
//			this.addSubmodel(GridMethod.getNewInstance(name));
//		}
//		
//		public Object getOptions()
//		{
//			return getAllOptions();
//		}
//	}
}
