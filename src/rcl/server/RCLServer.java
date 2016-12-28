package rcl.server;

import rcl.core.RCLProtocol;
import rcl.core.RCLUser;
import rcl.core.exceptions.UserNotFoundException;
import rcl.core.RemoteInterface;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RCLServer extends UnicastRemoteObject
        implements RemoteInterface {
    private static final String SERVER_VERSION = "1.6";

    private ConcurrentHashMap<String, RCLUser> users;
    private ConcurrentHashMap<RCLUser, RCLProtocol> protocols;
    private boolean isUsersLoaded = false;
    private int port;

    RCLServer() throws RemoteException {

    }

    public static void main(String[] args) throws IOException {
        RCLServer server = new RCLServer();
        server.startup(args);

        try {
            LocateRegistry.createRegistry(1099);

            Naming.bind("RCL", server);

            System.out.println("Server started on " + server.port + " port!");
        } catch (AlreadyBoundException e) {
            System.err.println("Init server exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void startup(String args[]) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        parseArgs(args);
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

    /**
     * Construct RCL session from given client authority
     *   format: username password
     */
    private RCLUser checkAuthority(String username, String password)
            throws Exception {
        if (!isUsersLoaded) {
            loadUsers();
        }

        RCLUser user = users.get(username);

        if ( user == null ) {
            throw new UserNotFoundException(username);
        }

        if ( !user.comparePassword(password) ) {
            throw new Exception("Wrong password!");
        }

        return user;
    }

    //TODO: real load from file
    private void loadUsers() {
        users = new ConcurrentHashMap<>();
        protocols = new ConcurrentHashMap<>();

        RCLUser test = new RCLUser("ambulance", "password");
        users.put(test.getUsername(), test);

        RCLUser test2 = new RCLUser("pavluc", "superpass");
        users.put(test2.getUsername(), test2);

        isUsersLoaded = true;
    }

    @Override
    public String exec(String username, String name, ArrayList<String> params) {
        RCLUser user;

        if ( (user = users.get(username)).isAuthorized() == false) {
            return "You should authorize first!";
        }

        RCLProtocol proto = protocols.get(user);
        return proto.processInput(name, params);
    }

    @Override
    public String authorize(String username, String password) {
        RCLUser user;
        try {
            user = this.checkAuthority(username, password);
        } catch (Exception e) {
            return "Authorization failed: " + e.getMessage();
        }

        user.setAuthorized(true);

        RCLProtocol proto = new RCLProtocol(user);
        protocols.put(user, proto);

        return "Welcome, " + user.getUsername() + "! Last login: " + user.getLastSession();
    }
}
