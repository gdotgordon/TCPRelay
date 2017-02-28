package org.gordon.relay;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The ServiceRegistrar is the listener for servers to register with the Relay Server.
 * A server wanting to employ the relay connects to the accepting socket and will use
 * the input and output streams from its socket for control operations.
 * 
 * There are two phases of control operations.  First, the server is notified of the
 * host/port of the relay that clients will use for contact.  After that initial notification,
 * the communication channel will stay open, and will be notified of subsequent client connects
 * requesting service.  This will actually happen in the ClientListener class.  The channel will
 * communicate a host/port combo the server should connect to to interact with the input and
 * output of the client, thereby avoiding the firewall issues.
 * 
 * After the connection is established, the daemon will continue it's operations in a 
 * new thread in the ClientListener, thereby freeing up the DaemonRegistrar to handle
 * additional services wishing to register.
 * @author Gary
 *
 */

public class ServiceRegistrar implements Runnable {
    
    private int portNumber;
    private ServerSocket listener;
    private Thread registrarThread;
    
    // Track client listeners for cleanup.
    private List<ClientListener> clientListeners = new ArrayList<>();
    
    public ServiceRegistrar(int portNumber) {
        this.portNumber = portNumber;
    }
    
    public void start() throws IOException {
        listener = new ServerSocket(portNumber);
        registrarThread = new Thread(this);
        //registrarThread.setDaemon(true);
        registrarThread.start();
    }
    
    public void shutdown() throws IOException {
        System.out.println("SHUTDOWN!");
        if (listener != null) {
            listener.close();
        }
        if (registrarThread != null) {
            registrarThread.interrupt();
        }
        for (ClientListener cl : clientListeners) {
            cl.shutdown();
        }
    }
    
    public void awaitCompletion() {
        try {
            registrarThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * The run loop simply listens for connections, and when received, attempts to create a
     * Client Listener object for the daemon (service).
     */
    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("service registrar: listening on: " + listener.getLocalSocketAddress());
                Socket socket = listener.accept();
                System.out.println("accepted service registration socket: " + socket);
                ClientListener cl = new ClientListener(socket);
                clientListeners.add(cl);
                cl.start(); 
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            System.out.println("closing registrar listener");
            try {
                if (listener != null) {
                    listener.close();
                }
            } catch (IOException e) {
                System.out.println("closing registrar socket: " + e.getMessage());
            }
        }
    }
}
