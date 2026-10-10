package com.martini.fish_gen.model.dao;


import com.martini.fish_gen.model.enumeration.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users") // "user" is a reserved word in many DBs
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUser {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false)
    private String username;

    @Getter
    @Setter
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Setter(value = AccessLevel.PRIVATE)
    private String roles;

    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardCollection> collections = new ArrayList<>();

    public Set<Role> getRoles() {
        return Arrays.stream(roles.split(","))
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public void removeRole(Role role) {
        String updatedRoles = getRoles().
                stream()
                .filter(r -> r != role)
                .map(Enum::name)
                .collect(Collectors.joining(","));
        setRoles(updatedRoles);
    }

    public void addRole(Role role) {
        Set<Role> roles = getRoles();
        roles.add(role);
        setRoles(roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(",")));
    }


}
