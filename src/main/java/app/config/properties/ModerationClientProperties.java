package app.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "moderation.service")
public class ModerationClientProperties {

    private String baseUrl;
    private String internalApiKey;
}
