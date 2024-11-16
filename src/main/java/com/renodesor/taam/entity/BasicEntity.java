package com.renodesor.taam.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BasicEntity  implements Serializable {
    @NotNull
    @Id
    private UUID id = UUID.randomUUID();
    @NotNull
    private String createdBy;
    @NotNull
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
}
