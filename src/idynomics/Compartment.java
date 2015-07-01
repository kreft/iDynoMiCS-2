package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import processManager.ProcessManager;
import spatialgrid.SoluteGrid;

public class Compartment
{
	
	protected String _name;
	
	/**
	 * Number of spatial dimensions in this compartment.
	 */
	private int _nDims;
	
	/**
	 * 
	 */
	protected AgentContainer _agents;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _soluteGrids; 
	
	/**
	 * 
	 */
	protected LinkedList<ProcessManager> _mechanisms;
	
	protected MechanismComparator mechComp;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Compartment()
	{
		
	}
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	protected int getNumDims() {
		return _nDims;
	}


	protected void setNumDims(int _nDims) {
		this._nDims = _nDims;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * 
	 */
	public void step()
	{
		ProcessManager currentMech = _mechanisms.getFirst();
		while ( currentMech.getTimeForNextStep() < Timer.getEndOfCurrentIteration() )
		{
			currentMech.step(_soluteGrids, _agents);
			Collections.sort(_mechanisms, mechComp);
			currentMech = _mechanisms.getFirst();
		}
	}

	protected static class MechanismComparator implements Comparator<ProcessManager>
	{
		@Override
		public int compare(ProcessManager mech1, ProcessManager mech2) 
		{
			Double temp = mech1.getTimeForNextStep() -
												mech2.getTimeForNextStep();
			/*
			 * TODO Should deal with numerical rounding errors here, rather
			 * than just checking for zero. 
			 */
			if ( temp == 0.0 )
				return mech1.getPriority() - mech2.getPriority();
			else
				return temp.intValue();
		}
	}
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}
