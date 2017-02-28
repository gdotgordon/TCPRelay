package org.gordon.relay;

import java.io.*;

/** The Relay is the starting point for launching the main components.  It's main task is
 * to launch a DaemonRegistrar so servers can register with the relay.
 * 
 * @author Gary
 *
 */
public class Relay {
    public static final int DEFAULT_LISTEN_PORT = 8087;
    
    private int portNumber;
    
    private ServiceRegistrar registrar;

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
        registrar = new ServiceRegistrar(portNumber);
        registrar.start();
        registrar.awaitCompletion();
    }
    
    public void shutdown() {
        try {
            if (registrar != null) {
                registrar.shutdown();
            }
        } catch (IOException e) {
        }
    }
}
