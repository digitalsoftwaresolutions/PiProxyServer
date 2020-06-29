package com.github.digitalsoftwaresolutions.PiProxyServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer {
    private final int mListenPort;
    private final String mRemoteServer;
    private final int mRemotePort;
    public ProxyServer(int listenPort , String remoteAddress , int remotePort) {
        this.mListenPort = listenPort;
        this.mRemoteServer = remoteAddress;
        this.mRemotePort = remotePort;
    }
    private Thread mMainThread;
    public void Start() {
        if(mMainThread !=null ){
            Stop();
        }
        mMainThread = new Thread(new ProxyServerHandler(mListenPort,mRemoteServer,mRemotePort));
        mMainThread.start();
    }
    public void Stop() {
        if(mMainThread!=null) {
            try {
                mMainThread.join(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
