package client;

import server.RegistrationInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageConnection implements Runnable{
    private Socket messageHandlingSocket;
    private RegistrationInfo myInfo;

    MessageConnection(Socket messageHandlingSocket, RegistrationInfo myInfo) {
        this.messageHandlingSocket = messageHandlingSocket;
        this.myInfo= myInfo;
    }


    @Override
    public void run() {
        InputStreamReader input;
        try {
            input = new InputStreamReader(messageHandlingSocket.getInputStream());
            BufferedReader inputReader = new BufferedReader(input);
            while (!messageHandlingSocket.isClosed()) {
                if (inputReader.ready()) {
                    System.out.println(inputReader.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
