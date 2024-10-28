/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 *
 * @author Md Belayet Hossin
 */
@Entity
@Table(name = "usermodule_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "This field cannot be blank.")
    public String name;

    @NotEmpty(message = "This field cannot be blank.")
    public String slug;

    @ManyToMany(mappedBy = "role")
    private Set<Users> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usermodule_role_privilege",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id"))
    private Set<Privilege> privilege;

    public Role(Long id, String name, String slug, Set<Users> users, Set<Privilege> privilege) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.users = users;
        this.privilege = privilege;
    }

    public Role() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Set<Users> getUsers() {
        return users;
    }

    public void setUsers(Set<Users> users) {
        this.users = users;
    }

    public Set<Privilege> getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Set<Privilege> privilege) {
        this.privilege = privilege;
    }

}
