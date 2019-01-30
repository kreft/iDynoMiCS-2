package epithelialLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import compartment.AgentContainer;
import compartment.Compartment;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Settable;
import surface.Plane;
import surface.Point;



public class EpithelialLayer {

private Agent _template;
	
private int _numberOfAgents;
	
private int _priority;
	
private Compartment _compartment;

private Settable _parentNode;

private int _numberOfDimensions;

private ArrayList <String> _dimensionNames = new ArrayList <String>();

private double[] _cellSideLengths;

private double[] _layerSideLengths;

private double[] _bottomCorner;

private double[] _topCorner;

private String _name;

private Plane _apicalSurface;


public EpithelialLayer (Element xmlElem, Compartment comp) {
	this.init(xmlElem, comp);
}




public void instantiate() {
	
}


public void init(Element xmlElem, Compartment comp) {
	
	this._name = xmlElem.getAttribute(XmlRef.nameAttribute);
	
	this._compartment = comp;
	
	
	
	Element layerShapeElement = XmlHandler.findUniqueChild(
			xmlElem, XmlRef.layerShape);
	
	Collection <Element> layerDimensions = XmlHandler.getElements(
			layerShapeElement, XmlRef.shapeDimension);
	
	ArrayList <String> layerDimensionNames = 
			this.getDimensionNames(layerDimensions);
	
	double[][] layerCorners = this.getCorners(layerDimensions);
	
	this._layerSideLengths = this.getSideLengths(layerCorners);
	
	this._bottomCorner = layerCorners[0];
	
	this._topCorner = layerCorners[1];
	
	Element cellShapeElement = XmlHandler.findUniqueChild(
			xmlElem, XmlRef.cellShape);
	
	Collection <Element> cellDimensions = XmlHandler.getElements(
			cellShapeElement, XmlRef.shapeDimension);
	
	ArrayList <String> cellDimensionNames = 
			this.getDimensionNames(cellDimensions);
	
	this._cellSideLengths = this.getSideLengths(
			this.getCorners(cellDimensions));
	
	if (this.compareDimensionNames(layerDimensionNames, cellDimensionNames)) {
		this._dimensionNames = layerDimensionNames;		
	}
	
	if (this.checkDimensions(layerDimensions)) {
		this._numberOfDimensions = layerDimensions.size();
	};
	
	this._numberOfAgents = this.getNumberOfAgents();
	
	
	this.createEpithelialLayer(xmlElem);
	
}


	
	public ArrayList <String> getDimensionNames(
			Collection <Element> dimensions) {
		ArrayList <String> dimensionNames = new ArrayList <String>();
		for (Element e: dimensions) {
			dimensionNames.add(XmlHandler.gatherAttribute(
					e, XmlRef.nameAttribute));
		}
		return dimensionNames;
	}
	
	
	/*
	 * Returns a 2D array of two rows by x columns, where x is the number of
	 * dimensions. The "top" row (corners[0][i]) contains the min values, while
	 * the "bottom" row (corners[1][i]) contains the max values.
	 */
	public double[][] getCorners(Collection <Element> dimensions) {
		int counter = 0;
		double[][] corners = new double[2][dimensions.size()];
		for (Element e: dimensions) {
			corners[0][counter] = Double.parseDouble(XmlHandler.gatherAttribute(
					e, XmlRef.min));
			corners[1][counter] = Double.parseDouble(XmlHandler.gatherAttribute(
					e, XmlRef.max));
			counter++;
		}
		return corners;
	}
	
	public double[] getSideLengths(double[][] corners) {
		
		double[] sideLengths = new double[corners[0].length];
		for (int i = 0; i < corners[0].length; i++) {
			sideLengths[i] = corners[1][i] - corners[0][i];			
		}
		return sideLengths;
	}
	
	
	public boolean checkDimensions(Collection <Element> layerDimensions) {
		
		if (layerDimensions.size() != 
				this._compartment.getShape().getNumberOfDimensions()) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Compartment "
						+ this._compartment + " and epithelial layer "
						+ this._name + " have different numbers of dimensions");
			return false;
		}
		
		int multiCellDimensions = 0;
		for (int i = 0; i < layerDimensions.size(); i++) {
			if (this._layerSideLengths[i] % this._cellSideLengths[i] != 0) {
				if( Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "Warning: Epithelial layer side"
							+ " length not divisible by cell side length in"
							+ " dimension " + this._dimensionNames.get(i));
				return false;
			}
			if (this._layerSideLengths[i] / this._cellSideLengths[i] != 1.0) {
				multiCellDimensions ++;
			}
		}
		if (multiCellDimensions == layerDimensions.size()) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Mismatch between cell"
						+ " thickness and epithelial layer thickness. All"
						+ " dimensions thicker than one cell.");
			return false;
		}
		return true;
	}
	
	
	
	public boolean compareDimensionNames (ArrayList<String> layerDimensionNames,
			ArrayList<String> cellDimensionNames) {

		if (layerDimensionNames.equals(cellDimensionNames)) {
			return true;
		}
		
		else {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Cell dimensions do not match"
						+ " layer dimensions in " + this._name);
			return false;
		}
		
	}
	
	
	public int getNumberOfAgents () {
		int numberOfEpithelialCells = 1;
		for (int i = 0; i < this._numberOfDimensions; i++) {
			numberOfEpithelialCells *= 
					this._layerSideLengths[i] / this._cellSideLengths[i];
		}
		return numberOfEpithelialCells;
	}
	
	public void createEpithelialLayer(Element spawnElem) {
		
		Element agentTemplate = XmlHandler.findUniqueChild(
				spawnElem, XmlRef.agentTemplate);
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
					
					createEpithelialCell(bottomCorner, agentTemplate);

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
	
	public Agent createEpithelialCell(
			double[] bottomCorner, Element agentTemplate) {
		double[] topCorner = new double[bottomCorner.length];
		for (int j = 0; j < bottomCorner.length; j++) {
			topCorner[j] = bottomCorner[j] + this._cellSideLengths[j];
		}
		
		Point bCPoint = new Point(bottomCorner);
		Point tCPoint = new Point(topCorner);
		Point[] bothPoints = {bCPoint, tCPoint};
		Body newBody = new Body(bothPoints, 0, 0);
		return new Agent((Node) agentTemplate, newBody, this._compartment);
	}
	
	
}
