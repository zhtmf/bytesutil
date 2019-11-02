[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Coverage Status](https://coveralls.io/repos/github/zhtmf/bytesutil/badge.svg?branch=master)](https://coveralls.io/github/zhtmf/bytesutil?branch=master) [![Build Status](https://travis-ci.org/zhtmf/bytesutil.svg?branch=master)](https://travis-ci.org/zhtmf/bytesutil) [![Maven Central](https://img.shields.io/maven-central/v/io.github.zhtmf/bytes-util.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.zhtmf%22%20AND%20a:%22bytes-util%22)
# Bytes-Util

## What's this
A Java library that handles serialization/deserialization between Java classes and byte streams defined by arbitrary binary protocols.

## Motivation
Nowadays there are still a lot of devices or systems lingering around which communicates by non-standard home-grown binary protocols. Implementing such protocols is always a **pain** as I feel during my daily work, due to data structures or data types which are not natively supported or hard to implement in Java like unsigned integral types, little-endian numbers or binary coded decimals, etc. So I make this library to ease the process of implementing and make programmers focus more on their real work.

## Quick Start
Consider the following definition of a data packet:

|Field Name      |Length|Type                  
|----------------|------|-----
|Header Mark     |1|1-byte unsigned integer
|Packet Type     |1|1-byte unsigned integer
|Sequence Number |2|2-byte unsigned integer
|End Mark        |1|1-byte unsigned integer
 
And declaration of the following Java class:
````
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class MyPacket extends DataPacket{
    @BYTE
    @Order(0)
    public int headerMark;
    @BYTE
    @Order(1)
    public int type;
    @SHORT
    @Order(2)
    public int sequenceNumber;
    @BYTE
    @Order(3)
    public byte endMark;
}
````
And coding serialization/deserialization become as simple:
````
InputStream in = ...; //socket input stream, or other input streams.
MyPacket packet = new MyPacket();
packet.deserialize(in); //fields will be filled with values in the stream.
````
````
OutputStream out = ...; //write to socket or other forms of output streams.
MyPacket packet = new MyPacket();
packet.headerMark = 0xEB;
packet.type = 0x01;
packet.sequenceNumber = 255;
packet.endMark = 0x07;
packet.serialize(out);
````
As you can see from the basic example above, to use this library:
1. Users have their class subclasses ````DataPacket````
2. Use ````Order```` to mark fields that requires processing by this library (other fields are effectively ignored) and other annotations to define the protocol  (````BYTE````, ````SHORT````) and properties of it (````Unsigned````).
3. Use ````DataPacket#serialize```` and ````DataPacket#deserialize```` to handle serialization/deserialization automatically.

## Basic Concepts

### Abstract Data Types
This library defines the following pseudo data types which serves as an abstraction over different defintions in various binary protocols:

Name | Meaning
----|----
BYTE | 1-byte integer
SHORT | 2-byte integer
INT | 4-byte integer
LONG | 8-bye integer
CHAR | sequences of bytes which are interpreted as human readable text
BCD | Binary-Coded Decimal
RAW | sequences of bytes that do not fall in the categories above and used as-is
 
Annotations for these pseudo data types can be found under the package ````io.github.zhtmf.annotations.types````.

Following conversion between above-mentioned data types and Java types are defined:

|  |byte/Byte|short/Short|int/Integer|long/Long|String|char/Character|boolean/Boolean|byte[]|java.util.Date|int[]|BigInteger|Enums
--|--|--|--|--|--|--|--|--|--|--|--|--
BYTE|⚪||||||⚪|||||⚪
SHORT|⚪|⚪||||||||||⚪
INT|⚪|⚪|⚪||||||⚪|||⚪
LONG|⚪|⚪|⚪|⚪|||||⚪||⚪|⚪
BCD|⚪|⚪|⚪|⚪|⚪||||⚪||||
CHAR|⚪|⚪|⚪|⚪|⚪|⚪|||⚪||⚪|⚪
RAW||||||||⚪||⚪|||
INT3\*|||⚪|⚪|||||||||
INT5\*||||⚪|||||||||
INT6\*||||⚪|||||||||
INT7\*||||⚪|||||||||

\* INT3, 5, 6 and 7 are non-standard integers which occupy 3, 5, 6 and 7 bytes. They are supported to implement protocols (like mysql's client-server binary protocol) which possesses such data types.


### Modifiers

This library utilizes additional annotations to define properties of data types defined above:

Name | Meaning
----|----
Signed/Unsigned | Specifies that a single integral field or all integral fields in a class should be interpreted as signed or unsigned (***default***).
BigEndian/LittleEndian| Specifies that a single integral field or all integral fields in a class should be interpreted as big-endian. (***default***) or little-endian
CHARSET| Specified charset for all CHAR type fields in a class or for a single field.
DatePattern| Specifies date pattern string for java.util.Date fields. 
Variant| Specifies this field is of a sub type of ````DataPacket```` and requires custom initialization logic during runtime, refer to dedicated chapter below for more info.
Length/ListLength| Statically or dyanmically determines length of an array or a list at compile time or runtime, refer to dedicated chapter below for more info.
EndsWith| Specifies a string is of indeterministic length and ends with special sequence of bytes, refer to dedicated chapter below for more info.
Conditional| Optionally skips processing certain fields.

These annotations can be found under the package ````io.github.zhtmf.annotations.modifiers````. They can be specified both at  type level and at field level, which applies to all fields in one class or to a single field. 
By combining modifiers and data types, this library enables implementing protocols that adopts different endian-ness and signedness, even ones that mix them.

The library would be rather useless until now, as it can only be used to implement static protocols but not ones that have conditional branches in its structure. The real power which enables it to accommodate for vast varieties of binary protocols lies in the mechanics we will cover next.

### Handlers

It is best explained with an example, consider the following protocol:

|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Header     |1|1-byte unsigned integer|0xF0
|Packet Length     |4|4-byte unsigned integer|`dynamic value`
|Identifier |4|4-byte array|0x01000000
|Sequence Number       |2|2-byte unsigned integer|`dynamic value`
|***CRC Flag***       |1|1-byte integer|0x00: with crc <br/>0x01: without crc
|***Body***      |||`dynamic value`, see definitions below
|***CRC Value***       |4|4-byte array|If CrcFlag is 0, this part is ommited, otherwise crc32 encoding of the Body part.
|Ending       |1|1-byte integer|0xFF
 
body type 1:

|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Body Header     |2|2-byte unsigned integer|0x5109
|Device ID     |4|4-byte unsigned integer|`dynamic value`
|IP|4|4-byte array|`dynamic value`
 
body type 2:
 
|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Body Header     |2|2-byte unsigned integer|0x55DE
|Files Count    |1|1-byte unsigned integer|`dynamic value`
|File List|100*N|100-character string per file|`dynamic value`
   
The above protocol exposes three challenges:
* The CRC part can be omitted, if the crcFlag is 0.
* The body part can be either of the two types and the body part itself has a "header" which states what kinds of data follows.
* If the body is of type 2, its length is indeterministic, which relies on how many file names are in the list.

These problems makes it *dynamic* and distinct. We must find a way to express conditional logics at runtime in order to handle such data packets.

The solution is ***handlers***:
````
import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Unsigned
@LittleEndian
public class DynamicPacket extends DataPacket{
    @Order(0)
    @BYTE
    public int header;
    @Order(1)
    @INT
    public long length;
    @Order(2)
    @RAW
    @Length(4)
    public byte[] identifier;
    @Order(3)
    @SHORT
    public int sequenceNumber;
    @Order(4)
    @BYTE
    public byte crcFlag;
    @Order(5)
    @Variant(BodyHandler.class)
    public Body body;
    @Order(6)
    @RAW
    @Length(handler=CrcHandler.class)
    public int[] crc;
    @Order(7)
    @BYTE
    public int ending;
    
    private static final class CrcHandler extends ModifierHandler<Integer>{
        
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            return ((DynamicPacket)entity).crcFlag==0 ? 0 : 4;
        }
        
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((DynamicPacket)entity).crcFlag==0 ? 0 : 4;
        }
        
    }
    
    private static final class BodyHandler extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            byte b1 = (byte) is.read();
            byte b2 = (byte) is.read();
            int header = b2 <<8 | b1;
            if(header==0x5109) {
                return new BodyType1();
            }else if(header==0x55DE) {
                return new BodyType2();
            }else {
                throw new Error("unknown body");
            }
        }
        
    }
}
````

