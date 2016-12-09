package rcl.core.exceptions;

/**
 * Created by jorgen on 02.12.16.
 */
public class UserNotFoundException extends Exception {

    public UserNotFoundException() {

    }

    public UserNotFoundException(String username) {
        super("User with given name (" + username + ") not found!");
    }

}
