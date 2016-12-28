package rcl.core;

import rcl.core.exceptions.MethodNotFoundException;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class RCLProtocol {
    private static final String PROTO_VERSION = "1.4";

    private RCLUser user;
    private RCLCommands cmd;

    /**
     * Construct RCL session with user
     * @param user user to speak with
     */
    public RCLProtocol(RCLUser user) {
        this.user = user;
        cmd = new RCLCommands(user);
    }

    // Execute cmd and
    public String processInput(String methodName, ArrayList<String> params) {

        return cmd.exec(methodName, params);
    }
}

class RCLCommands {

    private static final String RCL_SHELL_VERSION = "1.1";

    private Map<String, Runnable> functions;
    private Map<String, String> descriptions;
    private RCLUser user;
    private ArrayList<String> args;
    private StringBuilder result;

    // Init static functions
    private void initFunctions() {
        result = new StringBuilder();
        functions = new HashMap<>();
        descriptions = new HashMap<>();

        functions.put("hello", () -> result.append("==> Hello! I'm RCL, what is your name?\n"));
        descriptions.put("hello", "An interesting short conversation with a rcl shell...");

        functions.put("help", () -> {
            result.append("== RCLcmd - simple command shell v0.1a ==\n");
            result.append(" For list of commands type 'list'.\n");
            result.append(" For command help print rman <cmd>.\n");
            result.append("\n");
            result.append(" Program currently in beta, don't be strict to it :)\n");
        });
        descriptions.put("help", "Type this if you are confused");

        functions.put("list", () -> {
            result.append("== List of commands: ==");

            //noinspection CodeBlock2Expr
            functions.keySet().forEach((name) -> {
                result.append(String.format("%-10s:  %s\n", name, descriptions.get(name)));
            });
        });
        descriptions.put("list", "Displays complete list of commands in shell");

        functions.put("version", () -> result.append("RCL shell version: " + RCL_SHELL_VERSION));
        descriptions.put("version", "Displays the rcl shell version.");

        functions.put("time", () -> result.append("Current server time: " + LocalDateTime.now()));
        descriptions.put("time", "Displays the current server time.");

        functions.put("ls", () -> {
            execNative("ls");
        });
        descriptions.put("ls", "Prints listing of current directory");

        functions.put("view", () -> {
            execNative("cat");
        });
        descriptions.put("view", "View file content");

        functions.put("account", () -> {
            result.append("== Account information: ==\n");
            result.append(" Username:       " + user.getUsername() + "\n");
            result.append(" Last access:    " + user.getLastSession() + "\n");
            result.append(" Favorite shell: " + "RCL shell <3\n");
        });
        descriptions.put("account", "Prints account information");


    }

    public RCLCommands(RCLUser user) {
        this.user = user;

        initFunctions();
    }

    public String exec(String method, ArrayList<String> params) {

        String ret;
        try {
            if ( params == null ) {
                args = new ArrayList<>();
            } {
                args = params;
            }

            Runnable f = functions.get(method);
            if ( f == null ) {
                throw new MethodNotFoundException(method);
            }

            f.run();
            ret = result.toString();
            result = new StringBuilder();

        } catch (MethodNotFoundException e) {
            ret = "Exec error: " + e.getMessage();
        }

        return ret;
    }

    public void execNative(String cmd) {
        try {
            StringBuilder builder = new StringBuilder(cmd);
            if ( !args.isEmpty() ) {
                for ( int i = 0; i < args.size(); i++ ) {
                    builder.append(" " + args.get(i));
                }
            }

            cmd = builder.toString();

            Process proc = Runtime.getRuntime().exec(cmd);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));

            String line;
            while ( (line = reader.readLine()) != null ) {
                result.append(line + "\n");
            }

            reader.close();
            proc.waitFor();
        } catch (Exception e) {
            result.append(e.getMessage() + "\n");
        }
    }
}

