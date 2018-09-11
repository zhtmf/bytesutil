


# Bytes-Util

## What's this
A Java library that handles serialization/deserialization between Java objects and byte stream defined by arbitrary binary protocols.

## Motivation
Though JSON as well as other well-defined binary serialization schemes are becoming de facto standard of remote calls between heterogeneous systems, there are still a lot of old-style devices lingering around which communicates via raw socket and adopts arbitrarily home-grown binary protocols.

Implementing such protocols is always a **pain** as I feel during my daily work, due to data structures which are not natively supported by Java, like unsigned integral types, little-endian numbers or BCD (binary coded decimals) etc.

So I make this library to automate the process of converting Java objects from/to byte streams of binary protocols and make programmers focus more on their business logic.

## Disclaimer
All examples used in this document are from protocols I actually implemented during my work.  Some parts are intentionally modified for security reasons.

## Quick Start
Consider the following protocol, which gives a definition of data packet:

|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Header Mark     |1|1-byte unsigned integer|0xEB
|Packet Type     |1|1-byte unsigned integer|`dynamic value`
|Sequence Number |2|2-byte unsigned integer|`dynamic value`
|End Mark        |1|1-byte unsigned integer|0x07
 
Declare the following Java class:
````
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.SHORT;

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
And then serialization/deserialization become rather simple:
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
As you can see from the simple example above, this library requires users make their custom class subclass of ````DataPacket````, uses annotations to define the scheme  (````BYTE````, ````SHORT````), properties of it (````Unsigned````) and then handles serialization/deserialization for automatically.

## Basic Concepts

### Abstract Data Types
This library defines the following pseudo data types which serves as an abstraction over various definitions in binary protocols:

Name | Meaning
----|----
BYTE | 1-byte integer
SHORT | 2-byte integer
INT | 4-byte integer
CHAR | sequence of bytes which should be interpreted as human readable texts
BCD | Binary-Coded Decimal
RAW | sequences of bytes that do not fall in the categories above and should be handled as-is 
 
Annotations for these pseudo data types can be found under the package ````org.dzh.bytesutil.annotations.types````.

Following conversion between above mentioned data types and Java types are defined:
 _| byte/Byte | short/Short | int/Integer | long/Long| String| char/Character| byte[] | java.util.Date | int[]
----|----|--|--|--|--|--|--|--|--
BYTE | ⚪|  ⚪   |⚪ |⚪ |   | | | |
SHORT | |  ⚪   |⚪ |⚪ |   | | | |
INT | |     |⚪ |⚪ |   | | | |
CHAR | ⚪|  ⚪   |⚪ |⚪ | ⚪  |⚪ | |⚪ |
BCD | ⚪|  ⚪   |⚪ |⚪ | ⚪  | | |⚪ |
RAW | |     | | |   | |⚪ | |⚪
 
Though not apparent from the table above, some conversions are conditional. For example when converting unsigned numbers to Java types which are not large enough to hold unsigned values, conversion will fail and exception is thrown. This topic will be covered later.

### Modifiers

This library uses some additional annotations to define properties of data types defined above:

Name | Meaning
----|----
Signed | Specifies that a single integral field or all integral fields in a class should be interpreted as signed.
Unsigned | pecifies that a single integral field or all integral fields in a class should be interpreted as unsigned. ***default***
BigEndian| Specifies that a single integral field or all integral fields in a class should be interpreted as big-endian. ***default***
LittleEndian| Specifies that a single integral field or all integral fields in a class should be interpreted as little-endian.
CHARSET| Specified charset for all CHAR type fields in a class or for a single field.
DatePattern| Specifies date pattern string for java.util.Date fields. 

These annotations can be found under the package ````org.dzh.bytesutil.annotations.modifiers````. They can be specified both at the class level and the field level, which applies to all fields in the annotated class or to a single field. 
By combining modifiers and data types, users are able to deal with protocols that adopts different endian-ness and signedness, even they are mixed in the same protocol (which is indeed bad style).

The library would be rather useless if only equipped with features above, however the real power which enables it to accommodate for vast varieties of home-grown binary protocols lies in the mechanics we will cover next, the handlers.

## Handlers

It is best explained with an example, consider the following protocol:

