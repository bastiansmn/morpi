package fr.metamorpion.api.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "external-api")
@Getter
@Setter
public class ExternalAPIProperties {

    private String selected;

    private Map<String, ExternalAPI> allApis;

}

