package surface.collision;


import aspect.AspectInterface;
import instantiable.Instantiable;


/**
 * \brief CollisionFunctions are used to shape and scale the physical
 * interactions between two objects
 */
public interface CollisionFunction extends Instantiable
{
	/**
	 * \brief return the currently set force scalar for this CollisionFunction
	 * 
	 * @return double force scalar
	 */
	public double forceScalar();
	
	/**
	 * \brief calculate a force between two objects based on the distance
	 * 
	 * @param distance
	 * @param var: functions as a scratch book to pass multiple in/output 
	 * variables between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var,
			AspectInterface first, AspectInterface second);

//	/**
//	 * default pull CollisionFunction
//	 */
//	public CollisionFunction DefaultPullFunction = new CollisionFunction()
//	{
//		private double _forceScalar = Global.pull_scalar;
//		
//		/**
//		 * Implementation of the Instantiatable interface
//		 * FIXME currently not settable from xml yet
//		 */
//		public void instantiate(Element xmlElement, Settable parent)
//		{
//			String forceScalar = XmlHandler.gatherAttribute(xmlElement, 
//					XmlRef.forceScalar);
//			if( !Helper.isNullOrEmpty( forceScalar ) )
//					this._forceScalar = Double.valueOf( forceScalar );
//			if(Log.shouldWrite(Tier.BULK))
//				Log.out(Tier.BULK, "initiating " + 
//						this.getClass().getSimpleName());
//		}
//		
//		/**
//		 * \brief return the currently set force scalar for this 
//		 * CollisionFunction
//		 * 
//		 * @return double force scalar
//		 */
//		public double forceScalar()
//		{
//			return this._forceScalar;
//		}
//		
//		/**
//		 * \brief calculate a force between two objects based on the distance
//		 * 
//		 * @param distance
//		 * @param var: functions as a scratch book to pass multiple in/output 
//		 * variables between methods
//		 * @return force vector
//		 */
//		public CollisionVariables interactionForce(CollisionVariables var)
//		{
//			/*
//			 * If distance is in the range, apply the pull force.
//			 * Otherwise, return a zero vector. A small distance is allowed to
//			 * prevent objects bouncing in equilibrium 
//			 */
//			if ( var.distance > 0.001 && var.distance < var.pullRange ) 
//			{
//				/* Linear. */
//				double c = Math.abs(this._forceScalar * var.distance);
//				/* dP is overwritten here. */
//				Vector.normaliseEuclidEquals(var.interactionVector, c);
//				return var;
//			} 
//			Vector.setAll(var.interactionVector, 0.0);
//			return var;
//		}
//	};
//	
//	/**
//	 * default push CollisionFunction
//	 * FIXME currently not settable from xml yet
//	 */
//	public CollisionFunction DefaultPushFunction = new CollisionFunction()
//	{
//		private double _forceScalar = Global.collision_scalar;
//		
//		/**
//		 * Implementation of the Instantiatable interface
//		 */
//		public void instantiate(Element xmlElement, Settable parent)
//		{
//			String forceScalar = XmlHandler.gatherAttribute( xmlElement, 
//					XmlRef.forceScalar);
//			if( !Helper.isNullOrEmpty( forceScalar ) )
//					this._forceScalar = Double.valueOf( forceScalar );
//			if(Log.shouldWrite(Tier.BULK))
//				Log.out(Tier.BULK, "initiating " + 
//						this.getClass().getSimpleName());
//		}
//		
//		/**
//		 * \brief return the currently set force scalar for this 
//		 * CollisionFunction
//		 * 
//		 * @return double force scalar
//		 */
//		public double forceScalar()
//		{
//			return this._forceScalar;
//		}
//		
//		/**
//		 * \brief calculate a force between two objects based on the distance
//		 * 
//		 * @param distance
//		 * @param var: functions as a scratch book to pass multiple in/output 
//		 * variables
//		 * between methods
//		 * @return force vector
//		 */
//		public CollisionVariables interactionForce(CollisionVariables var)
//		{
//			/*
//			 * If distance is negative, apply the repulsive force.
//			 * Otherwise, return a zero vector. A small overlap is allowed to
//			 * prevent objects bouncing in equilibrium 
//			 */
//			if ( var.distance < -0.001 ) 
//			{
//				/* Linear. */
////				double c = Math.abs( this._forceScalar * var.distance );
//				/* two component, better force scaling for low vs high overlap */
//				double c = this._forceScalar * (  Math.abs( var.distance ) + var.distance * var.distance * 1e2 );
//				/* dP is overwritten here. */
//				Vector.normaliseEuclidEquals( var.interactionVector, c );
//				return var;
//			}
//			/* dP is not overwritten here. */
//			Vector.setAll(var.interactionVector, 0.0);
//			return var;
//		}
//	};
//	
//	/**
//	 * Herz soft sphere collision model
//	 */
//	public CollisionFunction HerzSoftSphere = new CollisionFunction()
//	{
//		private double _forceScalar = Global.collision_scalar;
//		
//		/**
//		 * Implementation of the Instantiatable interface
//		 * 
//		 * Young's Moduli ranging from aprox. 0.04 MPa (S. Putrefaciens CN32, 
//		 * PH10, Gaboriaud 2005) and 769 MPa (B. Casi, Kumar 2009) See also
//		 * Tuson 2012: 10.1111/j.1365-2958.2012.08063.x
//		 */
//		public void instantiate(Element xmlElement, Settable parent)
//		{
//			/*
//			 * For Herz we are looking for the effective Young's modulus (E_eff)
//			 * In case of hugely different Young's moduli we may want to extend
//			 * on this and calculate the effective modulus on the fly
//			 */
//			String forceScalar = XmlHandler.gatherAttribute( xmlElement, 
//					XmlRef.forceScalar);
//			if( !Helper.isNullOrEmpty( forceScalar ) )
//					this._forceScalar = Double.valueOf( forceScalar );
//			if(Log.shouldWrite(Tier.BULK))
//				Log.out(Tier.BULK, "initiating " + 
//						this.getClass().getSimpleName());
//		}
//		
//		/**
//		 * \brief return the currently set force scalar for this 
//		 * CollisionFunction
//		 * 
//		 * @return double force scalar
//		 */
//		public double forceScalar()
//		{
//			return this._forceScalar;
//		}
//		
//		/**
//		 * \brief calculate a force between two objects based on the distance
//		 * 
//		 * @param distance
//		 * @param var: functions as a scratch book to pass multiple in/output 
//		 * variables
//		 * between methods
//		 * @return force vector
//		 */
//		public CollisionVariables interactionForce(CollisionVariables var)
//		{
//			/*
//			 * If distance is negative, apply the repulsive force.
//			 * Otherwise, return a zero vector. A small overlap is allowed to
//			 * prevent objects bouncing in equilibrium 
//			 */
//			if ( var.distance < -0.001 ) 
//			{
//				double kn = 1.33333333 * Math.sqrt( var.radiusEffective ) * forceScalar();
//				
//				double c = kn * Math.pow(-var.distance, 1.5 );
//				/* dP is overwritten here. */
//				Vector.normaliseEuclidEquals( var.interactionVector, c );
//				return var;
//			}
//			/* dP is not overwritten here. */
//			Vector.setAll(var.interactionVector, 0.0);
//			return var;
//		}
//	};
}

