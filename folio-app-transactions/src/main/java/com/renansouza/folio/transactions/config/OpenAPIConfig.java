package com.renansouza.folio.transactions.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Generated
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Contact contact = new Contact();
        contact.setEmail("renan@duck.com");
        contact.setName("Renan Souza");
        contact.setUrl("https://github.com/renansouza-dev");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Transaction Management API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage stock transactions.").termsOfService("https://github.com/renansouza-dev/terms")
                .license(mitLicense);

        return new OpenAPI().info(info);
    }
}