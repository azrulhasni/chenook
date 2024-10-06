/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import com.azrul.chenook.annotation.WorkField;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author azrul
 */
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;
    
    private Long parentId;
    private String context;
    
    @WorkField(displayName = "Sender")
    private String writerUserName;
    
    @WorkField(displayName = "Sender full name")
    private String fullName;
    
    private String createdBy;
    private LocalDateTime createdDateTime;
    
    @WorkField(displayName = "Message")
    private String message;
    
    @WorkField(displayName = "Status")
    private MessageStatus status;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_metadata_mapping", 
      joinColumns = {@JoinColumn(name = "id", referencedColumnName = "id")})
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Double> metadata;

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the createdDateTime
     */
    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    /**
     * @param createdDateTime the createdDateTime to set
     */
    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

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
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;
        return Objects.equals(this.id, other.id);
    }

    /**
     * @return the parrentId
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * @param parrentId the parrentId to set
     */
    public void setParentId(Long parrentId) {
        this.parentId = parrentId;
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
     * @return the writerUserName
     */
    public String getWriterUserName() {
        return writerUserName;
    }

    /**
     * @param writerUserName the writerUserName to set
     */
    public void setWriterUserName(String writerUserName) {
        this.writerUserName = writerUserName;
    }

    /**
     * @return the metadata
     */
    public Map<String, Double> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(Map<String, Double> metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the status
     */
    public MessageStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
