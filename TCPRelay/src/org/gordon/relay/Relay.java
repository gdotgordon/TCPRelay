package org.gordon.relay;

import java.io.*;

/** The Relay is the starting point for launching the main components.  It's main task is
 * to launch a ServiceRegistrar so servers can register with the relay.
 * 
 * @author Gary
 *
 */
public class Relay {
    public static final int DEFAULT_LISTEN_PORT = 8080;
    
    private int portNumber;
    
    private ServiceRegistrar registrar;

    /**
     * The main entry point for the TCP Relay.  The listen port may
     * be configured as a command line argument.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int portNum;
        if (args.length > 0) {
            portNum = Integer.parseInt(args[0]);  
        } else {
            portNum = DEFAULT_LISTEN_PORT;
        }
        Relay rs = new Relay(portNum);
        
        // Make our best effort to clean up.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (rs != null) {
                    rs.shutdown();
                }
            }
        });
        rs.beginRegistration();
    }
    
    public Relay() {
        this(DEFAULT_LISTEN_PORT);
    }
    
    public Relay(int portNumber) {
        this.portNumber = portNumber;
    }
    
    public void beginRegistration() throws IOException {
        registrar = ServiceRegistrar.instance(portNumber);
        registrar.start();
        registrar.awaitCompletion();
    }
    
    public void shutdown() {
        try {
            if (registrar != null) {
                registrar.shutdown();
            }
        } catch (IOException e) {
            System.err.println("***Warning: problem shutting down Relay Server: " + e.getMessage());
        }
    }
}
