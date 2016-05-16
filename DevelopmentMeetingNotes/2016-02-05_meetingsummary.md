Present: Robert Clegg, Bastiaan Cockx, Stefan Lang

**Bas** has made great progress with the collision methods, particularly with the boundaries as the agent-agent collisions were already largely finished. This has been helped by Rob developing a Dimension class that handles cyclic dimensions in a much easier way than using boundary objects for it. This also helps unify the agent container and solute grids. The downside is that cyclic boundaries must always be parallel to one another (i.e. extremes of the same dimension) but this should satisfy 99% of potential users!

**Rob** has tidied the Cartesian and cylindrical grids, but they need testing. Spherical is not done yet so Stefan will try to finish this off soon.

Polar geometries: agent locations given in polar dimensions, or need converting? Should be able to keep these consistent across all of iDynoMiCS2

**Bas** has also has console launch working: just type in the path to the protocol file and the simulation runs. Rob will develop the XMLable interface further soon, but needs to check Bas's XMLload class as this may do a lot of what we need already.

Process managers should be allowed to access global parameters, but also overwrite these if needed. Bas can use a lot of the methods already developed for agents/species to achieve this.

**Rob** has started work on the manuscript. **Stefan** will update the ODD protocol reference to the revised article

**Reactions**: these need to be done. Rob's mathematical expressions package can be used for rates, but needs polishing first. It can calculate differentials of expressions, so making the numerics smoother.

Since we are approaching a working, stable version, we are spending the afternoon merging all our branches into the "master" branch. We'll then make a "develop" branch off the master, and make our branches branch off this. Only fully working code in the "develop" branch will be allowed into the "master" branch

This will also be an opportunity  to switch default line endings from Windows to Unix. This has been the bane of Rob's life the last few weeks, so hopefully Unix endings will behave better!
