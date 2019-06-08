# Example - Parsing Java Class File

Though not a binary protocol, `.class` files still have binary structure and are suitable to demonstrate features of this library.

This example shows how to parse ``DataPacket.class`` using this library and prints its structure, uncomment the ``System.out...`` line to see the output. You can compare the output with what outputs by `javap -v` for correctness checking.

Parsing is based on offical document at <a href='https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html'>https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html</a>.
