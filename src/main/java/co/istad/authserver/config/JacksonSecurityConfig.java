package co.istad.authserver.config;

import co.istad.authserver.security.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonSecurityConfig {

    @Bean(name = "securityObjectMapper")
    public ObjectMapper securityObjectMapper() {
        ClassLoader classLoader = JacksonSecurityConfig.class.getClassLoader();

        // Configure PolymorphicTypeValidator to allow both base classes and subtypes
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                // Allow specific custom class
                .allowIfBaseType(CustomUserDetails.class)
                .allowIfSubType(CustomUserDetails.class)
                // Allow Spring Security packages
                .allowIfBaseType("org.springframework.security")
                .allowIfSubType("org.springframework.security")
                // Allow your custom security package
                .allowIfBaseType("co.istad.authserver.security")
                .allowIfSubType("co.istad.authserver.security")
                // Allow Java base types that might be needed
                .allowIfBaseType(Object.class)
                .allowIfBaseType("java.util")
                .allowIfBaseType("java.lang")
                .build();

        // Map UserDetails interface to CustomUserDetails implementation
        SimpleModule customModule = new SimpleModule();
        customModule.addAbstractTypeMapping(UserDetails.class, CustomUserDetails.class);

        return JsonMapper.builder()
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL)
                .findAndAddModules()
                .addModules(SecurityJacksonModules.getModules(classLoader))
                .addModule(new OAuth2AuthorizationServerJacksonModule())
                .addModule(customModule)
                .build();
    }
}