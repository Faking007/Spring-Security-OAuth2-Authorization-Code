package com.oocl.felix.client.dto;

import lombok.Data;

@Data
public class ClientUser {

    private String username;
    private String password;
    private String code;
    private String accessToken;
    private String email;
}
