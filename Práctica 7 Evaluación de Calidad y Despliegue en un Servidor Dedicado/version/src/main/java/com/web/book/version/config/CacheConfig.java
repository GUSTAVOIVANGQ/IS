package com.web.book.version.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
/* 
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .initialCapacity(100)    // capacidad inicial
            .maximumSize(500)        // máximo número de entradas
            .expireAfterWrite(30, TimeUnit.MINUTES)    // tiempo de expiración
            .recordStats();          // habilitar estadísticas
    }
*/
    // Configuraciones específicas para diferentes cachés
    @Bean
    public CacheManager customCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configuración para caché de búsquedas de libros
        cacheManager.registerCustomCache("bookSearchCache",
            Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build()
        );
        
        // Configuración para caché de detalles de libros
        cacheManager.registerCustomCache("bookDetailsCache",
            Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(500)
                .build()
        );
        
        // Configuración para caché de libros similares
        cacheManager.registerCustomCache("similarBooksCache",
            Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(200)
                .build()
        );

        // Configuraciones para TVMaze
        cacheManager.registerCustomCache("featuredShowsCache",
            Caffeine.newBuilder()
                .expireAfterWrite(6, TimeUnit.HOURS)
                .maximumSize(50)
                .build()
        );
        
        cacheManager.registerCustomCache("showDetailsCache",
            Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .build()
        );
        
        cacheManager.registerCustomCache("showSearchCache",
            Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(500)
                .build()
        );
        
        cacheManager.registerCustomCache("similarShowsCache",
            Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(200)
                .build()
        );
        
        cacheManager.registerCustomCache("showRecommendationsCache",
            Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(100)
                .build()
        );

        // Configuraciones para User Service
        cacheManager.registerCustomCache("userCache",
            Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build()
        );
        
        cacheManager.registerCustomCache("userExistsCache",
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(500)
                .build()
        );
        
        cacheManager.registerCustomCache("allUsersCache",
            Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .build()
        );

        return cacheManager;
    }
}
