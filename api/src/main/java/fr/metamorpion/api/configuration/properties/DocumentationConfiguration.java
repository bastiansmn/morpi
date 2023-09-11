package fr.metamorpion.api.configuration.properties;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfiguration {

    @Bean
    public OpenAPI apiConfiguration() {
        Info info = new Info()
                .title("Meta morpion API")
                .version("1.0")
                .description("This API exposes endpoints to connect and play meta-morpion.");

        return new OpenAPI()
                .info(info);
    }
}
