package com.web.book.version.config;

import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class CompressionConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> serverFactoryCustomizer() {
        return factory -> {
            Compression compression = new Compression();
            compression.setEnabled(true);
            // Habilitar compresión para respuestas mayores a 2KB usando DataSize
            compression.setMinResponseSize(DataSize.ofKilobytes(2));
            // Configurar tipos MIME a comprimir
            compression.setMimeTypes(new String[]{
                "text/html",
                "text/xml",
                "text/plain",
                "text/css",
                "text/javascript",
                "application/javascript",
                "application/json",
                "application/xml",
                "image/svg+xml",
                "application/x-javascript"
            });
            // Excluir tipos MIME que ya están comprimidos
            compression.setExcludedUserAgents(new String[]{
                "MSIE 6.0",
                "Mozilla/4.0"
            });
            
            factory.setCompression(compression);
        };
    }
}
