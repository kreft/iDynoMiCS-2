# iDynoMiCS 1
CeCILL

# JAMA *(linear algebra)*
Website link [here](http://math.nist.gov/javanumerics/jama/)

**Reference Implementation.** The implementation of JAMA downloadable from this site is meant to be a reference implementation only. As such, it is pedagogical in nature. The algorithms employed are similar to those of the classic Wilkinson and Reinsch Handbook, i.e. the same algorithms used in EISPACK, LINPACK and MATLAB. Matrices are stored internally as native Java arrays (i.e., double[][]). The coding style is straightforward and readable. While the reference implementation itself should provide reasonable execution speed for small to moderate size applications, we fully expect software vendors and Java VMs to provide versions which are optimized for particular environments.

**Not Covered.** JAMA is by no means a complete linear algebra environment. For example, there are no provisions for matrices with particular structure (e.g., banded, sparse) or for more specialized decompositions (e.g. Shur, generalized eigenvalue). Complex matrices are not included. It is not our intention to ignore these important problems. We expect that some of these (e.g. complex) will be addressed in future versions. It is our intent that the design of JAMA not preclude extension to some of these additional areas.

**Copyright Notice** This software is a cooperative product of The MathWorks and the National Institute of Standards and Technology (NIST) which has been released to the public domain. Neither The MathWorks nor NIST assumes any responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, about its quality, reliability, or any other characteristic.

# R-Tree *(spatial registry)*

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

# Mersenne Twister *(random number generator)*
Copied from iDynoMiCS 1, only minor changes made (tidying).

This library is free software; you can redistribute it and/or modify it under the terms of the **GNU Lesser General Public License** as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

# Other...?
- XML import/export packages
- GUI & rendering packages
- jSBML (if we use it)
