package com.renodesor.taam.repository;

import com.renodesor.taam.entity.TaamUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaamUserRepository extends JpaRepository<TaamUser, UUID> {

    Optional<TaamUser> findByEmail(String email);

    Optional<List<TaamUser>> findByUsernameIsNotNull();
}
