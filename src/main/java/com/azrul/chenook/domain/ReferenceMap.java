package com.azrul.chenook.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class ReferenceMap<R extends Reference> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    
    private Long parentId;
    private String context;
    private String type;

    @ManyToMany( fetch = FetchType.EAGER, targetEntity = Reference.class)
	@JoinTable(name="referencemap_references", joinColumns=@JoinColumn(name="refmap_id"), inverseJoinColumns=@JoinColumn(name="ref_id"))
    private Set<R> references;

    public ReferenceMap(){
        this.parentId = null;
        this.type = null;
        this.context = null; 
        this.references = new HashSet<>();    
    }

    public ReferenceMap(Long parentId, String type, String context){
        this.parentId = parentId;
        this.type = type;
        this.context = context; 
        this.references = new HashSet<>();    
    }
    
    

    /**
     * @return Long return the id
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
     * @return Long return the parentId
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
     * @return Set<Long> return the referenceId
     */
    public Set<R> getReferences() {
        return references;
    }

    /**
     * @param referenceId the referenceId to set
     */
    public void setReferences(Set<R> references) {
        this.references = references;
    }


    /**
     * @return String return the context
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
     * @return String return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

}