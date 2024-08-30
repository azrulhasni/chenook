/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */
@Entity
@Audited 
@EntityListeners(AuditingEntityListener.class)
public class WorkItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String creator;
    
    private Status status;
    
    private String tenant;
    
    private Priority priority;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) 
    @JoinColumn(name = "work_id", referencedColumnName = "id")
    private Set<Approval> approvals;
    
    @Embedded
    private WorkflowInfo workflowInfo;
    
    private String supervisorApprovalSeeker;

    private String supervisorApprovalLevel;
    
    @OneToMany(fetch = FetchType.LAZY) //do not cascade. Will create problem due to 2 fields pointing to the same type i.e. Approvals
    @JoinColumn(name = "hist_work_id", referencedColumnName = "id", nullable = true)
    private Set<Approval> historicalApprovals;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the tenant
     */
    public String getTenant() {
        return tenant;
    }

    /**
     * @param tenant the tenant to set
     */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    /**
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * @return the approvals
     */
    public Set<Approval> getApprovals() {
        return approvals;
    }

    /**
     * @param approvals the approvals to set
     */
    public void setApprovals(Set<Approval> approvals) {
        this.approvals = approvals;
    }

    /**
     * @return the workflowInfo
     */
    public WorkflowInfo getWorkflowInfo() {
        return workflowInfo;
    }

    /**
     * @param workflowInfo the workflowInfo to set
     */
    public void setWorkflowInfo(WorkflowInfo workflowInfo) {
        this.workflowInfo = workflowInfo;
    }

    /**
     * @return the supervisorApprovalSeeker
     */
    public String getSupervisorApprovalSeeker() {
        return supervisorApprovalSeeker;
    }

    /**
     * @param supervisorApprovalSeeker the supervisorApprovalSeeker to set
     */
    public void setSupervisorApprovalSeeker(String supervisorApprovalSeeker) {
        this.supervisorApprovalSeeker = supervisorApprovalSeeker;
    }

    /**
     * @return the supervisorApprovalLevel
     */
    public String getSupervisorApprovalLevel() {
        return supervisorApprovalLevel;
    }

    /**
     * @param supervisorApprovalLevel the supervisorApprovalLevel to set
     */
    public void setSupervisorApprovalLevel(String supervisorApprovalLevel) {
        this.supervisorApprovalLevel = supervisorApprovalLevel;
    }

    /**
     * @return the historicalApprovals
     */
    public Set<Approval> getHistoricalApprovals() {
        return historicalApprovals;
    }

    /**
     * @param historicalApprovals the historicalApprovals to set
     */
    public void setHistoricalApprovals(Set<Approval> historicalApprovals) {
        this.historicalApprovals = historicalApprovals;
    }
    
}
