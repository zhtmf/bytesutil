
### Motivation
This is a script language for private use of this library. It is mainly used for simplifying ``ModifierHandler``s. Its grammar largely resembles that of Java language with some intentional modification for less writing.
### Basics
It supports the following syntactic structures:
* Expression Statements : those end with a semicolon. The last top-level expression statement in the script does not need to end with a semicolon.
* Compound Statements : those enclosed by braces ````{}````.
* If/Else statements : if/else if/else structure.
* Variable assignment: simply in the form ````Varname=Value````, like ````a=3;b='abcdef';```` etc. Variables are not explicitly typed and there is no variable declaration syntax.
* Method call: in the form ``obj.method(param1,param2)`` or call method by its name like ``obj['method'](param1,param2)``.

No other syntaxes like loops/switch are supported because this script is only meant to be an expression engine with a few additional features but not a full-fledged programming language.
There is no scope (even block scope) or closure.
Though there is a "return" operator but its use is not mandatory.  The last logical statement in a script is treated as the "return statement" while ``return`` is only used for premature termination. 

For example the following script: 
````
a=b+c;a*3;
````
will return (output) 15 where b is 2 and c is 3.
and this script:
````
if(a>=10){a;}else{a*2;}
```` 
will return 10 where a is 10 and return 18 where a is 9.
So ``return a+b+c;`` is same as ``a+b+c;``. Also because the last expression statement does not need to end with a semicolon ``;``. This small script can be furthur simplified as ``a+b+c``.

### Data Types
* Strings : strings are enclosed by **single** quotes, single quotes itself are escaped by \\. Valid escape sequences within a string are ``\b  \t  \n  \f  \r  \\'  and \\\\``.
* Numbers : supports decimal, octal and hexdecimal notations, like ``3, 3.1415, 0b10101, 0b101_101`` and ``0xABCDE``.  
* Boolean : represented by literal ``true`` and ``false``.
* Null : null values, represented by literal ``null``.

All numbers are represented by ``BigDecimal`` instances internally and calculated based on corresponding methods in this class under ``DECIAML64`` Math Context.

So results from this script may or may not be the same as what from other script engines (like nashorn) which utilizes ``Double`` directly.  For the same reason this script does not support positive or negative Infinity. 

Before setting values, comparing values or calling methods, the internal representation is converted and thus made interchangeable with numeric basic types of Java (excluding ``char``) / their wrapper classes / ``BigInteger``.

### Operators

Following operators are defined in this script language, which are not necessary a subset of Java's, grouped and listed in descent order from the hightest precedence:
##### Level 17
* ``[]`` : access array element / object property by name. Object property access using this operator is not available in Java but valid in Javascript. Dereferencing ``null`` and setting property on ``null`` both will raise exception.
* ``.`` : object property access, same as above except that the right operand must be Identifier.
##### Level 16
* ``**`` : exponentation, right operand is rounded down to an integer and left operator is raised to power of it.
##### Level 15
* ``++`` :  unary post-increment
* ````--```` : unary post-decrement
##### Level 14
* ``++`` : unary pre-increment
* ````--```` : unary pre-decrement
* ``+`` : unary plus, this operator cannot be used to coerce arbitrary value to number. 
* ``-`` : unary minus
* ``!`` : unary logical NOT
* ``~`` : unary bitwise NOT
##### Level 12
* ``* / %`` : multiplicative
* ``//`` : floor division, result is rounded down towards **negative infinity**.
##### Level 11
* ``+ - `` : additive and string concatenation.
##### Level 10
* ``<< >>`` : shift. Both operands are rounded down to an integer before calculation.
##### Level 9
* ``< <= > >=`` : relational
##### Level 8
* ``== !=`` : equality
##### Level 7
* ``&`` : bitwise AND
##### Level 6
* ``^`` : bitwise XOR
##### Level 5
* ``|`` : bitwise OR
##### Level 4
* ``&&`` : logical AND
##### Level 3
* ``||`` : logical OR
##### Level 2
* ``?:`` : ternary
##### Level 1
* ``=`` : assignment
##### Level 0
* ``return`` : return whatever value follows and terminates evaluation of this script prematurely.

 The lookup algorithm for operator ````[]```` and ````.```` is:
