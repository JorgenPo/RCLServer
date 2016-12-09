package rcl.core.exceptions;

/**
 * Created by jorgen on 02.12.16.
 */
public class MethodNotFoundException extends Exception {

    public MethodNotFoundException() {

    }

    public MethodNotFoundException(String f) {
        super("Function for given name (" + f + ") not found!" +
                " Type list for see complete command list.");
    }
}
