# TCPRelay
Implementation of TCP relay in Java plus a test Echo Server and test clients for that server.

The design rationale will be fleshed out here.  The are two packages, the "relay" and "server" packages.  For now,
please see the main comments of each file to understand the design.

As far as building, you will require the JDK (JDK 8) to be able to compile the files.  You could also import the project into
Eclipse, in theory.

To build on the command line, you will need to grab all of the source files and they will be in an org/gordon/relay
and org/gordon/server hierarchy.  To compile them all at once, go to the directory above "org" and type something
similar to the Windws command, apprpriate for your system:

>"c:\Program Files\Java\jdk1.8.0_121"\bin\javac -classpath . -d \users\gary\demodir org\gordon\server\*.java org\gordon\relay\*.java
where the -d flag is where the class files will be compiled to.

Now to run the various programs, you should change directory to where the class files live (e.g. "demodir" above).

First start the relay server:
java -classpath . org.gordon.relay.Relay <optional port number, defaults to 8080>

Now start the EchoServer:
java -classpath . org.gordon.server.EchoServer localhost 8080 (use the host and port where the relay was started)

The port number for clients to use for the service will be printed to stdout, something like:
service 'echo server' has established relay address: 127.0.0.1:64746 (windows likes to print out the numbers
instead of "localhost", but they are equivalent).  The key is that here port 64746 is the port the client should connect to.

Finally launch the test programs, using:
java -classpath . org.gordon.server.EchoClient localhost 64746 (use the proper port number advertised though!)
java -classpath . org.gordon.server.EchoStressClient localhost 64746 (use the proper port number advertised though!)

I don't think it is great to upload binaries to Git, so I can compile and provide a jar file with slightly different
instructions if needed.
