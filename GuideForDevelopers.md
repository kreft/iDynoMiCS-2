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
Please keep line lengths to a maximum of 80 characters wherever possible. This makes it easier to reading code on small screens, and to compare code side-by-side on one wider screen.

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

# Testing

Useful links:
- [Unit testing with JUnit](http://www.vogella.com/tutorials/JUnit/article.html)

# Teamwork

Useful links:
- [Atlassian Git Tutorial: Workflows](https://www.atlassian.com/git/tutorials/comparing-workflows)
- [A Visual Git Reference](http://marklodato.github.io/visual-git-guide/index-en.html)
- [Code Tuts+](http://code.tutsplus.com/tutorials/focusing-on-a-team-workflow-with-git--cms-22514)

## Using Git, GitHub and eGit

### Forking vs branching

### Merging vs rebasing

## Communication
GitHub enables discussion of issues and aspects of specific code, but there's nothing quite like a face-to-face conversation to resolve these and more general topics. We hold a [Google hangout](https://hangouts.google.com/) roughly every week or two 
