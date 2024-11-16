package com.renodesor.taam.repository;

import com.renodesor.taam.entity.Oauth2ClientRegistered;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Oauth2ClientRegisteredRepository extends JpaRepository<Oauth2ClientRegistered, String> {
}
