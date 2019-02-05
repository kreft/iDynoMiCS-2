package compartment.agentStaging;

import org.w3c.dom.Element;
import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import compartment.AgentContainer;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.BoundingBox;
import surface.Plane;
import surface.Point;



public class EpithelialLayerSpawner extends Spawner {

	private int _numberOfDimensions;
	
	private double[] _cellSideLengths;
	
	private double[] _layerSideLengths;
	
	private double[] _bottomCorner;
	
	private double[] _topCorner;
	
	private Plane _apicalSurface;
	
	private double[][] _layerShape;


	public void init(
		
		Element xmlElem, AgentContainer agents, String compartmentName) {
		
		super.init(xmlElem, agents, compartmentName);
		
		this._layerShape = this.calculateLayerCorners();
		
		this._layerSideLengths = this.getSideLengths(_layerShape);
		
		this._bottomCorner = _layerShape[0];
		
		this._topCorner = _layerShape[1];
		
		if ( XmlHandler.hasAttribute(xmlElem, XmlRef.cellShape) )
		{
			this._cellSideLengths =	Vector.dblFromString(
					xmlElem.getAttribute(XmlRef.cellShape));
		}
		
		this.checkDimensions();
		
		this._numberOfDimensions = _layerShape[0].length;
		
		this.setNumberOfAgents(this.calculateNumberOfAgents());
		
	}


	public void spawn() {
	
		double[] bottomCorner = new double[3];
		for (int i = 0; i < this._bottomCorner.length; i++) {
			bottomCorner[i] = this._bottomCorner[i];			
		}
		double[] topCorner = new double[3];
		for (int i = 0; i < this._topCorner.length; i++) {
			topCorner[i] = this._topCorner[i];			
		}
		double[] cellSideLengths = new double[3];
		for (int i = 0; i < this._cellSideLengths.length; i++) {
			cellSideLengths[i] = this._cellSideLengths[i];			
		}
		
		if (this._numberOfDimensions < 3) {
			bottomCorner[2] = 0.0;
			topCorner[2] = 1.0;
			cellSideLengths[2] = 1.0;
		}
		
		while (bottomCorner[2] < topCorner[2]) {
			
			while(bottomCorner[1] < topCorner[1]) {
				
				while (bottomCorner[0] < topCorner[0]) {
					
					createEpithelialCell(bottomCorner);
	
					bottomCorner[0] += cellSideLengths[0];
				}
				
				bottomCorner[0] = this._bottomCorner[0];
				
				bottomCorner[1] += cellSideLengths[1];
			}
			
			bottomCorner[0] = this._bottomCorner[0];
			
			bottomCorner[1] = this._bottomCorner[1];
			
			bottomCorner[2] += cellSideLengths[2];
		}
	
	}

	
	/*
	 * Returns a 2D array of two rows by x columns, where x is the number of
	 * dimensions. The "top" row (corners[0][i]) contains the min values (lower
	 * corner), while the "bottom" row (corners[1][i]) contains the max values
	 * (higher corner).
	 */
	public double[][] calculateLayerCorners() {
		BoundingBox spawnDomain = this.getSpawnDomain();
		double[] lower = spawnDomain.lowerCorner();
		double[] higher = spawnDomain.higherCorner();
		double[][] corners = {lower, higher};
		return corners;
	}
	
	public double[] getSideLengths(double[][] corners) {
		
		double[] sideLengths = new double[corners[0].length];
		for (int i = 0; i < corners[0].length; i++) {
			sideLengths[i] = corners[1][i] - corners[0][i];			
		}
		return sideLengths;
	}
	
	
	public void checkDimensions() {
		
		if ((_layerShape[0].length != 
				this.getCompartment().getShape().getNumberOfDimensions())
			|| (_layerShape[1].length != 
					this.getCompartment().getShape().getNumberOfDimensions())) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Compartment "
						+ this.getCompartment().getName() + " and epithelial "
						+ "layer have different numbers of dimensions");
			Idynomics.simulator.interupt("Interrupted due to dimension "
					+ "mismatch between compartment and epithelial layer.");
		}
		
		int multiCellDimensions = 0;
		for (int i = 0; i < _layerShape[0].length; i++) {
			if (this._layerSideLengths[i] % this._cellSideLengths[i] != 0.0) {
				if( Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "Warning: Epithelial layer side"
							+ " length not divisible by cell side length in"
							+ " dimension " + i+1);
				Idynomics.simulator.interupt("Interrupted to prevent bad cell "
						+ "fitting");
			}
			if (this._layerSideLengths[i] / this._cellSideLengths[i] != 1.0) {
				multiCellDimensions ++;
			}
		}
		if (multiCellDimensions == _layerShape[0].length) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Mismatch between cell"
						+ " thickness and epithelial layer thickness. All"
						+ " dimensions thicker than one cell.");
			Idynomics.simulator.interupt("Interrupted because epithelial"
					+ "layer is not exactly one cell thick");
		}
	}
	

	
	public int calculateNumberOfAgents () {
		int numberOfEpithelialCells = 1;
		for (int i = 0; i < this._numberOfDimensions; i++) {
			numberOfEpithelialCells *= 
					this._layerSideLengths[i] / this._cellSideLengths[i];
		}
		return numberOfEpithelialCells;
	}
	

	public void createEpithelialCell(double[] bottomCorner) {
		double[] topCorner = new double[bottomCorner.length];
		for (int j = 0; j < bottomCorner.length; j++) {
			topCorner[j] = bottomCorner[j] + this._cellSideLengths[j];
		}
		
		Point bCPoint = new Point(bottomCorner);
		Point tCPoint = new Point(topCorner);
		Point[] bothPoints = {bCPoint, tCPoint};
		Agent newEpithelialCell = new Agent(this.getTemplate());
		newEpithelialCell.set(AspectRef.agentBody, new Body(bothPoints, 0, 0));
		newEpithelialCell.setCompartment( this.getCompartment() );
		newEpithelialCell.registerBirth();
		
	}
	
	
}
