package com.kalibyte.foundry.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role", schema = "public")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
