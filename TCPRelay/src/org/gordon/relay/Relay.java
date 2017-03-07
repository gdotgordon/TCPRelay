package org.gordon.relay;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    // Make the Logger public and static.  This is sufficient for the purposes herein.
    
    public static Logger logger;
    static {
        logger = Logger.getLogger(Relay.class.toString());
        logger.setLevel(Level.INFO);
    }

    /**
     * The main entry point for the TCP Relay.  The listen port may
     * be configured as a command line argument.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int portNum = DEFAULT_LISTEN_PORT;
        for (int ptr = 0; ptr < args.length; ptr++) {
            String str = args[ptr];
            if (str.startsWith("-h")) {
                usage();
                System.exit(0);
            } else if ("-log".equals(args[ptr])) {
                if (ptr++ >= args.length) {
                    usage();
                    System.exit(1);
                }
                logger.setLevel(Level.parse(args[ptr]));
            } else {
                portNum = Integer.parseInt(args[0]);
            }
            ptr++;
        }

        Relay rs = new Relay(portNum);
        
        // Make our best effort to clean up.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (rs != null) {
                    rs.shutdown();
                }
            }
        });
        rs.beginRegistration();
    }
    
    public static void usage() {
        System.out.println("usage: Relay <portNumber> -log <LOG LEVEL> | -help");
    }
    
    public Relay() {
        this(DEFAULT_LISTEN_PORT);
    }
    
    public Relay(int portNumber) {
        this.portNumber = portNumber;
    }
    
    public void beginRegistration() throws IOException {
        logger.info("launching service registrar");
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
            logger.warning(() -> "***problem shutting down Relay Server: " + e.getMessage());
        }
    }
}
