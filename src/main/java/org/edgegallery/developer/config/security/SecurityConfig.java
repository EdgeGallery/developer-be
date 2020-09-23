package org.edgegallery.developer.config.security;

import javax.naming.ConfigurationException;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public AuthenticationManager authenticationManagerBean() throws ConfigurationException {
        try {
            return authenticationManager();
        } catch (Exception e) {
            throw new ConfigurationException("Security config exception");
        }
    }
}
