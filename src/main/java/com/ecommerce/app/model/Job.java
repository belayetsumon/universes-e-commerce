/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.model;


import com.ecommerce.app.model.enumvalue.Status;
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
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Job category cannot be blank.")
    @ManyToOne(optional = true)
    private Jobcategory jobcategory;

    @NotBlank(message = "Title  is required.")
    private String title;

    @NotNull
    private int vacancy;

    @Lob
    @NotBlank(message = "Responsibilities  is required.")
    private String responsibilities;

    @Lob
    @NotBlank(message = "Employment Status  is required.")
    private String employmentstatus;

    @Lob
    @NotBlank(message = "Educational Requirements  is required.")
    private String educationalrequirements;

    @Lob
    @NotBlank(message = "Experience Requirements  is required.")
    private String experiencerequirements;

    @Lob
    @NotBlank(message = "Additional Requirements Requirements  is required.")
    private String additionalrequirements;

    @Lob
    @NotBlank(message = "Job Location  is required.")
    private String joblocation;

    @Lob
    @NotBlank(message = "Salary  is required.")
    private String salary;

    @Lob
    @NotBlank(message = " Compensation & Other Benefits  is required.")
    private String benefits;

    @Lob
    @NotBlank(message = "Read Before Apply is required.")
    private String readbeforeapply;

    @Lob
    @NotBlank(message = "Apply Procedure is required.")
    private String applyprocedure;

    @NotBlank(message = "Application Deadline is required.")
    private String applicationdeadline;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    private Status status;

/// Audit /// 
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

    public Job(Long id, Jobcategory jobcategory, String title, int vacancy, String responsibilities, String employmentstatus, String educationalrequirements, String experiencerequirements, String additionalrequirements, String joblocation, String salary, String benefits, String readbeforeapply, String applyprocedure, String applicationdeadline, Status status, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.jobcategory = jobcategory;
        this.title = title;
        this.vacancy = vacancy;
        this.responsibilities = responsibilities;
        this.employmentstatus = employmentstatus;
        this.educationalrequirements = educationalrequirements;
        this.experiencerequirements = experiencerequirements;
        this.additionalrequirements = additionalrequirements;
        this.joblocation = joblocation;
        this.salary = salary;
        this.benefits = benefits;
        this.readbeforeapply = readbeforeapply;
        this.applyprocedure = applyprocedure;
        this.applicationdeadline = applicationdeadline;
        this.status = status;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    public Job() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Jobcategory getJobcategory() {
        return jobcategory;
    }

    public void setJobcategory(Jobcategory jobcategory) {
        this.jobcategory = jobcategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVacancy() {
        return vacancy;
    }

    public void setVacancy(int vacancy) {
        this.vacancy = vacancy;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public String getEmploymentstatus() {
        return employmentstatus;
    }

    public void setEmploymentstatus(String employmentstatus) {
        this.employmentstatus = employmentstatus;
    }

    public String getEducationalrequirements() {
        return educationalrequirements;
    }

    public void setEducationalrequirements(String educationalrequirements) {
        this.educationalrequirements = educationalrequirements;
    }

    public String getExperiencerequirements() {
        return experiencerequirements;
    }

    public void setExperiencerequirements(String experiencerequirements) {
        this.experiencerequirements = experiencerequirements;
    }

    public String getAdditionalrequirements() {
        return additionalrequirements;
    }

    public void setAdditionalrequirements(String additionalrequirements) {
        this.additionalrequirements = additionalrequirements;
    }

    public String getJoblocation() {
        return joblocation;
    }

    public void setJoblocation(String joblocation) {
        this.joblocation = joblocation;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getReadbeforeapply() {
        return readbeforeapply;
    }

    public void setReadbeforeapply(String readbeforeapply) {
        this.readbeforeapply = readbeforeapply;
    }

    public String getApplyprocedure() {
        return applyprocedure;
    }

    public void setApplyprocedure(String applyprocedure) {
        this.applyprocedure = applyprocedure;
    }

    public String getApplicationdeadline() {
        return applicationdeadline;
    }

    public void setApplicationdeadline(String applicationdeadline) {
        this.applicationdeadline = applicationdeadline;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
