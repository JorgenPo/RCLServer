package rcl.core;

import rcl.core.exceptions.MethodNotFoundException;
import rcl.core.xml.XMLUtil;

import java.io.*;
import java.rmi.NoSuchObjectException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.*;
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
    public void processInput(String methodName, ArrayList<String> params) {

        cmd.exec(methodName, params);
    }
}

class RCLCommands {

    private static final String RCL_SHELL_VERSION = "1.1";

    private Map<String, Runnable> functions;
    private Map<String, String> descriptions;
    private PrintWriter out;
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

    public RCLCommands(PrintWriter out, RCLUser user) {
        this.out = out;
        this.user = user;

        initFunctions();
    }

    public void exec(String method, ArrayList<String> params) {

        try {
            if ( params.size() > 0 ) {
                args = params;
            } else {
                args = new ArrayList<>();
            }

            Runnable f = functions.get(method);
            if ( f == null ) {
                throw new MethodNotFoundException(method);
            }

            f.run();
            out.println(XMLUtil.makeResponse(result.toString()));
            out.println("EOT");
            result = new StringBuilder();

        } catch (MethodNotFoundException e) {
            out.println(XMLUtil.makeResponse("cmd: " + e.getMessage()));
            out.println("EOT");
        }
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
            out.println(e.getMessage() + "\n");
        }
    }
}

