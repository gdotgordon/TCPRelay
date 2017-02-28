package org.gordon.server;

import java.io.*;
import java.net.*;

public class EchoStressClient {
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: EchoStressClient <host> <port number>");
            System.exit(1);
        }

        testBigPayload(args[0], Integer.parseInt(args[1]), "The quick black fox jumped over the lazy dog's back");
        testBigPayloadMultipleThreads(args[0], Integer.parseInt(args[1]));
    }
    
    public static void testBigPayload(String host, int port, String msg) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            System.out.println("Running big payload test ...");
            System.out.println("created request socket: " + socket);
            InputStream isc = socket.getInputStream();
            OutputStream osc = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(osc));
            BufferedReader br = new BufferedReader(new InputStreamReader(isc));
            StringBuilder sb = new StringBuilder(msg);
            for (int i = 0; i < 300; i++) {
                sb.append(msg);
            }
            String str = sb.toString();
            System.out.println("string length = " + str.length());
            pw.println(str);
            pw.flush();
            socket.shutdownOutput();
            String res = br.readLine();
            if (!str.equals(res)) {
                System.out.println(Thread.currentThread() + ": big payload test failed");
            } else {
                System.out.println(Thread.currentThread() + ": big payload test succeeded");
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
    
    public static void testBigPayloadMultipleThreads(String host, int port) throws InterruptedException {
        System.out.println("Testing the big Kahuna! ...");
        Thread.sleep(3000);  // So I can get two instances running at once.
        int numThreads = 20;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(new Runner(host, port, "Hello there, I am running thread " + i));
            threads[i] = t;
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
    }
    
    public static class Runner implements Runnable {
        String host;
        int port;
        String msg;
      
        public Runner(String host, int port, String msg) {
            this.host = host;
            this.port = port;
            this.msg = msg;
        }
        public void run() {
            try {
                testBigPayload(host, port, msg);
            } catch (IOException e) {
                System.out.println(Thread.currentThread() + ": exception " + e.getMessage());
            }
        }
    }
}
