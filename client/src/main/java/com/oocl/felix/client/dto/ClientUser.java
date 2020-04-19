package com.oocl.felix.client.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

@Data
public class ClientUser {

    private String username;
    private String password;
    private String accessToken;
}
