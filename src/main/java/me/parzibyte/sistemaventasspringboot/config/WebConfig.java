package me.parzibyte.sistemaventasspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String root = Path.of("uploads").toAbsolutePath().normalize().toString() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + root);
    }
}
