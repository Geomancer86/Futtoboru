package com.rndmodgames.futtoboru.data.jobs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Profession;

/**
 * Job Opening v1
 * 
 * Represents an open position at a club that unemployed players can apply for.
 * 
 * @author Geomancer86
 */
public class JobOpening implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Club offering the position
     */
    private Long clubId; // Store ID to avoid circular references in serialization
    
    /**
     * Profession/Position type
     */
    private Long professionId; // Store ID to avoid circular references
    
    /**
     * Dates
     */
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    
    /**
     * Status
     */
    private JobStatus status; // OPEN, FILLED, EXPIRED
    
    /**
     * Compensation (v1.0: basic)
     */
    private BigDecimal salary;
    private Integer contractLengthMonths; // Default: 12 months
    
    /**
     * Requirements (v1.0: simple - only reputation)
     */
    private Integer minReputation; // null = no requirement, range 0-100
    
    /**
     * Applicants
     */
    private List<Long> applicationIds = new ArrayList<>(); // Store IDs to avoid circular references
    
    /**
     * Selected candidate (if filled)
     */
    private Long selectedCandidateId; // null if not filled
    
    // Constructors
    public JobOpening() {
        this.status = JobStatus.OPEN;
        this.contractLengthMonths = 12; // Default 1 year
    }
    
    public JobOpening(Club club, Profession profession) {
        this();
        this.clubId = club.getId();
        this.professionId = profession.getId();
        this.postedDate = LocalDateTime.now();
        // Default deadline: 30 days from posting
        this.applicationDeadline = this.postedDate.plusDays(JobConstants.JOB_OPENING_DEFAULT_DEADLINE_DAYS);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getClubId() {
        return clubId;
    }
    
    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }
    
    public Long getProfessionId() {
        return professionId;
    }
    
    public void setProfessionId(Long professionId) {
        this.professionId = professionId;
    }
    
    public LocalDateTime getPostedDate() {
        return postedDate;
    }
    
    public void setPostedDate(LocalDateTime postedDate) {
        this.postedDate = postedDate;
    }
    
    public LocalDateTime getApplicationDeadline() {
        return applicationDeadline;
    }
    
    public void setApplicationDeadline(LocalDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    
    public JobStatus getStatus() {
        return status;
    }
    
    public void setStatus(JobStatus status) {
        this.status = status;
    }
    
    public BigDecimal getSalary() {
        return salary;
    }
    
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
    
    public Integer getContractLengthMonths() {
        return contractLengthMonths;
    }
    
    public void setContractLengthMonths(Integer contractLengthMonths) {
        this.contractLengthMonths = contractLengthMonths;
    }
    
    public Integer getMinReputation() {
        return minReputation;
    }
    
    public void setMinReputation(Integer minReputation) {
        this.minReputation = minReputation;
    }
    
    public List<Long> getApplicationIds() {
        if (applicationIds == null) {
            applicationIds = new ArrayList<>();
        }
        return applicationIds;
    }
    
    public void setApplicationIds(List<Long> applicationIds) {
        this.applicationIds = applicationIds;
    }
    
    public Long getSelectedCandidateId() {
        return selectedCandidateId;
    }
    
    public void setSelectedCandidateId(Long selectedCandidateId) {
        this.selectedCandidateId = selectedCandidateId;
    }
    
    /**
     * Utility: Check if opening is still accepting applications
     */
    public boolean isAcceptingApplications() {
        return status == JobStatus.OPEN && 
               applicationDeadline != null &&
               applicationDeadline.isAfter(LocalDateTime.now());
    }
    
    /**
     * Utility: Check if opening has expired
     */
    public boolean isExpired() {
        return applicationDeadline != null &&
               applicationDeadline.isBefore(LocalDateTime.now()) && 
               status == JobStatus.OPEN;
    }
}

