Present: Robert Clegg, Bastiaan Cockx, Stefan Lang, Jan Kreft


# XML output
Keep in Simulator - do we allow settable period? Could be iteration or timestep. The group agreed on an integer number of global steps.

Issue with random seed - could there be uses of Java random.

Bas will make a jUnit test to check random seed re-initialisation. Jan suggested that this may not work due to different processors being strict FP or not.

# Graphical output
Keep graphical output as a compartment-level process, as this depends on the compartment shape.

Bas and Stefan have achieved graphical output of rod-shaped cells. Jan suggested that Jamie Luo may be best placed to extends this to filaments, and will email him.

Solute output is already in the SVG output, could be in the POVRay output. The GL render is on a separate thread, so would need the solute grids to be thread-safe. Copying the grids would get around this, but they would likely be a bit out of date.

# Units
Bas has already started on a unit conversion class:
- First attempt forcing everything to SI units.
- Could set output format for output files? Stefan suggested "simplest form". Jan suggested same as input.

Jan suggests sticking with internal units that are most applicable to microbial systems: micrometer, etc.

Bas will write a jUnit test to check operations with doubles with very large/small exponents.

Jan suggests using `%g` in Java to format numbers to string.

Need to be careful about mass-mole conversions.

Bas has incorporated unit input to the ExpressionB class.

# Agent growth
Keep flexibility of one mass OR map of masses?

Issue arises when evaluating reaction rates and stoichiometries. Also pops up in Agent division.

Should keep flexibility but structure the manual so that beginners only see simplest case.

# Agent radius in 2D, 1D
Bas had previously questioned the use of cylinders in 2D, etc.

Stefan has noticed that the voxel volume does not include the dummy dimension in 2D, but this may be out of date. Rob will check on his branch.

Bas suggests calculating the third dimension size from the user's target x- and y- sizes, plus the desired volume.

Bas has suggested using "ghost" masses for agents in 2D. This will require further thinking by members of the group and discussion at the next meeting.

# Agents insertion/removal at biofilm boundaries
Use random walk even for non-motile cells, failed attempts repeated? Depends on purpose of model.

Cell attachment probability dependent on biofilm size? Calculate attachment probability upon arrival?

Rob suggested breaking the problem into rate of compartment entry and let success/failure of attachment emerge.

Jan suggests this will all depend a lot on the purpose of the model, and the state of the bulk liquid. Experimental observations are of rate of attachment (failed attempts are not observed).

Jan thinks we may have 5 or 10 different reasonable options, varying with scenario. Do we model these as separate boundary objects, or as parameters within one? We need to compile a list of possibilities, and rank priority. Start with iDyno1 approach, i.e. specific area. Can calculate volume and surface area of each compartment. Need to specify representative volume size. Will need to copy/destroy agents crossing different sized compartments.

This may lead to very random behavior at bottlenecks, etc. Worth warning the user about this!

By default assume that the compartment size is the same as the realistic volume. Introduce the scaling factor later in manual, etc.

Do we re-implement the detachment schemes of iDyno1? Re-implement DS_SolGrad? Dummy detachment solute? Could use the well-mixed setter to determine probability of detachment. Rob has an idea inspired by gravity, but needs to clarify details before persuading the rest of the group. Topic for discussion at the next meeting!

# The `input` option in Event
About to be removed, so don't use!

# Delete `modelBuilder` stuff now that it has been/is being replaced by `nodeFactory`?
Bas has removed some, but has left bits that still need to be implemented.

Bas has implemented everything that is used by the protocol files.

Rob and Bas will arrange an afternoon of going through the code and cleaning initialization class by class.

# Manuscript
Figures as separate files, tables at the end. Figure legends.

Rob has flagged the issue of many names for "aspect" - we should simplify this. Bas will take a look at this and think about a solution. Jan has suggested the use of "module".

The section on spatial units focusses too much on the technical details - move around to focus more on concentrations and the need for discretisation. Mention polar coordinates, but leave the details for somewhere else.

Rob will move over to V1 and filter over the comments.
