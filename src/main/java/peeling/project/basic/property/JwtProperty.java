package peeling.project.basic.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperty {

    private String secretKey;

    private Integer expirationTime;

    private String header;

}
