# TCPRelay
This was a test question I was given.  The goal is to Implement a TCP relay in Java plus a test Echo Server and test clients for that server.  The wrinkle in the requirement is that the Relay must be able to talk to clients and servers through a firewall.  This means all contact must be initiated by the client or server, not the Relay.

The design approach is for each server wishing to register with the Relay to establish a "control channel" with the server.  This channel will both establish a proxy in the relay for the server and notify the server when requests arrive on the relay once the proxy is established.  Therefore the relay needs a few essential components:

1. A channel to listen for server registration requests.
2. A channel to handle client requests.
3. A mechanism for notifying the server of a new client request and a way to get the request bytes to/from the server.

Further, to maintain performance, we need to offload the processing of each client request to a new thread, and similarly, the server must not get blocked handling any given request.

The are two packages, the "relay" and "server" packages.  Please see the main comments of each file for more details on the design and implementation details.

As far as building, you will require the JDK (JDK 8) to be able to compile the files.  You could also import the project into
Eclipse, in theory.

To build on the command line, you will need to grab all of the source files and they will be in an org/gordon/relay
and org/gordon/server hierarchy.  To compile them all at once, go to the directory above "org" and type something
similar to the Windws command, apprpriate for your system:

>"c:\Program Files\Java\jdk1.8.0_121"\bin\javac -classpath . -d \users\gary\demodir org\gordon\server\*.java org\gordon\relay\*.java
where the -d flag is where the class files will be compiled to.

Now to run the various programs, you should change directory to where the class files live (e.g. "demodir" above).

First start the relay server:
java -classpath . org.gordon.relay.Relay
note: optional port number may be specified, defaults to 8080 as in org.gordon.relay.Relay 8080

Now start the EchoServer:
java -classpath . org.gordon.server.EchoServer localhost 8080 (use the host and port where the relay was started)

The port number for clients to use for the service will be printed to stdout, something like:
service 'echo server' has established relay address: 127.0.0.1:64746 (windows likes to print out the numbers
instead of "localhost", but they are equivalent).  The key is that here port 64746 is the port the client should connect to.

Finally launch the test programs, using:
java -classpath . org.gordon.server.EchoClient localhost 64746 (use the proper port number advertised though!)
java -classpath . org.gordon.server.EchoStressClient localhost 64746 (use the proper port number advertised though!)

