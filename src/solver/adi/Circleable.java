package solver.adi;
//Title:        Gecko
//Version:
//Copyright:    Copyright (c) 1999, All rights reserved
//Author:       Ginger Booth, gingerbooth@cyber-wizard.com, ginger@gbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Ecology Simulator
/**
* 990929, vfb, original Java
* Equivalent to pickable support--has x, y, radius. Note that SET x, y, radius
* is optional.
*/

public interface Circleable {
public double getRadius();
public double getX();
public double getY();
} 
