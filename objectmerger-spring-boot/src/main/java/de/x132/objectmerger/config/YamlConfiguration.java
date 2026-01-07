package de.x132.objectmerger.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@Configuration
public class YamlConfiguration implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new YamlJackson2HttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("yaml", MediaType.parseMediaType("application/x-yaml"))
                .mediaType("yml", MediaType.parseMediaType("application/x-yaml"));
    }

    public static class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        public YamlJackson2HttpMessageConverter() {
            super(new YAMLMapper(), MediaType.parseMediaType("application/x-yaml"));
        }
    }
}
