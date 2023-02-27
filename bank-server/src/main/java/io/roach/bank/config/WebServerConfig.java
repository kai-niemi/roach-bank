package io.roach.bank.config;

import java.net.URL;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
public class WebServerConfig implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {
    @Value("${server.http.port}")
    private int httpPort;

    @Value("${server.gzip.enabled}")
    private boolean gzipEnabled;

    @Autowired
    private ServerProperties serverProperties;

    @Override
    public void customize(JettyServletWebServerFactory factory) {
        factory.addServerCustomizers(server -> {
            final HttpConfiguration httpConfiguration = new HttpConfiguration();
            httpConfiguration.setSecureScheme("https");
            httpConfiguration.setSecurePort(serverProperties.getPort());

            final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
            SecureRequestCustomizer src = new SecureRequestCustomizer();
            src.setSniHostCheck(false);
            httpsConfiguration.addCustomizer(src);

            Ssl ssl = serverProperties.getSsl();

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStoreType(ssl.getKeyStoreType());
            sslContextFactory.setKeyStorePassword(ssl.getKeyStorePassword());
            sslContextFactory.setKeyManagerPassword(ssl.getKeyPassword());

            try {
                URL url = ResourceUtils.getURL(ssl.getKeyStore());
                sslContextFactory.setKeyStoreResource(Resource.newResource(url));
            } catch (Exception ex) {
                throw new WebServerException("Could not load key store '" + ssl.getKeyStore() + "'", ex);
            }

            ServerConnector httpsConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpsConfiguration));
            httpsConnector.setPort(serverProperties.getPort());

            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.setPort(httpPort);

            server.setConnectors(new Connector[] {httpsConnector, httpConnector});

            if (gzipEnabled) {
                GzipHandler gzipHandler = new GzipHandler();
                gzipHandler.setInflateBufferSize(512);
                gzipHandler.setHandler(server.getHandler());
                gzipHandler.setIncludedMethods("GET", "POST", "DELETE", "PUT");
                server.setHandler(new HandlerCollection(gzipHandler));
            }
        });
    }
}

