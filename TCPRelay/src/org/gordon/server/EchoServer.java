package org.gordon.server;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

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
        
        EchoServer es = new EchoServer(host, portNum);
        es.processRequests();
    }
    
    public EchoServer(String host, int portNum) {
        super(host, portNum, SERVER_NAME);
    }
    
    public void handleRequest(InputStream is, OutputStream os) throws IOException {
        doEcho(is, os);
    }
 
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
