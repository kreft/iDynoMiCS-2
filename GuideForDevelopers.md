# Coding

Useful links:
- [Java code conventions (Oracle)](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf)

## General tips
A few extra **spaces** can make a huge difference to how human-readable code is. For example, this
``` java
if(i<3)x=x+Math.PI*2;
```
is a lot less readable than this
``` java
if ( i < 3 )
  x += Math.PI * 2;
```
despite being the same from the compiler's point of view.

## Setting up a class file
**Copyright information** should go at the very top of the file

**Import statements** should be split into two groups with an empty line between, and ordered alphabetically within these groups. The first group is of external imports, i.e. those not written as part of iDynoMiCS 2; the second group is, of course, those written as part of iDynoMiCS 2. For example:
``` java
import java.math.BigInteger;
import java.util.LinkedList;

import agent.Agent;
import agent.Species;
import idynomics.Compartment;
```

## Line lengths
Please keep line lengths to a maximum of 80 characters wherever possible. This makes it easier to reading code on small screens, and to compare code side-by-side on one wider screen. To make this easier, you can set up Eclipse to show a margin guide:

![Eclipse editor margin column](https://raw.githubusercontent.com/roughhawkbit/iDynoMiCS-2/master/Docs/EclipseEditorMarginColumn.png?token=ADJGIVtgUNEqXX8-vHyR2xguEIWLl8LRks5Wvg4xwA%3D%3D)


TODO wrapping lines

## Naming conventions
Constants should always be in capitals, with words separated by underscores (known as [SCREAMING_SNAKE_CASE](https://en.wikipedia.org/wiki/Snake_case)). For example:
``` java
public final static int ONE = 1;
public final static double ONE_POINT_ZERO = 1.0;
```

Classes and interfaces should be in lower case, except the first letter of each word which should be a capital (known as [CamelCase](https://en.wikipedia.org/wiki/CamelCase)). For example:
``` java
public interface AnInterface
{
  ...
}
private Class()
{
  ...
}
```

Packages and public variables should use the slightly different _CamelBack_ convention: the first letter is lowercase. For example:
``` java
package packageName;
public int variable;
public double nextVariable;
```

Protected and private variables should be the same as public, except that we place an underscore at the start. However, this can be cumbersome if the variable name is a single letter, and so can be omitted. For example:
``` java
protected double _protectedVariable;
private int _privateVariable;
private double a;
```

As a general rule of thumb: the more widely a name is used, the more descriptive (and therefore longer) it should be. So the single letter name is OK for a private variable, since all references to it will be in the file where it is defined. If you would like to abbreviate a long variable name, please explain the abbreviation in the javadoc at initialization. For example:
``` java
public double aLongVariableNameWithASpecificPurpose;
```
could become
``` java
/**
 * A variable with the specific purpose of demonstrating how to explain yourself
 * to other programmers with different backgrounds to your own.
 */
public double specPurp;
```

## Commenting
Commenting is your friend, whether you're writing code, reading someone else's, or even reading your own several weeks/months/years later. That said, the best code needs little in the way of comments as it is clear through good naming and layout.

**Block comments** span several lines, and are useful for long comments. Classes, variables, expressions, etc., should be described using **Javadoc** comments starting with a `/**` (shown in blue in Eclipse), whereas all others start with `/*' (green).
``` java
/**
 * \brief Short description about what this method does.
 * 
 * A bit more information about what this method does.
 * 
 * @param number An integer number required as a parameter.
 * @returns A double number calculated from the input.
 */
public double aMethod(int number)
{
  /*
   * Long internal comments should go here. These are useful for those who want
   * to understand how a method works, rather than what it does.
   */
  return 1.0 * number;
}
```

**End-of-line comments** are best kept for temporary comments that you intend to be deleted at some stage. For instance:
``` java
// FIXME Harry [11July1979]: This is broken!
int two = 1 + 1;
// TODO Sally [12July1979]: Is this better?
// int two = 1 + 0;
```
This approach may become redundant as we use GitHub's commenting more and more.

The [Java code conventions (Oracle)](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf) gives advice on trailing comments (p. 8), but we strongly discourage their use. They often lead to developers breaking the 80 character line-length rule (see above).

## Returning booleans
This is unnecessarily messy
``` java
if ( booleanVariableOrExpression )
{
  return true;
}
else
{
  return false;
}
```
when this returns exactly the same result
``` java
return booleanVariableOrExpression;
```

## `int` vs `Integer`, `double` vs `Double`
This is a confusing aspect of Java, but worth learning the distinction. All data types with a lower-case name are [primitives](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html), and in iDynoMiCS we tend only to use `int`, `double` and `boolean` the vast majority of the time. On the other hand, data types in _CamelCase_ are [objects](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html) and many familiar ones act as "wrappers" for primitives: `Integer`, `Double`, `Boolean`, and `String` (for `char`).

## Use of `instanceof`

## Everything should be possible to initialise through XML

## Visualisation should be the same regardless of whether it's during the simulation or from results

## Using modulo

We often need to use [modulo](https://en.wikipedia.org/wiki/Modulo_operation), for example when dealing with periodic boundaries or circular geometries. The in-built Java modulus is specified using the `%` sign:
``` java 
// dividend % divisor = remainder
1 % 3 ; // = 1
5 % 3 ; // = 2
-2 % 3 ; // = -2
```
Note that the sign of the answer is always that of the dividend. If you want it to be the same sign as the divisor (as is often the case), use `Math.floorMod(int dividend, int divisor)`:
``` java 
// dividend % divisor = remainder
Math.floorMod(1, 3) ; // = 1
Math.floorMod(5, 3) ; // = 2
Math.floorMod(-2, 3) ; // = 1
```

## Using aspects

### what is an aspect (and why)?
Aspects are introduced to have a flexible way of handling and storing properties
and behavior of the java object that hosts them, as well as to ensure sub-models
always work with up-to-date information. An aspect can be any java object, yet 
when creating an aspect (stored in an Aspect<A> object) a distinction is made 
between three different types: PRIMARY, CALCULATED and EVENT.


#### primary
Primary aspects store information or properties and are preferable the only 
place this information is stored (such as a double that represents an Agents 
mass or a String that tells an output writer what solute should be displayed).
The following example shows the creation of a simple primary state in xml:

``` XML
<aspect name="pigment" type="String" value="RED" />
```

Here 'name' is the name other aspects use to refer to this aspect. 'type'
indicates what type of aspect should be created and stored, value is the String
representation of the content of the stored object. This is generally the
scenario to store simple objects like primitive types or simple java objects
that are easily represented in a string. including simple arrays (see example
bellow).

``` XML
<aspect name="exampleState" type="double[]" value="2.0, 5.1" />
```

when storing arrays (indicated with the appended []) the individual array 
value's are always comma separated.

More complex objects may also be stored as a primary aspect, when such an object
is stored no value is indicated but instead the child nodes of the parent node
are passed to the java object. These child nodes are object specific and are
handled by the object's XMLable implementation (see paragraph XMLable interface).

``` XML
<aspect name="body" type="body">
	<point position="12.2, 12.2" />
</aspect>
```

#### calulated
Calculated aspects are used to calculate a property that depends on other 
aspects. For example calculated aspect may define an agents volume based on it's
mass and density. Calculated aspects do not store any information except for on what primary 
aspects they depend on. There are two way's of defining a calculated aspect: 1) 
calling a pre-made calculated aspect:

``` XML
<aspect name="surfaces"	type="calculated" class="AgentSurfaces" input="body" package="agent.state.library." />
```
Here 'name' is the name other aspects use to refer to this aspect. 'type'
indicates what type of aspect should be created. 'class' indicates what the
java class name of the calculated object is that should be created. 'input'
set the required input aspects and 'package' indicate the java package in which
this java class is stored. When the java class has been added to the 
general/classLibrary.xml no package specification is required in the protocol
file.

2) defining the calculated state as a mathematical expression:

``` XML
<aspect name="volume" type="calculated" class="StateExpression" input="mass/density" />
```

This type of calculated aspect always has "StateExpression" as it's defined
class, yet in stead of defining it's input as a comma separated list of aspects
it's input is directly defined as a mathematical expression (see paragraph Using
expressions). Whenever this calculated state is called it will always evaluate
this expression with the up-to-date value's and thus return an up to date double
value.

NOTE: the input aspects of an expression states should always be or castable as
java double value.

#### event
The third type of aspects are event aspects. This type of aspect does not store
or return any information, but instead when it is called it can mutate primary
agent states. For example create a new sibling and adjusting the cell sizes when
a coccoid cell divides:

``` XML
<aspect name="divide" type="event" class="CoccoidDivision" input="mass,radius,body"
package="agent.event.library." />
```

As can be seen in the above expression aspects of the event type can be defined
in a similar way as calculated states where the only difference is that the
'name' attribute now reads event rather than 'calculated'.

Note that, if the object that implements the aspect interface is Copyable all
of it's aspects should be Copyable to (the Copier.copy(Object) should be able
to return a deep copy of the aspect).

### The aspect interface - Using aspects in your object
Only one thing is required to use aspect with your java object. Your object
needs to implement the AspectInteface. This interface requires you to add one
field and one method to your class

``` java
/* The aspect registry can be used to store any java object: <Object>, but can
* also be use to store more specific objects types: <Double> */
public AspectReg<Object> aspectRegistry = new AspectReg<Object>();
    
/* Allows for direct access to the aspect registry  */
public AspectReg<?> reg() {
	return aspectRegistry;
}
```

The aspect interface itself implements all methods to further work with aspects,
the following three are essential when working with aspects:

``` java
void loadAspects(Node xmlNode)
```
This method loads all aspects from the given parent node, this method should be
called where you implement the XMLable interface.

``` java
boolean isAspect(String aspect)
```
This method returns true if the aspect exists in the root registry or in any of
it's branches (see branched aspect registries). Since every agent/aspect owner
may be unique check whether the called aspect is defined in this aspect registry
or in one of it's branches

``` java
Object getValue(String aspect)
```
This method returns any aspect as java object if the aspect exists in the root 
registry or in any of it's branches (see branched aspect registries). If the
aspect cannot be identified it will return null.

Apart from the getValue method there is a set of methods that directly return's
(or attempts to) in the specified form (thus it does no longer require additional
casting). These method's work for any primary or calculated aspect that can be
returned in the requested form.

``` java
Double getDouble(String aspect)
Double[] getDoubleA(String aspect)

String getString(String aspect)
String[] getStringA(String aspect)

Integer getInt(String aspect)
Integer[] getIntA(String aspect)

Float getFloat(String aspect)
Float[] getFloatA(String aspect)

Boolean getBoolean(String aspect)
Boolean[] getBooleanA(String aspect)
```

### The aspect registry
The aspect registry is what hold's the aspect's linked to a key (String), an
aspect registry can also have modules or branches. These are additional aspect
registries which are included in the root aspect registry. This approach is
used to allow direct access of species aspect's trough an agent. Any aspect
registry can have any number of branches with any number of branches out of
those branches, though agents will always have only a single branch/module
which is the species aspect registry, this species aspect registry may than
have any additional species modules which are handy to store specific behavior 
and parameters in a single place.

Typical species modules would be: the behavior of a coccoid cell, the 
metabolism of this subgroup, or specific features/changes of a mutant strain.

There is one GOLDEN RULE concerning branched aspect registry, when duplicate
named aspects exist within the aspect tree the aspect closest to the root will
override the further branch, but if two separate, independent branches use the
same aspect name the first one encountered will be used, hence:

You can overrule aspects of submodules, but individual submodules cannot 
override each other!

This approach can be used for example if an agent wants to vary it's individual
parameter from what is defined in the species aspect registry. Or, when you want
to implement a species submodule which fit's you needs except for a single
parameter or behavior.

Typically you should not interact with the aspect registry directly unless:
you are all are creating a new AspectInterface implementing object which has
mutable aspects, in that scenario you would implements:

``` java
/* setting an aspect in the root aspect registry */
public void set(String key, Object aspect)
{
	aspectRegistry.set(key, aspect);
}

/* performing an event (which may result in mutating aspects in the root registry */
public void event(aspectObject compliant, double timestep, String event)
{
	aspectRegistry.doEvent(this, compliant, timestep, event);
}
```
	

#### classes currently implementing the aspect interface
- Agent
- Species
- ProcessManager

## Using the helper class

## Using XmlHandler and XmlLoad classes

## Using NameReferences

## using expressions

## using reactions

# Testing

Useful links:
- [Unit testing with JUnit](http://www.vogella.com/tutorials/JUnit/article.html)

# Teamwork

Useful links:
- [Atlassian Git Tutorial: Workflows](https://www.atlassian.com/git/tutorials/comparing-workflows)
- [A Visual Git Reference](http://marklodato.github.io/visual-git-guide/index-en.html)
- [Code Tuts+](http://code.tutsplus.com/tutorials/focusing-on-a-team-workflow-with-git--cms-22514)
- [A Quick Introduction to Version Control with Git and GitHub](http://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004668)

## Using Git, GitHub and eGit

### Forking vs branching

### Merging vs rebasing

#### Merging with SmartGit
1. Commit and push all changes you have made in your branch.
2. Close all editors/IDEs and open SmartGit.
3. Make sure the branches you want to merge are up to date by:
	-	Check-out branch A (yours) and pull (ctrl+shift+7 to view your branches).
	-	Check-out branch B (Master) and pull.
4. Check-out the branch that will receive the merge commit (your branch).
5. Select the branch you want to merge on top of that (Master) and click merge.
6. **If there are conflicts**, open all files with the 'conflicts' tag and resolve conflicts (the conflicts 
resolve tool can be used to view your file on the left, the file from master on
right and the result in the middle). Per conflict you can pick either the
changes from your file or from master or write something new all together, you
need to look carefully which side has the correct code.
7. After resolving the conflicts in a file, 'stage' the file.
8. After all conflicts in all files are resolved and all the changes have been
staged commit the changes.
9. **If there are (no more) conflicts** push the merge-commit.
10. now there should be no more conflicts between your branch and the master
branch and you can create a pull-request in GitHub.

## Communication
GitHub enables discussion of issues and aspects of specific code, but there's nothing quite like a face-to-face conversation to resolve these and more general topics. We hold a [Google hangout](https://hangouts.google.com/) roughly every week or two 

## Git configuration
### Line endings
Please set the git configuration setting `core.autocrlf` to `false` for contributing to iDynoMiCS 2. It's generally a good idea to do this for your global settings if you are working on projects that are developed in both Windows and in Unix (Mac/Linux) environments. This only needs to be set once. 

To set this in Eclipse with eGit installed, go to *Preferences > Team > Git > Configuration*:
![eGit line endings](https://raw.githubusercontent.com/roughhawkbit/iDynoMiCS-2/master/Docs/eGit_line_endings.png?token=ADJGISle9ZPOp__NbvEDcHhg7ZJNePxqks5Wvg4WwA%3D%3D)

In the command line, this is done like so:
``` bash
git config --global core.autocrlf false
```

## GitHub hacks
If you've configured Git nicely and yet it still misbehaves, try these little hacks in your URL bar...

| Problem             | Fix   |
| ------------------- | ----- |
| Ignore whitespace   | ?w=1  |
| Change the tab size | ?ts=4 |
