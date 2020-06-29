package com.github.digitalsoftwaresolutions.PiProxyServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ProxyServerHandler implements Runnable {
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final int mListenPort;
    private final String mRemoteServer;
    private final int mRemotePort;
    public ProxyServerHandler(int listenPort , String remoteAddress , int remotePort) {
        this.mListenPort = listenPort;
        this.mRemoteServer = remoteAddress;
        this.mRemotePort = remotePort;
    }
    private void pipe(InputStream inStream, OutputStream outStream) {
        Thread thread = new Thread(()->{
            byte[] reply = new byte[4096];

            int bytesRead;
            try {
                while ((bytesRead = inStream.read(reply)) != -1) {
                    outStream.write(reply, 0, bytesRead);
                    outStream.flush();
                }
            } catch (IOException e) {
            }
        });
        thread.start();
    }
    private Runnable getProxyHandler(Socket client){
        return () -> {
            try {
                InputStream streamFromClient = client.getInputStream();
                OutputStream streamToClient = client.getOutputStream();
                Socket proxyToRemote = new Socket(mRemoteServer, mRemotePort);
                InputStream streamFromServer = proxyToRemote.getInputStream();
                OutputStream streamToServer = proxyToRemote.getOutputStream();
                pipe(streamFromClient, streamToServer);
                pipe(streamFromServer, streamToClient);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        };
    }
    @Override
    public void run() {
        try {
            ServerSocket proxyServerSocket = new ServerSocket(mListenPort);
            while(!proxyServerSocket.isClosed()){
                try {
                    Socket s = proxyServerSocket.accept();
                    Runnable handler = getProxyHandler(s);
                    executorService.submit(handler);
                }catch (Exception ex){
                    //Connection Closed
                    ex.printStackTrace();
                }
            }

        }catch (Exception ex){
            //Unable to Listen on Address
            ex.printStackTrace();
        }
    }
}
