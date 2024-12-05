/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import com.azrul.chenook.annotation.NotEmpty;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.converter.LocalDateTimeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.ValueConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */

@Entity
@Table(name = "work_item")
@Document(indexName = "workitem")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type",
        discriminatorType = DiscriminatorType.STRING)
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class WorkItem {

   
    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @WorkField(displayName = "Id", sortable = true)
    protected Long id;

    @WorkField(displayName = "Creator")
    protected String creator;

    @WorkField(displayName = "Tenant")
    protected String tenant;

    @WorkField(displayName = "Priority")
    protected Priority priority;

    @WorkField(displayName = "Context")
    protected String context;

    protected String startEventId;

    @WorkField(displayName = "Start Event")
    protected String startEventDescription;
    
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time_no_millis)
    @WorkField(displayName = "Start date")
    private ZonedDateTime startDate;
    
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time_no_millis)
    @WorkField(displayName = "End date")
    private ZonedDateTime endDate;
    
  
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true/*, mappedBy="workItem"*/)
    @JoinColumn(name = "work_id", referencedColumnName = "id")
    protected Set<BizUser> owners;

    @WorkField(displayName = "Worklist")
    protected String worklist;

    @ValueConverter(LocalDateTimeConverter.class)
    @WorkField(displayName = "Worklist update time")
    protected LocalDateTime worklistUpdateTime;

    @WorkField(displayName = "Workflow")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL/*, mappedBy="workItem"*/)
    @JoinColumn(name = "work_id", referencedColumnName = "id")
    protected Set<Approval> approvals = new HashSet<>();

    protected String supervisorApprovalSeeker;

    protected String supervisorApprovalLevel;

    @NotAudited //cannot be auditted. if not, WorkItem.id (hist_work_id) will be compulsorry when creating audit and when there is no historical approval, it should not
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL/*, mappedBy="workItem"*/) //do not cascade. Will create problem due to 2 fields pointing to the same type i.e. Approvals
    @JoinColumn(name = "hist_work_id", referencedColumnName = "id", nullable = true)
    protected Set<Approval> historicalApprovals = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name="WorkField",
        joinColumns=@JoinColumn(name="work_id"))
    @MapKeyColumn(name="key")
    @Column(name="value")
    private Map<String,Serializable> properties = new HashMap<>();
    
     @WorkField(displayName = "Status")
    protected Status status;
    
   

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
    public Set<BizUser> getOwners() {
        return owners;
    }

    /**
     * @param owners the owners to set
     */
    public void setOwners(Set<BizUser> owners) {
//        for (BizUser owner:owners){
//            owner.setWorkItem(this);
//        }
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

   
    
    public abstract String getTitle();
    
     /**
     * @return the properties
     */
    public Map<String, Serializable> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Serializable> properties) {
        this.properties = properties;
    }
    
    public void addApproval(Approval approval){
//        approval.setWorkItem(this);
        if (this.getApprovals()==null){
            this.setApprovals(new HashSet<>());
        }
        this.getApprovals().add(approval);
    }
    
    public void addOwner(BizUser bizUser){
//        bizUser.setWorkItem(this);
        this.getOwners().add(bizUser);
    }
    
    public void removeOwner(BizUser bizUser){
//        bizUser.setWorkItem(null);
        this.getOwners().remove(bizUser);
    }
    
    public void clearOwners(){
//        for (BizUser user:this.getOwners()){
//            user.setWorkItem(null);
//        }
        this.getOwners().clear();
    }
    
    public void clearApprrovals(){
//        for (Approval approval:this.getApprovals()){
//            approval.setWorkItem(null);
//        }
        this.getApprovals().clear();
    }

    /**
     * @return the startDate
     */
    public ZonedDateTime getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public ZonedDateTime getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }
    
      public Status getStatus() {
        return status;
    }
    
     public void setStatus(Status status) {
        this.status=status;
    }


}
