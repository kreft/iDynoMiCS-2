Present: Robert Clegg, Bastiaan Cockx, Stefan Lang

**Bas** has realised that the Species class should become a collection of shared modules - in the "classic" case there would just be one, unique module for each species. SpeciesModule seems like an appropriate name, but we can always change it later if needed.

XML-able and Check-able interfaces seem like a good idea, so Rob will have a play around with them. They would make it a) easier to read and write protocol files in a consistent manner within iDynoMiCS, and b) allow a pre-launch check (as in NetLogo).

Math expressions would be useful, so worth pursuing. The benefit of an in-house method is that we can make this differentiable

**Rob** has started playing around with unit testing and is finding it useful (one bug in the linear algebra found already!). Using jUnit in Eclipse also shows how quickly code runs. It should become more and more powerful as we use apply it to higher-level code.

**Stefan** has everything working in cylinders but not spheres. It's also quite slow but this is low priority for now. Rob will have a look at the tidiness on Monday (11th Jan) and push to a new branch of Stefan's branch so it doesn't interfere with his work.

**Bas** has been using SmartGit and recommends it for rebasing

`GuideForDevelopers.md` is growing steadily, and should become essential reading for anyone who wants to join the team!

**Rob** and **Bas** have arranged a joint coding session on agent-grid and agent-boundary interactions next Friday (15th Jan)

Once we have something running, we should change our branch structure: "master" will always work, "develop" will branch this and is only ever pushed to "master" if we're certain it works. All our development branches then branch off "develop"
