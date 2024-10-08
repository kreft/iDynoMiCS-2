# iDynoMiCS 2

iDynoMiCS 2 ia available under the CeCILL license [(txt)](CeCILL_LICENSE.TXT)

The [CeCILL license V2](http://www.cecill.info/index.en.html) follows the principles of the [GNU general public license (GPL)](http://www.gnu.org/licenses/gpl-3.0.en.html) and of the [BSD license](https://opensource.org/licenses/BSD-3-Clause). The [CeCILL FAQ page](http://www.cecill.info/faq.en.html) claims that it gives a clearer definition of the *copyleft* of code licensed with it. Copyleft is the requirement that anyone distributing the code (unmodified or modified) to adopt the same license, therefore keeping it open source.

iDynoMiCS 2 also utilizes a number of [licensed components](#components)


The following licenses are confirmed as upstream compatible (i.e. may be included in software with a CeCILL license):
- [GNU Affero GPL](https://www.gnu.org/licenses/agpl-3.0.en.html) (used for software on servers)
- [GNU general public license (GPL)](http://www.gnu.org/licenses/gpl-3.0.en.html)
- [GNU lesser general public license (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.en.html) (more permissive than the GPL)

The following licenses don't express explicit confirmation that they are upstream compatible, but given their permissiveness it is unlikely that they are incompatible:
- [BSD license](https://opensource.org/licenses/BSD-3-Clause)
- [MIT license](https://opensource.org/licenses/MIT)


<h1 id="components">Licensed components of iDynoMiCS 2 (third party components)</h1>

## JAMA *(linear algebra)*
Website link [here](http://math.nist.gov/javanumerics/jama/)

**Reference Implementation.** The implementation of JAMA downloadable from this site is meant to be a reference implementation only. As such, it is pedagogical in nature. The algorithms employed are similar to those of the classic Wilkinson and Reinsch Handbook, i.e. the same algorithms used in EISPACK, LINPACK and MATLAB. Matrices are stored internally as native Java arrays (i.e., double\[][]). The coding style is straightforward and readable. While the reference implementation itself should provide reasonable execution speed for small to moderate size applications, we fully expect software vendors and Java VMs to provide versions which are optimized for particular environments.

**Not Covered.** JAMA is by no means a complete linear algebra environment. For example, there are no provisions for matrices with particular structure (e.g., banded, sparse) or for more specialized decompositions (e.g. Shur, generalized eigenvalue). Complex matrices are not included. It is not our intention to ignore these important problems. We expect that some of these (e.g. complex) will be addressed in future versions. It is our intent that the design of JAMA not preclude extension to some of these additional areas.

**Copyright Notice** This software is a cooperative product of The MathWorks and the National Institute of Standards and Technology (NIST) which has been released to the public domain. Neither The MathWorks nor NIST assumes any responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, about its quality, reliability, or any other characteristic.

## R-Tree *(spatial registry)*

Copyright 2010 Russ Weeks rweeks@newbrightidea.com

Licensed under the **GNU LGPL License** details here: http://www.gnu.org/licenses/lgpl-3.0.txt

Bas has implemented some additional methods (cyclic boundaries).

# Collision detection *(surface package)*

Distance methods are based on closest point algorithms from originally from:
Ericson, C. (2005). Real-time collision detection. Computer (Vol. 1).

Implemented in Java for sphere-swept volume collisions by Tomas Storck:
https://github.com/tomasstorck/diatomas

The MIT License (MIT)

Copyright (c) 2011-2015 Tomas Storck

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

# gl Render #

classes based on lessons from:
http://nehe.gamedev.net/tutorial/creating_an_opengl_window_win32/13001/

## Mersenne Twister *(random number generator)*
Copied from iDynoMiCS 1, only minor changes made (tidying).

This library is free software; you can redistribute it and/or modify it under the terms of the **GNU Lesser General Public License** as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

# JOGL
The JOGL source code is mostly licensed under the 'New BSD 2-Clause License',
however it contains other licensed material as well.
Other licensed material is compatible with the 'New BSD 2-Clause License',
if not stated otherwise.
see http://jogamp.org/git/?p=jogl.git;a=blob;f=LICENSE.txt for more details

# GLUEGEN
The GlueGen source code is mostly licensed under the New BSD 2-clause license,
however it contains other licensed material as well.
Other licensed material is compatible with the 'New BSD 2-Clause License',
if not stated otherwise.
see http://jogamp.org/git/?p=gluegen.git;a=blob;f=LICENSE.txt for more details.

# Other
- JUnit - [Eclipse Public License v1.0](https://junit.org/junit4/license.html)
- EXIficient - [MIT License](https://github.com/EXIficient/exificient/blob/master/LICENSE.txt)
- vecmath - [GPLv2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