There are a lot of things to look at and we will go through them one by one.

Firstly , the `identifier` field is annotated with `Length(4)`, this is how this library defines static length.

Secondly, the `crc` field is also annotated with `Length`, however because its length depends on value of another field `crcFlag` so we use a handler to wrap the branching logic, which is the `CrcHandler` class.

The `CrcHandler` class is a subclass of `ModifierHandler<Integer>`, which defines two abstract methods. As is rather obvious in the code, these two methods are provided with name of the field in question, the (incomplete) entity object as well as the input stream (only for deserialization) as a ***context*** for custom processing. Within the handler body, we use the `crcFlag` field which is already known to us at this point to determine length of the `crc` array.

Thirdly comes the `body` part. This one is more complicated than the `Crc` field in that different types of body are not only different in length but also has different layout. It is possible to use a lot of handlers to handle all possibilities of data layout for this part but your code will soon be unmaintainable when new body types are added. 

This library adopts a different approach. Class field for the body part is declared as a common base class which also inherits `DataPacket` and it will be instantiated at runtime to be one of the concrete subclasses defines below:

````
@Unsigned
@LittleEndian
abstract class Body extends DataPacket{
    @Order(0)
    @SHORT
    public int bodyHeader;
}

@Unsigned
@LittleEndian
class BodyType1 extends Body{
    @Order(0)
    @INT
    public long deviceId;
    @Order(1)
    @RAW(4)
    public int[] IP;
}

