package com.renodesor.taam.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TaamUser extends BasicEntity {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
