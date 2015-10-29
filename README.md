# iDynoMiCS-2

## Compartments
In iDynoMiCS 2, the building blocks of a simulation are compartments. Any simulation smust have at least one of these, and if it has multiplie compartments then these should be somehow connected to one another. Examples of compartments include, but are not limited to:
- a chemostat, also known as a CSTR (i.e. a well-mixed container, where fluid is added and removed at the same rate to ensure fixed volume; a chemostat without any fluid flow is known as a batch culture)
- an aqueous biofilm (i.e. a spatially-structured region, where solute transport is diffusion-limited and cells aggregate)
- a gut mucosal layer (similar to an aqueous biofilm, but where strucutral support is given by host epithelial cells; these may produce mucous and other substances)
- a Petri dish (another spatially-structured region, but where nutrients are provided by a layer of Agar below the biofilm and there is air above).


Each compartment has four main components:
- a shape (this includes physical size, and any boundaries)
- an environment container (this holds any dissolved solutes modelled as continuous concentration fields)
- an agent container (this holds any agents - be they live or inert - modelled as discrete particles)
- an ordered list of process managers.

![compartment structure](https://github.com/roughhawkbit/iDynoMiCS-2/blob/master/ReadMeFigs/Slide1.png)

### Compartment shape

### Environment container

### Agent container

### Process managers
