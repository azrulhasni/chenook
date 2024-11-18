/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
public class ReferenceWork extends WorkItem {

    private Reference existingReference;
    private Reference newReference;
    @Override
    public String getTitle() {
        return "Reference CUD"; 
    }
    
}
