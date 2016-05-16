Present: Robert Clegg, Bastiaan Cockx


Sister papers: Jan not keen for practical purposes
- Difficult to get reviewers to read two papers
- Problems managing the editor
- Can cause big delays if one needs more work than the other

Rob has made the XMLable interface:
- JUnit test case made and working
- Careful initialisation implemented for Shapes

Further work:
- Make an init(Node) method?
- Lists of required and optional attributes for making a protocol file within the iDyno2 GUI?

Agent boundaries: what info do they need, how should they act?
- We could do with some kind of boundary surface object for collision detection (solid boundaries) in all geometries - Rob will look into this
- Bas has made a first attempt at cyclic boundaries in Cartesian geometry - it works but much is hard-coded that we want to be flexible
- Periodic boundaries in non-Cartesian will need more thought - Rob will look at Bas's work in Cartesian and come up with an approach for generalising it
- Control of cyclic responses might need to move from grids to the compartment shape. A method for finding minimum point-point distances?

Bas has come up with the idea of making output to file a process manager. This would work rather nicely, but we need to do several things:
- Check if the process manager initialisation would need to change much. Making the output folder path a public static in Simulator could solve this issue. We would need to ensure that compartment names are unique (and of acceptable format for a sub-folder name) for this to work
- Make it possible for Simulator to create output process managers in all compartments at the same time period as a default, so that the user doesn't need to specific them all individually
- Perhaps make process managers able to "own" other process managers, to ensure that they happen in a specified order. This may or may not be necessary

Bas has managed to get agents to write their biomass to grids, but not yet got them to read from grids. This shouldn't be much trouble. At the moment the agent just uses its cell centre, as in iDyno 1

Stefan and Rob have resolved a number of issues in Cylindrical and Spherical grids, and Stefan is hoping to polish them off in the near future.
