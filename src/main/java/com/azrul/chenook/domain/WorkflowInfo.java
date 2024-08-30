/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 *
 * @author azrul
 */
@Embeddable
public class WorkflowInfo {
     private String startEventId;
     private String startEventDescription;
     private Set<String> owners;
     private String worklist;
     private LocalDateTime worklistUpdateTime;

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
}
