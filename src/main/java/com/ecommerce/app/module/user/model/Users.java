package com.ecommerce.app.module.user.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.*;
import java.util.*;
import org.springframework.data.jpa.domain.support.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "usermodule_users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Users implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(name = "firstName")
    @NotBlank(message = "*Please provide your first name")
    @Size(min = 3, max = 50, message = "First name must be 3-50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)*$",
            message = "First name can only contain letters, numbers, single spaces, underscores, or hyphens, and cannot start or end with a space"
    )
    private String firstName;

    @Column(name = "lastName")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+(?: [a-zA-Z0-9_-]+)*$",
            message = "Last name can only contain letters, numbers, single spaces, underscores, or hyphens"
    )
    private String lastName;

    @Column(name = "email", unique = true)
    @NotBlank(message = "*Please provide your email")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "*Please provide your mobile")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number")
    @Column(nullable = false, unique = true)
    private String mobile;

    @Column(length = 60)
    @JsonIgnore
    private String password;

    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_id")
    //@JsonBackReference // parent is "back" side
    Users parent;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    // @JsonManagedReference // roles are "forward" side
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
    @JsonIgnore
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
//    @OneToOne(mappedBy = "users")
//    public Wallet wallet;
//
//    @OneToOne(mappedBy = "users")
//    public Referral referral;
//
//    @OneToOne(mappedBy = "userId")
//    public ProfileImage profileImage;
    public Users() {
    }

    public Users(Long id, String firstName, String lastName, String email, String mobile, String password, Users parent, Status status, UserType userType, String remarks, Date lastLogin, Date lastLogOut, String createdBy, Date updatedOn, String updatedBy) {
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

}