@Unsigned
@LittleEndian
class BodyType2 extends Body{
    @Order(0)
    @CHAR(100)
    @Length(type=DataType.BYTE)
    public List<String> fileList;
}
````

The `BodyHandler ` is a subclass of `ModifierHandler` which simplifies the interface. Within the handler method body, we peek the body header and return instances of proper subclass of `Body`. Normally such peeking is wrong because data needed for later processing will be consumed prematurely. But in this library, the `InputStream` passed as parameter is a special one which supports `mark` and `reset`. It is automatically reset after the handler method call and the two reads are rewound.

Also `@Length(type=DataType.BYTE)` in `BodyType2` means "write length of the list into the stream as a BYTE prior to serializing list content and read in list length prior to deserialize list itself", this is another usage of `Length` annotation which provides support for the "Length-Value" pattern. Such usage is especially useful in Java because length of a `List` is implicit in the list itself and using another field to record the length is fairly redundant.

Annotations and helper classes used in this chapter can be found under package `io.github.zhtmf.annotations.modifiers` and `io.github.zhtmf.converters.auxiliary`.

## Length Calculation
### Length and ListLength
The library supports following usage of ````Length```` or ````ListLength```` annotation to define length of an array typed, List typed or String typed field:
````
    @Order(0)
    @CHAR(3)
    public String str;
````
````
    @Order(0)
    @RAW(3)
    public byte[] rawBytes; 
````
Defines a static length, without a ````Length```` annotation.
````
    @Order(0)
    @CHAR
    @Length(3)
    public String str;
````
Defines a static length, with a ````Length```` annotation, same as above.
````
    @Order(0)
    @CHAR
    @Length(handler=SomeClassExtendsModifierHandler<Integer>.class)
    public String str;
````
Defines a dynamic length, which value will be returned by the handler at runtime.
````
    @Order(0)
    @CHAR
    @Length(type=DataType.BYTE)
    public String str;
````
Defines a dynamic length, use length of whatever string the field holds. Also the length is ***written into the stream**** prior to the string itself (a common length-value pattern) as a ````BYTE```` value.
````
    @Order(0)
    @INT
    @Length(3)
    public List<Integer> ints;
````
Defines a static length of 3, which means this field should be serialized as a List which holds 3 4-byte ints.
````
    @Order(0)
    @CHAR(3)
    @ListLength(3)
    public List<String> strings;
