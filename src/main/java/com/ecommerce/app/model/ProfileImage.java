/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


/**
 *
 * @author User
 */
@Entity
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "*Government Id cannot be blank.")
    @OneToOne(optional = false)
    private Users userId;

    private String imageName;

    public ProfileImage() {
    }

    public ProfileImage(Long id, Users userId, String imageName) {
        this.id = id;
        this.userId = userId;
        this.imageName = imageName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    

}
