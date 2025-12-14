package com.rndmodgames.futtoboru.data.jobs;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Person;

/**
 * Job Application v1
 * 
 * Represents a player's application for a job opening.
 * 
 * @author Geomancer86
 */
public class JobApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Applicant
     */
    private Long applicantId; // Store ID to avoid circular references
    
    /**
     * Job opening
     */
    private Long jobOpeningId; // Store ID to avoid circular references
    
    /**
     * Application date
     */
    private LocalDateTime applicationDate;
    
    /**
     * Status
     */
    private ApplicationStatus status; // PENDING, REJECTED, OFFER_RECEIVED, ACCEPTED, WITHDRAWN
    
    /**
     * Match percentage (how well applicant matches requirements)
     * v1.0: Simple calculation based on reputation
     */
    private Integer matchPercentage; // 0-100
    
    // Constructors
    public JobApplication() {
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDateTime.now();
    }
    
    public JobApplication(Person applicant, com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening) {
        this();
        this.applicantId = applicant.getId();
        this.jobOpeningId = jobOpening.getId();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getApplicantId() {
        return applicantId;
    }
    
    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }
    
    public Long getJobOpeningId() {
        return jobOpeningId;
    }
    
    public void setJobOpeningId(Long jobOpeningId) {
        this.jobOpeningId = jobOpeningId;
    }
    
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
    
    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public Integer getMatchPercentage() {
        return matchPercentage;
    }
    
    public void setMatchPercentage(Integer matchPercentage) {
        this.matchPercentage = matchPercentage;
    }
    
    /**
     * Utility: Check if application is still active
     */
    public boolean isActive() {
        return status == ApplicationStatus.PENDING || 
               status == ApplicationStatus.OFFER_RECEIVED;
    }
}

