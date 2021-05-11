package uk.co.ankeetpatel.encryptedfilesystem.guiconnector;


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ClientConfiguration {

    @Value("https://localhost:8080")
    private String serverURL;

    @Bean
    public WebClientEFS webClientEFS(WebClient webClient) throws NoSuchAlgorithmException {
        return new WebClientEFS(webClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClient webClient() throws SSLException {

        SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        return WebClient.builder().baseUrl(serverURL).exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(-1))
                .build())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
