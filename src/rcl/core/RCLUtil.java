package rcl.core;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jorgen on 01.12.16.
 */
public class RCLUtil {

    // 128 bit hash function
    public static String md5(String st) throws Exception {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Failed to generate hash: no md5 support");
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5hex = bigInt.toString(16);

        // TODO: string builder?
        while ( md5hex.length() < 32 ) {
            md5hex = "0" + md5hex;
        }

        return md5hex;
    }
}
