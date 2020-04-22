package com.oocl.felix.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2.auth-server")
@Data
public class Oauth2AuthServerProperties {

    private String host;
    private String authorizeUrl;
    private String tokenUrl;
    private String checkTokenUrl;
}
