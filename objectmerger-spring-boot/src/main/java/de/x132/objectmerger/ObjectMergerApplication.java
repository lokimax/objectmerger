package de.x132.objectmerger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ObjectMergerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjectMergerApplication.class, args);
    }

    @Bean
    public OpenAPI objectMergerOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("ObjectMerger REST API")
                                .description(
                                        "REST API for merging multiple data sources with configurable strategies")
                                .version("0.1.0"));
    }
}
