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

    public UserDetails getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    public ModelAndView getUserInfoPage() {
        BeanUtils.copyProperties(getCurrentUser(), currentUser);
        if (Objects.nonNull(currentUser) && StringUtils.isEmpty(currentUser.getAccessToken())) {
            return new ModelAndView("redirect:" + getAuthorizeUrl());
        }
        ModelAndView modelAndView = new ModelAndView("user-info");
        getUserInfoFromResourceServer(modelAndView, currentUser);
        currentUser.setAccessToken(null);
        return modelAndView;
    }

    private void getUserInfoFromResourceServer(ModelAndView modelAndView, ClientUser currentUser) {
        //正常请求资源服务器，获取用户信息
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getAccessToken());
        RequestEntity<MultiValueMap<String, String>> requestEntity
                = new RequestEntity<>(headers, HttpMethod.GET, URI.create("http://localhost:9090/user/WANGFE"));
        ResponseEntity<UserInfoDTO> exchange = null;
        try {
            //尝试访问资源
            exchange = restTemplate.exchange(requestEntity, UserInfoDTO.class);
        } catch (HttpClientErrorException exception) {
            //未认证会报错，重定向到授权页面，获取新token
            modelAndView.setViewName("redirect:" + getAuthorizeUrl());
            return;
        }
        assert exchange != null;
        if (exchange.getStatusCode().is2xxSuccessful()) {
            UserInfoDTO body = exchange.getBody();
            modelAndView.addObject("currentLoginUsername", currentUser.getUsername());
            modelAndView.addObject("user", body);
        }
    }

    private String getAuthorizeUrl() {
        List<String> params = new ArrayList<>();
        params.add("client_id" + "=" + oauth2ClientProperties.getClientId());
        params.add("redirect_uri" + "=" + URLEncoder.encode(oauth2ClientProperties.getRedirectUri()));
        params.add("response_type" + "=" + oauth2ClientProperties.getResponseType());
        params.add("scope" + "=" + URLEncoder.encode(oauth2ClientProperties.getScope()));
        return oauth2ServerProperties.getAuthorizeUrl() + "?" + params.stream().reduce((a, b) -> a + "&" + b).get();
    }

    public ModelAndView callback(String code) throws UnsupportedEncodingException {
        String token = getToken(code);
        if (!StringUtils.isEmpty(token)) {
            currentUser.setAccessToken(token);
            return new ModelAndView("redirect:/user-info");
        }
        throw new RuntimeException("请求超时");
    }

    public String getToken(String code) throws UnsupportedEncodingException {
        RequestEntity httpEntity = new RequestEntity<>(getHttpBody(code), getHttpHeaders(), HttpMethod.POST, URI.create(oauth2ServerProperties.getTokenUrl()));
        ResponseEntity<TokenDTO> exchange = restTemplate.exchange(httpEntity, TokenDTO.class);
        if (exchange.getStatusCode().is2xxSuccessful()) {
            return Objects.requireNonNull(exchange.getBody()).getAccessToken();
        }
        throw new RuntimeException("请求令牌失败！");
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
}
