package client;

import server.PresenceService;
import server.RegistrationInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageListener implements Runnable {
    private RegistrationInfo myInfo;
    private PresenceService server;

    MessageListener(RegistrationInfo myInfo, PresenceService server) {
        this.myInfo = myInfo;
        this.server = server;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            myInfo.setPort(serverSocket.getLocalPort());
            server.updateRegistrationInfo(myInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            Socket socket = null;
            try {
                //System.out.println("Listening for connections in " + myInfo.getPort());
                if (serverSocket != null) {
                    socket = serverSocket.accept();
                }
                //handle incoming message and wait for the next
                Thread messageThread = new Thread(new MessageConnection(socket));
                messageThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
