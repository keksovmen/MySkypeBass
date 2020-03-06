package com.Abstraction.Networking.Utility;

import com.Abstraction.Util.Algorithms;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerSocketProxy extends ServerSocket {

    private final List<Socket> acceptedSockets = Collections.synchronizedList(new LinkedList<>());
    private int counter = 0;

    public ServerSocketProxy() throws IOException {
    }

    public ServerSocketProxy(int port) throws IOException {
        super(port);
    }

    public ServerSocketProxy(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    public ServerSocketProxy(int port, int backlog, InetAddress bindAddr) throws IOException {
        super(port, backlog, bindAddr);
    }

    @Override
    public void close() throws IOException {
        super.close();
        acceptedSockets.forEach(Algorithms::closeSocketThatCouldBeClosed);
        acceptedSockets.clear();
    }

    @Override
    public Socket accept() throws IOException {
        Socket accept = super.accept();
        acceptedSockets.add(accept);
        counter++;
        if (counter >= 5) {
            partialCleaning();
            counter = 0;
        }
        return accept;
    }

    private void partialCleaning(){
        acceptedSockets.removeIf(Socket::isClosed);

    }
}
