# Guide for developers

## Line lengths
Please keep line lengths to a maximum of 79 characters wherever possible. This makes it easier to reading code on small screens, and to compare code side-by-side on one larger screen.

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

## `int` vs `Integer`, `double` vs `Double`



## Using Git, GitHub and eGit

### Forking vs branching
