/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import com.azrul.chenook.annotation.WorkField;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */
@Entity
@DiscriminatorValue("REFERENCES")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class ReferenceWork<R extends Reference> extends WorkItem {
    
    private String className;
    
    private ReferenceWorkType referenceWorkType;

    @WorkField(displayName = "Existing References")
    @ManyToMany( fetch = FetchType.LAZY , targetEntity = Reference.class, cascade = CascadeType.ALL)
    @JoinTable(name="refwork_refs", joinColumns=@JoinColumn(name="refwork_id"), inverseJoinColumns=@JoinColumn(name="ref_id"))
    private Set<R> existingReferences;
    
    @WorkField(displayName = "New References")
    @ManyToMany( fetch = FetchType.LAZY, targetEntity = Reference.class, cascade = CascadeType.ALL)
    @JoinTable(name="refwork_refs", joinColumns=@JoinColumn(name="refwork_id"), inverseJoinColumns=@JoinColumn(name="ref_id"))
    private Set<R> newReferences;
    
    public static <R> Class<R> getClazz(){
        return (Class<R>)ReferenceWork.class;
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
    
    public void addNewReference(R r){
        if (this.newReferences==null){
            this.newReferences=new HashSet<>();
        }
        this.newReferences.add(r);
    }
    
    public void addExistingReference(R r){
        if (this.existingReferences==null){
            this.existingReferences=new HashSet<>();
        }
        this.existingReferences.add(r);
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

    /**
     * @return the existingReferences
     */
    public Set<R> getExistingReferences() {
        return existingReferences;
    }

    /**
     * @param existingReferences the existingReferences to set
     */
    public void setExistingReferences(Set<R> existingReferences) {
        this.existingReferences = existingReferences;
    }

    /**
     * @return the newReferences
     */
    public Set<R> getNewReferences() {
        return newReferences;
    }

    /**
     * @param newReferences the newReferences to set
     */
    public void setNewReferences(Set<R> newReferences) {
        this.newReferences = newReferences;
    }
    
}