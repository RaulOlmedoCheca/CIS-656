package edu.gvsu.cis;

/**
 * <p>Title: Lab2</p>
 * <p>Description: Old School Instant Messaging Application </p>
 * @author Jonathan Engelsma
 * @version 1.0
 */

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Vector;
import java.net.*;
import java.io.*;

/**
 * A simple chat client.
 */
public class ChatClient 
{

	PresenceService nameServer;
    ZMQ.Socket serviceSkt = null;
    ZMQ.Socket broadcastSkt = null;
    ZContext serviceContext;
    SvrThread svrThread;
    BroadcastThread broadcastThread;
    RegistrationInfo regInfo;
    String uname;

    /**
     * Constructor.
     * @param uname The name of the chimp running the client.
     * @parm hostPortStr The host/port string in the form host:port
     * where the ":port" portion is optional. This is the host/port
     * of the presence service we are connecting to.  If set to null,
     * we'll attempt to connect to port 1099 on the localhost.
     */
    public ChatClient(String uname, String hostPortStr)
    {
        this.uname = uname;
        // Step 0. Figure out local host name.
        String myHost;
        try {
            myHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            myHost = "localhost";
        }

        // Step 1. We need to establish a server socket where we will listen for
        // incoming chat requests.
        serviceContext = new ZContext();
        this.serviceSkt = serviceContext.createSocket(ZMQ.REP);
        this.broadcastSkt = serviceContext.createSocket(ZMQ.SUB);

        // Step 2. we bind to the nameserver so we can register our client.
        if(hostPortStr == null) {
            hostPortStr = myHost;
        }
        System.out.println("User name is " + uname);

        System.out.println("Trying to connect to name server at " + hostPortStr);
        try {
            this.nameServer = (PresenceService)Naming.lookup("//" + hostPortStr + "/PresenceService");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to bind to name server.  Make sure its running.");
            System.exit(1);
        }

        System.out.println("Registering...");

        // Step 3. Create the registration info bundle.
        this.regInfo = new RegistrationInfo(uname,myHost,this.serviceSkt.bindToRandomPort("tcp://" + myHost),true);

        // Step 3.1. Connect the broadcast socket to the publisher
        try {
            broadcastSkt.connect("tcp://" + hostPortStr + ":" + this.nameServer.getBroadcastPort());
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        // Step 4. register the client with the presence service to advertise it
        // is available for chatting.
        try {
            if(!this.nameServer.register(this.regInfo)) {
                System.out.println("Sorry, that username is already taken.  Please try another.");
                System.exit(1);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("Error: Failed to register with name service.");
            System.exit(1);
        }

        // Step 5. Kick off a separate thread to listen to incoming requests on the
        // Server socket.
        this.svrThread = new SvrThread();
        this.broadcastThread = new BroadcastThread();
        Thread t1 = new Thread(this.svrThread);;
        Thread t2 = new Thread(this.broadcastThread);;
        t1.start();
        t2.start();
    }



    /**
     * Simple command shell that interprets commands from the user.
     */
    public void runCmdShell()
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean done = false;
        String cmd;

        // Read and process commands from standard input, until done.
        while(!done) {

        	this.promptUser();

            try {

                // Read an input line.
                cmd = in.readLine();

                // If we have a valid command, try to parse/process it.
                if(cmd!=null) {

                    if(cmd.toLowerCase().trim().startsWith("chat")) {
                        // look up user in the presence server.
                        String str = cmd.toLowerCase().trim();
                        int pos = str.indexOf(' ');
                        if(pos == -1) {
                            System.out.println("Missing userName.  Enter: chat {username} {msg}");
                            continue;
                        }
                        str = str.substring(pos+1);
                        pos = str.indexOf(' ');
                        String name;
                        String msg;
                        if(pos == -1) {
                            name = str;
                            msg = "";
                        } else {
                            name = str.substring(0,pos);
                            msg = str.substring(pos+1);
                        }

                        if(!lookupAndSendMsg(name,msg)) {
                            System.out.println("'" + name + "' is not currently online or is unavailable.");
                            continue;
                        }

                    } else if(cmd.toLowerCase().trim().startsWith("friends")) {

                        Vector<RegistrationInfo> clients = this.nameServer.listRegisteredUsers();
                        if(clients != null) {
                            System.out.println("\nThe following users are logged on:\n");
                            for(RegistrationInfo client : clients) {
                                System.out.println(client.getUserName() + (client.getStatus() ? " (available)":" (away)") );
                            }
                        } else {
                            System.out.println("No users are currently registered.\n");
                        }

                    } else if(cmd.toLowerCase().trim().startsWith("broadcast")) {

                        int pos = cmd.indexOf(' ');
                        if(pos == -1) {
                            System.out.println("Missing the message.  Enter: broadcast {msg}");
                            continue;
                        }
                        String msg = cmd.substring(pos+1);
                        this.nameServer.broadcast(this.regInfo.getUserName() + ": " + msg);
                        
                    } else if(cmd.toLowerCase().trim().startsWith("busy")) { 
                    		if(this.regInfo.getStatus()) {
	                    		this.regInfo.setStatus(false);
	                    		this.nameServer.updateRegistrationInfo(this.regInfo);
	                    		System.out.println("Status currently set to unavailable");
                    		}
                    		
                    } else if(cmd.toLowerCase().trim().startsWith("available")) { 
                		if(!this.regInfo.getStatus()) {
                    		this.regInfo.setStatus(true);
                    		this.nameServer.updateRegistrationInfo(this.regInfo);
                    		System.out.println("Status currently set to available");
                		}
                    			

                    } else if(cmd.toLowerCase().trim().startsWith("exit")) {

                        // we need to unregister from name server.
                        if(this.nameServer != null) {
                            System.out.println("Logging off.");
                            this.nameServer.unregister(this.regInfo.getUserName());
                        }
                        this.svrThread.stop();
                        this.broadcastThread.stop();
                        System.out.println("Goodbye.");
                        done = true;
                        this.serviceContext.close();
                    } else {
                        System.out.println("Hmm, not sure what you meant there. Try again.");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a message to a user with the given name.
     * @param userName The name of the user you wish to send the message to.
     * @param msg The msg string you wish to send the user.
     * @return true if the message was sent, false otherwise.
     */
    private boolean lookupAndSendMsg(String userName, String msg)
    {
        boolean retval = true;
        try {
            // look up this user's registration info so we can send message.
            RegistrationInfo reg = this.nameServer.lookup(userName);
            retval = this.sendMsgToKnownUser(reg, msg);
        } catch (RemoteException re) {
            System.out.println("Could not connect to presence server!");
            retval = false;
        }
        return retval;
    }
    
    /**
     * Send a message to a user with the given RegistrationInfo.
     * @param reg The RegistrationInfo of the user you wish to send the message to.
     * @param msg The msg string you wish to send the user.
     * @return true if the message was sent, false otherwise.
     */
    private boolean sendMsgToKnownUser(RegistrationInfo reg, String msg)
    {
    	boolean retval = true;
        if(reg==null || !reg.getStatus()) {
            // User is not registered!
            retval = false;
        } else {
            try {
                // open a socket connection remote user's client and send message.
                ZMQ.Socket skt = serviceContext.createSocket(ZMQ.REQ);
                skt.connect("tcp://" + reg.getHost() + ":" + reg.getPort());
                String completeMsg = "Message from " + this.regInfo.getUserName() + ": " + msg + "\n";
                skt.send(completeMsg);
                skt.close();
            } catch (Exception e) {
                // hmmm, user was registered, but it looks like they suddenly went away.
                //e.printStackTrace();
                retval = false;
            }
        }
        return retval;
    }

    private void promptUser()
    {
    	if(this.regInfo.getStatus()) {
    		System.out.println(this.regInfo.getUserName() + ": Enter command (friends, chat, broadcast, busy, or exit):");
    	} else {
    		System.out.println(this.regInfo.getUserName() + ": Enter command (friends, chat, broadcast, available, or exit):");
    	}
    }
    /**
     * Simple inner class that implements the thread that will be responsible
     * for handling incoming chat messages.
     */
    class SvrThread implements Runnable
    {
        // We'll use this to flag when the thread can stop accepting new
        // connections and exit.
        boolean done = false;

        /**
         * Thread's entry point.
         */
        public void run()
        {

            //
            // wait for incoming requests.
            //
            while(!done) {
                //
                // Process incoming chat message right here.
                //
                try {
                    String msg = serviceSkt.recvStr();

                    // We'll refresh the prompt, lest the chimp on the console
                    // get's confused.
                    System.out.println(msg);
                    ChatClient.this.promptUser();

                } catch (ZMQException ie) {
                }

            }

            // ok, we're outta here.  Turn the lights out before you leave.
            try {
                ChatClient.this.serviceSkt.close();
            } catch (ZMQException e) {
                System.out.println("Warning: caught ZMQException while closing socket.");
            }
            System.out.println("Server thread is exiting.");
        }
        

        /**
         * This is how we signal the ServerSocket thread which is likely to
         * be happily camped out on an accept() when the chimp types exit.
         */
        public void stop()
        {
            // set done to true.
            done = true;

            //
            // Just in case svr thread is blocked on accept, we give it a nudge.
            //
            ZMQ.Socket skt;
            try  {
                skt = serviceContext.createSocket(ZMQ.REP);
                skt.bind("tcp://" + InetAddress.getLocalHost() + ":" + ChatClient.this.regInfo.getPort());
                skt.close();
            } catch (Exception e) {
            }
        }
    }

    class BroadcastThread implements Runnable {
        boolean done = false;

        @Override
        public void run() {
            broadcastSkt.subscribe(ZMQ.SUBSCRIPTION_ALL);

            while(!done) {
                //
                // Process incoming chat message right here.
                //
                try {
                    String str = broadcastSkt.recvStr();

                    int pos = str.indexOf(' ');

                    String name;
                    String msg;
                    if(pos == -1) {
                        name = str;
                        msg = "";
                    } else {
                        name = str.substring(0,pos);
                        msg = str.substring(pos+1);
                    }

                    // We'll refresh the prompt, lest the chimp on the console
                    // get's confused.
                    if (!name.equals(ChatClient.this.uname + ":")) {
                        System.out.println(msg);
                        ChatClient.this.promptUser();
                    }


                } catch (ZMQException ie) {
                }

            }
            // ok, we're outta here.  Turn the lights out before you leave.
            try {
                ChatClient.this.serviceSkt.close();
            } catch (ZMQException e) {
                System.out.println("Warning: caught ZMQException while closing socket.");
            }
            System.out.println("Broadcast thread is exiting.");
        }

        public void stop() {
            // set done to true.
            done = true;

            //
            // Just in case svr thread is blocked on accept, we give it a nudge.
            //
            ZMQ.Socket skt;
            try  {
                skt = serviceContext.createSocket(ZMQ.SUB);
                skt.bind("tcp://" + InetAddress.getLocalHost() + ":" + ChatClient.this.regInfo.getPort());
                skt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Main routine for client process.
     */
    public static void main(String[] args)
    {
        if(args.length == 0) {
            System.out.println("Usage: \n\tjava ChatClient userName [host[:port]]\n");
            System.out.println("\nwhere userName is your username, and host/port is the name service.");
            System.exit(0);
        }

        // Create a client object.
        ChatClient myClient;
        if(args.length > 1) {
            myClient = new ChatClient(args[0], args[1]);
        } else {
            myClient = new ChatClient(args[0],null);
        }

        // Now we will process chat commands.
        myClient.runCmdShell();
    }
}
