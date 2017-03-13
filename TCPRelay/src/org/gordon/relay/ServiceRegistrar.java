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
    private static volatile ServiceRegistrar instance;
    
    // Track client listeners for cleanup.
    private List<ClientListener> clientListeners = new ArrayList<>();
    
    /**
     * Creates the singleton ServiceRegistrar.  There are no potential race conditions
     * as this is called exactly once, but let's be safe.  DCL should work due to
     * the volatile declaration of the instance.
     * @param portNumber the port number to listen on
     */
    public static ServiceRegistrar instance(int portNumber) {
        if (instance == null) {
            synchronized (ServiceRegistrar.class) {
                if (instance == null) {
                    instance = new ServiceRegistrar(portNumber);
                }
            }
        }
        return instance;
    }

    private ServiceRegistrar(int portNumber) {
        this.portNumber = portNumber;
    }
    
    /**
     * Launches the thread to listen for servers requesting proxy service.
     * @throws IOException if the socket cannot be created
     */
    public void start() throws IOException {
        listener = new ServerSocket(portNumber);
        registrarThread = new Thread(this);
        registrarThread.start();
    }
    
   /**
    * Make an attempt to clean up at shutdown.
    * @throws IOException issues due to closing sockets and such
    */
    public void shutdown() throws IOException {
        Relay.logger.info("Shutdown initiated for Service Registrar");
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
    
    /**
     * Gives the Relay object something to prevent it from falling off the end of
     * the invoking method, basically.
     */
    public void awaitCompletion() {
        try {
            registrarThread.join();
        } catch (InterruptedException e) {
            Relay.logger.warning("Service Registrar thread was interrupted.");
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
                Relay.logger.info(
                        () -> "Service Registrar: listening for services on: " + listener.getLocalSocketAddress());
                Socket socket = listener.accept();
                Relay.logger.info(() -> "Service Registrar accepted service registration socket: " + socket);
                ClientListener cl = new ClientListener(socket);
                clientListeners.add(cl);
                cl.start(); 
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            Relay.logger.info("closing registrar listener");
            try {
                if (listener != null) {
                    listener.close();
                }
            } catch (IOException e) {
                Relay.logger.warning(() -> "closing registrar socket: " + e.getMessage());
            }
        }
    }
}
