package com.renodesor.taam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Oauth2ClientRegistered {
    @Id
    private String registrationId;
    private String clientId;
    private String clientSecret;
    private String clientAuthenticationMethod;
    private String authorizationGrantType;
    private String clientName;
    private String redirectUri;
    private String scopes;
    private String authorizationUri;
    private String tokenUri;
    private String jwkSetUri;
    private String issuerUri;
    private String userInfoUri;
    private String userInfoAuthenticationMethod;
    private String userNameAttributeName;
    private String configurationMetadata;
}
