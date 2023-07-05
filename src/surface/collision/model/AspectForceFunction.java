package surface.collision.model;

import aspect.AspectInterface;
import dataIO.XmlHandler;
import expression.Expression;
import idynomics.Global;
import linearAlgebra.Vector;
import org.w3c.dom.Element;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Settable;
import surface.collision.CollisionFunction;
import surface.collision.CollisionVariables;

import java.util.HashMap;
import java.util.Map;

/**
 * utilizes aspects for custom interaction force
 *
 * @author Bastiaan
 *
 */
public class AspectForceFunction implements CollisionFunction
{
    private double _forceScalar = Global.collision_scalar;

    /**
     * Implementation of the Instantiatable interface
     */
    public void instantiate(Element xmlElement, Settable parent)
    {
        Double forceScalar = XmlHandler.gatherDouble( xmlElement,
                XmlRef.forceScalar);
        if( forceScalar != null )
            this._forceScalar = forceScalar;
    }

    /**
     * \brief return the currently set force scalar for this
     * CollisionFunction
     *
     * @return double force scalar
     */
    public double forceScalar()
    {
        return this._forceScalar;
    }

    public CollisionVariables interactionForce(CollisionVariables var,
                                               AspectInterface first, AspectInterface second)
    {

        /*
        If the force function has a distance limit and we are beyond that -> no force
         */
        if( first.isAspect(AspectRef.functionLimit ) &&
                var.getDistance() > first.getDouble( AspectRef.functionLimit ) )
        {
            Vector.setAll(var.interactionVector, 0.0);
            return var;
        }

        /*
        Add all collision parameters to one map, partner parameters have prefix partner_
         */
        HashMap<String, Double> parameters = new HashMap<String, Double>();
        if( first.isAspect( XmlRef.collisionDictionary )) {
            Map<String, Double> vocal =
                    (Map<String, Double>) second.getValue(XmlRef.collisionDictionary);
            parameters.putAll( vocal );
        }

        if( second.isAspect( XmlRef.collisionDictionary )) {
            Map<String, Double> partner =
                    (Map<String, Double>) second.getValue(XmlRef.collisionDictionary);
            for( String s : partner.keySet() )
            {
                parameters.put( "partner_" + s, partner.get(s) );
            }
        }
        parameters.put("dh", var.getDistance());
        parameters.put("scalar", this._forceScalar);

        String forceFunction = first.getString(XmlRef.forceFunction);
        Expression function = new Expression(forceFunction, parameters);

        /*
        Normalizing the interactionVector with the calculated force is needed to have the correct
        direction and force.
         */
        double c = function.getValue();
        Vector.normaliseEuclidEquals( var.interactionVector, c );

        return var;

    }
}