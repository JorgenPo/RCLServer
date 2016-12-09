package rcl.server;

import rcl.core.RCLProtocol;
import rcl.core.RCLUser;
import rcl.core.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RCLServer {
    private static final String SERVER_VERSION = "1.0";

    private  Map<String, RCLUser> users;
    private  boolean isUsersLoaded = false;
    private  ServerSocket socket;
    private  int port;

    private  ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        RCLServer server = new RCLServer();
        server.startup(args);
    }

    private void startup(String args[]) {
        BufferedReader sysin = new BufferedReader(
                new InputStreamReader(System.in));

        if (args.length < 2) {
            printUsage();
            return;
        }

        parseArgs(args);

        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Cannot create server socket: " + e.getMessage());
            return;
        }

        System.out.println(String.format("rcl: server is listening on %d port...", port));

        while (true) {
            try {
                if ( sysin.ready() ) {
                    if (sysin.readLine().equals("shutdown")) {
                        socket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Input error: " + e.getMessage());
            }

            Socket client;
            try {
                client = socket.accept();   //Waiting for user to connect


            } catch (IOException e) {
                System.err.println("Cannot accept socket connection: " + e.getMessage());
                continue;
            }

            System.out.printf("rcl: %s has been connected!\n", client.getInetAddress());

            final Socket clientSocket = client;
            Thread clientThread = new Thread(() -> processClient(clientSocket));

            threads.add(clientThread);

            clientThread.start();
        }

        // Waiting for all client threads to finish (?)
        threads.stream().filter(th -> th != null).forEach(th -> {
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void parseArgs(String[] args) {
        if ( args.length > 1 ) {
            port = Integer.parseInt(args[1]);
        } else {
            port = 2016;
        }
    }

    private void printUsage() {
        System.out.println("RCLServer usage: rclserver -p port");
    }

    private void processClient(Socket client) {
        try (
                PrintWriter out =
                        new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()))
        )
        {
            String clientOutBuffer;
            String clientInBuffer = "";

            boolean isAuthorized = false;

            // Check user and trying to make RCL session
            while ( !isAuthorized && !client.isClosed()) {

                // Read line with client name and password
                if ( (clientOutBuffer = in.readLine()) == null ) {
                    break;
                }

                try {
                    RCLUser user = checkAuthority(clientOutBuffer);

                    RCLProtocol proto = new RCLProtocol(user);
                    proto.connectTo(client.getOutputStream());

                    out.println("Hello, " + user.getUsername() + "! Last log on "
                            + user.getLastSession() + ".");
                    out.println("EOT");

                    System.out.println(user.getUsername() + " just login");
                    isAuthorized = true;

                    user.setLastSession(new Date());

                    while ((clientInBuffer = in.readLine()) != null) {
                        if ( clientInBuffer.equals("disconnect") ) {
                            client.close();
                            System.out.printf("rcl: %s has just disconnected!\n", client.getInetAddress());
                            return;
                        }
                        proto.processInput(clientInBuffer);
                    }

                    if ( client.isClosed() ) {
                        System.out.printf("rcl: %s has just disconnected!\n", client.getInetAddress());
                        return;
                    }
                } catch (Exception e) {
                    out.println(String.format("rcl error: (%s)", e.getMessage()));
                    out.println("EOT");
                }
            }

        } catch (IOException e) {
            System.out.printf("rcl: i/o error with socket (%s)", client.getInetAddress());
            //System.out.printf("rcl: %s has just disconnected!", client.getInetAddress());
            return;
        }

        try {
            client.close();
        } catch (IOException e) {
            System.err.println("Cannot close client socket (" + socket.getInetAddress() + ")!");
            e.printStackTrace();
        }

        System.out.printf("rcl: %s has just disconnected!\n", client.getInetAddress());
    }

    /**
     * Construct RCL session from given client authority
     *
     * @param authority
     *   Well-known string that prove client authority
     *
     *   format: username password
     */
    private RCLUser checkAuthority(String authority)
            throws Exception {
        if (!isUsersLoaded) {
            loadUsers();
        }

        String[] userAuthority = authority.split(" ");

        if ( userAuthority.length < 2 ) {
            throw new Exception("Wrong authority format!");
        }

        RCLUser user = users.get(userAuthority[0]);

        if ( user == null ) {
            throw new UserNotFoundException(userAuthority[0]);
        }

        if ( !user.comparePassword(userAuthority[1]) ) {
            throw new Exception("Wrong password!");
        }

        return user;
    }

    //TODO: real load from file
    private void loadUsers() {
        users = new HashMap<>();

        RCLUser test = new RCLUser("ambulance", "password");
        users.put(test.getUsername(), test);

        RCLUser test2 = new RCLUser("pavluc", "superpass");
        users.put(test2.getUsername(), test2);

        isUsersLoaded = true;
    }
}
