package peeling.project.basic.util.aop;

import java.util.UUID;

public class AspectUUID {
    private String uuid;

    public AspectUUID() {
        this.uuid = createUUID();
    }

    private String createUUID() {
        return UUID.randomUUID().toString().substring(0, 13);
    }

    public String getUUID() {
        return uuid;
    }
}
