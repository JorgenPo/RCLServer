package rcl.core;

import rcl.core.exceptions.MethodNotFoundException;

import java.io.*;
import java.rmi.NoSuchObjectException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Exchanger;

public class RCLProtocol {
    private static final String PROTO_VERSION = "1.0";

    private RCLUser user;
    private RCLCommands cmd;

    private PrintWriter userOut;

    /**
     * Construct RCL session with user
     * @param user user to speak with
     */
    public RCLProtocol(RCLUser user) {
        this.user = user;
    }

    /**
     * Inject protocol in user <-> server communication
     * @param out User output character stream
     */
    public void connectTo(OutputStream out) {
        this.userOut = new PrintWriter(out, true);

        // TODO think for optimization
        cmd = new RCLCommands(userOut, user);
    }

    // Execute cmd and
    public void processInput(String input) {
        cmd.exec(input.trim());
        userOut.println("EOT");
    }
}

class RCLCommands {

    private static final String RCL_SHELL_VERSION = "1.1";

    private Map<String, Runnable> functions;
    private Map<String, String> descriptions;
    private PrintWriter out;
    private RCLUser user;

    // Init static functions
    private void initFunctions() {
        functions = new HashMap<>();
        descriptions = new HashMap<>();

        functions.put("hello", () -> out.println("==> Hello! I'm RCL, what is your name?"));
        descriptions.put("hello", "An interesting short conversation with a rcl shell...");

        functions.put("help", () -> {
            out.println("== RCLcmd - simple command shell v0.1a ==");
            out.println(" For list of commands type 'list'.");
            out.println(" For command help print rman <cmd>.");
            out.println("");
            out.println(" Program currently in beta, don't be strict to it :)");
        });
        descriptions.put("help", "Type this if you are confused");

        functions.put("list", () -> {
            out.println("== List of commands: ==");

            //noinspection CodeBlock2Expr
            functions.keySet().forEach((name) -> {
                out.printf("%-10s:  %s\n", name, descriptions.get(name));
            });
        });
        descriptions.put("list", "Displays complete list of commands in shell");

        functions.put("version", () -> out.println("RCL shell version: " + RCL_SHELL_VERSION));
        descriptions.put("version", "Displays the rcl shell version.");

        functions.put("time", () -> out.println("Current server time: " + LocalDateTime.now()));
        descriptions.put("time", "Displays the current server time.");

        functions.put("ls", () -> {
            try {
                Process proc = Runtime.getRuntime().exec("ls -l");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proc.getInputStream()));
                String line;
                while ( (line = reader.readLine()) != null ) {
                    out.println(line);
                }

                reader.close();
                proc.waitFor();
            } catch (Exception e) {
                out.println(e.getMessage());
                out.println(e.getMessage());
            }
        });
        descriptions.put("ls", "Prints listing of current directory");

        functions.put("account", () -> {
            out.println("== Account information: ==");
            out.println(" Username:       " + user.getUsername());
            out.println(" Last access:    " + user.getLastSession());
            out.println(" Favorite shell: " + "RCL shell <3");
        });
    }

    public RCLCommands(PrintWriter out, RCLUser user) {
        this.out = out;
        this.user = user;

        initFunctions();
    }

    public void exec(String cmd) {
        try {
            Runnable f = functions.get(cmd);
            if ( f == null ) {
                throw new MethodNotFoundException(cmd);
            }

            f.run();
        } catch (MethodNotFoundException e) {
            out.println("cmd: " + e.getMessage());
        }
    }
}

