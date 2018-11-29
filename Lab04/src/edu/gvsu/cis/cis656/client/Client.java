package edu.gvsu.cis.cis656.client;

import com.sun.tools.internal.ws.wsdl.document.Port;
import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.clock.VectorClockComparator;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageComparator;
import edu.gvsu.cis.cis656.message.MessageTypes;
import edu.gvsu.cis.cis656.queue.PriorityQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
    PriorityQueue<Message> messages;
    MessageReceiverThread messageThread;
    DatagramSocket serverSocket;
    InetAddress serverAddress;
    VectorClock myVectorClock;
    Integer serverPort = 8000;
    Integer myPid = -1;
    BufferedReader in;
    String userName;

    public Client() {
        in = new BufferedReader(new InputStreamReader(System.in));
        messages = new PriorityQueue<>(new MessageComparator());
        myVectorClock = new VectorClock();
        int possiblePort = 0;
        while (serverSocket == null && possiblePort < 65535) {
            try {
                serverSocket = new DatagramSocket(possiblePort);
            } catch (SocketException e) {
                possiblePort++;
            }
        }
        try {
            serverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // Unable to get localhost internet address
            e.printStackTrace();
        }

        registerUser();

        this.messageThread = new MessageReceiverThread();
        Thread t1 = new Thread(this.messageThread);
        t1.start();

        cmdExecutor();
    }

    private void registerUser() {
        System.out.println("Enter a valid username");
        try {
            userName = in.readLine();
            if (userName != null) {
                Message registrationMessage = new Message(MessageTypes.REGISTER, userName, myPid, myVectorClock, userName);
                Message.sendMessage(registrationMessage, serverSocket, serverAddress, serverPort);
                Message replyMessage = Message.receiveMessage(serverSocket);
                if (replyMessage.type == MessageTypes.ERROR) {
                    System.out.println("Failure while registering user, exiting...");
                    System.exit(1);
                } else if (replyMessage.type == MessageTypes.ACK) {
                    myPid = replyMessage.pid;
                    myVectorClock.addProcess(myPid, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cmdExecutor() {
        String messageData;
        while (true) {
            System.out.print("Message: ");
            try {
                messageData = in.readLine();
                myVectorClock.tick(myPid);
                Message message = new Message(MessageTypes.CHAT_MSG, userName, myPid, myVectorClock, messageData);
                Message.sendMessage(message, serverSocket, serverAddress, serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Client myClient = new Client();
    }

    class MessageReceiverThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                Message message = Message.receiveMessage(serverSocket);
                if (message.type == MessageTypes.CHAT_MSG) {
                    messages.add(message);
                }
                Message topMsg = messages.peek();
                VectorClockComparator vcc = new VectorClockComparator();
                if (vcc.compare(myVectorClock, topMsg.ts) == 0) {
                    System.out.println("Less Than Current: " + topMsg.sender + ": " + topMsg.message);
                    myVectorClock.update(topMsg.ts);
                    messages.poll();
                    continue;
                }

                while (topMsg != null && !messages.isEmpty()) {
                    topMsg = messages.peek();
                    VectorClock merge = new VectorClock();
                    merge.update(myVectorClock);
                    merge.update(topMsg.ts);

                    boolean print = false;
                    if (topMsg.ts.getTime(topMsg.pid) == myVectorClock.getTime(topMsg.pid) + 1) {
                        print = true;
                        for (String key : merge.getClock().keySet()) {
                            if (topMsg.pid == Integer.valueOf(key)) {
                                continue;
                            }
                            if (topMsg.ts.getTime(Integer.parseInt(key)) > (myVectorClock.getTime(Integer.parseInt(key)))) {
                                print = false;
                            }
                        }
                    }
                    if (print) {
                        System.out.println(topMsg.sender + ": " + topMsg.message);
                        myVectorClock.update(topMsg.ts);
                        messages.poll();
                    } else {
                        topMsg = null;
                    }
                }
            }
        }
    }
}

/*

                VectorClockComparator comparator = new VectorClockComparator();

                Message message = Message.receiveMessage(serverSocket);
                if (message.type == MessageTypes.CHAT_MSG) {
                    messages.add(message);
                }
                Message topMsg = messages.peek();
                while (topMsg != null) {
                    System.out.println(comparator.compare(myVectorClock, topMsg.ts));
                    System.out.println(topMsg.message);
                    if (comparator.compare(myVectorClock, topMsg.ts) == 0) {
                        // Old messages
                        System.out.println(topMsg.sender + ": " + topMsg.message);
                        myVectorClock.update(topMsg.ts);
                        messages.poll();
                        topMsg = messages.peek();
                    } else {
                        topMsg = null;
                    }

                    if (Thread.interrupted()){
                        break;
                    }
                }
 */