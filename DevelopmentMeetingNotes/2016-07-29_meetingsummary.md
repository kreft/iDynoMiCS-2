---
output: word_document
---
iDynoMiCS developer meeting 29-07-2016 - present: Stefan, Jan, Jamie, Bas

### issue's with pde solver in spherical grid

* Increasing flux between grid cells in the same shell (should be 0.0)
wave propagation after a while, increasing error

* The issue already occurs with only diffusion, this is strange since diffusion should damp this effect. The issue does not occur on cylindrical or Cartesian grids

* Smaller time-step gives better results

* Stability criterion - Courant condition helped to resolve time-step previously for pde solver Jan

* Could be an issue due to non-continues distances between grid cells

* Implicit method may help

### planning, next
* Jan has started implementing plasmid transfer from iDyno 1 to 2 and has a new student who may be able to help with plasmid transfer

* Stefan arranges a meeting with Rob, others join if they are available at that time.

* General note: focus on the essentials and keep the momentum