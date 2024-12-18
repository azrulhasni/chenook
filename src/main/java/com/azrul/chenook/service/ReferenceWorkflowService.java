/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceWork;
import com.azrul.chenook.repository.ReferenceWorkRepository;
import com.azrul.chenook.repository.WorkItemRepository;
import com.azrul.chenook.search.repository.ReferenceWorkSearchRepository;
import com.azrul.chenook.search.repository.WorkItemSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
@Transactional
@Qualifier("Reference")
public class ReferenceWorkflowService<R extends Reference> extends WorkflowService<ReferenceWork<R>> {

    private final ReferenceWorkRepository<R> refWorkRepo;
    private final ReferenceWorkSearchRepository<R> refWorkSearchRepo;

    public ReferenceWorkflowService(
            @Autowired ReferenceWorkRepository<R> refWorkRepo,
            @Autowired ReferenceWorkSearchRepository<R> refWorkSearchRepo
    ) {
        this.refWorkRepo = refWorkRepo;
        this.refWorkSearchRepo = refWorkSearchRepo;
        
    }

    @Override
    public ReferenceWork<R> save(ReferenceWork<R> work) {
        var finappWithId = refWorkRepo.save(work);
        refWorkSearchRepo.save(finappWithId);
        return finappWithId;
    }

    @Override
    public WorkItemRepository<ReferenceWork<R>> getWorkItemRepo() {
       return refWorkRepo;
    }

    @Override
    public WorkItemSearchRepository<ReferenceWork<R>> getWorkItemSearchRepo() {
        return refWorkSearchRepo;
    }
    
    @Transactional
    public void remove(ReferenceWork<R> app) {
            refWorkSearchRepo.delete(app);
            refWorkRepo.delete(app);
    }
    
    public ReferenceWork<R> refresh(ReferenceWork<R> rw){
        return this.refWorkRepo.getReferenceById(rw.getId());
    }

}
