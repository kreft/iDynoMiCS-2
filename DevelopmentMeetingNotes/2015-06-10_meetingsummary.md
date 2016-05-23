2015-06-16

This is a short summary of the 2015-06-10 iDynoMiCS meeting at the center  for computational biology in Birmingham. Present at the meeting: Jan-Ulrich Kreft, Robert Clegg and Bastiaan Cockx.

iDynoMiCS
---

Encountered issues
After division agent _movement was updated, not _location. This caused problems in the mechanical relaxation because it is not able to handle cells that are in exactly the same location. (Resolved)
In some cases object implementing the cloneable interface seem to cause issues, some sources recommend not using this interface, this needs some further investigation. (Problem resolved by avoiding cloning mass points).
Currently mass-points are recreated before each mechanical time step  to avoid problems with cloning. Now the mechanical module seems to run reliably the issues with cloneables may be resolved so this is no longer necessary. (Pending)

The implementation of cyclic boundaries with the mechanical relaxation module was not yet finished. (Was pending, but is now implemented).

Agent morphologies and mechanical relaxation
--

'Mass'-points
Points are currently stored as a linked list in the locatedAgent class.

Rest lengths and angles for multi-point cells (rods, filaments) are also stored within the locatedAgent class however as an array of Doubles.

An RTree is creating when the mechanical relaxation is initiated and updated during the relaxation. A cyclic search method was implemented so that the RTree can be used with cyclic domains.

Forces and velocities:
--
Currently only simple forces are implemented (attraction, repulsion and internal spring forces).
Stochastic movement can be implemented as force, velocity or position. We think a change in velocity or position is probably the best approach. A change in position would achieve the aim of 'shaking' the particles a bit to avoid stalemates, where cells are caught in local minima. A change of force is in principle more correct but more difficult to tune to achieve the shake up.

Small movement steps required for small particles: having small EPS particles forces us to make small timesteps so that particles don't jump through others during one timestep.
Range of attachment force 80 nm.

Rod-cells:
--
Shape of rod cells given by sphere-swept volume, not sphere - cylinder- sphere.
Kinks in filaments pose special issues.
Distribution of biomass over grid
Two options: 
-	Work out overlap of bounding box/ rectangle with grid cells to determine distribution
-	Determine distribution via point sampling
o	Point sampling can be done on a cell by cell basis

Conjugation
The current conjugation method is written for coccoid type cells, the method needs to be updated to also take rod-cells into account.

Excretion
For excretion we first need to work out whether the particle needs to be excreted on the caps or on the cylinder part of the rod cell. This can be done by calculating % surface area of the caps and cylinder. The relative surface area determines the probability the excreted particle ends up on that body part. The next step is to find where on the cap or where on the cylinder the particle needs to be excreted. The caps require two random angles to determine the point of excretion. The cylinder requires one random angle and a random fraction along the length of the cylinder.

Input/ output files
Current input/ output files cannot handle complex(er) body structures. An option would be to switch to xml input/output files . However this may cause complications with some software tools.

Motility
Cell motility for rod cells can be achieved by adding/distributing a force or velocity on the points of the cell body. Rod cells swim along their body length.

Branching filaments
Branching filaments can maintain their relative position by creating torsion springs between every connected rod cell that attaches in a single point.

Cell-cell attachment
Cell membranes behave as fluids thus attached particles moving 'freely' over each other surface due to the fact there is not angular orientation of the cells is not a bug but a feature and resembles what is expected in natural systems.
Division
The division module for rod shaped cells is ready, however not yet implemented in iDynoMiCS.
Polarity
Polarity of rod shape cells needs to be taken into account; this can play an important role with motility and division.
2D versus 3D. What to do with rod cells in 2D?
Note that unforeseen issues may be discovered with the implementation of multi-point agent morphologies.

iDynoMiCS 2.0
--

Activities:
--
Static objects managed by 'mechanisms' 
Activities hard coded as library in iDynoMiCS
Additional activities: Signalling, chemotaxis
