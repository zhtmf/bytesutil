# Example - Simple Mysql connector

A working example which demonstrates features of this library by implementing a basic mysql connector.

It connects to the mysql server running on localhost:3306, print system variables and content of 'city' table in 'sakila' schema and then exits.
Please modify host, port, user name and password according to your environment if you want to run it by yourself.

This example has been tested on 5.6.44 and 5.6.2. As the protocol largely remains the same it should be working on other mysql releases too.

Parsing is largely based on offical document located at 
<a href="https://dev.mysql.com/doc/dev/mysql-server/8.0.12/PAGE_PROTOCOL.html">https://dev.mysql.com/doc/dev/mysql-server/8.0.12/PAGE_PROTOCOL.html</a>
