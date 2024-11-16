package com.renodesor.taam.security;

import com.renodesor.taam.entity.TaamUser;
import com.renodesor.taam.repository.TaamUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class OauthSecurityConfiguration {

    private final TaamUserRepository taamUserRepository;
    private final TaamUser taamUser;

    @Value("${spring.security.oauth2.client.provider.taam.issuer-uri}")

    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.taam.client-id}")

    private String clientId;

    @Value("${spring.security.oauth2.client.registration.taam.client-secret}")

    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.taam.token-uri}")

    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.taam.redirect-uri}")

    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.taam.scope}")

    private String scope;

    @Value("${spring.security.oauth2.client.provider.taam.authorization-uri}")

    private String authUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")

    private String jwkSetUri;

    @Value("${allowed-origins-uri}")

    private String originsUri;

    @Bean

    @Order(Ordered.HIGHEST_PRECEDENCE)

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth

                .requestMatchers("/oauth2/*", "/swagger-ui/", "/v3/", "/public/", "/whitelabeling/*").permitAll()

                .anyRequest()

                .authenticated());

        http.csrf(AbstractHttpConfigurer::disable);


        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtConverter(taamUserRepository, taamUser)))
                );


        return http.build();

    }

    private ClientRegistration clientRegistration() {

        return ClientRegistration.withRegistrationId(clientId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri(redirectUri)
                .scope(scope)
                .clientName(clientId).authorizationUri(authUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri).issuerUri(issuerUri)
                .build();
    }


    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(JdbcTemplate jdbcTemplate) {
        JdbcClientRegistrationRepository clientRegistrations = new JdbcClientRegistrationRepository(jdbcTemplate);
        clientRegistrations.save(clientRegistration());
        return clientRegistrations;

    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, oAuth2AuthorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;

    }

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                          OAuth2AuthorizedClientService authorizedClientService,
                                                          OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> tokenResponseClient,
                                                          OAuth2AuthorizationSuccessHandler authorizationSuccessHandler,
                                                          OAuth2AuthorizationFailureHandler authorizationFailureHandler) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(r -> r.accessTokenResponseClient(tokenResponseClient)).clientCredentials().build();

        var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        authorizedClientManager.setAuthorizationSuccessHandler(authorizationSuccessHandler);
        authorizedClientManager.setAuthorizationFailureHandler(authorizationFailureHandler);
        return authorizedClientManager;

    }

    @Bean
    OAuth2AuthorizationSuccessHandler authorizationSuccessHandler(
            OAuth2AuthorizedClientService authorizedClientService) {
        return (authorizedClient, principal, attributes) -> {
            log.info("Authorization successful for clientRegistrationId={}, tokenUri-{}",
                    authorizedClient.getClientRegistration().getRegistrationId(),
                    authorizedClient.getClientRegistration().getProviderDetails().getTokenUri());
            authorizedClientService.saveAuthorizedClient(authorizedClient, principal);
        };

    }

    @Bean
    OAuth2AuthorizationFailureHandler authorizationFailureHandler(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, attributes) -> {
                    log.info("Authorization failed for clientRegistrationId={}", clientRegistrationId);
                    authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName());
                });
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> tokenResponseClient() {
        return new DefaultClientCredentialsTokenResponseClient();
    }

    @Bean
    WebClient authenticatedWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.setAllowedOrigins(List.of(originsUri));
        corsConfig.setAllowedHeaders(List.of("Origin", "Access-Control-Allow-Origin", "Content-Type",
                "Accept", "Jwt-Token", "Authorization", "Origin, Accept", "X-Requested-With",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        corsConfig.setExposedHeaders(List.of("Origin", "Content-Type", "Accept", "Jwt-Token", "Authorization",
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Filename"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}