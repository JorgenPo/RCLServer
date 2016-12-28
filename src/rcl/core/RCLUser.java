package rcl.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jorgen on 01.12.16.
 */
public class RCLUser {
    private static final String salt = "4$J39Op][/!";

    private String username;
    private String password;

    private List<String> cmdHistory;

    private Date lastSession;

    private boolean isAuthorized = false;

    public RCLUser(String username, String password) {
        cmdHistory = new ArrayList<>();

        try {
            this.password = RCLUtil.md5(RCLUtil.md5(password) + salt);
            this.username = username;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean comparePassword(String password) {
        try {
            String hashedPassword = RCLUtil.md5(password);
            String saltedPassword = RCLUtil.md5(hashedPassword + salt);

            return saltedPassword.equals(this.password);
        } catch (Exception e) {

            // TODO: logging
            e.printStackTrace();
            return false;

        }
    }

    public Date getLastSession() {
        return lastSession;
    }

    public void setLastSession(Date lastSession) {
        this.lastSession = lastSession;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }
}
