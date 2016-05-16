Present: Robert Clegg, Bastiaan Cockx, Stefan Lang

# Manuscript writing
Figure formats: VSD (Microsoft Visual?), SVG?

Where to discuss the Process Managers? Fitting into the ODD protocol. Will want to adapt the ODD protocol, not stick to it exactly.

**Bas** has entered citations, switch to author-date while writing.
Bas has assigned some roles
 - Introduction (**Bas** to organise, will need input from all authors)
 - Simulator-compartment (**Rob**)
 - Agents (**Bas**)
 - Environment (**Rob**)
 - Shape, grids, etc (**Stefan**)
 - Process overview and scheduling (**Rob**)
 - Design concepts (**Stefan** will look into these)
  * Observation (**Bas**)
  * Write about intended uses, but emphasise flexibility.
 - Initialisation, Input (**Bas**)
 - Submodels
  * Agent relaxation (**Bas**)
  * PDE solver (**Rob**)
  * ODE solver (**Rob**)
  * Boundaries/connections (**Rob**)
 - Case studies (**Bas** & **Stefan**)
  * Need to be careful about word count, maybe merge some
 - Discussion & Conclusion (**Rob** to organise, will need input from all authors)
Sections 4.2, 4.3 and 4.7 are top priorities
"Collectives": what are they?
We should get into the habit of letting others know when we're writing
**Rob** will delete manuscript folder in GitHub repository

# Other notes

Cyclic neighbour: use -1 or correct coord

**Stefan** will move iterator tests from manual to automated

Need to check that the Shape iterator is not null before calling updateNVoxel

Default resolution should be for one voxel, expect to be overwritten

Coordinates in Shape should always be 3D - transfer over from Grid

Stuff should be stripped out of Grid, give it reference to the Shape

Output to XML is in progress, should be done once **Bas** gets back from Newcastle

**Rob** will put his meeting notes on GitHub

Set up larger group meeting for beginning of next week(?)
