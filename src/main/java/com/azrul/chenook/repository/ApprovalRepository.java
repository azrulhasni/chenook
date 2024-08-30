/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author azrul
 */
public interface ApprovalRepository extends JpaRepository<Approval, Long>, JpaSpecificationExecutor<Approval> {
    
}
