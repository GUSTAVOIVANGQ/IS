package com.web.book.version.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.http.CacheControl;

import java.util.concurrent.TimeUnit;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.resource.EncodedResourceResolver;

import org.springframework.web.servlet.resource.ContentVersionStrategy;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración para imágenes de perfil
        registry.addResourceHandler("/profile-photos/**")
                .addResourceLocations("file:uploads/profiles/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
        
        // Configuración para recursos estáticos
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new VersionResourceResolver()
                    .addContentVersionStrategy("/**"));
        
        // Configuración para recursos externos
        registry.addResourceHandler("/external/**")
                .addResourceLocations("https://covers.openlibrary.org/", 
                                    "https://static.tvmaze.com/",
                                    "https://dl.dropboxusercontent.com/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        
        // Asegura que las imágenes por defecto sean accesibles con compresión
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }
}
