package com.oocl.felix.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2.resource-server")
@Data
public class Oauth2ResourceServerProperties {

    private String host;
}
