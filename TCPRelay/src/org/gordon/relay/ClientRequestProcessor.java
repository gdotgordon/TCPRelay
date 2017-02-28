package org.gordon.relay;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientRequestProcessor implements Runnable {

    private Socket clientSocket;
    private Socket daemonSocket;

    public ClientRequestProcessor(Socket clientSocket, Socket daemonSocket) {
        this.clientSocket = clientSocket;
        this.daemonSocket = daemonSocket;
    }

    public void start() {
        Thread requestThread = new Thread(this);
        requestThread.start();
    }

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {

        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {

            }
            try {
                if (daemonSocket != null) {
                    daemonSocket.close();
                }
            } catch (IOException e) {

            }
        }
    }
    
    public static class IOTransfer implements Runnable {
        InputStream is;
        OutputStream os;
        Socket writerSocket;
  
        public IOTransfer(InputStream is, OutputStream os, Socket writerSocket) {
            this.is = is;
            this.os = os;
            this.writerSocket = writerSocket;
        }
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