|Field Name      |Length|Type | Value                                             
|----------------|------|-----|------------------------------
|Header     |1|1-byte unsigned integer|0xF0
|Packet Length     |4|4-byte unsigned integer|`dynamic value`
|Identifier |4|4-byte array|0x01000000
|Sequence Number       |2|2-byte unsigned integer|`dynamic value`
|***Crc Flag***       |1|1-byte integer|0x00: with crc <br/>0x01: without crc
|***Body***      |||`dynamic value`, see definitions below
|***Crc***       |4|4-byte array|If CrcFlag is 0, this part is ommited, otherwise crc32 encoding of the Body part.
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
   
  The above protocol exposes three problems:
  * The crc part can be omitted, if the crcFlag is 0.
  * The body part is not static and can be either of the two types. Furthermore, the body part itself has a "header" which states what kinds of data follows.
  * If the body is of type 2, its total length is indeterministic, which relies on how many file names are in the list.

These problems makes it *dynamic* and distinct from static schemes like JSON. If data packets from this kind of protocol are to be converted to Java objects, we must find a way to express the logic for modifying its structure at runtime.

The solution is ***handlers***, for the protocol above, we declare the following Java class:
````
import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

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

Secondly, the `crc` field is also annotated with `Length`, however because its length depends on value of another field, `crcFlag` so we use a handler to wrap the branching logic, which is the user-defined `CrcHandler` class.

The `CrcHandler` class is a subclass of `ModifierHandler<Integer>`, which defines two abstract methods. As is rather straight forward in the code, these two methods are provided with name of the field in question, the (incomplete) entity object as well as the input stream (only for deserialization) as a ***context*** for custom processing. Within the handler body, we use the `crcFlag` field which is already known to us at this point to determine length of the `crc` array.

Thirdly comes the `body` part. This one is more complicated than the `Crc` field in that different types of body are not only different in length but also has different layout. It is possible to use a lot of handlers to handle all possibilities of data layout for this part but your code will soon be unmaintainable when new body types are added. 

So this library adopts a different approach. We encourage users to define such variant body part as another class which inherits `DataPacket`. Declares common properties (like the `Body Header` field in the protocol above) in super class and properties specific to one type of body in subclasses. For example, the following declarations suffice:
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

The `BodyHandler ` used in declaration of outer packet is in fact a subclass of `ModifierHandler` which simplifies the interface and defines only method for deserialization. Within the handler method body, we look ahead for the body header value and return instances of proper subclass of `Body`. Normally such looking ahead is wrong because data still needed for later processing has been consumed prematurely. But for this library, the `InputStream` passed as parameter is special which supports `mark` and `reset`. It is automatically reset after the handler method call and the two reads are effectively rewound.

Also as a side note, the `@Length(type=DataType.BYTE)` part in `BodyType2` means "write length of the list into the stream as a BYTE prior to serializing list content and read in list length prior to deserialize list itself", this is another usage of `Length` annotation which provides support for the "Length-Value" pattern. Such usage is especially useful in Java because length of a `List` is implicit in the list itself and using another field to record list length is certainly redundant. 

Annotations and helper classes used in this chapter can be found under package `org.dzh.bytesutil.annotations.modifiers` and `org.dzh.bytesutil.converters.auxiliary`.

## Leftover Topics

### Length Calculation
There is a third method, `length()` in `DataPacket`, which calculates length in byte for this entity class as if it was serialized into a destination. Some protocols tend to define a "length" field at the beginning of data packet which stores length in byte for the total packet which eases processing for socket frameworks like Netty. And by providing the `length` method, this library automates this progress for users.

Note that this method is still ***NOT*** a constant-time operation though it does not calculate by serialization.

### Collective Type Fields
Currently only `java.util.List` is supported for collection type fields. Such fields are not denoted by an annotation named `List`  but inferred by the declaration.  Annotations for the component type (like `CHAR(100)` in the example of previous chapter) is used in the same way as their non-collective counterpart.

List of Lists is not currently supported.

### Initialization

This library lazily initializes internal status for a class when it is first encountered required for optimizing later processing in a thread-safe way. So the first serialization or deserialization will be slow but later ones will be fast.