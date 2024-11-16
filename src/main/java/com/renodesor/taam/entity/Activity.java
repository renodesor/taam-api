package com.renodesor.taam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Activity extends BasicEntity {
    @ManyToOne(fetch = LAZY)
    //@JoinColumn(name ="UUID", referencedColumnName = "UUID")
    private Category category;
    @NotNull
    private String name;
    private String description;
}
