/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.converter.LocalDateTimeConverter;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import org.hibernate.envers.Audited;
import org.springframework.data.elasticsearch.annotations.ValueConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */
@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Approval {
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @WorkField(displayName = "User name")
    private String username;
    
    @WorkField(displayName = "First name")
    private String firstName;
    
    @WorkField(displayName = "Last name")
    private String lastName;
    
    @WorkField(displayName = "Approval worklist")
    private String worklist;
    
    @WorkField(displayName = "Approved")
    private Boolean approved;
    
     @WorkField(displayName = "Note")
    private String note;
    
    @ValueConverter(LocalDateTimeConverter.class)
    @WorkField(displayName = "Approval date")
    private LocalDateTime approvalDateTime;
    
//    @Transient
//    @ManyToOne
//    @JoinColumn(name = "work_id", referencedColumnName = "id")
//    private WorkItem workItem;
//    
//    @Transient
//    @ManyToOne
//    @JoinColumn(name = "hist_work_id", referencedColumnName = "id", nullable = true)
//    private WorkItem historicalWorkItem;
    
    
    
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
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the apprroved
     */
    public Boolean getApproved() {
        return approved;
    }

    /**
     * @param apprroved the apprroved to set
     */
    public void setApproved(Boolean apprroved) {
        this.approved = apprroved;
    }

    /**
     * @return the approvalDateTime
     */
    public LocalDateTime getApprovalDateTime() {
        return approvalDateTime;
    }

    /**
     * @param approvalDateTime the approvalDateTime to set
     */
    public void setApprovalDateTime(LocalDateTime approvalDateTime) {
        this.approvalDateTime = approvalDateTime;
    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + Objects.hashCode(this.id);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Approval other = (Approval) obj;
//        return Objects.equals(this.id, other.id);
//    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

//    /**
//     * @return the workItem
//     */
//    public WorkItem getWorkItem() {
//        return workItem;
//    }
//
//    /**
//     * @param workItem the workItem to set
//     */
//    public void setWorkItem(WorkItem workItem) {
//        this.workItem = workItem;
//    }
//
//    /**
//     * @return the historicalWorkItem
//     */
//    public WorkItem getHistoricalWorkItem() {
//        return historicalWorkItem;
//    }
//
//    /**
//     * @param historicalWorkItem the historicalWorkItem to set
//     */
//    public void setHistoricalWorkItem(WorkItem historicalWorkItem) {
//        this.historicalWorkItem = historicalWorkItem;
//    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }
    
}
