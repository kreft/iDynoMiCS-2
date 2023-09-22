package optimization.geneticAlgorithm;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import optimization.constraint.Bound;
import optimization.constraint.Constraint;
import optimization.objectiveFunction.ObjectiveFunction;
import optimization.sampling.LatinHyperCubeSampling;

/**
 * This gentetic algorithm implementation is based on the online tutorial by:
 * Lee Jacobson, 2012
 * 
 * http://www.theprojectspot.com/tutorial-post/creating-a-genetic-algorithm-for-beginners/3
 */
public class Population {
	
    /* GA parameters */
    private double _uniformRate = 0.25;
    private double _mutationRate = 0.6;
    private double _mutationScale = 0.1;
    private int _tournamentSize = 8;
    private int _elite = 2;

    private Individual[] _individuals;
    private Collection<Constraint> _constraints;
    private ObjectiveFunction _of;
    
    /**
     * Constructor for initial population (tester method).
     * 
     * @param of
     * @param slices
     * @param lowerBound
     * @param upperBound
     * @param x
     * 
     * @deprecated
     */
    public Population(ObjectiveFunction of, int slices, 
    		Collection<Constraint> constraints, double[] x )
    {
    	/* initialize population parameters */
    	this(of, slices, constraints);
    	
    	/* Latin hyper cube sampling for good coverage of parameter space */
    	LatinHyperCubeSampling lhcSampler = new LatinHyperCubeSampling( slices, 
    			constraints.size());
    	double[][] lhc = lhcSampler.sample();
    	
    	/* identify absolute bounds */
    	double[] upper = null;
    	double[] lower = null;
    	
    	/* TODO currently only considering domain constraints in order to have a
    	 * stable number of individuals out of LHC sampling, for fully random
    	 * sampling methods we may want to consider conditional constraints to.
    	 */
    	for( Constraint c : constraints)
    		if( c instanceof Bound)
    			if( c.isUpperBound() )
    				upper = ((Bound) c).bound();
    			else
    				lower = ((Bound) c).bound();
    	
    	/* generate individuals and scale random position with domain bounds */
        for (int i = 0; i < this.size(); i++) 
        {
            Individual newIndividual = new Individual( Vector.add( lower, 
            		Vector.times(Vector.minus( upper, lower) , lhc[i]) ) );
            newIndividual.evaluate(x);
            set(i, newIndividual);
        }
    }
    
    public Population(ObjectiveFunction of, double[][] inMatrix, 
    		double[][] outMatrix, Collection<Constraint> constraints)
    {
    	/* initialize population parameters */
    	this(of, inMatrix.length, constraints);
        for (int i = 0; i < this.size(); i++) 
        {
            Individual newIndividual = new Individual( inMatrix[i], 
            		outMatrix[i] );
            set(i, newIndividual);
        }
    }

    /**
     * basic population constructor
     * 
     * @param of
     * @param size
     */
    public Population(ObjectiveFunction of, int size, 
    		Collection<Constraint> constraints) 
    {
		this._of = of;
		this._constraints = constraints;
		_individuals = new Individual[size];
	}
    
    /**
     * Set parameters for genetic algorithm
     * @param crossOverProbability
     * @param mutationProbability
     * @param mutationScalar
     * @param tournamentSize
     * @param elite
     */
    public void setGeneticAlgorithm( double crossOverProbability, 
    		double mutationProbability, double mutationScalar,
    		int tournamentSize, int elite )
    {
    	this._uniformRate = crossOverProbability;
    	this._mutationRate = mutationProbability;
    	this._mutationScale = mutationScalar;
    	this._tournamentSize = tournamentSize;
    	this._elite = elite;
    }
    
    /**
     * amount of individuals in this population
     * @return
     */
    public int size() 
    {
        return _individuals.length;
    }

    /**
     * save an individual
     * 
     * @param index
     * @param indiv
     */
    public void set(int index, Individual indiv) 
    {
        _individuals[index] = indiv;
    }

    /**
     * get an individual
     * @param index
     * @return
     */
    public Individual get(int index) 
    {
        return _individuals[index];
    }

