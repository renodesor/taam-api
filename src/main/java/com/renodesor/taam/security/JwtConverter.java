package com.renodesor.taam.security;

import com.renodesor.taam.entity.TaamUser;
import com.renodesor.taam.repository.TaamUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static com.renodesor.taam.utils.Constants.*;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final TaamUserRepository taamUserRepository;
    private final TaamUser taamUser;

    private static Collection<? extends GrantedAuthority> translateAuthorities(final Jwt jwt) {

        Collection<String> userRoles = jwt.getClaimAsStringList("roles");
        if (userRoles != null)

            return userRoles
                    .stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" +
                            role.toUpperCase()))
                    .collect(toSet());
        return Collections.emptySet();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        TaamUser taamUserLocal = taamUserRepository.findByEmail(source.getClaim(EMAIL)).orElse(null);
        boolean canSave;
        if (taamUserLocal == null) {
            log.info("Adding new user after successful login: {}", source.getClaims());
            taamUserLocal = new TaamUser();
            setTaamUserDetails(taamUserLocal, source);
            setTaamUserDetails(this.taamUser, source);

        }
        canSave = updateTaamUserDetail(source, taamUserLocal);
        updateTaamUserDetail(source, taamUser);

        if (canSave) taamUserRepository.save(taamUserLocal);
        return new JwtAuthenticationToken(source, Stream.concat(new JwtGrantedAuthoritiesConverter().convert(source)
                        .stream(), translateAuthorities(source).stream())
                .collect(toSet()));
        }

        private boolean updateTaamUserDetail(Jwt source, TaamUser taamUser) {
            log.info("Update existing user after successful login: {}", source.getClaims());
            boolean canSave = false;
            if (taamUser.getFirstName() == null || !taamUser.getFirstName().equalsIgnoreCase(source.getClaim(GIVEN_NAME))) {
                taamUser.setFirstName(source.getClaim(GIVEN_NAME));
                canSave = true;
            }
            if (taamUser.getLastName() ==null || !taamUser.getLastName().equalsIgnoreCase(source.getClaim(FAMILY_NAME))) {
                taamUser.setLastName(source.getClaim(FAMILY_NAME));
                canSave = true;
            }
            if (taamUser.getUsername() == null || !taamUser.getUsername().equalsIgnoreCase(source.getClaim(USERNAME))) {
                taamUser.setUsername(source.getClaim(USERNAME));
                canSave = true;
            }
            if(taamUser.getId() == null) {
                taamUser.setId(UUID.fromString(source.getClaim(SUB)));
            }
            if(taamUser.getCreatedBy() == null) {
                taamUser.setCreatedBy(source.getClaim(USERNAME));
            }
            if(taamUser.getUpdatedOn() != null) {
               taamUser.setCreatedBy(source.getClaim(USERNAME));
            }
            if(taamUser.getEmail() == null) {
                taamUser.setEmail(source.getClaim(EMAIL));
            }
            if(canSave) {
                taamUser.setUpdatedBy(source.getClaim(USERNAME));
                taamUser.setUpdatedOn( LocalDateTime.now());
            }
            return canSave;
        }

        private void setTaamUserDetails(TaamUser taamUser, Jwt source) {
            taamUser.setId(UUID.fromString(source.getClaim(SUB)));
            taamUser.setFirstName(source.getClaim(GIVEN_NAME));
            taamUser.setLastName(source.getClaim(FAMILY_NAME));
            taamUser.setEmail(source.getClaim(EMAIL));
            taamUser.setUsername(source.getClaim(USERNAME));
            taamUser.setCreatedBy(source.getClaim(USERNAME));
            taamUser.setCreatedOn( LocalDateTime.now());
        }

}
