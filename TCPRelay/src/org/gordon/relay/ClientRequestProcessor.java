package org.gordon.relay;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * To achieve concurrency, each client request is run in a separate thread here.
 * @author Gary
 *
 */
public class ClientRequestProcessor implements Runnable {

    private Socket clientSocket;
    private Socket daemonSocket;

    /**
     * Create a request processor for the client request.
     * @param clientSocket the socket from the requesting client
     * @param daemonSocket the socket form the servicing service.
     */
    public ClientRequestProcessor(Socket clientSocket, Socket daemonSocket) {
        this.clientSocket = clientSocket;
        this.daemonSocket = daemonSocket;
    }

    /**
     * Hide the thread details from the caller.
     */
    public void start() {
        Thread requestThread = new Thread(this);
        requestThread.start();
    }

    /**
     * The main logic to process a request.  The trick is to concurrently process the
     * input and output (in separate threads) using the IOTransfer object bi-directionally.
     */
    @Override
    public void run() {
        try {
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            InputStream isc = daemonSocket.getInputStream();
            OutputStream osc = daemonSocket.getOutputStream();

            Thread toServer = new Thread(new IOTransfer(is, osc, daemonSocket));
            toServer.start();
            Thread fromServer = new Thread(new IOTransfer(isc, os, clientSocket));
            fromServer.start();
            try {
                toServer.join();
                fromServer.join();
            } catch (InterruptedException e) {
                System.err.println("***Warning: the client request thread was interrupted");
            }
        } catch (IOException e) {
            System.err.println("***Error: the client request received an error: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("***Warning: cannot close client socket: " + e.getMessage());
            }
            try {
                if (daemonSocket != null) {
                    daemonSocket.close();
                }
            } catch (IOException e) {
                System.err.println("***Warning: cannot close server socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * The server read from and writes to the client or vice versa, depending on
     * how the parameters are passed in.
     * @author Gary
     *
     */
    public static class IOTransfer implements Runnable {
        InputStream is;
        OutputStream os;
        Socket writerSocket;
  
        /**
         * Creates a new transfer object
         * @param is the input stream, could be form either client or server
         * @param os the output stream, could be form either client or server
         * @param writerSocket so we can shut it down after we are done writing
         */
        public IOTransfer(InputStream is, OutputStream os, Socket writerSocket) {
            this.is = is;
            this.os = os;
            this.writerSocket = writerSocket;
        }
        
        /**
         * Do the dirty work of moving bits back and forth in a new thread.
         */
        @Override
        public void run() {
            try {
                int cnt;
                byte[] buf = new byte[1024];
                while ((cnt = is.read(buf)) >= 0) {
                    if (cnt > 0) {
                        os.write(buf, 0, cnt);
                    }
                }
                writerSocket.shutdownOutput();
            } catch (IOException e) {
                System.err.println("***Error transferring bytes" + e.getMessage());
            }
        }
    }
}
