# REngine

This library is a modified version of [REngine library](https://github.com/s-u/REngine) 
by [Simon Urbanek](https://github.com/s-u).

REngine is a full client suite that allows any Java application (JDK 1.6 or higher)
to access a remote R server running Rserve. The suite is written entirely in Java. 
It provides automatic type translation for most objects such as int, double, arrays, 
String or Vector and classes for special R objects such as RBool, RList etc. 

## Modifications

Modifications to original library are focused (but not limited to) better connection 
error handling and stability. 

Other modifications include: mavenized project stucture, code cleanup and reformat, 
usage of Docker in tests. 