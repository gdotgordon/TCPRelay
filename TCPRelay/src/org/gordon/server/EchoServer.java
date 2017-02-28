package org.gordon.server;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The implementation of the EchoServer.  This uses a socket control channel both to
 * establish the client listener and then join in on subsequent client requests received.
 * Due to the inheritance from the base class, the gory details can be found in the
 * <code>RelayCompatibleServer</code> class. 
 * @author Gary
 *
 */
       

public class EchoServer extends RelayCompatibleServer {
    
    public static final String SERVER_NAME = "echo server";
    
    public static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: echoserver <host> <port number>");
            System.exit(1);
        }
        
        String host = args[0];
        int portNum = Integer.parseInt(args[1]);
        
        // Process requests forever.
        EchoServer es = new EchoServer(host, portNum);
        es.processRequests();
    }
    
    public EchoServer(String host, int portNum) {
        super(host, portNum, SERVER_NAME);
    }
    
    /**
     * The required implementation of the abstract method from the base class.
     * Here we simply delegate to our own method to do the echo.
     */
    @Override
    public void handleRequest(InputStream is, OutputStream os) throws IOException {
        doEcho(is, os);
    }
 
    // Simply echo anything received back.
    private void doEcho(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int cnt;
        while ((cnt = is.read(buf)) >= 0) {
            if (cnt > 0) {
                os.write(buf, 0, cnt);
            }
        }
    }
}