    /**
     * get the fittest individual
     * @return
     */
    public Individual fittest() 
    {
        Individual fittest = _individuals[0];
        for (Individual i : _individuals)
        {
        	Log.out("individual: "+i.toString() +"\nMSQ: "+i.loss(_of));
            if (fittest.loss( _of ) > i.loss( _of ) )
            {
                fittest = i;
            }
        }
        return fittest;
    }
    
    /**
     * select the most fit out of a population
     * @param num
     * @return
     */
    public Individual[] fittest( int num ) {
    	
    	Individual[] fittest = new Individual[num];
    	double[] fitness = Vector.vector(num, Double.MAX_VALUE);
    	int slowest = 0;
        double current;
        
        for (int i = 0; i < size(); i++) 
        {
        	current = get(i).loss( _of );
            if ( fitness[slowest] > current ) 
            {
                fittest[slowest] = get(i);
                fitness[slowest] = current;
                slowest = largest( fitness );
            }
        }
        return fittest;
    }
    
    /**
     * Helper method that identifies largest field in double array 
     * @param vector
     * @return
     */
    public int largest(double[] vector)
    {
    	int out = 0;
    	double slowest = vector[out];
    	for( int i = 1; i < vector.length; i++)
    	{
    		if( vector[i] > slowest )
    		{
    			slowest = vector[i];
    			out = i;
    		}
    	}
    	return out;
    }
    
    /**
     * Returns the inMatrix
     * @return
     */
    public double[][] getInMatrix()
    {
    	double[][] out = new double[ size() ][ _individuals[0].size() ];
    	for(int i = 0; i < this.size(); i++)
    		out[i] = Vector.copy( _individuals[i].get() );
    	return out;
    }
    
    /**
     * get the fittest individual from a random subset
     * 
     * @param of
     * @param tournamentSize
     * @return
     */
    public Individual tournament( int tournamentSize ) 
    {
        /* Create a tournament population */
        Population tournament = new Population( this._of, tournamentSize, 
        		this._constraints );
        
        /* For each place in the tournament get a random individual */
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * this.size() );
            tournament.set(i, this.get( randomId ) );
        }
        /* Get the fittest */
        Individual fittest = tournament.fittest();
        return fittest;
    }

    /**
     * get a new generation (Tester method)
     * 
     * @param of
     * @param x
     * @return
     * 
     * @deprecated
     */
    public Population evolvePopulation( double[] x ) 
    {
 
        Population newPopulation = new Population( _of , this.size(), 
        		this._constraints );
        
        for (int i = 0; i < _elite; i++)
        	newPopulation.set(i, fittest( _elite )[i] );
        
        /* crossover population */
        for (int i = _elite; i < newPopulation.size(); i++) 
        {
            Individual indiv1 = this.tournament( _tournamentSize );
            Individual indiv2 = this.tournament( _tournamentSize );
            newPopulation.set(i, indiv1.crossover(indiv2, _uniformRate) );
        }

        /* Mutate population */
        for (int i = _elite; i < newPopulation.size(); i++) 
        {
            newPopulation.get(i).mutate(_mutationRate, _mutationScale, 
            		this._constraints);
            newPopulation.get(i).evaluate(x);
        }

        return newPopulation;
    }
    
    /**
     * 
     * @return
     */
    public Population evolvePopulation() 
    {
        Population newPopulation = new Population( _of , this.size(), 
        		this._constraints );
        
        Individual[] elites = fittest( _elite );
        for (int i = 0; i < _elite; i++)
        {
        	newPopulation.set(i, new Individual( elites[i] ) );
        	if( Log.shouldWrite(Tier.DEBUG) )
        		Log.out(Tier.DEBUG, elites[i].toString() );
        }
        
        
        /* crossover population */
        for (int i = _elite; i < newPopulation.size(); i++) 
        {
            Individual indiv1 = 
            		new Individual( this.tournament( _tournamentSize ) );
            Individual indiv2 = 
            		new Individual( this.tournament( _tournamentSize ) );
            newPopulation.set(i, indiv1.crossover(indiv2, _uniformRate) );
        }

        /* Mutate population */
        for (int i = _elite; i < newPopulation.size(); i++) 
        {
            newPopulation.get(i).mutate(_mutationRate, _mutationScale, 
            		this._constraints);
        }
        
        return newPopulation;
    }

}