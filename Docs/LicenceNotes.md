# iDynoMiCS 1 & the CeCILL license

[This](http://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1002598) article is a useful read.

The [CeCILL license V2](http://www.cecill.info/index.en.html) follows the principles of the [GNU general public license (GPL)](http://www.gnu.org/licenses/gpl-3.0.en.html) and of the [BSD license](https://opensource.org/licenses/BSD-3-Clause). The [CeCILL FAQ page](http://www.cecill.info/faq.en.html) claims that it gives a clearer definition of the *copyleft* of code licensed with it. Copyleft is the requirement that anyone distributing the code (unmodified or modified) to adopt the same license, therefore keeping it open source.

The following licenses are confirmed as upstream compatible (i.e. may be included in software with a CeCILL license):
- [GNU Affero GPL](https://www.gnu.org/licenses/agpl-3.0.en.html) (used for software on servers)
- [GNU general public license (GPL)](http://www.gnu.org/licenses/gpl-3.0.en.html)
- [GNU lesser general public license (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.en.html) (more permissive than the GPL)

We cannot find explicit confirmation that the following licenses are upstream compatible, but given their permissiveness it is unlikely that they are incompatible:
- [BSD license](https://opensource.org/licenses/BSD-3-Clause)
- [MIT license](https://opensource.org/licenses/MIT)


# Licensed components of iDynoMiCS 2

## JAMA *(linear algebra)*
Website link [here](http://math.nist.gov/javanumerics/jama/)

**Reference Implementation.** The implementation of JAMA downloadable from this site is meant to be a reference implementation only. As such, it is pedagogical in nature. The algorithms employed are similar to those of the classic Wilkinson and Reinsch Handbook, i.e. the same algorithms used in EISPACK, LINPACK and MATLAB. Matrices are stored internally as native Java arrays (i.e., double[][]). The coding style is straightforward and readable. While the reference implementation itself should provide reasonable execution speed for small to moderate size applications, we fully expect software vendors and Java VMs to provide versions which are optimized for particular environments.

**Not Covered.** JAMA is by no means a complete linear algebra environment. For example, there are no provisions for matrices with particular structure (e.g., banded, sparse) or for more specialized decompositions (e.g. Shur, generalized eigenvalue). Complex matrices are not included. It is not our intention to ignore these important problems. We expect that some of these (e.g. complex) will be addressed in future versions. It is our intent that the design of JAMA not preclude extension to some of these additional areas.

**Copyright Notice** This software is a cooperative product of The MathWorks and the National Institute of Standards and Technology (NIST) which has been released to the public domain. Neither The MathWorks nor NIST assumes any responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, about its quality, reliability, or any other characteristic.

## R-Tree *(spatial registry)*

Copyright 2010 Russ Weeks rweeks@newbrightidea.com

Licensed under the **GNU LGPL License** details here: http://www.gnu.org/licenses/lgpl-3.0.txt

Bas has implemented some additional methods (cyclic boundaries).

## Mersenne Twister *(random number generator)*
Copied from iDynoMiCS 1, only minor changes made (tidying).

This library is free software; you can redistribute it and/or modify it under the terms of the **GNU Lesser General Public License** as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

# Other...?
- XML import/export packages
- GUI & rendering packages
- jSBML (if we use it)
