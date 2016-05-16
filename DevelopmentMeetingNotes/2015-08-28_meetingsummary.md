Present: Jan-Ulrich Kreft, Robert Clegg

# SBML
SBML namespace within iDyno namespace, or vice versa? Look at how BioNetGen, etc, have done it?

Use Systems Biology Ontology in protocol file generator?

E.g. occurring entity representation > encapsulating process > biomass production

Haven't found cell division/death!

SBO doesn't deal with units

CellMLOntologies is only intended at the moment

"Boundary conditions" have very different meaning in SBML: reactants whose values are not determined by kinetic laws (Section 7.7, p. 114). The "spatial" package uses them in the same way that we do though.

"lumen" is a unit! Obviously also "mole"

Careful about time units (SI is seconds)

Need to cater for rational numbers as constants, initial values, etc

Compartment sizes can vary, but not number of compartments

Events: trigger and execution separate (may be a delay). Reassess all event triggers whenever an event is executed

Need to implement discrete solver? Could be stochastic or discrete (Section 7.4, p. 107)

Need to combine steady-state solver with transient (for ODEs)?

Do we want to wed ourselves to the SBML way?
If so...

# New package?
iDyno2 mark-up include SBML, or extend it?

Could make an "ibm" package in SMBL, like "spatial", that could allow exchange of models.
	• spatial deals with diffusion, advection, etc
	• Ibm could deal with multiple compartments on the fly

Setting spatial: required="false" makes it a chemostat simulation

Setting ibm: required="false" makes it a PDE simulation

Setting both to be unrequired makes it an ODE simulation


# Jan's comments:
Check if we can link 2 spatial compartments, as would be needed for eGut

Read up on CellML
