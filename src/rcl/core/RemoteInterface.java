package rcl.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by jorgen on 28.12.16.
 */
public interface RemoteInterface extends Remote {
    String exec(String username, String name, ArrayList<String> params) throws RemoteException;

    String authorize(String username, String password) throws RemoteException;
}
