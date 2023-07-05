---
author: Bastiaan Cockx
output: word_document
date: 
---

# iDynoMiCS analysis package

## General description

The aim if the iDynoMiCS analysis package is: 

1) To provide an easy to use interface that allows the export of selected data.
This data may later be used for data analysis in 3rd party software.

2) To create a report that can be included in the default model output. This 
report should include basic summarizing information on the model output such as 
cell count and biomass per species and total amount of substrate/product.

The analysis package is to be integrated with the iDynoMiCS framework and will 
have dependencies on packages already implemented in the framework (such as the
state loaders or the file writer).

### Data export:

- Total amount of agents.
- Amount of agents matching a filter (for example amount of agents that are 
inactive).
- Amount of agents for all known options of a specific filter (amount of agents
of every observed distinct species).
- [optional] Amount of agents matching multiple filters simultaneously.


- Total amount of biomass.
- Total amount of biomass matching filter (for example amount of agents that are
inactive).
- Total amount of biomass for all known options of a specific filter (amount of
agents of every observed distinct species).
- Total amount of internal product for agents matching a specific filter.


- [advanced] Stratification of agents matching a specific filter: Are agents of
a specific filter found in a specific distance/range from a boundary or are they
spread?
- [advanced] Amount/level of clustering of agents matching a specific filter: Do
agents of a specific filter cluster together or are they stochastically
distributed trough the biofilm?
- [advanced] Cluster size: how many agents/biomass are within one cluster?

### Classes

#### Category filter
The category filter returns a category the agent belongs to. filter.test(agent)
will return a string, this can be for example the species, the morphological
state or any other characteristic of an agent which can be expressed as String.

#### Specification filter

The specification filter is an object any agent can be tested against.
filter.test(agent) return true (the agent matches the filter) or false the
agent does not match the filter.

#### Counter

The counter creates a map and adds up the number of agents belonging to all the
available categories, in case of a specification filter the categories will be
true (match) and false. Alternatively the counter can add up the value of a
specific field to a category, for example the total mass of the agent, or the
mass of an internal compound.

#### Analyzer 

The analyzer receives a collection of agents and can perform basic analysis on a
specific characteristic of the agents. (for example an analyzer could return the
distribution of the mass of the agents)

#### FilterLogic

Create filters from string

0 					? a > 1.0 	+ b == mySpecies 										~ mass 			| glucose > 1e-2 , glucose > 0.5e-2 

first Compartment	? filter specification for what needs to be included in the table 	~ filter column (value filter) | filter column (Category filter)
	(match filter a and b, leave blank for all )

### Use cases

Raw data output can be quite difficult/time consuming to interpret. The analysis
package has two typical use cases:

* The user wants to get a quick quantitative overview of the difference between
to simulation results. This way the user may easily judge the relevance of the
simulation to their research question or spot flaws quickly.

* The user wants to use 3rd party tools such as R, Matlab or Excel to do in
depth analysis on specific aspects of the simulation result or wants to use this
data for publication either as table or in graphical form.