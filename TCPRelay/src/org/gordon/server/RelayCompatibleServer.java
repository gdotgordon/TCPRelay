package org.gordon.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This is one idea on how to reuse the base functionality of the echo server in other servers.
 * The EchoServer inherits from this abstract base class, as well as runs the functional method for
 * the server (i.e. echoing characters for the echo server).
 * 
 * This base class handles the protocol channel interactions between the server and the TCP Relay.
 * The protocol basically involves text-based line-by-line exchanges between the server and relay,
 * both to initially request a proxy for the service and in all subsequent exchanges, a notification
 * from the relay that a client is waiting for service.  In the latter case, the server receives a
 * host:port combination to which is should establish a socket connection, enabling it to receive and
 * return bits sent by the relay on behalf of the client request.
 * 
 * If the developer wishes to preserve the existing functionality, a standard accept() loop could be
 * added here, so the server could be responsive to all cases.  I have not added that feature yet,
 * due to the lack of time, but it is straightforward.
 * 
 * Given the relatively low-level of TCP, vis a vis HTTP, where we can propagate useful return error
 * messages, we are a little limited with TCP.  For now we print error messages to stderr.  In real
 * life, we would probably add them to a log file, a la WebLogic and others of that ilk.  The policy
 * I have chosen is that an IO problem with one request is not enough to bring down the whole system,
 * but this is a policy that is not cast in stone.
 * 
 * @author Gary
 */

public abstract class RelayCompatibleServer {
    
    private String host;
    private int portNum;
    private String serverName;
    
    /**
     * Base constructor for the relay-compatible server.
     * @param host where the relay server lives
     * @param portNum the port number to connect the relay server
     * @param serverName the name of the server, e.g. "echo service".  This is passed to the server
     * so it has a name for each service.
     */
    public RelayCompatibleServer(String host, int portNum, String serverName) {
        this.host = host;
        this.portNum = portNum;
        this.serverName = serverName;
    }
    
    /**
     * Runs the logic of registering with the server and then waiting to connect
     * to subsequent client requests.  It is up to the caller as to whether this
     * should be run in a separate thread, which would allow the regular accept()
     * logic to coexist.
     * @throws IOException if something network-related went wrong or a read or write
     * is unsuccessful
     */
    public void processRequests() throws IOException {
        try (Socket cli = new Socket(host, portNum)) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(cli.getOutputStream()));
            pw.println(serverName);
            pw.flush();
            InputStream is = cli.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String resp = br.readLine();
            System.out.println("established relay address: " + resp);

            while (true) {
                System.out.println(serverName + " get next task ...");
                String nextConnect = br.readLine();
                System.out.println("next connection: " + nextConnect);
                pw.println("ACK");
                pw.flush();
                String[] hp = nextConnect.split(":");
                new Thread(new TaskRunner(hp[0], Integer.parseInt(hp[1]))).start();
            }
        }
    }
    
    /**
     * The server implementer should implement this method to do its processing.
     * @param input the input stream coming from the relay
     * @param output the stream to write back to the relay
     * @throws IOException things could go wrong with the communications
     */
    public abstract void handleRequest(InputStream input, OutputStream output) throws IOException;
    
    /**
     * To achieve concurrent processing capability, each request from a client that
     * is forwarded by the relay is handled in its own thread here.
     * @author Gary
     *
     */
    public class TaskRunner implements Runnable {
        private String host;
        private int port;
        
        /**
         * Build the task runner to handle one client request.
         * @param host the host to connect to
         * @param port the port to conecct to 
         */
        public TaskRunner(String host, int port) {
            this.host = host;
            this.port = port;
        }
 
        /**
         * The thread to do the execution.  Note we call the abstract "handleRequest"
         * method here.
         */
        @Override
        public void run() {
            Socket sock = null;
            try {
                sock = new Socket(host, port);
                InputStream isc = sock.getInputStream();
                OutputStream osc = sock.getOutputStream();
                handleRequest(isc, osc);
                
                // This is recommended so the relay will not be stuck in a read wait.
                sock.shutdownOutput();
            } catch (IOException e) {
                System.err.println("***Error, processing of client request failed: " + e.getMessage());
            } finally {
                try {
                if (sock != null) {
                    sock.close();
                }
                } catch (IOException e) {
                    System.err.println("***Warning, task socket could not be closed: " + e.getMessage());
                }
            }
        }
    }
}
