# PDE solvers in iDynoMiCS 2

## Use of PDE solvers in iDynoMiCS 2
The most typical use of PDE solvers in iDynoMiCS 2 is for simulating the dynamics of solutes in spatial compartments. In non-spatial compartments, such as chemostats or batch cultures, an ODE solver typically takes this role.

Solutes are typically produced or consumed by one of three different ways:
* Environmental reactions
* Agent-based reactions
* Boundaries

Note that production and consumption are considered ‘two sides of the same coin’: positive production implies negative consumption, and vice versa. By convention in iDynoMiCS 2 we take production to be positive and consumption negative.

## Transient vs steady-state solvers
In iDynoMiCS 2 there are two approaches for solving PDE dynamics of solutes: transient and steady-state. The transient approach is the more “correct” approach, since it makes fewer assumptions and works in all systems. However, the transient approach is often far more computationally expensive and so users may wish to adopt the steady-state approach if they find the compromises acceptable.

The assumptions inherent in a steady-state approach are:
* Separation of time-scales  The process of reaction-diffusion is assumed to occur far quicker than that of microbial growth, and so the two processes are separated. In iDynoMiCS 2 this means that the solute concentrations are first resolved and, after this is completed, then the agents asked to calculate any growth. This is different to the transient approach, where agent growth is applied throughout the time step. Using a steady state solver in a compartment where there is only consumption of a solute will therefore inevitably lead to concentrations numerically equivalent to zero and therefore no microbial growth.
* Balancing production and consumption is the aim This is in contrast to the transient approach, where the aim is to manage carefully the redistribution of matter so that mass is conserved. The length of the time step is only considered important when calculating the agent growth: since reaction-diffusion is considered to proceed far quicker, the original solute concentration in the compartment is irrelevant and so conservation of mass cannot be guaranteed.

