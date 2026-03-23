package com.TsukasaChan.ShopVault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${shop-vault.upload.path:./uploads}")
    private String configuredUploadPath;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path path = Paths.get(configuredUploadPath);
        String absolutePath;
        
        if (path.isAbsolute()) {
            absolutePath = path.toString();
        } else {
            String userDir = System.getProperty("user.dir");
            absolutePath = Paths.get(userDir, configuredUploadPath).toAbsolutePath().normalize().toString();
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
