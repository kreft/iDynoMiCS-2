Present: Jan-Ulrich Kreft, Robert Clegg and Bastiaan Cockx.

Bugs with overlap/clones largely resolved, but difficulty integrating into iDynoMiCS
- New cells were being placed in exactly same position, fixed by moving immediately after division
- Would stochastic noise movement term work also? Not yet included, but wouldn't be difficult
- Cloneable interface may cause problems, possible Bas has just misinterpreted it
- Mass points being thrown away, then recalculated, at the beginning of each time step instead of just updating. Cheap compared to relaxation step? Bas will try updating now that it's running
- Boundaries mostly implemented, by not cyclic

Sphere-swept volume, bendable cell. Overlap between line segments?
- Mass points stored as LinkedList
- rest lengths stored as array of Doubles

How exactly should iDynoMiCS deal with rod-shaped cells
- Trigger volume for division? Change from radius? Convert at start of simulation, make clear to user.
- Sampling voxels: find all possible voxels, sample with resolution smaller than the radius. Bas will try out method & report on how fast/accurate it is.
- Finding neighbours within a certain distance. Scaffolding there, may be implemented easily(?)
- Writing/reading body shapes to/from output. Data structure will become more complicated but it's unavoidable.
Polarity of rod-shaped cells may influence motility and/or attachment
- Branching filaments have angular torsion spring between every pair of line segments branching from a mass point
- Cell swims along length of access, apply forces to all mass points equally? Modify velocities? Or just move the cell? Should be easy to do either

iDyno2
- Activities, reactions, species, solute info, etc contained within each compartment. Better for parallel computing. Can have in "include" line in protocol file that reads in separate file, but prints it in full in the output directory.
- Can infer activities an agent performs by its state variables, often by their absence/presence
- "Soft" activities that have fixed set of arguments, "hard" activities that need to be hard coded with special arguments.
- Sporulation requires inactivation of most reactions
- Pick and pick of activities, preferably hard coded
- Change "mechanism" to "ProcessManager"? "ActivityManager"?
