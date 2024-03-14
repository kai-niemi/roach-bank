package io.roach.bank.config;

import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {
    @Override
    public void customize(JettyServletWebServerFactory factory) {
    }
}

