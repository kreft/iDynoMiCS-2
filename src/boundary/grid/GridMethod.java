/**
 * 
 */
package boundary.grid;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import generalInterfaces.XMLable;
import grid.SpatialGrid;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import nodeFactory.ModelNode;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class GridMethod implements IsSubmodel, XMLable
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
	
	public List<InputSetter> getRequiredInputs()
	{
		// TODO quick fix, do properly
		return new LinkedList<InputSetter>();
	}
	
	public void acceptInput(String name, Object input)
	{
		// TODO quick fix, do properly
	}
	
	/*************************************************************************
	 * XML-ABLE
	 ************************************************************************/
	
	public static GridMethod getNewInstance(String className)
	{
		return (GridMethod) XMLable.getNewInstance(className, 
									"boundary.grid.GridMethodLibrary$");
	}
	
	// TODO required for xmlable interface
	public ModelNode getNode()
	{
		return null;
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
	
	public static class GridMethodMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -2794244870765785699L;
		
		// TODO give this a name?
		
		/**\brief TODO
		 * 
		 * @param name
		 * @param req
		 * @param target
		 */
		public GridMethodMaker(String name, Requirement req, IsSubmodel target)
		{
			super(name, req, target);
		}
		
		@Override
		protected void doAction(ActionEvent e)
		{
			String name;
			if ( e == null )
				name = "";
			else
				name = e.getActionCommand();
			this.addSubmodel(GridMethod.getNewInstance(name));
		}
		
		public Object getOptions()
		{
			return getAllOptions();
		}
	}
}