````
When the component of the list also has length property, you should use ````ListLength```` to define length of the List instead.

### EndsWith
````
    @Order(0)
    @CHAR
    @EndsWith({\0})
    public String str;
````
Specifies a CHAR type field is of indeterministic length and its end is marked by specific sequence of bytes. Typical use of this annotation is implementing NULL terminated or line-feed terminated strings.

When deserialized, the ending array is discarded and not included in the field value. Similarly when serialized, user must not include the ending array in the field value.

This annotation can not be use in tandem with ````Length```` or ````CHAR#value()````

### length() Method
There is a third method, `length()` in `DataPacket`, which calculates length in byte for this entity class as if it was serialized into a destination. Some protocols tend to define a "length" field at the beginning of data packet which stores length in bytes of the total packet which eases processing in other languages. This library automates this progress for users.

Note that this method is ***NOT*** a constant-time operation though it does not calculate by doing a serialization.

## Variant
As is already covered above, this annotation specifies a handler which wraps custom initialization logic at runtime. It is useful whenever you need additional logic other than calling no-arg constructor, like copying field value from "parent" object to sub objects. 

It applies to a single object as well as to ````List```` of objects.

## User-defined Conversion
Library supports user-defined conversion by using the annotation of ````UserDefined````, as in the following example:
````
    @Order(0)
    @UserDefined(Converter1.class)
    @Length(8)
    public Timestamp ts;
````
As this library only "natively" supports java.util.Date, conversion to a java.sql.Timestamp requires custom logic. By using this annotation, you should clearly defines how many bytes this conversion process requires (in this example it is 8) and specifies a handler class to do the actual conversion work:\
````
public static class Converter1 extends TypeConverter<Timestamp>{
        @Override
        public void serialize(Timestamp obj, Output context) throws IOException {
            context.writeLong(((Timestamp)obj).getTime());
        }
        @Override
        public Timestamp deserialize(Input input) throws IOException{
            return new Timestamp(input.readLong());
        }
}
````
TypeConverter is a dedicated base class for custom conversion, and the ````Output```` and ````Input```` classes are utility classes which serves as an abstraction over the underlying stream and many other functions this library provides. Check the java docs of them for more information.

### Enums
This library supports conversion directly to enum-typed Java classes, which although requires additional effort. User-defined enum types should meet one of the following requirements:
1. Implements ````NumericEnum```` or ````StringEnum```` interface.
2. Returns distinct strings (numeric ones when mapping to a numeric data type) in the ````toString```` method of each enum members.
To give you an example:
````
private enum NEnum1 implements NumericEnum{
    FLAG1 {
        @Override
        public long getValue() {
            return 1;
        }
    },
    FLAG2 {
        @Override
        public long getValue() {
            return 2;
        }
    };
    @Override
    public abstract long getValue();
}
````
````
@INT
@Order(0)
public NEnum1 enumFlags1;
````
Then if the library sees 1 when the serializing process reaches enumFlags1, this field is automatically serialized as NEnum1.FLAG1. If there is 2, then it is serialized as NEnum1.FLAG2. The deserialization process is similar.

Alternatively the definition can be look like the following:
````
private enum NEnum2{
    FLAG1 {
        @Override
        public String toString() {
            return "127";
        }
    },
    FLAG2 {
        @Override
        public String toString() {
            return "255";
        }
    };
}
````
You can choose to not extending the interface but returns distinct values as strings in the ````toString```` method.

## Conditional Processing
The ````Conditional```` annotation is used to indicate that a field should be skipped under certain situations. Users should always supply a ````ModifierHandler```` defined like examples above but returns a ````boolean```` value. When this handler returns false, processing of this field will be skipped. 

For this reason, this handler should return same value for the same field during both serialization/deserialization processes.

## Lazy-Initialization
This library lazily initializes internal status for a class when it is first encountered in a thread-safe way. So the first serialization or deserialization will be slow but later ones will be fast.

## Examples
<a href='test/examples/javaclass'>Parsing Java class file</a>

<a href='test/examples/mysql/connector'>Simple Mysql connector</a>
