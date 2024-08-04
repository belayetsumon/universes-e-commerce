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
@Table(name="usermodule_application_privilege")
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Modules module;

    @NotEmpty(message = "*Name cannot be blank.")
    public String name;

    @NotEmpty(message = "*Slug cannot be blank.")
    public String slug;

    @ManyToMany(mappedBy = "privilege")
    private Set<Role> role;

    public Privilege() {
    }

    public Privilege(Long id, Modules module, String name, String slug, Set<Role> role) {
        this.id = id;
        this.module = module;
        this.name = name;
        this.slug = slug;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Modules getModule() {
        return module;
    }

    public void setModule(Modules module) {
        this.module = module;
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

    public Set<Role> getRole() {
        return role;
    }

    public void setRole(Set<Role> role) {
        this.role = role;
    }

}
