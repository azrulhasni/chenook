package com.azrul.chenook.domain;


import com.azrul.chenook.annotation.DateTimeFormat;
import org.hibernate.envers.Audited;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.azrul.chenook.annotation.WorkField;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Document(indexName = "reference")
@EntityListeners(AuditingEntityListener.class)
@Audited
public abstract class Reference  implements Serializable {
    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //@WorkField(displayName = "Id", sortable = true)
    private Long id;

    
    @Field(type = FieldType.Keyword)
    @WorkField(displayName = "Status", sortable = true)
    private ReferenceStatus status;
    
    private Long refWorkId;
    
    private Long replacementOf;
    
    @Audited(withModifiedFlag = true)
    private Integer version;

 
    @CreatedBy
    private String createdBy;


    @CreatedDate
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    private Instant  creationDate;


    @LastModifiedBy
    private String lastModifiedBy;


    @LastModifiedDate
    @WorkField(displayName = "Last update", sortable = true,showAtAudit=true)
    @DateTimeFormat(format = "${finapp.datetime.format}")
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    private Instant  lastModifiedDate;
    
    @Transient
    @WorkField(displayName = "Oper", sortable = true, showAtAudit=true)
    private String operation;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    public abstract String toString();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reference other = (Reference) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /**
     * @return the status
     */
    public ReferenceStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ReferenceStatus status) {
        this.status = status;
    }

    /**
     * @return the refWorkId
     */
    public Long getRefWorkId() {
        return refWorkId;
    }

    /**
     * @param refWorkId the refWorkId to set
     */
    public void setRefWorkId(Long refWorkId) {
        this.refWorkId = refWorkId;
    }

    /**
     * @return the replacementOf
     */
    public Long getReplacementOf() {
        return replacementOf;
    }

    /**
     * @param replacementOf the replacementOf to set
     */
    public void setReplacementOf(Long replacementOf) {
        this.replacementOf = replacementOf;
    }

    /**
     * @return the version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

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
     * @return the creationDate
     */
    public Instant  getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Instant  creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @param lastModifiedBy the lastModifiedBy to set
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * @return the lastModifiedDate
     */
    public Instant  getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * @param lastModifiedDate the lastModifiedDate to set
     */
    public void setLastModifiedDate(Instant  lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

   

    
}