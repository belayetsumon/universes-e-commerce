package com.ecommerce.app.module.user.model;

import com.ecommerce.app.model.*;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.*;
import java.util.*;
import org.springframework.data.jpa.domain.support.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "usermodule_users")
public class Users implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(name = "firstName")
    @NotBlank(message = "*Please provide your first name")
    private String firstName;

    private String lastName;

    @Column(name = "email", unique = true)
    @NotBlank(message = "*Please provide your email")
    @Email
    private String email;

    @NotBlank(message = "*Please provide your mobile")
    private String mobile;

    @Column(length = 60)
    private String password;

    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_id")
    Users parent;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> role = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Lob
    private String remarks;

    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "last_loginout")
    private Date lastLogOut;

    /**
     * ***************** Start Auditor ********************************
     */
    //@Version
    @Column(name = "version")
    private long version = 1L;

    @Column(name = "created_on", nullable = false, insertable = true, updatable = false)
    //  @CreatedDate
    private Date createdOn = new Date();

    @Column(name = "created_by", insertable = true, updatable = false)
    // @CreatedBy
    private String createdBy;

    @Column(name = "updated_on", insertable = false, updatable = true)
    //@LastModifiedDate
    private Date updatedOn;

    @Column(name = "updated_by", insertable = false, updatable = true)
    //  @LastModifiedBy
    private String updatedBy;

//    @OneToOne(mappedBy = "userId")
//    public Profile profile;
    @OneToOne(mappedBy = "users")
    public Wallet wallet;

    @OneToOne(mappedBy = "users")
    public Referral referral;

    @OneToOne(mappedBy = "userId")
    public ProfileImage profileImage;

    public Users() {
    }

    public Users(Long id, String firstName, String lastName, String email, String mobile, String password, Users parent, Status status, UserType userType, String remarks, Date lastLogin, Date lastLogOut, String createdBy, Date updatedOn, String updatedBy, Wallet wallet, Referral referral, ProfileImage profileImage) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.parent = parent;
        this.status = status;
        this.userType = userType;
        this.remarks = remarks;
        this.lastLogin = lastLogin;
        this.lastLogOut = lastLogOut;
        this.createdBy = createdBy;
        this.updatedOn = updatedOn;
        this.updatedBy = updatedBy;
        this.wallet = wallet;
        this.referral = referral;
        this.profileImage = profileImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Users getParent() {
        return parent;
    }

    public void setParent(Users parent) {
        this.parent = parent;
    }

    public Set<Role> getRole() {
        return role;
    }

    public void setRole(Set<Role> role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastLogOut() {
        return lastLogOut;
    }

    public void setLastLogOut(Date lastLogOut) {
        this.lastLogOut = lastLogOut;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

}
