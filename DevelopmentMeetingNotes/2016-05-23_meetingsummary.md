Present: Robert Clegg, Bastiaan Cockx

### Bas's visit to Newcastle
- Very positive, people there keen to use iDyno2
- IBM in microbiology wiki, with iDyno2 as a sub-wiki?

### Output as XML
- Bas has finished the structure, but is still implementing it throughout the source code
- Currently working on `Reaction` stoichiometry
- `Shape` classes are done, but not `Dimension`
- Much of the stuff in `ObjectFactory` is being replaced with `NodeConstructer` approach
- Bas is uncertain about how to read/write concentration arrays. **Rob will check this**

### Removing aspect interface from process managers?
- Leave for now, but needs to be done using XML output/input.
- Obligatory aspects, requirements?

### Reaction Library
- Use as default, allow extra/exclude in a `Compartment`
- Should be easy to code, as follows the `SpeciesLib` approach
- Solutes could also be done, needs more thought

### Process managers
- `SolveDiffusionTransient`: Need to clarify how the agents grow
- `SolveChemostat`: Need to do agent growth!

### Boundaries
- Rob has been working on Agent and soute boundary methods, getting there!
- Tricky defining what is attached (i.e. biofilm) and what is floating in boundary layer
   - Boolean flag for sloughing on boundary layer
- Poorly defined boundaries specifying solute concentrations on adjacent boundaries?
   - Spatially structured compartment with well-mixed sub-regions
   - Could we prohibit this?
   - Or flag a message and make not well-mixed?

### Agent events for Toledo group
- run other events?
- change species (death, etc)


*Notes for later*
### Restructure from Grid to Shape
- Rob started restructure, Stefan has been continuing it
- Making great progress but not quite done yet: errors

