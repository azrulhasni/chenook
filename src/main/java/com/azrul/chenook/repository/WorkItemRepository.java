/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.WorkItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author azrul
 */
@NoRepositoryBean
public interface WorkItemRepository<T extends WorkItem> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    //public T findOneByParentIdAndContext(Long parentId,String context);
//    @Query(value = "SELECT * FROM work_item WHERE :username = ANY(work_item.owners)", nativeQuery = true)    
//    public Page<T> findByOwner(String username,  Pageable pageable);
//    
//    @Query(value = "SELECT COUNT(*) FROM work_item WHERE :username = ANY(work_item.owners)", nativeQuery = true)    
//    public Long countByOwner(String username);
//    
//    @Query(value = "SELECT * FROM work_item WHERE owners = '{}' AND worklist = :worklist", nativeQuery = true)    
//    public Page<T> findByWorklistAndNoOwner(String worklist,  Pageable pageable);
//    
//     @Query(value = "SELECT COUNT(*) FROM work_item WHERE owners = '{}' AND worklist = :worklist", nativeQuery = true)    
//    public Long countByWorklistAndNoOwner(String worklist);
    
  
}
