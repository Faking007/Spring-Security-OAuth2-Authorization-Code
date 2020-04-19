package com.oocl.felix.resourceserver.controller;

import com.oocl.felix.resourceserver.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceController {

    @GetMapping("/user/{username}")
    public UserDTO user(@PathVariable String username) {
        return new UserDTO(username, username + "@oocl.com");
    }
}
