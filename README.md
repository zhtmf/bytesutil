# Bytes-Util

## What's this
A Java library that handles serialization/deserialization between Java objects and byte stream defined by arbitrary binary protocols.

## Motivation
Though JSON as well as other well-defined binary serialization schemes are becoming de facto standard of remote calls between heterogenous systems, there are still a lot of old-style devices lingering around which communicates via raw socket and adopts arbitrarily home-grown binary protocols.

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
Conversion table between pseudo data types and types in Java is as follows:

