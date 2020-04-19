package com.oocl.felix.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoDTO {

    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
}
