package io.github.guojiaxing1995.easyJmeter.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${socket.server.enable}")
    private boolean enableServer;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (!enableServer) {
            http
                    .authorizeRequests()
                    .anyRequest().denyAll()
                    .and()
                    .csrf().disable();
        } else {
            http
                    .authorizeRequests()
                    .anyRequest().permitAll()
                    .and()
                    .csrf().disable();
        }
    }
}
