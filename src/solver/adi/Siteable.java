//Title:        Gecko
//Version:
//Copyright:    Copyright (c) 1999, All rights reserved
//Author:       Ginger Booth, gingerbooth@cyber-wizard.com, ginger@gbooth.com
//Company:      Yale IBS/CCE
//Description:  Individual-Based Ecology Simulator

package solver.adi;

public interface Siteable {

public void setSite(Site s);
// if you need sites with more than one resource, you'd need to subclass Site
public int getResource();
}
