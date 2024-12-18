/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.search.repository;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceWork;
import com.azrul.chenook.search.repository.WorkItemSearchRepository;

/**
 *
 * @author azrul
 */
public interface ReferenceWorkSearchRepository<R extends Reference> extends WorkItemSearchRepository<ReferenceWork<R>>{
    
}
