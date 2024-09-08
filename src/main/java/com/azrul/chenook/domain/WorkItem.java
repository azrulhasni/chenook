/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
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

    @Column(nullable = false, unique=true)
    private Long parentId;

    private String context;

    private String startEventId;

    private String startEventDescription;

    private Set<String> owners;

    private String worklist;

    private LocalDateTime worklistUpdateTime;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "work_id", referencedColumnName = "id")
    private Set<Approval> approvals = new HashSet<>();

    private String supervisorApprovalSeeker;

    private String supervisorApprovalLevel;

    @NotAudited
    @OneToMany(fetch = FetchType.LAZY) //do not cascade. Will create problem due to 2 fields pointing to the same type i.e. Approvals
    @JoinColumn(name = "hist_work_id", referencedColumnName = "id", nullable = true)
    private Set<Approval> historicalApprovals = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name="WorkField",
        joinColumns=@JoinColumn(name="work_id"))
    @MapKeyColumn(name="key")
    @Column(name="value")
    private Map<String, String> fields = new HashMap<>();

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

    /**
     * @return the parentId
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @return the startEventId
     */
    public String getStartEventId() {
        return startEventId;
    }

    /**
     * @param startEventId the startEventId to set
     */
    public void setStartEventId(String startEventId) {
        this.startEventId = startEventId;
    }

    /**
     * @return the startEventDescription
     */
    public String getStartEventDescription() {
        return startEventDescription;
    }

    /**
     * @param startEventDescription the startEventDescription to set
     */
    public void setStartEventDescription(String startEventDescription) {
        this.startEventDescription = startEventDescription;
    }

    /**
     * @return the owners
     */
    public Set<String> getOwners() {
        return owners;
    }

    /**
     * @param owners the owners to set
     */
    public void setOwners(Set<String> owners) {
        this.owners = owners;
    }

    /**
     * @return the worklist
     */
    public String getWorklist() {
        return worklist;
    }

    /**
     * @param worklist the worklist to set
     */
    public void setWorklist(String worklist) {
        this.worklist = worklist;
    }

    /**
     * @return the worklistUpdateTime
     */
    public LocalDateTime getWorklistUpdateTime() {
        return worklistUpdateTime;
    }

    /**
     * @param worklistUpdateTime the worklistUpdateTime to set
     */
    public void setWorklistUpdateTime(LocalDateTime worklistUpdateTime) {
        this.worklistUpdateTime = worklistUpdateTime;
    }

    /**
     * @return the fields
     */
    public Map<String, String> getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

}
