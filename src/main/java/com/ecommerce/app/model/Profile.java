/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author User
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User cannot be blank.")
    @OneToOne(optional = true)
    private Users userId;

    @Column(name = "name")
    @NotBlank(message = "*Please provide your name")
    private String name;

    @NotBlank(message = "*Please provide your fathers name")
    private String fathersName;

    @NotBlank(message = "*Please provide your mothers name")
    private String mothersName;

    @NotBlank(message = "*Please provide your nid name")
    private String nid;

    @NotBlank(message = "*Please provide your education qualification")
    private String lastEducationQualification;

    private String occupation;

    private String designation;

    private String organization;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull(message = "*Please provide your blood group")
    @ManyToOne(optional = true)
    private BloodGroup bloodGroup;

    @NotBlank(message = "*Please provide your name")
    private String mobile;

    @NotBlank(message = "*Please provide your present addresss")
    private String presentAddress;

    @NotBlank(message = "*Please provide your permanent addresss")
    private String permanentAddress;

    @NotBlank(message = "*Please provide your present city")
    private String presentCity;

    @NotBlank(message = "*Please provide your country")
    private String presentCountry;

    @NotBlank(message = "*Please provide your nominee name")
    private String nominee;
    
        @NotBlank(message = "*Please provide your nominee name")
    private String nomineeRelation;

    private String facebookUrl;

    private String linkdinUrl;

    private String whatupNo;

    //    /// Audit /// 
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modified;

    /// End Audit //// 
    public Profile() {
    }

    public Profile(Long id, Users userId, String name, String fathersName, String mothersName, String nid, String lastEducationQualification, String occupation, String designation, String organization, Gender gender, BloodGroup bloodGroup, String mobile, String presentAddress, String permanentAddress, String presentCity, String presentCountry, String nominee, String nomineeRelation, String facebookUrl, String linkdinUrl, String whatupNo, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.fathersName = fathersName;
        this.mothersName = mothersName;
        this.nid = nid;
        this.lastEducationQualification = lastEducationQualification;
        this.occupation = occupation;
        this.designation = designation;
        this.organization = organization;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.mobile = mobile;
        this.presentAddress = presentAddress;
        this.permanentAddress = permanentAddress;
        this.presentCity = presentCity;
        this.presentCountry = presentCountry;
        this.nominee = nominee;
        this.nomineeRelation = nomineeRelation;
        this.facebookUrl = facebookUrl;
        this.linkdinUrl = linkdinUrl;
        this.whatupNo = whatupNo;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFathersName() {
        return fathersName;
    }

    public void setFathersName(String fathersName) {
        this.fathersName = fathersName;
    }

    public String getMothersName() {
        return mothersName;
    }

    public void setMothersName(String mothersName) {
        this.mothersName = mothersName;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getLastEducationQualification() {
        return lastEducationQualification;
    }

    public void setLastEducationQualification(String lastEducationQualification) {
        this.lastEducationQualification = lastEducationQualification;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPresentAddress() {
        return presentAddress;
    }

    public void setPresentAddress(String presentAddress) {
        this.presentAddress = presentAddress;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getPresentCity() {
        return presentCity;
    }

    public void setPresentCity(String presentCity) {
        this.presentCity = presentCity;
    }

    public String getPresentCountry() {
        return presentCountry;
    }

    public void setPresentCountry(String presentCountry) {
        this.presentCountry = presentCountry;
    }

    public String getNominee() {
        return nominee;
    }

    public void setNominee(String nominee) {
        this.nominee = nominee;
    }

    public String getNomineeRelation() {
        return nomineeRelation;
    }

    public void setNomineeRelation(String nomineeRelation) {
        this.nomineeRelation = nomineeRelation;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getLinkdinUrl() {
        return linkdinUrl;
    }

    public void setLinkdinUrl(String linkdinUrl) {
        this.linkdinUrl = linkdinUrl;
    }

    public String getWhatupNo() {
        return whatupNo;
    }

    public void setWhatupNo(String whatupNo) {
        this.whatupNo = whatupNo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

   

}
