/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceWork;
import com.azrul.chenook.repository.WorkItemRepository;

/**
 *
 * @author azrul
 */
public interface ReferenceWorkRepository<R extends Reference> extends WorkItemRepository<ReferenceWork<R>>{
    
//    @Query("SELECT rw FROM ReferenceWork rw LEFT JOIN FETCH rw.existingReferences LEFT JOIN FETCH rw.newReferences WHERE rw.id = ?1")
//    ReferenceWork<R> findAndFetchDependencies(Long id);
}
