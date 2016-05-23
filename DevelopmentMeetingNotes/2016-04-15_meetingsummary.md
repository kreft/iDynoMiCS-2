Present: Robert Clegg, Bastiaan Cockx, Jan-Ulrich Kreft, Barth Smets

1. The release features of iDynoMiCS 2.0 (What must be included, what parts still need to be finished or made, which features are of lower priority and can wait until a post release patch).
  -  High priorities
    * Saving model states/XML output. Need to be careful about file size (try serialisation… might be larger! Also look at compression). Already have SVG and POV exporters. Ability to change default names could be source of mistakes.
    * Loading solute grid that does not have a homogeneous concentration
    * Boundaries, especially agent methods. Specific area-type scaling factors, but clearer. Need to be in case study
    * Grid diffusivitySetter and wellmixedSetter ("alpha curve"?)
    * Initiation and behaviour/peripherals (division, cyclic boundaries, graphical output) of non-coccoid agents - prioritise rod-shaped
    * Gui simulation construction/editing
    * A lot of parts need cleaning and more extensive commenting
  - Low priorities
    * PDE solvers (ADI 1st, multigrid 2nd, etc), currently only explicit solver. Iterating over all agents a bit slow… but much better (stick with higher quality results, can use larger timesteps).
    * Agents and solute input/output in grids other than Cartesian grid
    * Expansion manager (need to think about licensing - must make source code available?)
    * Compartment manager for dynamic creation/destruction
2. The general structure, content, story and author list of the manuscript.
 - British English - but American spelling (modelling, etc) in keywords
 - Switch to writing in Word
 - Try Mendeley for reference management
 - Statement of joint contribution for Rob & Bas
 - Corresponding authors: both Rob & Bas
 - Aim for 6,000 words and no more than 10 figures/tables
    § Half on model description
3. The timeline towards the submission of our manuscript
 - Aim for end of May, so we can check things and have a buffer. Focus on model description


Need to contact Stefan about whether or not to include polar stuff - could wait until a later paper with his own research topic as case study. If on this paper, would also need Stefan Schuster



## Rob's tasks
Convert manuscript to Word, put on Dropbox
Solute boundaries
Reading/writing spatial grids
diffusivitySetter and wellMixedSetter?
Restructure/clarify resolution setting
Solidify PDE stuff: voxel-voxel fluxes

## Bas's tasks
Set up Mendeley, import citations
Complex agent morphology - discuss with Cristian?
XML/GUI editor - but coordinate with Rob
Library management - e.g. ProcessManager
Use expressions in surface-surface force functions, may want to use sine, cosine, etc!

## Joint tasks
Agent boundaries
Units
Writing paper
Cleaning and commenting
