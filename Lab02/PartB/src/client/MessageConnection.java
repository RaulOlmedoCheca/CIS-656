package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageConnection implements Runnable{
    private Socket messageHandlingSocket;

    MessageConnection(Socket messageHandlingSocket) {
        this.messageHandlingSocket = messageHandlingSocket;
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
