/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.repository;

import com.azrul.chenook.repository.WorkItemRepository;
import com.azrul.smefinancing.domain.FinApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author azrul
 */
public interface FinApplicationRepository extends WorkItemRepository<FinApplication> {

    
}
