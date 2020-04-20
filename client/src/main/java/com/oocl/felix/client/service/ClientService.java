package com.oocl.felix.client.service;

import com.oocl.felix.client.config.Oauth2ClientProperties;
import com.oocl.felix.client.config.Oauth2ServerProperties;
import com.oocl.felix.client.dto.ClientUser;
import com.oocl.felix.client.dto.TokenDTO;
import com.oocl.felix.client.dto.UserInfoDTO;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Service
public class ClientService {

    @Autowired
    private Oauth2ServerProperties oauth2ServerProperties;

    @Autowired
    private Oauth2ClientProperties oauth2ClientProperties;

    private RestTemplate restTemplate = new RestTemplate();

    private ClientUser currentUser = new ClientUser();

    public ClientUser getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        BeanUtils.copyProperties(principal, currentUser);
        return currentUser;
    }

    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("currentUser", getCurrentUser());
        return modelAndView;
    }

    public ModelAndView getEmail() {
        if (StringUtils.isEmpty(currentUser.getCode())) {
            return new ModelAndView("redirect:code");
        }
        if (StringUtils.isEmpty(currentUser.getAccessToken())) {
            return new ModelAndView("redirect:accesstoken");
        }
        return new ModelAndView("redirect:resource");
    }

    public ModelAndView getCode() {
        List<String> params = new ArrayList<>();
        params.add("client_id" + "=" + oauth2ClientProperties.getClientId());
        params.add("redirect_uri" + "=" + URLEncoder.encode(oauth2ClientProperties.getRedirectUri()));
        params.add("response_type" + "=" + oauth2ClientProperties.getResponseType());
        params.add("scope" + "=" + URLEncoder.encode(oauth2ClientProperties.getScope()));
        String authorizeUrl = oauth2ServerProperties.getAuthorizeUrl() + "?" + params.stream().reduce((a, b) -> a + "&" + b).get();
        return new ModelAndView("redirect:" + authorizeUrl);
    }

    public ModelAndView callback(String code) {
        currentUser.setCode(code);
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("currentUser", currentUser);
        return modelAndView;
    }

    public ModelAndView getAccessToken() throws UnsupportedEncodingException {
        String token = getAccessTokenFromAuthServer(currentUser.getCode());
        if (!StringUtils.isEmpty(token)) {
            currentUser.setAccessToken(token);
            ModelAndView modelAndView = new ModelAndView("index");
            modelAndView.addObject("currentUser", currentUser);
            return modelAndView;
        }
        throw new RuntimeException("Access-token is empty");
    }

    public String getAccessTokenFromAuthServer(String code) throws UnsupportedEncodingException {
        RequestEntity httpEntity = new RequestEntity<>(getHttpBody(code), getHttpHeaders(), HttpMethod.POST, URI.create(oauth2ServerProperties.getTokenUrl()));
        ResponseEntity<TokenDTO> exchange = restTemplate.exchange(httpEntity, TokenDTO.class);
        if (exchange.getStatusCode().is2xxSuccessful()) {
            return Objects.requireNonNull(exchange.getBody()).getAccessToken();
        }
        throw new RuntimeException("Failed to get access-token from auth-server");
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(oauth2ClientProperties.getClientId(), oauth2ClientProperties.getClientSecret());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

    private MultiValueMap<String, String> getHttpBody(String code) throws UnsupportedEncodingException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", oauth2ClientProperties.getRedirectUri());
        params.add("scope", oauth2ClientProperties.getScope());
        return params;
    }

    public ModelAndView getResource() {
        UserInfoDTO userInfoDTO = getUserInfoFromResourceServer();
        if (Objects.nonNull(userInfoDTO)) {
            ModelAndView modelAndView = new ModelAndView("index");
            currentUser.setEmail(userInfoDTO.getEmail());
            modelAndView.addObject("currentUser", currentUser);
            return modelAndView;
        }
        System.out.println("Failed to get resource, try to get code again...");
        return new ModelAndView("redirect:code");
    }

    private UserInfoDTO getUserInfoFromResourceServer() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getAccessToken());
        RequestEntity<MultiValueMap<String, String>> requestEntity
                = new RequestEntity<>(headers, HttpMethod.GET, URI.create("http://localhost:9090/user/WANGFE"));
        ResponseEntity<UserInfoDTO> exchange;
        try {
            exchange = restTemplate.exchange(requestEntity, UserInfoDTO.class);
        } catch (HttpClientErrorException exception) {
            return null;
        }
        return exchange.getStatusCode().is2xxSuccessful() ? exchange.getBody() : null;
    }

    public ModelAndView clear() {
        currentUser = new ClientUser();
        return new ModelAndView("redirect:index");
    }
}
