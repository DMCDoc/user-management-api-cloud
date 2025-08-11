package com.example.usermanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "roles") 
@Data @NoArgsConstructor 
@AllArgsConstructor 
@Builder

public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // ex: ROLE_USER, ROLE_ADMIN
}
