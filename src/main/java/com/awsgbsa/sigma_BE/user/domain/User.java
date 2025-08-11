package com.awsgbsa.sigma_BE.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
@Table(name = "sigma_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String userName;

    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;

    private LocalDateTime createdAt;

    @Column(name = "profile_url", columnDefinition = "TEXT")
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private SubscriptStatus subscriptStatus;
}
