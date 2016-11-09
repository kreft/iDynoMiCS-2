Participants: Jan-Ulrich Kreft, Stefan Lang, Jamie Luo, Munehiro Asally, Bastiaan Cockx

### Opening

discussing models with complex morphologies and physical interactions.

* Streptomyces
* Filamentous fungi
* Myxobacteria (Cell Flexibility Affects the Alignment of Model Myxobacteria)

### code discussion

(GUI = Graphical User Interface)

* the Collision package now uses separate class to pass variables for clarity.

* With the new ClassRef (and also PackageRef) we now have 5 reference classes. Reference classes allow to make changes in aspect names, change xml labels and rename classes in a single place and thus we do not have to go trough all code to make those changes. Also the ClassRef allows us to query all classes within a specific package, ObjectRef can list all objects that are allowed to be a Primary aspect.

* A lot of bug fixes have been done. A big one is that there is now no longer an growthrate artefact when changing the target resolution to values <1

* Stephan and Rob will discuss what still needs to be done to get all boundary types (connected compartments, wellmixed) working and fully implemented

* In order to model diffusion from and in an agar plate we should have an additional region specification that defines the agar. In the protocol file the concentration of the region and it's diffusivity can be set to properly model agar. Multiple compartments for agar have also been discussed but are probably less feasible if the agar regions are connected directly

* To prevent confusion and prevent "missed" components on division or missed components when the user implements both, as well as to reduce code complexity: all agent mass component will be stored in a HashMap data structure, the added computational cost is estimated to be insignificant.
* Take note of checking for Log.level before constructing an output string
* The splitTree delivers a speed improvement important in order to model larger scale systems. There are still some issues with rods in periodic domains. The approach is very similar to BacSim's quad tree.

* the modelNode newBlank method is now deprecated, we are switching to implementing the Instantiatable interface for all classes that need to be instantiated from the protocol file. Possible child classes are defined as Constructable in the modelNode structure. This also allows for easy construction from the gui.

* Two new classes: Bundle and Pile (to be renamed to ModelMap and ModelList) are extensions of the Java HashMap and LinkedList classes and implement the instantiatable and NodeConstructor interfaces. Because these classes self manage their modelNode structure XmlOutput and GUI intergration of Maps and Lists is now very easy (automated). This prevents us from writing iterative methods for every list and map implemented in iDynoMiCS that needs to be saved or requires user interaction.

Issue with non-identical simulations with identical seed:
* Bas will write a unit test to check for inconsistencies with random number generator
* Stephan will write a script to check for instances of the java default Math.random

* A steady-state solver is an essential part of iDynoMiCS 2 and should be included, especially for simulations with realistic agent growth rates. It should be feasible to port the multi-grid solver for cartesian domains. For other domains we will need the ADI solver or alternatives. BacSim had an ADI solver implemented and can be used for inspiration.

* For non-cartesian simulations with agents the agent-tree should use the same none cartesian coordinate system to work properly, conversion to cartesian will not work for periodic dimensions. There may be some adjustments required to the bounding volumes to make this work, Stefan will investigate.


### planning

Bas has indicated that he has to put more focus on the other aspects of his PhD project. Stefan has offered to pick up and discuss with Rob the unfinished tasks Rob has been working on. Jamie has indicated that he can probably pick up some small and clear task packages and may also be able to write some missing Java doc while he goes trough the code.

The total time spent on coding should be 3 to max 4 weeks full time coding. In order to achieve this tasks have been put on the postpone list (see below this section).

Completing the paper would probably take a similar amount of time.

The tasks have been distributed as follows:

###Rob:
* radii and agent size in 2d and representation, scaling mass/concentration/density? cylinder representation? what are the artefacts that we will introduce with our approach? Discuss with Jan and Bas
* Assist and advise Stefan on unfinished implementations.
* finish work on steady state reaction diffusion solver.

###Stefan:

grid

* setting difusivity other than 1 and settable from xml
* dimension min other than 0

Boundary package (together with Rob)

* proper initiation from protocol file
* node construction
* detachment, attachment
* biofilm boudary implemenentation, uses aspects thus should become redirectable or restructured in an other way
* Membrane boundary layers are unfinished

Shape

* what is still to be done for non-Cartesian shapes

surface

* check bounding volumes for non cartesian domains

###Bas:

agent package

* Agent register death method
* Make body relocate robust for relocations between 2d and 3d domains
* Adding complex body types with gui

aspect package

* Aspect redirecting stored in output and editable in gui
* Remove filial linkage methods
* in some scenarios eps cumulative seems to cause a stack overflow error
* investigate the usage of the internal production event now that rob has intergrated it (partly?) in transient diffusion solver
* level pull distance rather than adding up
* clean-up rod division and make sure rods perform tip-tip divide
* internal products are currently ignored for volume calculation

guiTools

* A lot of classes are not implementing the model node structure yet and thus are not loaded in the gui yet.
* A lot of classes do not allow for editing (set node method missing or unfinished)

Agent container

* move all aspect related methods out of the agent container they should be in redirectable classes and agent container should remain general
* make rtree / splittree settable

nodeFactory

* finish replacing newBlank paradigm with constructable paradigm
* finish Bundle and Pile implementation

surface

* Collision and adhesion functions settable from protocol file

###Jamie

Will communicate with Stefan, Bas and Rob when he has some time to spend on small tasks for iDynoMiCS 2.
Jamie happy to review code and test code.

### topics / elements that will **not** be included in iDynoMiCS 2 release (postponed)

* Units
* Log allowing for different display and log file log levels
* JSBML
* on demand agent / surface trees (we keep the agent tree central in the agent container)
* upon event processManagers