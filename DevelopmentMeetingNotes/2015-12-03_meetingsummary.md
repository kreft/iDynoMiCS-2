Present: Robert Clegg, Bastiaan Cockx, Stefan Lang

**Bas** explained rebasing, merging, etc

**Stefan** has CylindricalGrid and SphericalGrid working, but they need neighbour iterator and cleaning/commenting. Note: they should each extend SpatialGrid, not CartesianGrid! Resolution will become grid-specific, as it needs to be different in Cartesian vs Cylindrical, etc

**Bas** has re-implemented the shoving algorithm, for comparison with iDyno1 simulations.
He is also making good progress with the agent "states" and "events", particularly with the input & output. `dataIO.PovExport` is stable, so Rob will check, clean and comment it.
New "ones" methods in `linearAlgebra.Vector` needs commenting
Bas will speed up collision detection by initialising with the number of dimensions, so that the difference vector will be overwritten rather than recreated each time

**Bas**'s idea: viscosity grid in environment container? This would need an updater method (constant everywhere by default)

**Rob** is busy polishing the linearAlgebra package - Vector is practically done, just needs checking. He will make his own git branch to help the rest of the team, and will replace "float"s with "double"s in the RTree. MTRandom integration is probably fine, but needs checking

The test classes are proving a useful way of checking that changes in one part of the code haven't broken anything anywhere else, but so far they are quite manual… Rob will look into automation, particularly of the more deterministic classes
