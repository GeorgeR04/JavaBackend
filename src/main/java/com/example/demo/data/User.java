package com.example.demo.data;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "Firstname", nullable = false, length = 50)
    private String firstname;

    @Column(name = "Lastname", nullable = false, length = 50)
    private String lastname;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "passworld", nullable = false, length = 64)
    private String password;

    @Column(name = "authkey", nullable = false)
    private Integer authKey;

}