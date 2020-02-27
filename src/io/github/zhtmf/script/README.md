

### Motivation
This is a script language for private use in this library mainly used for simplifying <tt>ModifierHandler</tt>s.
 
Its grammar largely resembles that of Java language with some intentional modification for less writing.

### Basics
It supports the following syntactic structures:

* Expression Statements : those end with a semicolon. The last top-level expression statement in the script does not need to end with a semicolon.
* Compound Statements : those enclosed by braces ````{}````.
* If/Else statements : if/else if/else structure.
* Variable assignment: simply in the form ````Varname=Value````, like ````a=3;b='abcdef';```` etc. Variables are not explicitly typed and there is no variable declaration syntax.

No other grammar like loops/switch are supported because this is only meant to be an expression script with some additional features but not a full-fledged programming language.

There is no scope (even block scope) or closure.

Though this script does have a "return" operator but its use is not mandatory.  The last logical statement in a script is treated as the "return statement" while <tt>return</tt> is only used for premature termination. 

For example the following script: 
````
a=b+c;a*3;
````
will return (output) 15 where b is 2 and c is 3.
and this script:
````
if(a>=10){a;}else{a*2;}
```` 
will return 9 where a is 9 and return 20 where a is 10.

So <tt>return a+b+c;</tt> is same as <tt>return a+b+c</tt> and in turn same as <tt>a+b+c</tt>.

### Data Types

* Strings : strings are enclosed by **single** quotes, single quotes itself are escaped by \\. Valid escape sequences in a string are <tt>\b  \t  \n  \f  \r  \\'  and \\\\</tt>.
* Numbers : supports decimal, octal and hexdecimal notations, like <tt>3, 3.1415, 0b10101, 0b101_101</tt> and <tt>0xABCDEF</tt>.  
* Boolean : represented by literal <tt>true</tt> and <tt>false</tt>.
* Null : null values, represented by literal <tt>null</tt>.

All numbers are represented by <tt>BigDecimal</tt> instances internally and calculated based on corresponding methods in this class under <tt>DECIAML64</tt> Math Context.

So results from this script may or may not be the same as what from other script engines (like nashorn) which utilizes <tt>Double</tt> directly.  For the same reason this script does not have positive or negative Infinity. 

When setting values or comparing numbers the internal representation is converted and thus made interchangeable with numeric basic types of Java / their wrapper classes / <tt>BigInteger</tt>.
TODO: char/ Characters

### Operators

Following operators are defined in this script language, which are not necessary a subset of Java's, grouped and listed in descent order from the hightest precedence:
##### Level 17
* <tt>**</tt> : exponentation, right operand is rounded down to an integer and left operator is raised to power of it.
##### Level 16
* <tt>[]</tt> : access array element / object property by name. Object property access using this operator is not available in Java but valid in Javascript.  While dereferencing ````null```` is not an error, setting properties on a ````null```` reference will raise an exception.
* <tt>.</tt> : object property access, same as above except that the right operand must be Identifiers.
##### Level 15
* <tt>++</tt> :  unary post-increment
* ````--```` : unary post-decrement
##### Level 14
* <tt>++</tt> : unary pre-increment
* ````--```` : unary pre-decrement
* <tt>+</tt> : unary plus, this operator cannot be used to coerce arbitrary value to number. 
* <tt>-</tt> : unary minus
* <tt>!</tt> : unary logical NOT
* <tt>~</tt> : unary bitwise NOT
##### Level 12
* <tt>* / %</tt> : multiplicative
* <tt>//</tt> : floor division, result is rounded down towards **negative infinity**.
##### Level 11
* <tt>+ - </tt> : additive and string concatenation.
##### Level 10
* <tt><< >></tt> : shift. Both operands are rounded down to an integer before calculation.
##### Level 9
* <tt>< <= > >=</tt> : relational
##### Level 8
* <tt>== !=</tt> : equality
##### Level 7
* <tt>&</tt> : bitwise AND
##### Level 6
* <tt>^</tt> : bitwise XOR
##### Level 5
* <tt>|</tt> : bitwise OR
##### Level 4
* <tt>&&</tt> : logical AND
##### Level 3
* <tt>||</tt> : logical OR
##### Level 2
* <tt>?:</tt> : ternary
##### Level 1
* <tt>=</tt> : assignment
##### Level 0
* <tt>return</tt> : return whatever value follows and terminates evaluation of this script prematurely.

 The lookup algorithm for operator ````[]```` and ````.```` is:
* if the expression between <tt>[]</tt> can be converted to a number, then it is recognized as an index. Otherwise it is recognized as property name.
* Indexing is only valid for ````array````s and ````java.util.List```` implementations. Exceptions are raised for objects of other types.
* For property accessing, the expression between <tt>[]</tt> must be a string or an expression which evaluates to a string.
* If the property name is ````length```` or ````size```` **and** the object is of type ````java.util.Collection```` , ````array```` , ````java.util.Map```` or ````java.util.String```` then its size or length is retrieved instead of actual property lookup.
* Otherwise, lookup correspondent ````getter```` or ````setter```` methods. Examples for such methods are ````getName````, ````setName```` or ````isEmpty````. 
* If the previous step fails, lookup the instance field or class field with exactly that name.
* If the previous step fails, lookup a method with exactly that name and has no parameter and, if success, use that method as the "getter". This is to support getter-like methods like ````java.time.LocalDate.toEpochDay````. This step will lookup public methods of this class and inherited methods from super classes.
* If all the steps above fail, ````null```` is returned.

Both instance methods/fields and class (static) methods/fields will be looked up if necessary. The lookup will be done on first encounter of a property and the result (including failure) will be cached in thread-safe way so if the class is modified at runtime result of the same script may not subject to change. 

Static fields/methods can be referenced by including the full qualified name of a class in the script, like ````a=java.lang.System.currentTimeMillis;return a/1000;```` or ````b=com.acme.Constants.SOME_CONSTANT;return b*2;````. 

### Exposed APIs
Implementations of ``javax.script.*`` interfaces are provided under the name ``__zhtmf-script``. So users can obtain ``ScriptEngine`` instances through calling ``new ScriptEngineManager().getEngineByName("__zhtmf-script")`` and use this script through standard APIs.

The ``ScriptEngine`` implementation also implements ``Compilable``.

However, the ``ScriptEngine`` implementation as well as internal classes of this script are intentionally made thread-safe at the cost of breaking some contracts of interfaces in ``javax.script.*``:
* Methods that receive a ``Bindings`` are modified to treat the bindings as temporary and use it directly for internal evaluation, rather than treat it as ``ENGINE_SCOPE`` mappings and merge it with existing ``ScriptContext``.
* Methods that receive a ``ScriptContext`` are modified to convert that object into a map with all values from all scopes while values from "higher" scopes overriding those in "lower" ones. So modifications to the mappings from within the script is not reflected in later evaluations.