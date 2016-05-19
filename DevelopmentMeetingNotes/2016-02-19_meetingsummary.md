Present: Robert Clegg, Bastiaan Cockx, Stefan Lang

**Rob** and **Bas** have managed to get the PDE solver up and running with reactions mediated by agents. These agents grow and divide accordingly. Agent biomass is distributed over all voxels that the agent covers. Bas & Rob need to fine tune this in how it deals with mini-timesteps and growth rate contributions of the different voxels.

The code is also rather messy at the moment, so we'll work on tidying and documenting it. Rob needs to fix the periodic boundaries for solutes - these already work for agents.
The PDE solver is time-dependent explicit, and Rob has plans to implement more sophisticated solvers (including steady-state), but this is a big hurdle overcome!

**Stefan** has the PDE solver running in polar grids, but hasn't yet tried with agents. This will be tested in the next few days or so

Surface objects and their bounding boxes make collision detection very quick

A lot of the code can be initialised from an XML protocol file, and we have a few example protocols that run (including the PDE test case above). XMl handler and XMLhelper classes
Bas has also made a central collection of standard state names

Reactions are very flexible in the mathematical expressions they can take, and can be built from a simple string input. There is an agent state that also uses this capability

**Bas** has generalised the AspectRegistry so that it can store states for more than just agents. This works very nicely for storing parameters of process managers. Bas will write up best practice of using this in the GuideForDevelopers.md

**ResCalcFactory** is overly complicated, and doesn’t even read in from XML. Stefan and Rob will sort this out together on Tuesday

**Stefan** has developed very nice visualisation of polar grids inside Java, and Bas has a process manager for writing solutes and agents together to SVG.

Diffusion setters and Domain setters are written but not yet implemented.

**Bas** flagged up the name "domain", defining where the solver should include for diffusion-reaction, as being rather vague. Rob suggested flipping the problem on its head and inside finding where is "well-mixed", and so where the solver should ignore.


TreeMap for process managers in Compartment?
