Notes on
[Yates & Flegg (2015)](http://rsif.royalsocietypublishing.org/content/12/106/20150141)
"The pseudo-compartment method for coupling partial differential equation and
compartment-based models of diffusion" *J R Soc Interface* **12**(106):20150141
with regards to the development of iDynoMiCS 2.

The [SBML specification (Level 3 Version 1 Core)](http://sourceforge.net/projects/sbml/files/specifications/Level%203%20Ver.%201/sbml-level-3-version-1-core-rel-1.pdf/download?use_mirror=heanet)
and [JSBML user guide](http://sbml.org/Special/Software/JSBML/latest-stable/doc/user_guide/User_Guide.pdf)
are referred to throughout.

# Method
The paper deals with hybrid models where soluble compounds diffuse between
well-mixed compartments and spatiallys-tructured domains.
* In well-mixed compartments, quantities are assumed to be integer numbers.
* In spatially-structured domains, quantities are assumed to be real-numbered
probability densities (equivalent to concentration gradients).

This disparity between quantity types must be resolved at the boundary between
the two regions. Yates & Flegg solve this by creating a thin
*pseudo-compartment* around the outside of each compartment, where compounds
are converted from one type to another. This, they suggest, far simpler and
computationally cheaper than trying to resolve such conversions at the boundary
itself.

**Note that Yates & Flegg assume the resolution of the spatial domain to be
much finer than the typical resolution of a compartment.**

The pseudo-compartment is, in a sense, spatially similar to existing features
of iDynoMiCS:
* the *boundary layer* between biofilm and bulk
* the *shove factor* around cells



# Possible applications

## Reaction kinetics


## Small particles
This method may be useful in the event that we want to model the dynamics of
small particles, but do not wish to model them as agents. Examples include:
* phage
* nanoparticles
