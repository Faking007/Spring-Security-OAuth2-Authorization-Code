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

    @GetMapping(value = {"/", "/index"})
    public ModelAndView indexPage() {
        return clientService.indexPage();
    }

    @GetMapping("/email")
    public ModelAndView getEmail() {
        return clientService.getEmail();
    }

    @GetMapping("/code")
    public ModelAndView getCode() {
        return clientService.getCode();
    }

    @GetMapping("/callback")
    public ModelAndView callback(@RequestParam("code") String code) throws UnsupportedEncodingException {
        return clientService.callback(code);
    }

    @GetMapping("/accesstoken")
    public ModelAndView getAccessToken() throws UnsupportedEncodingException {
        return clientService.getAccessToken();
    }

    @GetMapping("/resource")
    public ModelAndView getResource() {
        return clientService.getResource();
    }

    @GetMapping("/clear")
    public ModelAndView clear() {
        return clientService.clear();
    }
}
