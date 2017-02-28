package org.gordon.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class RelayCompatibleServer {
    
    private String host;
    private int portNum;
    private String serverName;
    
    public RelayCompatibleServer(String host, int portNum, String serverName) {
        this.host = host;
        this.portNum = portNum;
        this.serverName = serverName;
    }
    
    public void processRequests() throws IOException, InterruptedException {
        //Thread.sleep(2000);
        try (Socket cli = new Socket(host, portNum)) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(cli.getOutputStream()));
            pw.println(serverName);
            pw.flush();
            InputStream is = cli.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String resp = br.readLine();
            System.out.println("read: " + resp);

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
    
    public abstract void handleRequest(InputStream is, OutputStream os) throws IOException;
    
    public class TaskRunner implements Runnable {
        private String host;
        private int port;
        
        public TaskRunner(String host, int port) {
            this.host = host;
            this.port = port;
        }
 
        public void run() {
            Socket sock = null;
            try {
                sock = new Socket(host, port);
                InputStream isc = sock.getInputStream();
                OutputStream osc = sock.getOutputStream();
                System.out.println("getting data!");
                handleRequest(isc, osc);
                
                // This is recommended so the relay will not be stuck in a read wait.
                sock.shutdownOutput();
                System.out.println("finito!");
            } catch (IOException e) {
            } finally {
                try {
                if (sock != null) {
                    sock.close();
                }
                } catch (IOException e) {
                    
                }
            }
            
        }
        
    }
}
