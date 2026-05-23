package com.gestornotas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.gestornotas"}) // <--- Esta línea obliga a Spring a buscar tu CorsConfig
public class GestorNotasApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestorNotasApplication.class, args);
    }
}