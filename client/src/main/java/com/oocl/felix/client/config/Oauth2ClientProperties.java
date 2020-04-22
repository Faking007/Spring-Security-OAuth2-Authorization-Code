package com.oocl.felix.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2.client")
@Data
public class Oauth2ClientProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String responseType;
}
