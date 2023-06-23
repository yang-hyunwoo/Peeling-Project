package peeling.project.basic.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperty {

    private static String secretKey;
    private static int expirationTime;
    private static String tokenPrefix;
    private static String header;

    @Value("${jwt.secret-key}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public static String getSecretKey() {
        return secretKey;
    }

    @Value("${jwt.expiration-time}")
    public void setExpirationTime(int date) {
        expirationTime = date;
    }

    public static int getExpirationTime() {
        return expirationTime;
    }

    @Value("${jwt.token-prefix}")
    public void setTokenPrefix(String prefix) {
        tokenPrefix = prefix;
    }

    public static String getTokenPrefix() {
        return tokenPrefix;
    }

    @Value("${jwt.header}")
    public void setHeader(String header) {
        this.header = header;
    }

    public static String getHeader() {
        return header;
    }
}
