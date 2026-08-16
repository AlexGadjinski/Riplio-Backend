package app.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@Data
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private DataSize maxImageSize;
    private DataSize maxVideoSize;
}