* if the expression between ``[]`` can be converted to a number, then it is recognized as an index. Otherwise it is recognized as property name.
* Indexing is only valid for ````array````s and ````java.util.List```` implementations. Exceptions are raised for objects of other types.
* For property accessing, the expression between ``[]`` must be a string or an expression which evaluates to a string.
* If the property name is ````length```` or ````size```` **and** the object is of type ````java.util.Collection```` , ````array```` , ````java.util.Map```` or ````java.util.String```` then its size or length is retrieved instead of actual property lookup.
* Otherwise, lookup correspondent ````getter```` or ````setter```` methods. Examples for such methods are ````getName````, ````setName```` or ````isEmpty````. 
* If the previous step fails, lookup the instance field or class field with exactly that name.
* If the previous step fails, lookup a method with exactly that name and has no parameter and, if success, use that method as the "getter". This is to support getter-like methods like ````java.time.LocalDate.toEpochDay````. This step will lookup public methods of this class and inherited methods from super classes.
* If all the steps above fail, ````null```` is returned.

Both instance methods/fields and class (static) methods/fields will be searched. The lookup result (including failure) will be cached in thread-safe way so if the class is modified at runtime result of the same script is not subject to change. 

Static fields/methods can be referenced directly through a variable of some type, or by including the full qualified name of a class in the script, like ````a=java.lang.System.currentTimeMillis;return a/1000;```` or ````b=com.acme.Constants.SOME_CONSTANT;return b*2;````. 

### Method Calls
This script engine supports calling method by using dot operator like ``obj.method(param1,param2)`` or call method by its name like ``obj['method'](param1,param2)``. 

Obviously this feature only supports calling method with parameters of limited java types with corresponding types in this script. However ``null`` literal can be used to call any methods with parameters with non-primitive types.

When calling method on a variable, only instance methods are searched. In contrast, when calling method on the full qualified name of a class, only static methods are checked.

As for calling overloaded methods, similar to what JSR states, an algorithm is applied in looking up a single candidate method:
<ol>
<li>Lookup candidate methods from public/protected methods in this class and inherited ones.</li>
<li>If no candidate methods are found, an exception is thrown to indicate the failure.</li>
<li>Within the candidates, remove those with a different number of parameters than the method call expression.</li>
<li>Then for every remaining methods, try to find the <tt>most specific</tt> method. Method A is more specific than method B if for formal parameters S1,S2,S3...Sn from method A and formal parameters T1,T2,T3...Tn from method b, there is Si > Ti for 1<= i <= n.
<p>
A type S is more specific than type T regarding method calls if:
<ul>
<li>The actual argument in this script is of type Number and S comes earlier than T in the sequence <tt>BigDecimal, double, Double, float, Float,  BigInteger, long, Long, int, Integer, short, Short, byte, Byte</tt> or S matches any of these types but T does not.</li>
<li>The actual argument in this script is of type Boolean and S comes earlier than T in the sequence <tt>boolean, Boolean</tt> or S matches any of these types but T does not.
<li>The actual argument in this script is of type String and S is of type java.lang.String but T is not.</li>
<li>The actual argument in this script is of type Null and S is of a non-primitive type but T is primitive.</li>
</ul>
</li>
<li>If in the previous step all candidate methods fail to pass the test, the  call fails. If multiple candidates remains, the call is ambiguous. In both cases, exception is thrown to indicate the failure.</li>
<li>Otherwise, the lookup result is remembered and cached using the key formed by class of the object, method name and all actual arguments types in the script.</li>
</ol>

### Exposed APIs
Implementations of ``javax.script.*`` interfaces are provided under the name ``__zhtmf-script``. So users can obtain ``ScriptEngine`` instances through ``new ScriptEngineManager().getEngineByName("__zhtmf-script")`` and use this script through standard APIs thereof.

The ``ScriptEngine`` implementation also implements ``Compilable``.

However, the implementation is intentionally made thread-safe at the cost of breaking some contracts of interfaces in ``javax.script.*``:
* Methods that receive a ``Bindings`` are modified to treat the bindings as temporary and use it directly for internal evaluation, rather than treat it as ``ENGINE_SCOPE`` mappings and merge it with existing ``ScriptContext``.
* Methods that receive a ``ScriptContext`` are modified to convert that object into a map with all values from all scopes while values from "higher" scopes overriden by those in "lower" ones. So modifications to the mappings from within the script is not reflected in later evaluations.