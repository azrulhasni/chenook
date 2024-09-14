/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.Approval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author azrul
 */
public interface ApprovalRepository extends JpaRepository<Approval, Long>, JpaSpecificationExecutor<Approval> {
    
     @Query(value = "SELECT * FROM approval WHERE work_id = :workId", nativeQuery = true)    
    public Page<Approval> findByWork(Long workId, Pageable pageable);
    
    @Query(value = "SELECT COUNT(*) FROM approval WHERE work_id = :workId", nativeQuery = true)    
    public Long countByWork(Long workId);
}
