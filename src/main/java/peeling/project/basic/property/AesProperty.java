package peeling.project.basic.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesProperty {

    private static String aesBody;
    private static String aesCreate;
    private static String aesRefresh;

    @Value("${aes.body}")
    public void setAesBody(String body) {
        aesBody = body;
    }

    public static String getAesBody() {
        return aesBody;
    }

    @Value("${aes.create}")
    public void setAesCreate(String create) {
        aesCreate = create;
    }

    public static String getAesCreate() {
        return aesCreate;
    }

    @Value("${aes.refresh}")
    public void setAesRefresh(String refresh) {
        aesRefresh = refresh;
    }

    public static String getAesRefresh() {
        return aesRefresh;
    }
}
