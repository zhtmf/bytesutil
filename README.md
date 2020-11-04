
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Coverage Status](https://coveralls.io/repos/github/zhtmf/bytesutil/badge.svg?branch=master)](https://coveralls.io/github/zhtmf/bytesutil?branch=master) [![Build Status](https://travis-ci.org/zhtmf/bytesutil.svg?branch=master)](https://travis-ci.org/zhtmf/bytesutil) [![Maven Central](https://img.shields.io/maven-central/v/io.github.zhtmf/bytes-util.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.zhtmf%22%20AND%20a:%22bytes-util%22)
# Bytes-Util

## What's this
A Java library that enables easing parsing of binary encoding schemes.
## Motivation
Nowadays there are still a lot of systems which communicates by non-standard home-grown binary protocols. Implementing such protocols is always a **pain** as I feel during my daily work, due to data structures or types not natively supported or hard to implement in Java, like unsigned integral types, little-endian numbers or binary coded decimals, just to name a few. So I make this library to ease the process of parsing and make programmers focus more on their real work.

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
As you can see from the basic example above, to use this library users need to:
1. Define a class which subclasses ````DataPacket````
2. Use ````Order```` as well as other annotations to define properties of instance fields which corresponds directly with fields in original binary schemes.
3. Use inheritied methods like ````DataPacket#serialize```` and ````DataPacket#deserialize```` to handle serialization/deserialization.

## Basic Concepts

### Abstract Data Types
This library defines the following pseudo data types which serves as an abstraction over various definitions in binary encoding schemes, and their annotations can be found under the package ````io.github.zhtmf.annotations.types````.

Name | Meaning
----|----
BYTE | 1-byte integer
SHORT | 2-byte integer
INT | 4-byte integer
INT3 | 3-byte integer
INT5 | 5-byte integer
INT6 | 6-byte integer
INT7 | 7-byte integer
BCD | Binary-Coded Decimal
RAW | sequences of bytes that do not fall in the categories above and used as-is
CHAR | sequences of bytes which are interpreted as human readable text
LONG | 8-bye integer
BIT | Numbers that stored as groupings of less than 8 _bits_
FIXED| Real data type that has a fixed number of digits after the radix point, aka. the ``Q`` format.



And following conversion between data types above and Java types are defined:

  ||    byte/Byte|  short/Short|    int/Integer|    long/Long|  Enums|  String| char/Character| boolean/Boolean|    byte[]| int[]|  java.util.Date| BigInteger| Double/double|  BigDecimal|
--| --| --| --| --| --| --| --| --| --| --| --| --| --| --|
BYTE|   ⚪|  ⚪|  ⚪|  ⚪|  ⚪|  |   |   ⚪|  |   |   |   |   |   |
SHORT|  |   ⚪|  ⚪|  ⚪|  ⚪|  |   |   |   |   |   |   |   |   |
INT|    |   |   ⚪|  ⚪|  ⚪|  |   |   |   |   |   ⚪|  |   |   |
INT3|   |   |   ⚪|  ⚪|  |   |   |   |   |   |   |   |   |   |
INT5|   |   |   |   ⚪|  |   |   |   |   |   |   |   |   |   |
INT6|   |   |   |   ⚪|  |   |   |   |   |   |   |   |   |   |
INT7|   |   |   |   ⚪|  |   |   |   |   |   |   |   |   |   |
BCD|    ⚪|  ⚪|  ⚪|  ⚪|  |   ⚪|  |   |   |   |   ⚪|  |   |   |
RAW|    |   |   |   |   |   |   |   |   ⚪|  ⚪|  |   |   |   |
CHAR|   ⚪|  ⚪|  ⚪|  ⚪|  ⚪|  ⚪|  ⚪|  |   |   |   ⚪|  ⚪|  |   |
LONG|   |   |   |   ⚪|  ⚪|  |   |   |   |   |   ⚪|  ⚪|  |   |
BIT|    ⚪|  |   |   |   ⚪|  |   |   ⚪|  |   |   |   |   |   |
FIXED|  |   |   |   |   |   |   |   |   |   |   |   |   ⚪|  ⚪|

In addition, an instance field can also be another ``Data Packet``. It will be automatically handled by calling its ``serialize`` or ``deserialize`` method and does not need to be annotated with data type annotations above.

Most of the conversions are intuitive but conversion from numeric types to ``enum``s deserves a separate chapter in later part of this document.

### Modifiers
Modifiers are another set of annotations which describes properties of data types above,  they can be found under the package ````io.github.zhtmf.annotations.modifiers````.

Name | Meaning
----|----
Order | Order of this field in the original encoding scheme. This is a must because reflection API in Java does not guarantee the order of Field objects returned by ``Class.getDeclaredFields``.
Signed/Unsigned | Specifies that a single field or all fields in a class should be interpreted as signed or unsigned (***default***).
BigEndian/LittleEndian| Specifies that a single field or all fields in a class should be interpreted as big-endian (***default***) or little-endian.
CHARSET| Charset for all CHAR type fields in a class or for a single field.
DatePattern| Date pattern string for a java.util.Date field. 
ListLength | length of ``java.util.List`` when handling with a list of basic data types.
Length | length of ``CHAR`` arrays or ``RAW`` arrays.
EndsWith | Indicates that a ``CHAR`` field is of indeterministic length and its end is marked by a specific sequence of bytes.
ListEndsWith | Indicates that length of a list is neither static nor calculated but depends on external conditions at runtime. It enables using a ModifierHandler to encapsulate additional logic. Users can refer to external resources or even modify the list itself within this handler. See how this library handles length of the ````ConstantPool```` field in a Java class file in the related example as an example for how to use this modifier.

These annotations can be found under the package ````io.github.zhtmf.annotations.modifiers````. They can be specified both at type level and at field level. And with not surprise, field level annotation overrides same annotation at type level. 

### Handler

Handler is a mechanism to express dynamic logic in a binary encoding, namely fields whose content depends on value another field or some other conditions and can only be determined at runtime. 

There are three types of handlers as listed below:
|Type     |Assosiated Annotations      |Usage                  
|----|------|-----
|Length handler     |Length, ListLength|dynamic length
|Conditional handler|Conditional       |conditionally omitting some fields
|Entity handler     |Variant           |custom instantiation logic of "sub" ``DataPacket``s.

Their usage are best explained through a small example, consider the following encoding scheme:

|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Header     |1|1-byte unsigned integer|0xF0
|Packet Length     |4|4-byte unsigned integer|
|Identifier |4|4-byte array|0x01000000
|Sequence Number       |2|2-byte unsigned integer|
|CRC Flag       |1|1-byte integer|0x00: with crc <br/>0x01: without crc
|***Body***      |||multiple types, depends on body header
|***CRC Value***       |4|4-byte array|If CrcFlag is 0, this part is ommited, otherwise crc32 encoding of the Body part.
|Ending       |1|1-byte integer|0xFF
 
body of type 1:

|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Body Header     |2|2-byte unsigned integer|0x5109
|Device ID     |4|4-byte unsigned integer|
|IP|4|4-byte array|
 
body of type 2:
 
|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Body Header     |2|2-byte unsigned integer|0x55DE
|Files Count    |1|1-byte unsigned integer|
|**File Name List**|100*N|100-character string per file name|

There are three parts in this binary encoding scheme that cannot be statically defined:
* The CRC part can be omitted, if the crcFlag is 0.
* The body part can be either of the two types and the body part itself has a "header" which states what kinds of data follows.
* In body type 2, its length is indeterministic, which relies on how many file names are in the list.

These problems makes it *dynamic* and we use handlers to express the logics.

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

Firstly , the `identifier` field is annotated with `Length(4)`, this is how this library defines static length.

Secondly, the `crc` field is also annotated with `Length`, however because its length depends on value of another field `crcFlag` so we use a handler to wrap the branching logic, namely is the `CrcHandler` class.

The `CrcHandler` class is a subclass of `ModifierHandler<Integer>`, which defines two abstract methods. These two methods are provided with name of the field in question, the (incomplete) entity object as well as the input stream (only for deserialization) as a ***context*** for custom processing. Within the handler body, we use the `crcFlag` field which is already known to us at this point to determine length of the `crc` array.  

This part can also be implemented using ``Conditional`` which is only introduced in later version of this library.

Thirdly comes the `body` part. This one is more complicated than the `Crc` field in that different types of body are not only different in length but also has different layout. It is possible to use a lot of handlers to handle all possibilities of data layout for this part but your code will soon be unmaintainable when new body types are added. 

This library adopts a different approach. Instance field for the body can be declared as a common base class which also inherits `DataPacket` and it will be instantiated as one of the concrete subclasses:

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

The `BodyHandler ` is a subclass of `ModifierHandler` which simplifies the interface. Within the handler method body, we peek the body header and return instances of proper subclass of `Body`. Normally such peeking is problematic because data for later processing will be consumed prematurely. But in this library, the `InputStream` passed as parameter is a special one which is automatically rewound after the handler method returns.

Also `@Length(type=DataType.BYTE)` in `BodyType2` means "write length of the list into the stream as a BYTE prior to serializing list content and read in list length prior to deserialize list itself", this is another usage of `Length` annotation which provides support for the "Length-Value" pattern. Such usage is especially useful in Java because length of a `List` is implicit in the list itself and using another field to record the length is fairly redundant.

Annotations and handler class definitions can be found under package `io.github.zhtmf.annotations.modifiers` and `io.github.zhtmf.converters.auxiliary`.

### Enums
This library supports conversion directly to java enums. Although it requires some effort. User-defined enum types should meet one of the following requirements:
1. Implements ````NumericEnum```` or ````StringEnum```` interface.
2. Returns distinct strings (numeric strings when mapping to numeric data types) from ````toString```` method of each enum member.
3. Enum members are mapped implicitly as integers of their corresponding ordinals.

As in the following example:
````
private enum NEnum1 implements NumericEnum{
    FLAG1 {
        @Override
        public long getValue() {
            return 0;
        }
    },
    FLAG2 {
        @Override
        public long getValue() {
            return 1;
        }
    };
    @Override
    public abstract long getValue();
}
````
````
@BYTE
@Order(0)
public NEnum1 enumFlags1;
````
The library deserializes ``0`` as NEnum1.FLAG1 and ``1`` as NEnum1.FLAG2. Similarly, NEnum1.FLAG1 is serialized as a byte value ``0`` and NEnum1.FLAG2 as ``1``.

Alternatively the definition can be like the following:
````
private enum NEnum1 {
    FLAG1 {
        @Override
        public String toString() {
            return "0";
        }
    },
    FLAG2 {
        @Override
        public String toString() {
            return "1";
        }
    };
}
````
This class does not implement the interface but returns distinct values as strings from ````toString```` method.

Or the definition can be as simply as :
````
private enum NEnum1 {
    FLAG1,
    FLAG2;
}
````
If the encoding scheme only defines ``0`` and ``1`` for this field and they are deserialized as ``FLAG1`` and ``FLAG2`` respectively according to their ordinals.

## Script

Admittedly, writing handlers is cumbersome and making the code unnecessarily lengthy. So this library introduces a script engine to simplify handlers. 

For example the following handler definition ( an excerpt from MySQL connector example):
```
    @Order(11)
    @RAW
    @Length(handler=RestPluginLength.class)
    public byte[] restPluginProvidedData;

    public static class RestPluginLength extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            HandshakeV10 v10 = (HandshakeV10)entity;
            return Math.max(13, v10.authPluginDataLen-8);
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            HandshakeV10 v10 = (HandshakeV10)entity;
            return Math.max(13, v10.authPluginDataLen-8);
        }
    }
```

can be simplified as 
```
    @Order(11)
    @RAW
    @Length(scripts = @Script("a=entity.authPluginDataLen-8;a<13 ? 13 : a;"))
    public byte[] restPluginProvidedData;
```
Using script sacrifices some performance and script cannot be used for Entity Handlers as this script engine does not aim to be a full-fledged programming language and users should write a real handler when expressing complex logic.

The script does not possess its own annotation but associates with existing modifier annotations through their ``scripts`` property, check java doc of these annotations and the marker annotation ``io.github.zhtmf.annotations.modifiers.Script`` for more information.

Refer to another document <a href='src/io/github/zhtmf/script'>here</a> for syntax of this script.

## Length Calculation

There is a third method, `length()` in `DataPacket`, which calculates length in byte for this entity class as if it was serialized into a destination. It is useful for some encoding schemes which defines a "length" field.

Note that this method is ***NOT*** a constant-time operation but it does not calculate by doing a serialization.

From 1.1.3, a new method ````offset```` has been added to ````ModifierHandler```` class. This method returns an ````int```` telling the current offset (position) in bytes relative to beginning of current DataPacket object. This is useful to implement some data structures (mainly strings) which does not have a deterministic length but occupies all remaining spaces in a data packet.

## User-defined Conversion
Library supports user defined conversion by using the annotation of ````UserDefined````, as in the following example:
````
    @Order(0)
    @UserDefined(Converter1.class)
    @Length(8)
    public Timestamp ts;
````
As this library only "natively" supports java.util.Date, conversion to a java.sql.Timestamp requires some effort. By using this annotation, you should clearly defines how many bytes this conversion process requires (in this example it is 8) and specifies a handler class to do the actual conversion work:\
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
``io.github.zhtmf.TypeConverter`` is a dedicated base class for custom conversion. ````Output```` and ````Input```` are utility classes which serves as an abstraction over the underlying stream and many other functions this library provides.

## Examples

<a href='test/examples/javaclass'>parsing Java class file</a>

<a href='test/examples/mysql/connector'>simple MySQL connector</a>

<a href='test/examples/websocket'>simple Websocket server</a>

<a href='test/examples/ntp'>Network Time Protocol client</a>
