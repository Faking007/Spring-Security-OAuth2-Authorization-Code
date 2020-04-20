package com.oocl.felix.client.controller;

import com.oocl.felix.client.service.ClientService;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/user-info")
    public ModelAndView userInfoPage() {
        return clientService.getUserInfoPage();
    }

    @GetMapping("/callback")
    public ModelAndView callback(@RequestParam("code") String code) throws UnsupportedEncodingException {
        return clientService.callback(code);
    }
}
