package fr.metamorpion.api.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "redirection", ignoreUnknownFields = false)
public class RedirectionProperties {

    private String url;
}
