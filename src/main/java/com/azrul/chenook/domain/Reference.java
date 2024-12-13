package com.azrul.chenook.domain;


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
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Document(indexName = "reference")
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class Reference  implements Serializable {
    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @WorkField(displayName = "Id", sortable = true)
    private Long id;

    
    @Field(type = FieldType.Keyword)
    @WorkField(displayName = "Status", sortable = true)
    private ReferenceStatus status;
    
    private Long refWorkId;
    
    private Long replacementOf;


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

   

    
}