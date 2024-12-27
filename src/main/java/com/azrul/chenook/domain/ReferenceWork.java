/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.service.ReferenceService;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */
@Entity
@DiscriminatorValue("REFERENCES")
@EntityListeners(AuditingEntityListener.class)
public class ReferenceWork<R extends Reference> extends WorkItem {

    private String className;

    private ReferenceWorkType referenceWorkType;
    
    @WorkField(displayName = "Attachments")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "finapp_id")
    private Set<Attachment> attachments = new HashSet<>();

    public static <R> Class<R> getClazz() {
        return (Class<R>) ReferenceWork.class;
    }

    @Override
    public String getTitle() {
        return "Reference Operation";
    }

    /**
     * @return the className
     */
    public String getClassName() {

        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }



    /**
     * @return the referenceWorkType
     */
    public ReferenceWorkType getReferenceWorkType() {
        return referenceWorkType;
    }

    /**
     * @param referenceWorkType the referenceWorkType to set
     */
    public void setReferenceWorkType(ReferenceWorkType referenceWorkType) {
        this.referenceWorkType = referenceWorkType;
    }



    public void calculateRefStatus(ApplicationContext appContext) {
     
       ReferenceService refService = appContext.getBean(ReferenceService.class);
        if ("S0.REF.CREATE.MAKER".equals(getStartEventId())) {
            refService.updateRefStatusByRefWork(ReferenceStatus.CONFIRMED, ReferenceStatus.DRAFT, id);
        } else if ("S0.REF.UPDATE.MAKER".equals(getStartEventId())) {
             refService.updateRefStatusByRefWork(ReferenceStatus.CONFIRMED, ReferenceStatus.DRAFT, id);
              refService.updateRefStatusByRefWork(ReferenceStatus.RETIRED, ReferenceStatus.DEPRECATED, id);
        } else if ("S0.REF.DELETE.MAKER".equals(getStartEventId())) {
             refService.updateRefStatusByRefWork(ReferenceStatus.RETIRED, ReferenceStatus.DEPRECATED, id);
        }
        this.setStatus(Status.DONE);
    }

    /**
     * @return the attachments
     */
    public Set<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

}
