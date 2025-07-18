package org.example.apicalls.client;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.PassthroughTrustManager;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Configuration
public class PageClient {

    public Response getPageResponse(String url) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null,
                new TrustManager[] { new PassthroughTrustManager() },
                new SecureRandom());
        return ResteasyClientBuilder
                .newBuilder()
                .sslContext(sslContext)
                .build()
                .target(url)
                .request()
                .get();
    }
}
